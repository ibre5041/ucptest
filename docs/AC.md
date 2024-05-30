# Configure ucp.xml

          connection-factory-class-name="oracle.jdbc.replay.OracleDataSourceImpl"

# Run AppJPA/AppHibernate
This opens a transaction in Oracle DB, never commits after 100000 loops

    Inserting:(340)Book [id=2000000340, name=MP4WAEZA8YP2FBPYUQT1, author=Hello world task author]
    Hibernate: select bookentity0_.id as id1_0_0_, bookentity0_.author as author2_0_0_, bookentity0_.name as name3_0_0_ from BOOK bookentity0_ where bookentity0_.id=?
    Hibernate: insert into BOOK (author, name, id) values (?, ?, ?)
    Inserting:(341)Book [id=2000000341, name=WJLVVPCLV90ZDMNGNRD5, author=Hello world task author]
    ...

# Detect this session process

    SQL> select inst_id, machine, STATUS, taddr, s.username, SPID from gv$session s join v$process p on s.paddr = p.addr and TADDR is not null;
    INST_ID    MACHINE        STATUS   TADDR            USERNAME                       SPID     
    ---------- -------------- -------- ---------------- ------------------------------ ------------------------
             1 nuc            INACTIVE 000000007237BE50 ONSTEST                        192990

# Kill it

    SQL> !kill -9 192990
    SQL> !kill -9 192990
    /bin/bash: line 1: kill: (192990) - No such process

When is session process is killed, application does not notice, transaction is rolled back and, is replied and runs again.
