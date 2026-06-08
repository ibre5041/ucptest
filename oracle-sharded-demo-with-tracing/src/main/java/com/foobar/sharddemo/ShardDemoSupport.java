package com.foobar.sharddemo;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ShardDemoSupport {

    static final String ROUTING_QUERY =
            "select i.host_name, d.db_unique_name, d.database_role, " +
            "sys_context('USERENV','SERVICE_NAME') as service_name, " +
            "sys_context('USERENV','CON_NAME') as con_name " +
            "from v$instance i cross join v$database d";

    private static final Map<String, String> DOTENV = loadDotEnv();

    private ShardDemoSupport() {
    }

    static List<PoolTarget> createConfiguredPools() throws SQLException {
        String user = requiredEnv("DB_USER");
        String password = requiredEnv("DB_PASSWORD");

        List<PoolTarget> targets = new ArrayList<>();
        addPoolIfConfigured(targets, "RO", "DB_URL_RO", "ShardDemoPoolRO", "shardDemoDataSourceRO", user, password);
        addPoolIfConfigured(targets, "RW", "DB_URL_RW", "ShardDemoPoolRW", "shardDemoDataSourceRW", user, password);

        if (targets.isEmpty()) {
            String legacyUrl = optionalEnv("DB_URL");
            if (legacyUrl != null) {
                targets.add(createPool("DB", legacyUrl, "ShardDemoPool", "shardDemoDataSource", user, password));
            }
        }

        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Missing required environment variable: DB_URL_RO or DB_URL_RW");
        }

        return List.copyOf(targets);
    }

    static void printPoolSummary(List<PoolTarget> targets) {
        System.out.println("User       = " + requiredEnv("DB_USER"));
        for (PoolTarget target : targets) {
            System.out.printf("%-10s = %s%n", "URL_" + target.name(), target.url());
        }
    }

    static void printRoutingResult(ResultSet rs) throws SQLException {
        System.out.printf(
                "host_name=%s db_unique_name=%s role=%s service_name=%s con_name=%s%n",
                rs.getString("host_name"),
                rs.getString("db_unique_name"),
                rs.getString("database_role"),
                rs.getString("service_name"),
                rs.getString("con_name")
        );
    }

    static void logThrowable(Throwable t) {
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

    private static void addPoolIfConfigured(
            List<PoolTarget> targets,
            String name,
            String envName,
            String poolName,
            String dataSourceName,
            String user,
            String password
    ) throws SQLException {
        String url = optionalEnv(envName);
        if (url != null) {
            targets.add(createPool(name, url, poolName, dataSourceName, user, password));
        }
    }

    private static PoolTarget createPool(
            String name,
            String url,
            String poolName,
            String dataSourceName,
            String user,
            String password
    ) throws SQLException {
        PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource();
        pds.setConnectionPoolName(poolName);
        pds.setDataSourceName(dataSourceName);
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
        return new PoolTarget(name, url, pds);
    }

    private static String optionalEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = DOTENV.get(name);
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String requiredEnv(String name) {
        String value = optionalEnv(name);
        if (value == null) {
            throw new IllegalArgumentException("Missing required environment variable: " + name);
        }
        return value;
    }

    private static Map<String, String> loadDotEnv() {
        Path path = Path.of(".venv");
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }

        Map<String, String> values = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(path)) {
                parseDotEnvLine(line, values);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read .venv", e);
        }
        return Map.copyOf(values);
    }

    private static void parseDotEnvLine(String line, Map<String, String> values) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        int separator = trimmed.indexOf('=');
        if (separator <= 0) {
            return;
        }

        String key = trimmed.substring(0, separator).trim();
        String value = trimmed.substring(separator + 1).trim();
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            value = value.substring(1, value.length() - 1);
        }
        if (!key.isEmpty()) {
            values.put(key, value);
        }
    }

    record PoolTarget(String name, String url, PoolDataSource pool) {
    }
}
