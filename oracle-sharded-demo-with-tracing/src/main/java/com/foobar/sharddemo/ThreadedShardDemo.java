package com.foobar.sharddemo;

import oracle.jdbc.OracleType;
import oracle.jdbc.pool.OracleShardingKeyBuilderImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ThreadedShardDemo {

    private static final int THREAD_COUNT = 10;
    private static final int ITERATIONS_PER_THREAD = 100;

    private static final List<String> SHARDING_KEYS = List.of(
            "Denmark",
            "France",
            "Germany",
            "Poland",
            "Spain",
            "United Kingdom",
            "India",
            "Australia",
            "Japan",
            "New Zealand",
            "Saudi Arabia",
            "Singapore",
            "Turkey"
    );

    public static void main(String[] args) throws Exception {
        OracleTracing.enable();

        String url = requiredEnv("DB_URL");
        String user = requiredEnv("DB_USER");
        String password = requiredEnv("DB_PASSWORD");

        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionPoolName("ThreadedShardDemoPool");
        pds.setDataSourceName("threadedShardDemoDataSource");
        pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        pds.setURL(url);
        pds.setUser(user);
        pds.setPassword(password);
        pds.setInitialPoolSize(0);
        pds.setMinPoolSize(0);
        pds.setMaxPoolSize(20);
        pds.setMaxConnectionsPerShard(5);
        pds.setConnectionWaitDuration(Duration.ofSeconds(30));
        pds.setValidateConnectionOnBorrow(true);

        System.out.println("URL        = " + url);
        System.out.println("User       = " + user);
        System.out.println("Threads    = " + THREAD_COUNT);
        System.out.println("Iterations = " + ITERATIONS_PER_THREAD);
        System.out.println("ShardingKeys = " + SHARDING_KEYS);

        runWorkers(pds);
    }

    private static void runWorkers(PoolDataSource pds) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            List<Future<?>> futures = new ArrayList<>(THREAD_COUNT);
            for (int workerId = 1; workerId <= THREAD_COUNT; workerId++) {
                int currentWorkerId = workerId;
                futures.add(executor.submit(() -> runWorker(pds, currentWorkerId)));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    private static void runWorker(PoolDataSource pds, int workerId) {
        for (int iteration = 1; iteration <= ITERATIONS_PER_THREAD; iteration++) {
            String shardKey = randomShardKey();
            executeForKey(pds, workerId, iteration, shardKey);
        }
    }

    private static String randomShardKey() {
        return SHARDING_KEYS.get(ThreadLocalRandom.current().nextInt(SHARDING_KEYS.size()));
    }

    private static void executeForKey(PoolDataSource pds, int workerId, int iteration, String shardKeyValue) {
        try {
            ShardingKey shardKey = new OracleShardingKeyBuilderImpl()
                    .subkey(shardKeyValue, OracleType.VARCHAR2)
                    .build();

            try (Connection conn = pds.createConnectionBuilder().shardingKey(shardKey).build();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select i.host_name, d.db_unique_name, d.database_role, " +
                         "sys_context('USERENV','SERVICE_NAME') as service_name, " +
                         "sys_context('USERENV','CON_NAME') as con_name " +
                         "from v$instance i cross join v$database d")) {

                while (rs.next()) {
                    System.out.printf(
                            "worker=%02d iteration=%03d sharding_key=%s host_name=%s db_unique_name=%s role=%s service_name=%s con_name=%s%n",
                            workerId,
                            iteration,
                            shardKeyValue,
                            rs.getString("host_name"),
                            rs.getString("db_unique_name"),
                            rs.getString("database_role"),
                            rs.getString("service_name"),
                            rs.getString("con_name")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.printf(
                    "Connection failed for worker=%02d iteration=%03d sharding_key=%s%n",
                    workerId,
                    iteration,
                    shardKeyValue
            );
            logThrowable(e);
            throw new RuntimeException(e);
        }
    }

    private static void logThrowable(Throwable t) {
        int depth = 0;
        while (t != null) {
            System.err.printf("cause[%d] %s: %s%n", depth, t.getClass().getName(), t.getMessage());
            if (t instanceof SQLException sql) {
                SQLException next = sql.getNextException();
                int n = 0;
                while (next != null) {
                    System.err.printf("  nextSQLException[%d] %s: %s%n", n, next.getClass().getName(), next.getMessage());
                    next = next.getNextException();
                    n++;
                }
            }
            t = t.getCause();
            depth++;
        }
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required environment variable: " + name);
        }
        return value.trim();
    }
}
