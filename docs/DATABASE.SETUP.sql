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

grant select_catalog_role to "ONSTEST"
/

drop table "ONSTEST"."BOOK"
/

create table "ONSTEST"."BOOK"
(
	id number,
	name varchar2(128),
	author varchar2(128),
	cnt number default(1),
	CONSTRAINT BOOKS_PK PRIMARY KEY (id)
)
/

create sequence book_id_seq start with 10 increment by 10
/

create or replace trigger book_id_trig  before insert ON "ONSTEST"."BOOK"
FOR EACH ROW
WHEN (new.ID IS NULL)
BEGIN
  :NEW.ID := book_id_seq.NEXTVAL;
END book_id_trig;
/

drop table "ONSTEST"."ACDEMOTAB"
/

create table "ONSTEST"."ACDEMOTAB"
(
	message varchar2(128),
	inst_id number	
)
/

create index "ONSTEST"."BOOK_IX1" on "ONSTEST"."BOOK"(upper(name))
/

CREATE OR REPLACE PROCEDURE "ONSTEST"."CANICARRYON" (pi_message varchar2)
as
begin
 insert into acdemotab (message) values (pi_message);
 dbms_lock.sleep(20);
 commit;
end;
/

CREATE OR REPLACE FUNCTION "ONSTEST"."SLOW_NUMBER" (n number)
return number
as
begin
 dbms_lock.sleep(n);
 return n;
end;
/

show errors

CREATE OR REPLACE FUNCTION "ONSTEST"."SLOW_NUMBER1" (n number)
return number
as
 temp_full EXCEPTION;
 deadlock  EXCEPTION;
 PRAGMA EXCEPTION_INIT (temp_full, -1652);
 PRAGMA EXCEPTION_INIT (deadlock, -60);
 random number;
begin
 --dbms_lock.sleep(0);
 random := dbms_random.value(1,10);
 if random > 7 then
	RAISE deadlock;
 end if;
 return n;
end;
/

show errors

-- srvctl add service -db INFRADB -service ACTEST -preferred INFRADB1 -available INFRADB2 -policy automatic -failovertype TRANSACTION -clbgoal SHORT -rlbgoal SERVICE_TIME -notification true -commit_outcome TRUE
-- srvctl add service -db TEST19C_1 -service ACTEST -preferred TEST19C1 -available TEST19C2 -policy automatic -failovertype TRANSACTION -clbgoal SHORT -rlbgoal SERVICE_TIME -notification true -commit_outcome TRUE
-- srvctl start service -db TEST19C_1 -service ACTEST
-- srvctl relocate service -db TEST19C_1 -service ACTEST