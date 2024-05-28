
CREATE OR REPLACE FUNCTION "TESTUSER"."SLOW_NUMBER1" (n number)
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
