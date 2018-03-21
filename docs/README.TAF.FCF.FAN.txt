
 - Transparent Application Failover (TAF)

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

 - Fast Connection Failover (FCF)
 
