Tomcat's JDBC pool
 - deploy the app (context.xml points onto data source jdbc/TomcatCP).
 - Check database connections:

SQL> select count(1), min(logon_time), max(logon_time), inst_id, service_name from gv$session where machine = ...' group by inst_id, service_name;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID SERVICE_NAME
---------- ---------------- ---------------- ---------- ----------------------------------------------------------------
        10 2018.03.15 20:05 2018.03.15 20:05          2 ACTEST


Initial 10 connections on 1st node.
Relocate the service:

$ srvctl relocate service -d INFRADB -s ACTEST -oldinst INFRADB1 -newinst INFRADB2

Check connections(after some time):

SQL> select count(1), min(logon_time), max(logon_time), inst_id, service_name from gv$session where machine = ...' group by inst_id, service_name;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID SERVICE_NAME
---------- ---------------- ---------------- ---------- ----------------------------------------------------------------
        10 2018.03.15 20:05 2018.03.15 20:05          2 ACTEST

Run the client REST app.

$ cd client-py/
$ python booksclientmt.py
200
The response contains 1099 properties

...

Relocate service several times, check db connections.

Result: PASSED

Apache DBCP2 pool
 - deploy the app (context.xml points onto data source jdbc/TomcatCP).
 - Check database connections:

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

no rows selected

Run the client REST app.

$ cd client-py/
$ python booksclientmt.py
200
The response contains 1099 properties

...


 - Check database connections:
 
SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
        20 2018.03.15 20:22 2018.03.15 20:22          1


Relocate service several time, check db connections.

$ srvctl relocate service -d INFRADB -s ACTEST -oldinst INFRADB1 -newinst INFRADB2

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
        10 2018.03.15 20:23 2018.03.15 20:24          2


SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
        15 2018.03.15 20:26 2018.03.15 20:26          1
         5 2018.03.15 20:26 2018.03.15 20:26          2

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
         4 2018.03.15 20:26 2018.03.15 20:26          1
        11 2018.03.15 20:26 2018.03.15 20:27          2

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = ...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
         1 2018.03.15 20:26 2018.03.15 20:26          1
         9 2018.03.15 20:26 2018.03.15 20:27          2

Mar 15, 2018 8:28:06 PM org.apache.tomcat.dbcp.dbcp2.SwallowedExceptionLogger onSwallowException
WARNING: An internal object pool swallowed an Exception.
org.apache.tomcat.dbcp.dbcp2.LifetimeExceededException: The lifetime of the connection [90,010] milliseconds exceeds the maximum permitted value of [90,000] milliseconds
	at org.apache.tomcat.dbcp.dbcp2.PoolableConnectionFactory.validateLifetime(PoolableConnectionFactory.java:426)
	at org.apache.tomcat.dbcp.dbcp2.PoolableConnectionFactory.passivateObject(PoolableConnectionFactory.java:366)
	at org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool.returnObject(GenericObjectPool.java:563)
	at org.apache.tomcat.dbcp.dbcp2.PoolableConnection.close(PoolableConnection.java:205)
	at org.apache.tomcat.dbcp.dbcp2.DelegatingConnection.closeInternal(DelegatingConnection.java:241)
	at org.apache.tomcat.dbcp.dbcp2.DelegatingConnection.close(DelegatingConnection.java:223)
	at org.apache.tomcat.dbcp.dbcp2.PoolingDataSource$PoolGuardConnectionWrapper.close(PoolingDataSource.java:244)
	at sandbox.dbcp.utils.BookSimpleDao.update(BookSimpleDao.java:121)
	at sandbox.dbcp.rest.BookServer.update(BookServer.java:63)
	at sun.reflect.GeneratedMethodAccessor41.invoke(Unknown Source)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
	at java.lang.reflect.Method.invoke(Unknown Source)
        ...

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = 'CZCHOWN5020546' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
         1 2018.03.15 20:30 2018.03.15 20:30          2


Result: PASSED (partially), it looks like due to networking problems one abandonned connection was left on 1st node.
PS: TODO check which parameter is set to 10ms.


