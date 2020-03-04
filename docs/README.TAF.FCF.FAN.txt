
 - Transparent Application Failover (TAF)
   Implemented on OCI library level.

   srvctl add service
    - tafpolicy [ NONE | BASIC | PRECONNECT]
    - failovertype [ NONE | SESSION | SELECT | TRANSACTION ]
    - failovermethod [ NONE | BASIC ]   #not strictly necessary
    - failoverretry <failover_retries>  #depends on used architecture    
    - failoverdelay <failover_delay>

   i.e. tnsnames.ora (FAILOVER_MODE = (TYPE = SELECT) (METHOD = BASIC/PRECONNECT) (RETRIES = 2) (DELAY = 1))))
    
 - Transparent Application Failover (TAF) is a feature of the Java Database Connectivity (JDBC) Oracle Call Interface (OCI) driver.
   It enables the application to automatically reconnect to a database, if the database instance to which the connection is made fails. In this case, the active transactions roll back.

 - (Doc ID 465423.1) Can the JDBC Thin Driver Do Failover by Specifying FAILOVER_MODE?
 No. JDBC Thin cannot use FAILOVER_MODE. The failover defined by FAILOVER_MODE is Transparent Application Failover (TAF). JDBC Thin does not support TAF.
 However JDBC Thin does support Fast Connection Failover (FCF).

 TAF is NOT subject of this testing

PS: FAILOVER_TYPE=TRANSACTION is used by "Application Continuity for Java" and is supported by JDBC drivers


 - Fast Application Notification (FAN)
   Indenpendent from JDBC/OCI drivers.

   srvctl add service
    ...
    - notification [TRUE| FALSE] #To enable FAN for JDBC/OCI/ODP.net connections
     
   ONS notifications are sent via dedicated port 6200. Use SimpleFan.jar to receive notifications about clusterware events.   


 - Fast Connection Failover (FCF)
   ucp.xml
    fast-connection-failover-enabled="true"

   - Implemented on UCP level
   - Uses FAN events.
   - Does not protect JDBC transactions nor JDBC connections after failure


 - Application Continuity (AC) / Transaction Guard(TG)
   Addresses temporary recoverable outages of instances, databases and network communications.
   Implemented in thin JDBC drivers(AC)
   Implemented on server side(TG)

   srvctl add service
    - failovertype TRANSACTION   # to enable Application Continuity
    - commit_outcome TRUE        # to enable Transaction Guard
    - retention 86400            # the number of seconds the commit outcome is retained
    - replay_init_time 900       # seconds after which replay will not be initiated
    - failoverretry 20
    - failoverdelay 2
    - notification TRUE          # with Oracle Restart, to avoid ORA-44781 during service start

    oracle.jdbc.replay.OracleDataSourceImpl - records all information on JDBC driver side,
    when SQLRecoverableException is thrown JDBC driver re-connects and replays the transaction.

