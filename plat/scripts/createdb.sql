
CREATE DATABASE swifiic;

CREATE USER 'swifiic'@'localhost' IDENTIFIED BY 'fixit';
GRANT ALL PRIVILEGES ON swifiic.* TO 'swifiic'@'localhost';


GRANT USAGE ON swifiic.* TO 'swifiic'@'localhost' IDENTIFIED BY 'fixit';

use swifiic;


/* Ledgers are for accounting : most communication responses will include a 
   reference to either OperatorLedger or AppLedger
   Operator Ledger is primarily for AppHub accounting and consistency
   E.g. Adding a new user, Re-charge of accounts, setting App Credit limits etc. 
   
   Each SWiFiIC operation ends up hitting either OperatorLedger or AppLedger (minimum once)
   
   Periodically (possibly hourly) Audit runs, close off (garbage collect) completed info
   and abort timed-out transactions (e.g. pending for > 1 week).
*/

CREATE TABLE OperatorLedger 
/* user additions + credits - i.e. Operator has physical interaction with real world */
/* access to this table should be limited i.e. do not use GRANT *.* - TBD XXX */
(
    LogId BIGINT UNSIGNED AUTO_INCREMENT, /* Unique Id for each Ledger entry*/
    EventNotes VARCHAR(256), /* description of the Ledger entry*/
    ReqUserId INT, /* User Id of the User who requested the Ledger entry*/
    ReqDeviceId INT, /* Device Id , where the Ledger Request came from*/
    CreditUserId INT, /* Id of the device to be credited */
    DebitUserId INT, /* Id od the device to be debited */
    Details VARCHAR(1024), /* Details of the Ledger Entry - Credit or Debit */
    Time TIMESTAMP, /* Timestamp of the ledger entry
    AuditLogId BIGINT,  /* LogId of the Audit that fulfills the current ledger entry  */
    AuditNotes BIGINT, /* Small note about the audit*/
    /* The above two fields are filled from the settlement script as the ledger entry gets audited */   
    Amount BIGINT , /* Amount to be credited or debited */
    PRIMARY KEY (LogId) 
);

CREATE TABLE AppLedger 
(
    LogId BIGINT UNSIGNED AUTO_INCREMENT,
    EventNotes VARCHAR(256),
    ReqUserId INT,
    ReqAppId INT,
    ReqAppRole VARCHAR(64),
    ReqDeviceId INT,
    CreditUserId INT,
    DebitUserId INT,
    Details VARCHAR(1024),
    TimeReq TIMESTAMP,
    TimeCommitted TIMESTAMP,
    ReqOrCommitID BIGINT,
    DevSeqId BIGINT,
    Value INT,
    Status ENUM('audited','success', 'failed', 'aborted', 'commit-pending','ack-pending','others'),
    StatusNotes VARCHAR(64), /* especially for others */
    AlibiDevDetails VARCHAR(256),
    AuditLogId BIGINT,
    AuditNotes BIGINT,
    PRIMARY KEY (LogId)
);

/* Audit runs like cron jobs, or on user request - e.g. account closure
   All entries in Ledgers are sanitized if pending for too long */
   
CREATE TABLE Audit
(
    AuditId BIGINT UNSIGNED AUTO_INCREMENT, /* Unique Id for each Audit entry*/
    AuditNotes VARCHAR(256), /* Details about who did the audit etc.. */ 
    StartedAt TIMESTAMP, /* Start time of the audit*/ 
    CompletedAt TIMESTAMP, /*End time of the audit */
    FirstAffectedOperatorLogId BIGINT UNSIGNED, /* LogId of the First Ledger entry in the current audit*/  
    LastAffectedOperatorLogId BIGINT UNSIGNED,/* LogId of the Last Ledger entry in the current audit*/  
    NumAffectedOperatorLogId INT UNSIGNED,/* Number of ledger entries audited */ 
    FirstAffectedAppLogId BIGINT UNSIGNED,
    LastAffectedAppLogId BIGINT UNSIGNED,
    NumAffectedAppLogId INT UNSIGNED,
    NumValueTransfers INT UNSIGNED, /* Total number of ledger entries audited */
    TotalValueTransferAmount INT UNSIGNED, /* total amount (both credit and debit) in the current audit */  
    AuditType ENUM('periodic','user-requested','billing','others'), 
    /* type of the audit */
    PRIMARY KEY (AuditId)
);

/** following is for 0.1.0 release - target  **/
CREATE TABLE User 
(
    UserId INT UNSIGNED AUTO_INCREMENT, 
    Name VARCHAR(64),
    DtnId VARCHAR(64),
    Alias VARCHAR(64), /* Alias name of the user */
    EmailAddress VARCHAR(64),
    MobileNumber VARCHAR(32),
    Address VARCHAR(256),
    ImageFile BLOB , /* Actual user photo */
    IdProofFile BLOB , /* IdProof image */ 
    AddrProofFile BLOB , /* Address Proof image */
    ProfilePic BLOB , /* Profile pic of the User */
    AddressVerificationNotes VARCHAR(256), /* Address Notes */
    CreateTime DATETIME, /* TimeStamp when the user is added*/
    CreatedLedgerId BIGINT, 
    RemainingCreditPostAudit INT, /* Credit in the User's account after the recent audit */
    Status ENUM('active', 'suspended', 'deleted', 'operator'), /* at least one operator should exist */
    Password VARCHAR(128), 
    LastAuditedActivityAt TIMESTAMP, /* Timestamp when the user's credit is last audited */
    PRIMARY KEY (UserId)
);

CREATE TABLE Device 
(
    DeviceID INT AUTO_INCREMENT,
    MAC VARCHAR(64),
    UserId INT,
    CreatedLedgerEntry BIGINT,
    Notes VARCHAR(64),
    CreateTime DATETIME,
    PeriodicAuditTime TIMESTAMP,
    PeriodicAuditNotes VARCHAR(256),
    LastAuditedActivityAt TIMESTAMP,
    PRIMARY KEY (DeviceId)
);

CREATE TABLE App
(
    AppId VARCHAR(64),
    AppName VARCHAR(64),
    PRIMARY KEY (AppId)
);

CREATE TABLE AppUserMaps
(
    AppId VARCHAR(64),
    UserId INT,
    Role VARCHAR(64), /* May need Revisit */
    PRIMARY KEY (AppId,UserId,Role)
);

CREATE TABLE PearlApp
(
    AppId VARCHAR(64),
    AppName VARCHAR(64),
    PRIMARY KEY (AppId)
);


