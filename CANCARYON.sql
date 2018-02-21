create user onstest identified by onstest default tablespace users
/

alter user onstest quota unlimited on users
/

grant connect, resource to onstest
/

grant select on sys.v_$instance to "ONSTEST"
/

grant execute on sys.dbms_lock to "ONSTEST"
/

drop table "ONSTEST"."ACDEMOTAB"
/

create table "ONSTEST"."ACDEMOTAB"
(
	message varchar2(128),
	inst_id number	
)
/

CREATE OR REPLACE PROCEDURE "ONSTEST"."CANICARRYON" (pi_message varchar2)
as
begin
 insert into acdemotab (message) values (pi_message);
 dbms_lock.sleep(20);
 commit;
end;
/

show errors

-- srvctl add service -db INFRADB -service ACTEST -preferred INFRADB1 -available INFRADB2 -policy automatic -failovertype TRANSACTION -clbgoal SHORT -rlbgoal SERVICE_TIME -notification true -commit_outcome TRUE
