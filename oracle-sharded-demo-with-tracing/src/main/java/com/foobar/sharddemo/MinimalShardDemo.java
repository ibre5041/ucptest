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
import java.util.Arrays;
import java.util.List;

public class MinimalShardDemo {

    public static void main(String[] args) throws Exception {
        //OracleTracing.enable();

        List<String> shardKeys = parseKeys(args, System.getenv("SHARDING_KEYS"));
        if (shardKeys.isEmpty()) {
            shardKeys = List.of("Singapore");
        }

        List<PoolTarget> targets = ShardDemoSupport.createConfiguredPools();

        System.out.println("UCP/JDBC tracing enabled.");
        ShardDemoSupport.printPoolSummary(targets);
        System.out.println("ShardingKeys = " + shardKeys);

        for (PoolTarget target : targets) {
            for (String key : shardKeys) {
                System.out.println();
                System.out.println("=== CONNECT USING POOL=" + target.name() + " SHARDING_KEY=" + key + " ===");
                executeForKey(target, key);
            }
        }
    }

    private static void executeForKey(PoolTarget target, String shardKeyValue) {
        try {
            OracleShardingKey shardKey = new OracleShardingKeyBuilderImpl()
                    .subkey(shardKeyValue, OracleType.VARCHAR2)
                    .build();

            try (Connection conn = target.pool().createConnectionBuilder().shardingKey(shardKey).build();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(ShardDemoSupport.ROUTING_QUERY)) {

                while (rs.next()) {
                    System.out.print("pool=" + target.name() + " ");
                    ShardDemoSupport.printRoutingResult(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection failed for pool=" + target.name() + " sharding key: " + shardKeyValue);
            ShardDemoSupport.logThrowable(e);
            throw new RuntimeException(e);
        }
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
