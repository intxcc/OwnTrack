SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

CREATE TABLE IF NOT EXISTS `location_history` (
	`timestamp` datetime NOT NULL,
	`latitude` double NOT NULL,
	`longitude` double NOT NULL,
	`accuracy` float NOT NULL,
	`speed` float NOT NULL,
	`provider` char(8) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = latin1;

CREATE TABLE IF NOT EXISTS `used_salt` (
	`salt` char(28) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = latin1;


ALTER TABLE `location_history`
	ADD PRIMARY KEY (`timestamp`);

ALTER TABLE `used_salt`
	ADD UNIQUE KEY `salt` (`salt`);
