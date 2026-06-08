# Oracle Sharded Database Minimal Demo

This is a very small Java/UCP example for Oracle sharded database routing. It follows the standard UCP sharding pattern using `PoolDataSource`, a sharding key, and `createConnectionBuilder().shardingKey(...)`.

## What it does

- connects to a GSM/shard-director service
- creates one connection pool for each configured service URL
- supplies a sharding key at connection checkout
- prints the actual database host, `db_unique_name`, database role, service name, and PDB name

UCP pools are created from `PoolDataSource`, and sharding key routing is done by supplying the sharding key when borrowing the connection.

## Build

```bash
mvn -q clean package
```

## Run

Set your connection details first. You can configure a read-only URL, a read-write URL, or both:

```bash
export DB_URL_RO='jdbc:oracle:thin:@//gsm-emea.prod.vmware.haf:1521/ro_service.orasdb.oradbcloud'
export DB_URL_RW='jdbc:oracle:thin:@//gsm-emea.prod.vmware.haf:1521/rw_service.orasdb.oradbcloud'
export DB_USER='app_user'
export DB_PASSWORD='app_password'
export SHARDING_KEYS='Singapore,Spain'
```

The demos also read the same `KEY='value'` format from a local `.venv` file when a value is not present in the real process environment.

Then run:

```bash
mvn -q exec:java
```

`MinimalShardDemo` creates one pool for each configured URL and executes the routing SQL against each pool for each sharding key.

You can also pass sharding keys as arguments:

```bash
mvn -q exec:java -Dexec.args='Singapore Spain India'
```

To run the threaded demo:

```bash
mvn -q exec:java -Dexec.mainClass=com.foobar.sharddemo.ThreadedShardDemo
```

`ThreadedShardDemo` creates the same configured pools and randomly picks one pool for each SQL execution.

## Notes

- `DB_URL_RO` and `DB_URL_RW` should point at your GSM/global services, for example `ro_service` and `rw_service`.
- If neither `DB_URL_RO` nor `DB_URL_RW` is set, the demos fall back to legacy `DB_URL`.
- The demo uses a single-level sharding key only.
- The pool is deliberately kept small and lazy (`initialPoolSize=0`, `minPoolSize=0`) to avoid startup-side pool creation issues while testing.

## Tracing

This demo always enables UCP logging and Oracle JDBC logging at startup. Ring-buffer tracing is available as an opt-in JVM flag because UCP tracing is a separate diagnosability path from logging, and in your environment the trace path is the one that was failing while loading the internal UCP trace actor. UCP tracing uses an in-memory ring buffer that is dumped by diagnosability events, while logging uses Java Util Logging.

To try the ring-buffer trace as well, run with:

```bash
-Ddemo.enable.ucp.trace=true
```
