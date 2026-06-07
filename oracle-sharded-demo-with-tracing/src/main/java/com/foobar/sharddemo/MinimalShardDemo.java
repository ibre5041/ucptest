package com.foobar.sharddemo;

import oracle.jdbc.OracleType;
import oracle.jdbc.pool.OracleShardingKeyBuilderImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ShardingKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinimalShardDemo {

    public static void main(String[] args) throws Exception {
        OracleTracing.enable();

        String url = requiredEnv("DB_URL");
        String user = requiredEnv("DB_USER");
        String password = requiredEnv("DB_PASSWORD");

        List<String> shardKeys = parseKeys(args, System.getenv("SHARDING_KEYS"));
        if (shardKeys.isEmpty()) {
            shardKeys = List.of("Singapore");
        }

        PoolDataSourceImpl pds = new PoolDataSourceImpl();
        pds.setConnectionPoolName("ShardDemoPool");
        pds.setDataSourceName("shardDemoDataSource");
        pds.setURL(url);
        pds.setUser(user);
        pds.setPassword(password);
        pds.setInitialPoolSize(0);
        pds.setMinPoolSize(0);
        pds.setMaxPoolSize(20);
        pds.setMaxConnectionsPerShard(5);
        pds.setConnectionWaitDuration(Duration.ofSeconds(30));
        pds.setValidateConnectionOnBorrow(true);

        System.out.println("UCP/JDBC tracing enabled.");
        System.out.println("URL        = " + url);
        System.out.println("User       = " + user);
        System.out.println("ShardingKeys = " + shardKeys);

        for (String key : shardKeys) {
            System.out.println();
            System.out.println("=== CONNECT USING SHARDING_KEY=" + key + " ===");
            executeForKey(pds, key);
        }
    }

    private static void executeForKey(PoolDataSource pds, String shardKeyValue) {
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
                            "host_name=%s db_unique_name=%s role=%s service_name=%s con_name=%s%n",
                            rs.getString("host_name"),
                            rs.getString("db_unique_name"),
                            rs.getString("database_role"),
                            rs.getString("service_name"),
                            rs.getString("con_name")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection failed for sharding key: " + shardKeyValue);
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

    private static List<String> parseKeys(String[] args, String envValue) {
        List<String> keys = new ArrayList<>();
        if (envValue != null && !envValue.isBlank()) {
            keys.addAll(Arrays.stream(envValue.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList());
        }
        if (args != null && args.length > 0) {
            keys.clear();
            keys.addAll(Arrays.stream(args)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList());
        }
        return keys;
    }
}
