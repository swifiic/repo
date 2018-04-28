/* Stored procedure for settling the accounts for users and updating the balance */
/* also invokes the procedure - assumed to be run as cron job once every hour - or early in the morning */
/* Initial Revision - Sudheer Reddy ; adapted in April 2018 - Abhishek Thakur */

delimiter //

use swifiic;
select count(*) OperatorLedgerEntries from OperatorLedger;

drop procedure if exists util ;
create procedure util(in lastLogId int ,in max_size int)
begin
declare curAuditId int ;
declare temp int default 0;
declare opLogId int ;
declare details text default '';
declare msg text default '';
declare i int default 0 ;
declare debitUid int;
declare creditUid int default null;
declare user_id int default null; 
declare amt int ;
declare totalAmt int signed default 0;
declare logs cursor for select LogId,CreditUserId,DebitUserId,Details,Amount from OperatorLedger where LogId>lastLogId limit 0,max_size;

open logs;

set temp = found_rows();

if temp>0 then 
    insert into Audit (AuditNotes,StartedAt,CompletedAt,FirstAffectedOperatorLogId,LastAffectedOperatorLogId,NumAffectedOperatorLogId,
    NumValueTransfers,TotalValueTransferAmount,AuditType) values ('Audit from script',now(),null,lastLogId+1,lastLogId
    +temp,temp,temp,0,'periodic');

    select max(AuditId) from Audit into curAuditId;

   while i<temp do 
   	fetch logs into opLogId,creditUid,debitUid,details,amt;
	

	/* if it is cedit we are just adding to the user (not debiting from operator)
	if it ids debit we are deducting from user and not adding to operator
	*/
	set totalAmt = totalAmt+amt;
	if strcmp(details,"Credit") then
        set msg = concat(msg,' ',creditUid);
	   	set user_id = creditUid;	
	elseif strcmp(details,"Debit") then 
       	set msg = concat(msg,' ',debitUid);
	   	set user_id = debitUid;	   
	   	set amt = -1*amt;
	else 
		select concat('Error in util procedure as the  ', details ,'should be either Credit or Debit') Message;
	end if; 
	
	update User set RemainingCreditPostAudit  = RemainingCreditPostAudit + amt,LastAuditedActivityAt  = now() where UserId =user_id;
   
   	update OperatorLedger set AuditLogId = curAuditId,AuditNotes='Audited from script' where LogId = opLogId; 
   	set i = i+1;
    END while;

	
    update Audit set CompletedAt = now(), TotalValueTransferAmount  = totalAmt where AuditId = curAuditId;
    select msg Effected_Users;
    close logs;
end if;
/*select temp T,lastLogId L; */
set msg = concat('Audited ',temp,' logs starting from LogId : ',(lastLogId+1));
select msg Message;
end// 


drop procedure if exists settlement;
create procedure settlement()
begin
declare n INT DEFAULT 0;
declare i INT DEFAULT 0;
declare amt INT DEFAULT 0;
declare max_size int default 100;
declare msg2 text default '';
declare lastLogId int default 0;
declare lastAuditId int default 0;
declare temp, temp2, temp3 int ;
declare lastAuditTime timestamp default null ;

select count(*) from Audit into temp;
select count(*) from OperatorLedger into temp2;

if temp2>0 then /* If atleast one OperatorLedger  entry exists*/
    if temp>0 then  /* prior audit */
        select max(AuditId) from Audit into lastAuditId;
        select CompletedAt from Audit where AuditId = lastAuditId into lastAuditTime;
        select LastAffectedOperatorLogId from Audit where AuditId = lastAuditId into lastLogId; 
        select count(*) from OperatorLedger where LogId > lastLogId  into temp3;

    else
        select min(LogId)-1 from OperatorLedger into lastLogId;
        select count(*) from OperatorLedger where LogId > lastLogId  into temp3;
    end if;


    if temp3 >0 then /*If atlest one entry in OperatorLedger is not Audited */

        if temp=0 or lastAuditTime is not null then 

            /*set lastAuditId = lastAuditId + 1; */
            call util(lastLogId,max_size);
        else 
            select concat('Error in Settlement procedure as the last Audit with Id ',lastAuditId,' is running..!!') Message;
        end if;

    else
        select 'No entries in OperatorLedger table. that are not yet Audited' Message;
    end if; /* temp3 >0  end if */

else /* else , if OperatorLedger table is empty*/
    select 'No entries in OperatorLedger table..!!' Message;
end if;
End;

//
delimiter ;

call settlement();

