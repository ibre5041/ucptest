# Oracle Sharded Database Minimal Demo

This is a very small Java/UCP example for Oracle sharded database routing. It follows the same UCP sharding pattern shown in the Oracle documentation and the `oracle-db-examples` repository, which includes a Spring Sharding sample and a UCP sharding example that uses `PoolDataSource`, `createShardingKeyBuilder()`, and `createConnectionBuilder().shardingKey(...)`. citeturn953818view0turn413006view0

## What it does

- connects to a GSM/shard-director service
- supplies a sharding key at connection checkout
- prints the actual database host, `db_unique_name`, database role, service name, and PDB name

Oracle’s JDBC/UCP docs say UCP pools are created from `PoolDataSource`, and sharding key routing is done by supplying the sharding key when borrowing the connection. citeturn587448view1turn953818view0

## Build

```bash
mvn -q clean package
```

## Run

Set your connection details first:

```bash
export DB_URL='jdbc:oracle:thin:@//gsm-emea.prod.vmware.haf:1521/ro_service.orasdb.oradbcloud'
export DB_USER='app_user'
export DB_PASSWORD='app_password'
export SHARDING_KEYS='Singapore,Spain'
```

Then run:

```bash
mvn -q exec:java
```

You can also pass sharding keys as arguments:

```bash
mvn -q exec:java -Dexec.args='Singapore Spain India'
```

## Notes

- `DB_URL` should point at your GSM/global service, for example `ro_service` or `rw_service`.
- The demo uses a single-level sharding key only.
- The pool is deliberately kept small and lazy (`initialPoolSize=0`, `minPoolSize=0`) to avoid startup-side pool creation issues while testing.


## Tracing

This demo always enables UCP logging and Oracle JDBC logging at startup. Ring-buffer tracing is available as an opt-in JVM flag because UCP documents tracing as a separate diagnosability path from logging, and in your environment the trace path is the one that was failing while loading the internal UCP trace actor. Oracle describes UCP tracing as an in-memory ring buffer that is dumped by diagnosability events, while logging uses Java Util Logging. citeturn910459search0turn715177search3

To try the ring-buffer trace as well, run with:

```bash
-Ddemo.enable.ucp.trace=true
```
