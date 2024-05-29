# Configure dedicated Application service on RAC cluster

    $ srvctl mofify service -db TEST19C_1 -service ACTEST -preferred TEST19C1,TEST19C2 \
       -policy automatic -failovertype TRANSACTION -clbgoal SHORT -rlbgoal SERVICE_TIME -notification true -commit_outcome TRUE

    $ srvctl status service -db TEST19C_1 -service ACTEST
    Service ACTEST is not running.
    $ srvctl start service -db TEST19C_1 -service ACTEST
    $ srvctl status service -db TEST19C_1 -service ACTEST
    Service ACTEST is running on instance(s) TEST19C1,TEST19C2

# Start the app having initial-pool-size=15, min-pool-size=10

    SQL> select inst_id, count(1) from gv$session where username = 'TESTUSER' group by inst_id;
       INST_ID   COUNT(1)
    ---------- ----------
             1         10
             2          5

    SQL> !srvctl stop service -db TEST19C_1 -service ACTEST -i TEST19C1
    SQL> select inst_id, count(1) from gv$session where username = 'TESTUSER' group by inst_id;
    INST_ID   COUNT(1)
    ---------- ----------
             2         10

    SQL> !srvctl start service -db TEST19C_1 -service ACTEST -i TEST19C1
    SQL> select inst_id, count(1) from gv$session where username = 'TESTUSER' group by inst_id;
    
    INST_ID   COUNT(1)
    ---------- ----------
             1          5
             2          6
