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

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

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

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
         1 2018.03.15 20:30 2018.03.15 20:30          2


Result: PASSED (partially), it looks like due to networking problems one abandonned connection was left on 1st node.
PS: TODO check which parameter is set to 10ms.

PS: Error reported when stopping Tomcat:
Mar 15, 2018 9:03:44 PM org.apache.catalina.loader.WebappClassLoaderBase clearReferencesThreads
WARNING: The web application [tomcat-dbcp-rest] appears to have started a thread named [Timer-0] but has failed to stop it. This is very likely to create a memory leak. Stack trace of thread:
 java.lang.Object.wait(Native Method)
 java.lang.Object.wait(Unknown Source)
 java.util.TimerThread.mainLoop(Unknown Source)
 java.util.TimerThread.run(Unknown Source)
Mar 15, 2018 9:03:44 PM org.apache.catalina.loader.WebappClassLoaderBase clearReferencesThreads
WARNING: The web application [tomcat-dbcp-rest] appears to have started a thread named [oracle.jdbc.driver.BlockSource.ThreadedCachingBlockSource.BlockReleaser] but has failed to stop it. This is very likely to create a memory leak. Stack trace of thread:
 java.lang.Object.wait(Native Method)
 oracle.jdbc.driver.BlockSource$ThreadedCachingBlockSource$BlockReleaser.run(BlockSource.java:329)
Mar 15, 2018 9:03:44 PM org.apache.catalina.loader.WebappClassLoaderBase clearReferencesThreads
WARNING: The web application [tomcat-dbcp-rest] appears to have started a thread named [InterruptTimer] but has failed to stop it. This is very likely to create a memory leak. Stack trace of thread:
 java.lang.Object.wait(Native Method)
 java.lang.Object.wait(Unknown Source)
 java.util.TimerThread.mainLoop(Unknown Source)
 java.util.TimerThread.run(Unknown Source)


Oracle UCP
 - deploy the app tomcat-ucp-rest
 - modify 
 - Check database connections:

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
         5 2018.03.15 21:38 2018.03.15 21:38          2

 - relocate the service:

$ srvctl relocate service -d INFRADB -s ACTEST -oldinst INFRADB2 -newinst INFRADB1

 - check database connections:

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
         5 2018.03.15 21:42 2018.03.15 21:42          1

 - run the client
$ python booksclientmt.py

 - check database connections
SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
        15 2018.03.15 21:43 2018.03.15 21:44          1


 - Error ported:
Python:
500
Exception in thread Thread-15:
Traceback (most recent call last):
  File "/usr/lib/python2.7/threading.py", line 801, in __bootstrap_inner
    self.run()
  File "/usr/lib/python2.7/threading.py", line 754, in run
    self.__target(*self.__args, **self.__kwargs)
  File "./booksclientmt.py", line 53, in updateAll
    getAllResponse.raise_for_status()
  File "/usr/lib/python2.7/site-packages/requests/models.py", line 935, in raise_for_status
    raise HTTPError(http_error_msg, response=self)
HTTPError: 500 Server Error:  for url: http://localhost:8080/tomcat-ucp-rest/books/

Tomcat:
Caused by: java.io.IOException: Socket read timed out, socket connect lapse 90 ms. /165.72.165.198 1521 90 1 true
	at oracle.net.nt.TcpNTAdapter.connect(TcpNTAdapter.java:209)
	at oracle.net.nt.ConnOption.connect(ConnOption.java:161)
	at oracle.net.nt.ConnStrategy.execute(ConnStrategy.java:470)
	... 82 more
Caused by: java.io.InterruptedIOException: Socket read timed out
	at oracle.net.nt.TimeoutSocketChannel.handleInterrupt(TimeoutSocketChannel.java:307)
	at oracle.net.nt.TimeoutSocketChannel.<init>(TimeoutSocketChannel.java:84)
	at oracle.net.nt.TcpNTAdapter.connect(TcpNTAdapter.java:169)

SQL> select count(1), min(logon_time), max(logon_time), inst_id from gv$session where machine = '...' group by inst_id;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID
---------- ---------------- ---------------- ----------
        15 2018.03.15 21:45 2018.03.15 21:46          2

Tomcat:
java.sql.SQLException: Exception occurred while getting connection: oracle.ucp.UniversalConnectionPoolException: All connections in the Universal Connection Pool are in use
	at oracle.ucp.util.UCPErrorHandler.newSQLException(UCPErrorHandler.java:456)
	at oracle.ucp.util.UCPErrorHandler.throwSQLException(UCPErrorHandler.java:133)

RESULT: partially passed.
Action: check connection pool size and another pool parameters, like connection timeout. Blocking wait time when no free connection is available. The python app is configured to run in 20 threads, while UCP is configured have 15 connections max.

PS: UCP manages its own background threads:
Mar 15, 2018 10:28:47 PM org.apache.catalina.loader.WebappClassLoaderBase clearReferencesThreads
WARNING: The web application [tomcat-ucp-rest] appears to have started a thread named [UCP-worker-thread-5] but has failed to stop it. This is very likely to create a memory leak. Stack trace of thread:
 sun.misc.Unsafe.park(Native Method)
 java.util.concurrent.locks.LockSupport.parkNanos(Unknown Source)
 java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(Unknown Source)
 java.util.concurrent.ArrayBlockingQueue.poll(Unknown Source)
 java.util.concurrent.ThreadPoolExecutor.getTask(Unknown Source)
 java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)
 java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)
 java.lang.Thread.run(Unknown Source)


- Fast Connection Failover (UCP) test.
 - Configure UCP to react to ONS messages. In ucp.xml set:
      ons-configuration="nodes=node846:6200,node847:6200"
      fast-connection-failover-enabled="true"
 - Connections status:
SQL> select service_name, osuser, program, machine, module, action, terminal, inst_id from gv$session where machine in('...','UCP machine');

SERVICE_NA OSUSER                         PROGRAM                        MACHINE                        MODULE               ACTION               TERMINAL                          INST_ID
---------- ------------------------------ ------------------------------ ------------------------------ -------------------- -------------------- ------------------------------ ----------
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            1
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            1
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            1
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            1
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            1

 - Relocate the service:
$ srvctl relocate service -d INFRADB -s ACTEST -oldinst INFRADB1 -newinst INFRADB2

 - Connections status:
SQL> select service_name, osuser, program, machine, module, action, terminal, inst_id from gv$session where machine in('CZCHOWN5020546','UCP machine');

SERVICE_NA OSUSER                         PROGRAM                        MACHINE                        MODULE               ACTION               TERMINAL                          INST_ID
---------- ------------------------------ ------------------------------ ------------------------------ -------------------- -------------------- ------------------------------ ----------
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            2
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            2
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            2
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            2
ACTEST     UCP osuser                     UCP program                    CZCHOWN5020546                 UCP program                               UCP terminal                            2

 RESULT: PASSED. All unused connections failed over instanly.

HikariCP
 - deploy the app tomcat-hikari-rest
 - Check database connections:

SQL> select count(1), min(logon_time), max(logon_time), inst_id, service_name from gv$session where machine = '...' group by in
st_id, service_name;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID SERVICE_NAME
---------- ---------------- ---------------- ---------- ----------------------------------------------------------------
         5 2018.03.15 23:05 2018.03.15 23:05          1 ACTEST


 - start the python client app

 - Check database connections:
SQL> select count(1), min(logon_time), max(logon_time), inst_id, service_name from gv$session where machine = '...' group by inst_id, service_name;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID SERVICE_NAME
---------- ---------------- ---------------- ---------- ----------------------------------------------------------------
        20 2018.03.15 23:10 2018.03.15 23:11          1 ACTEST


 - relocate the service
SQL> select count(1), min(logon_time), max(logon_time), inst_id, service_name from gv$session where machine = '...' group by inst_id, service_name;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID SERVICE_NAME
---------- ---------------- ---------------- ---------- ----------------------------------------------------------------
         5 2018.03.15 23:11 2018.03.15 23:11          2 ACTEST
        15 2018.03.15 23:11 2018.03.15 23:11          1 ACTEST

SQL> select count(1), min(logon_time), max(logon_time), inst_id, service_name from gv$session where machine = '...' group by inst_id, service_name;

  COUNT(1) MIN(LOGON_TIME)  MAX(LOGON_TIME)     INST_ID SERVICE_NAME
---------- ---------------- ---------------- ---------- ----------------------------------------------------------------
        20 2018.03.15 23:13 2018.03.15 23:14          2 ACTEST


RESULT: PASSED
