SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

CREATE DATABASE IF NOT EXISTS `swifiic` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `swifiic`;

DROP TABLE IF EXISTS `App`;
CREATE TABLE IF NOT EXISTS `App` (
  `AppId` varchar(64) NOT NULL DEFAULT '',
  `AppName` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`AppId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `AppLedger`;
CREATE TABLE IF NOT EXISTS `AppLedger` (
  `LogId` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `EventNotes` varchar(256) DEFAULT NULL,
  `ReqUserId` int(11) DEFAULT NULL,
  `ReqAppId` int(11) DEFAULT NULL,
  `ReqAppRole` varchar(64) DEFAULT NULL,
  `ReqDeviceId` int(11) DEFAULT NULL,
  `CreditDeviceId` int(11) DEFAULT NULL,
  `DebitDeviceId` int(11) DEFAULT NULL,
  `Details` varchar(1024) DEFAULT NULL,
  `TimeReq` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TimeCommitted` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `ReqOrCommitID` bigint(20) DEFAULT NULL,
  `DevSeqId` bigint(20) DEFAULT NULL,
  `Value` int(11) DEFAULT NULL,
  `Status` enum('audited','success','failed','aborted','commit-pending','ack-pending','others') DEFAULT NULL,
  `StatusNotes` varchar(64) DEFAULT NULL,
  `AlibiDevDetails` varchar(256) DEFAULT NULL,
  `AuditLogId` bigint(20) DEFAULT NULL,
  `AuditNotes` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`LogId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `AppUserMaps`;
CREATE TABLE IF NOT EXISTS `AppUserMaps` (
  `AppId` varchar(64) NOT NULL DEFAULT '',
  `UserId` int(11) NOT NULL DEFAULT '0',
  `Role` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`AppId`,`UserId`,`Role`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `Audit`;
CREATE TABLE IF NOT EXISTS `Audit` (
  `AuditId` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `AuditNotes` varchar(256) DEFAULT NULL,
  `StartedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CompletedAt` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `FirstAffectedOperatorLogId` bigint(20) unsigned DEFAULT NULL,
  `LastAffectedOperatorLogId` bigint(20) unsigned DEFAULT NULL,
  `NumAffectedOperatorLogId` int(10) unsigned DEFAULT NULL,
  `FirstAffectedAppLogId` bigint(20) unsigned DEFAULT NULL,
  `LastAffectedAppLogId` bigint(20) unsigned DEFAULT NULL,
  `NumAffectedAppLogId` int(10) unsigned DEFAULT NULL,
  `NumValueTransfers` int(10) unsigned DEFAULT NULL,
  `TotalValueTransferAmount` int(10) unsigned DEFAULT NULL,
  `AuditType` enum('periodic','user-requested','billing','others') DEFAULT NULL,
  PRIMARY KEY (`AuditId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

DROP TABLE IF EXISTS `Device`;
CREATE TABLE IF NOT EXISTS `Device` (
  `DeviceID` int(11) NOT NULL AUTO_INCREMENT,
  `MAC` varchar(64) DEFAULT NULL,
  `UserId` int(11) DEFAULT NULL,
  `CreatedLedgerEntry` bigint(20) DEFAULT NULL,
  `Notes` varchar(64) DEFAULT NULL,
  `CreateTime` datetime DEFAULT NULL,
  `PeriodicAuditTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `PeriodicAuditNotes` varchar(256) DEFAULT NULL,
  `LastAuditedActiveityAt` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`DeviceID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;


DROP TABLE IF EXISTS `OperatorLedger`;
CREATE TABLE IF NOT EXISTS `OperatorLedger` (
  `LogId` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `EventNotes` varchar(256) DEFAULT NULL,
  `ReqUserId` int(11) DEFAULT NULL,
  `ReqDeviceId` int(11) DEFAULT NULL,
  `Details` varchar(1024) DEFAULT NULL,
  `Time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `AuditLogId` bigint(20) DEFAULT NULL,
  `AuditNotes` bigint(20) DEFAULT NULL,
  `CreditUserId` int(11) DEFAULT NULL,
  `DebitUserId` int(11) DEFAULT NULL,
  `Amount` int(11) DEFAULT NULL,
  PRIMARY KEY (`LogId`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=269 ;



DROP TABLE IF EXISTS `User`;
CREATE TABLE IF NOT EXISTS `User` (
  `UserId` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Name` varchar(64) DEFAULT NULL,
  `EmailAddress` varchar(64) DEFAULT NULL,
  `MobileNumber` varchar(32) DEFAULT NULL,
  `Address` varchar(256) DEFAULT NULL,
  `AddressVerificationNotes` varchar(256) DEFAULT NULL,
  `CreateTime` datetime DEFAULT NULL,
  `CreatedLedgerId` bigint(20) DEFAULT NULL,
  `RemainingCreditPostAudit` int(11) DEFAULT NULL,
  `Status` enum('active','suspended','deleted','operator') DEFAULT NULL,
  `Password` varchar(128) DEFAULT NULL,
  `ImageFile` blob,
  `IdProofFile` blob,
  `AddrProofFile` blob,
  `Alias` varchar(32) DEFAULT NULL,
  `ProfilePic` blob,
  `LastAuditedActivityAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `DtnId` varchar(64) DEFAULT NULL,
  `MacAddress` varchar(64) DEFAULT NULL,
  `HubRecievedAt` datetime DEFAULT NULL,
  `TimeOfLastUpdateFromApp` datetime DEFAULT NULL,
  `LastHubValueSutaReports` datetime DEFAULT NULL,
  `LastHubUpdateSutaGotAT` datetime DEFAULT NULL,
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `Name` (`Name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

INSERT INTO `User` (`UserId`, `Name`, `EmailAddress`, `MobileNumber`, `Address`, `AddressVerificationNotes`, `CreateTime`, `CreatedLedgerId`, `RemainingCreditPostAudit`, `Status`, `Password`, `ImageFile`, `IdProofFile`, `AddrProofFile`, `Alias`, `ProfilePic`, `LastAuditedActivityAt`, `DtnId`, `MacAddress`, `TimeOfLastUpdateFromApp`) VALUES
(1, 'SWiFiIC Operator', 'temp@gmail.com', '9876543210', 'India', 'Default', now(), 0, 0, 'operator', 'simple', 0x89504e470d0a1a0a0000000d494844520000010000000100010300000066bc3a2500000003504c5445b5d0d0630416ea0000001f494441546881edc1010d000000c2a0f74f6d0e37a00000000000000000be0d210000019a60e1d50000000049454e44ae426082 , 0x89504e470d0a1a0a0000000d494844520000010000000100010300000066bc3a2500000003504c5445b5d0d0630416ea0000001f494441546881edc1010d000000c2a0f74f6d0e37a00000000000000000be0d210000019a60e1d50000000049454e44ae426082, 0x89504e470d0a1a0a0000000d494844520000010000000100010300000066bc3a2500000003504c5445b5d0d0630416ea0000001f494441546881edc1010d000000c2a0f74f6d0e37a00000000000000000be0d210000019a60e1d50000000049454e44ae426082, 'operator', 0x89504e470d0a1a0a0000000d494844520000010000000100010300000066bc3a2500000003504c5445b5d0d0630416ea0000001f494441546881edc1010d000000c2a0f74f6d0e37a00000000000000000be0d210000019a60e1d50000000049454e44ae426082, now(), 'dtn://opertor-mobile.dtn', '00:00:00:00:00:00', '2015-01-01 00:00:00');

INSERT INTO `Device` (`DeviceID`, `MAC`, `UserId`, `CreateTime`) VALUES (1, '00:00:00:00:00:00', 1, now());

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
