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

create table "ONSTEST"."ACDEMOTAB"
(
	message varchar2(128),
	inst_id number	
)
/

create index "ONSTEST"."BOOK_IX1" on "ONSTEST"."BOOK"(upper(name))
/

drop table "ONSTEST"."ACDEMOTAB"
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
