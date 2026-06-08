package com.foobar.sharddemo;

import oracle.jdbc.OracleType;
import oracle.jdbc.OracleShardingKey;
import oracle.jdbc.pool.OracleShardingKeyBuilderImpl;
import com.foobar.sharddemo.ShardDemoSupport.PoolTarget;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        //OracleTracing.enable();

        List<PoolTarget> targets = ShardDemoSupport.createConfiguredPools();

        ShardDemoSupport.printPoolSummary(targets);
        System.out.println("Threads    = " + THREAD_COUNT);
        System.out.println("Iterations = " + ITERATIONS_PER_THREAD);
        System.out.println("ShardingKeys = " + SHARDING_KEYS);

        runWorkers(targets);
    }

    private static void runWorkers(List<PoolTarget> targets) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            List<Future<?>> futures = new ArrayList<>(THREAD_COUNT);
            for (int workerId = 1; workerId <= THREAD_COUNT; workerId++) {
                int currentWorkerId = workerId;
                futures.add(executor.submit(() -> runWorker(targets, currentWorkerId)));
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

    private static void runWorker(List<PoolTarget> targets, int workerId) {
        for (int iteration = 1; iteration <= ITERATIONS_PER_THREAD; iteration++) {
            PoolTarget target = randomPool(targets);
            String shardKey = randomShardKey();
            executeForKey(target, workerId, iteration, shardKey);
        }
    }

    private static PoolTarget randomPool(List<PoolTarget> targets) {
        return targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
    }

    private static String randomShardKey() {
        return SHARDING_KEYS.get(ThreadLocalRandom.current().nextInt(SHARDING_KEYS.size()));
    }

    private static void executeForKey(PoolTarget target, int workerId, int iteration, String shardKeyValue) {
        try {
            OracleShardingKey shardKey = new OracleShardingKeyBuilderImpl()
                    .subkey(shardKeyValue, OracleType.VARCHAR2)
                    .build();

            try (Connection conn = target.pool().createConnectionBuilder().shardingKey(shardKey).build();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(ShardDemoSupport.ROUTING_QUERY)) {

                while (rs.next()) {
                    System.out.printf(
                            "worker=%02d iteration=%03d pool=%s sharding_key=%s ",
                            workerId,
                            iteration,
                            target.name(),
                            shardKeyValue
                    );
                    ShardDemoSupport.printRoutingResult(rs);
                }
            }
        } catch (SQLException e) {
            System.err.printf(
                    "Connection failed for worker=%02d iteration=%03d pool=%s sharding_key=%s%n",
                    workerId,
                    iteration,
                    target.name(),
                    shardKeyValue
            );
            ShardDemoSupport.logThrowable(e);
            throw new RuntimeException(e);
        }
    }
}
