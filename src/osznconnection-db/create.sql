/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/* System and helper tables */

DROP TABLE IF EXISTS `sequence`;
CREATE TABLE `sequence`(
   `sequence_name` VARCHAR(100) NOT NULL COMMENT 'Наименование генератора',
   `sequence_value` bigint UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Значение генератора',
   PRIMARY KEY (`sequence_name`)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Вспомогательная таблица для хранения значений, генерируемых табличными генераторами';

DROP TABLE IF EXISTS `locales`;

CREATE TABLE `locales` (
  `locale` VARCHAR(2) NOT NULL,
  `system` TINYINT(1) NOT NULL default '0',
  PRIMARY KEY  (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `string_culture`;

CREATE TABLE `string_culture` (
  `pk_id` BIGINT(20) NOT NULL auto_increment,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`, `locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_string_culture__locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity`;

CREATE TABLE `entity` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `entity_table` VARCHAR(100) NOT NULL,
  `entity_name_id` BIGINT(20) NOT NULL,
  `strategy_factory` VARCHAR(100) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `unique_entity_table` (`entity_table`),
  KEY `key_entity_name_id` (`entity_name_id`),
  CONSTRAINT `fk_entity__string_culture` FOREIGN KEY (`entity_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity_type`;

CREATE TABLE `entity_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `entity_id` BIGINT(20) NOT NULL,
  `entity_type_name_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `key_entity_id` (`entity_id`),
  KEY `key_entity_type_name_id` (`entity_type_name_id`),
  CONSTRAINT `fk_entity_type__entity` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_entity_type__string_culture` FOREIGN KEY (`entity_type_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity_attribute_type`;

CREATE TABLE `entity_attribute_type` (
  `id` BIGINT(20) NOT NULL auto_increment,
  `entity_id` BIGINT(20) NOT NULL,
  `mandatory` TINYINT(1) default 0 NOT NULL,
  `attribute_type_name_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL default CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL default NULL,
  `system` TINYINT(1) default 0 NOT NULL,
  PRIMARY KEY (`id`),
  KEY `key_entity_id` (`entity_id`),
  KEY `key_attribute_type_name_id` (`attribute_type_name_id`),
  CONSTRAINT `fk_entity_attribute_type__entity` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_entity_attribute_type__string_culture` FOREIGN KEY (`attribute_type_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity_attribute_value_type`;

CREATE TABLE `entity_attribute_value_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `attribute_value_type` VARCHAR(100) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  CONSTRAINT `fk_entity_attribute_value_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Entities */

-- ------------------------------
-- Apartment
-- ------------------------------
DROP TABLE IF EXISTS `apartment`;

CREATE TABLE `apartment` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (object_id),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_apartment__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_apartment__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `apartment_attribute`;

CREATE TABLE `apartment_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_apartment_attribute__apartment` FOREIGN KEY (`object_id`) REFERENCES `apartment`(`object_id`),
  CONSTRAINT `fk_apartment_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_apartment_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `apartment_string_culture`;

CREATE TABLE `apartment_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_apartment_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Room --
-- ------------------------------
DROP TABLE IF EXISTS `room`;

CREATE TABLE `room` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_room__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_room__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `room_attribute`;

CREATE TABLE `room_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_room_attribute__room` FOREIGN KEY (`object_id`) REFERENCES `room`(`object_id`),
  CONSTRAINT `fk_room_attribute__entity_attribute_type`
    FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_room_attribute__entity_attribute_value_type`
    FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `room_string_culture`;

CREATE TABLE `room_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_room_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Street
-- ------------------------------
DROP TABLE IF EXISTS `street`;

CREATE TABLE `street` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_street__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_street__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_attribute`;

CREATE TABLE `street_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_street_attribute__street` FOREIGN KEY (`object_id`) REFERENCES `street`(`object_id`),  
  CONSTRAINT `fk_street_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_street_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_string_culture`;

CREATE TABLE `street_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_street_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- City
-- ------------------------------
DROP TABLE IF EXISTS `city`;

CREATE TABLE `city` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),  
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_city__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `ft_city__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_attribute`;

CREATE TABLE `city_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_city_attribute__city` FOREIGN KEY (`object_id`) REFERENCES `city`(`object_id`),
  CONSTRAINT `fk_city_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_city_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_string_culture`;

CREATE TABLE `city_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_city_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Building
-- ------------------------------
DROP TABLE IF EXISTS `building`;

CREATE TABLE `building` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_building__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_building__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_attribute`;

CREATE TABLE `building_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_building_attribute__building` FOREIGN KEY (`object_id`) REFERENCES `building`(`object_id`),
  CONSTRAINT `fk_building_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_building_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_string_culture`;

CREATE TABLE `building_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_building_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- District --
-- ------------------------------
DROP TABLE IF EXISTS `district`;

CREATE TABLE `district` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_district__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_district__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `district_attribute`;

CREATE TABLE `district_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_district_attribute__district` FOREIGN KEY (`object_id`) REFERENCES `district`(`object_id`),
  CONSTRAINT `fk_district_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_district_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `district_string_culture`;

CREATE TABLE `district_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_district_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Region
-- ------------------------------
DROP TABLE IF EXISTS `region`;

CREATE TABLE `region` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),  
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_region__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_region__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `region_attribute`;

CREATE TABLE `region_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_region_attribute__region` FOREIGN KEY (`object_id`) REFERENCES `region`(`object_id`),
  CONSTRAINT `fk_region_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_region_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `region_string_culture`;

CREATE TABLE `region_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_region_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Country
-- ------------------------------
DROP TABLE IF EXISTS `country`;

CREATE TABLE `country` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),  
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_country__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_country__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `country_attribute`;

CREATE TABLE `country_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_country_attribute__country` FOREIGN KEY (`object_id`) REFERENCES `country`(`object_id`),
  CONSTRAINT `fk_country_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_country_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `country_string_culture`;

CREATE TABLE `country_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_country_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Organization
-- ------------------------------
DROP TABLE IF EXISTS `organization`;

CREATE TABLE `organization` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),  
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_organization__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_organization__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organization_attribute`;

CREATE TABLE `organization_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_organization_attribute__organization` FOREIGN KEY (`object_id`) REFERENCES `organization`(`object_id`),
  CONSTRAINT `fk_organization_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_organization_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organization_string_culture`;

CREATE TABLE `organization_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_organization_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Forms of ownership
-- ------------------------------
DROP TABLE IF EXISTS `ownership`;

CREATE TABLE `ownership` (
  `pk_id` bigint(20) NOT NULL auto_increment,
  `object_id` bigint(20) NOT NULL,
  `parent_id` bigint(20),
  `parent_entity_id` bigint(20),
  `entity_type_id` bigint(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),  
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_ownership__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_ownership__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ownership_attribute`;

CREATE TABLE `ownership_attribute` (
  `pk_id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20),
  `value_type_id` bigint(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL default CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL default NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_ownership_attribute__ownership` FOREIGN KEY (`object_id`) REFERENCES `ownership`(`object_id`),
  CONSTRAINT `fk_ownership_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_ownership_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ownership_string_culture`;

CREATE TABLE `ownership_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_ownership_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Privileges
-- ------------------------------
DROP TABLE IF EXISTS `privilege`;

CREATE TABLE `privilege` (
  `pk_id` bigint(20) NOT NULL auto_increment,
  `object_id` bigint(20) NOT NULL,
  `parent_id` bigint(20),
  `parent_entity_id` bigint(20),
  `entity_type_id` bigint(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_privilege__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_privilege__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `privilege_attribute`;

CREATE TABLE `privilege_attribute` (
  `pk_id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20),
  `value_type_id` bigint(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL default CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL default NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_privilege_attribute__privilege` FOREIGN KEY (`object_id`) REFERENCES `privilege`(`object_id`),
  CONSTRAINT `fk_privilege_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_privilege_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `privilege_string_culture`;

CREATE TABLE `privilege_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_privilege_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- User info
-- ------------------------------
DROP TABLE IF EXISTS `user_info`;

CREATE TABLE `user_info` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),  
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_user_info__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_user_info__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_info_attribute`;

CREATE TABLE `user_info_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `attribute_id` BIGINT(20) NOT NULL,
  `object_id` BIGINT(20) NOT NULL,
  `attribute_type_id` BIGINT(20) NOT NULL,
  `value_id` BIGINT(20),
  `value_type_id` BIGINT(20) NOT NULL,
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_id` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  KEY `key_value_id` (`value_id`),
  KEY `key_value_type_id` (`value_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_user_info__user_info` FOREIGN KEY (`object_id`) REFERENCES `user_info`(`object_id`),
  CONSTRAINT `fk_user_info__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_user_info__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_info_string_culture`;

CREATE TABLE `user_info_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(64),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `FK_user_info_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- User
-- ------------------------------
DROP TABLE IF EXISTS `user`;

CREATE TABLE  `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `login` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `user_info_object_id` BIGINT(20),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_login` (`login`),
  KEY `key_user_info_object_id` (`user_info_object_id`),
  CONSTRAINT `fk_user__user_info` FOREIGN KEY (`user_info_object_id`) REFERENCES `user_info` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Usergroup
-- ------------------------------
DROP TABLE IF EXISTS `usergroup`;

CREATE TABLE  `usergroup` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `login` VARCHAR(45) NOT NULL,
  `group_name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_login__group_name` (`login`, `group_name`),
  CONSTRAINT `fk_usergroup__user` FOREIGN KEY (`login`) REFERENCES `user` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Log
-- ------------------------------
DROP TABLE IF EXISTS `log`;

CREATE TABLE  `log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `date` DATETIME,
  `login` VARCHAR(45),
  `module` VARCHAR(100),
  `object_id` BIGINT(20),
  `controller` VARCHAR(100),
  `model` VARCHAR(100),
  `event` VARCHAR(100),
  `status` VARCHAR(100),
  `description` VARCHAR(255),
  PRIMARY KEY (`id`),
  KEY `key_login` (`login`),
  KEY `key_date` (`date`),
  KEY `key_controller` (`controller`),
  KEY `key_model` (`model`),
  KEY `key_event` (`event`),
  KEY `key_module` (`module`),
  KEY `key_status` (`status`),
  KEY `key_description` (`description`),
  CONSTRAINT `fk_log__user` FOREIGN KEY (`login`) REFERENCES `user` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Log change
-- ------------------------------
DROP TABLE IF EXISTS `log_change`;

CREATE TABLE `log_change` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `log_id` BIGINT(20) NOT NULL,
    `attribute_id` BIGINT(20),
    `collection` VARCHAR(100),
    `property` VARCHAR(100),
    `old_value` VARCHAR(500),
    `new_value` VARCHAR(500),
    `locale` VARCHAR(2),
    PRIMARY KEY (`id`),
    KEY `key_log` (`log_id`),
    CONSTRAINT `fk_log_change__log` FOREIGN KEY (`log_id`) REFERENCES `log` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ------------------------------
-- Request File
-- ------------------------------
DROP TABLE IF EXISTS `request_file_group`;

CREATE TABLE `request_file_group` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ------------------------------
-- Request File
-- ------------------------------
DROP TABLE IF EXISTS `request_file`;

CREATE TABLE `request_file` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT(20),
    `loaded` DATETIME NOT NULL,
    `name` VARCHAR(20) NOT NULL,
    `directory` VARCHAR(255),
    `organization_id` BIGINT(20) NOT NULL,
    `registry` INT(2) NOT NULL,
    `month` INT(2) NOT NULL,
    `year` INT(4) NOT NULL,
    `dbf_record_count` BIGINT(20) NOT NULL DEFAULT 0,
    `length` BIGINT(20),
    `check_sum` VARCHAR(32),
    `type` VARCHAR(50),
    `status` INTEGER NULL COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestFile$STATUS',
    `status_detail` INTEGER NULL COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestFile$STATUS_DETAIL',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_id` (`name`, `organization_id`, `registry`, `month`, `year`), 
    KEY `key_group_id` (`group_id`),
    KEY `key_loaded` (`loaded`),
    KEY `key_name` (`name`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_registry` (`registry`),
    KEY `key_month` (`month`),
    KEY `key_year` (`year`) ,
    KEY `key_type` (`type`) ,
    KEY `key_status` (`status`) ,
    KEY `key_status_detail` (`status_detail`),
    CONSTRAINT `fk_request_file__request_file_group` FOREIGN KEY (`group_id`) REFERENCES `request_file_group` (`id`),
    CONSTRAINT `fk_request_file__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Payment
-- ------------------------------
DROP TABLE IF EXISTS `payment`;

CREATE TABLE `payment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `request_file_id` BIGINT(20) NOT NULL,
    `account_number` VARCHAR(100),

    `internal_city_id` BIGINT(20),
    `internal_street_id` BIGINT(20),
    `internal_street_type_id` BIGINT(20),
    `internal_building_id` BIGINT(20),
    `internal_apartment_id` BIGINT(20),

    `outgoing_city` VARCHAR(100),
    `outgoing_district` VARCHAR(100),
    `outgoing_street` VARCHAR(100),
    `outgoing_street_type` VARCHAR(100),
    `outgoing_building_number` VARCHAR(100),
    `outgoing_building_corp` VARCHAR(100),
    `outgoing_apartment` VARCHAR(100),

    `status` INTEGER NOT NULL DEFAULT 200 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus',

    `OWN_NUM` VARCHAR (15) COMMENT 'Номер дела',
    `REE_NUM` INT(2) COMMENT 'Номер реестра',
    `OPP` VARCHAR(8) COMMENT 'Признаки наличия услуг',
	`NUMB` INT(2) COMMENT 'Общее число зарегистрированных',
	`MARK` INT(2) COMMENT 'К-во людей, которые пользуются льготами',
	`CODE` INT(4) COMMENT 'Код ЖЭО',
	`ENT_COD` INT(10) COMMENT 'Код ЖЭО ОКПО',
	`FROG`  DECIMAL(5,1) COMMENT 'Процент льгот',
    `FL_PAY` DECIMAL(9,2) COMMENT 'Общая плата',
	`NM_PAY` DECIMAL(9,2) COMMENT 'Плата в пределах норм потребления',
	`DEBT` DECIMAL(9,2) COMMENT 'Сумма долга',
	`CODE2_1` INT(6) COMMENT 'Оплата жилья',
	`CODE2_2` INT(6) COMMENT 'система',
	`CODE2_3` INT(6) COMMENT 'Горячее водоснабжение',
	`CODE2_4` INT(6) COMMENT 'Холодное водоснабжение',
	`CODE2_5` INT(6) COMMENT 'Газоснабжение',
	`CODE2_6` INT(6) COMMENT 'Электроэнергия',
	`CODE2_7` INT(6) COMMENT 'Вывоз мусора',
	`CODE2_8` INT(6) COMMENT 'Водоотведение',
	`NORM_F_1` DECIMAL(10,4) COMMENT 'Общая площадь (оплата жилья)',
	`NORM_F_2` DECIMAL(10,4) COMMENT 'Объемы потребления (отопление)',
	`NORM_F_3` DECIMAL(10,4) COMMENT 'Объемы потребления (горячего водо.)',
	`NORM_F_4` DECIMAL(10,4) COMMENT 'Объемы потребления (холодное водо.)',
	`NORM_F_5` DECIMAL(10,4) COMMENT 'Объемы потребления (газоснабжение)',
	`NORM_F_6` DECIMAL(10,4) COMMENT 'Объемы потребления (электроэнергия)',
	`NORM_F_7` DECIMAL(10,4) COMMENT 'Объемы потребления (вывоз мусора)',
	`NORM_F_8` DECIMAL(10,4) COMMENT 'Объемы потребления (водоотведение)',
	`OWN_NUM_SR` VARCHAR(15) COMMENT 'Лицевой счет в обслуж. организации',
	`DAT1` DATE COMMENT 'Дата начала действия субсидии',
	`DAT2` DATE COMMENT 'Дата формирования запроса',
	`OZN_PRZ` INT(1) COMMENT 'Признак (0 - автоматическое назначение, 1-для ручного расчета)',
	`DAT_F_1` DATE COMMENT 'Дата начала для факта',
	`DAT_F_2` DATE COMMENT 'Дата конца для факта',
	`DAT_FOP_1` DATE COMMENT 'Дата начала для факта отопления',
	`DAT_FOP_2` DATE COMMENT 'Дата конца для факта отопления',
	`ID_RAJ` VARCHAR(5) COMMENT 'Код района',
	`SUR_NAM` VARCHAR(30) COMMENT 'Фамилия',
	`F_NAM` VARCHAR(15) COMMENT 'Имя',
	`M_NAM` VARCHAR(20) COMMENT 'Отчество',
	`IND_COD` VARCHAR(10) COMMENT 'Идентификационный номер',
	`INDX` VARCHAR(6) COMMENT 'Индекс почтового отделения',
	`N_NAME` VARCHAR(30) COMMENT 'Название населенного пункта',
	`VUL_NAME` VARCHAR(30) COMMENT 'Название улицы',
	`BLD_NUM` VARCHAR(7) COMMENT 'Номер дома',
	`CORP_NUM` VARCHAR(2) COMMENT 'Номер корпуса',
	`FLAT` VARCHAR(9) COMMENT 'Номер квартиры',
	`CODE3_1` INT(6) COMMENT 'Код тарифа оплаты жилья',
	`CODE3_2` INT(6) COMMENT 'Код тарифа отопления',
	`CODE3_3` INT(6) COMMENT 'Код тарифа горячего водоснабжения',
	`CODE3_4` INT(6) COMMENT 'Код тарифа холодного водоснабжения',
	`CODE3_5` INT(6) COMMENT 'Код тарифа - газоснабжение',
	`CODE3_6` INT(6) COMMENT 'Код тарифа-электроэнергии',
	`CODE3_7` INT(6) COMMENT 'Код тарифа - вывоз мусора',
	`CODE3_8` INT(6) COMMENT 'Код тарифа - водоотведение',
	`OPP_SERV` VARCHAR(8) COMMENT 'Резерв',
	`RESERV1` INT(10) COMMENT 'Резерв',
	`RESERV2` VARCHAR(10) COMMENT 'Резер',
        `calc_center_code2_1` DOUBLE COMMENT 'Код тарифа на оплату жилья, пришедший из центра начислений',
    PRIMARY KEY (`id`),
    KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_internal_city_id` (`internal_city_id`),
    KEY `key_internal_street_id` (`internal_street_id`),
    KEY `key_internal_building_id` (`internal_building_id`),
    KEY `key_internal_apartment_id` (`internal_apartment_id`),
    KEY `key_F_NAM` (`F_NAM`),
    KEY `key_M_NAM` (`M_NAM`),
    KEY `key_SUR_NAM` (`SUR_NAM`),
    KEY `key_N_NAME` (`N_NAME`),
    KEY `key_VUL_NAME` (`VUL_NAME`),
    KEY `key_BLD_NUM` (`BLD_NUM`),
    KEY `key_FLAT` (`FLAT`),
    KEY `key_OWN_NUM_SR` (`OWN_NUM_SR`),
    CONSTRAINT `fk_payment__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    CONSTRAINT `fk_payment__city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_payment__street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_payment__building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`object_id`),
    CONSTRAINT `fk_payment__apartment` FOREIGN KEY (`internal_apartment_id`) REFERENCES `apartment` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Benefit
-- ------------------------------
DROP TABLE IF EXISTS `benefit`;

CREATE TABLE `benefit` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `request_file_id` BIGINT(20) NULL,
    `account_number` VARCHAR(100) NULL,
    `status` INTEGER NOT NULL DEFAULT 200 COMMENT 'См. таблицу status_description и org.complitex.osznconnection.file.entity.RequestStatus',

	`OWN_NUM` VARCHAR(15) COMMENT 'Номер дела',
	`REE_NUM` INT(2) COMMENT 'Номер реестра',
	`OWN_NUM_SR` VARCHAR(15) COMMENT 'Лицевой счет в обслуж. организации',
	`FAM_NUM` INT(2) COMMENT 'Номер члена семьи',
	`SUR_NAM` VARCHAR(30) COMMENT 'Фамилия',
	`F_NAM` VARCHAR(15) COMMENT 'Имя',
	`M_NAM` VARCHAR(20) COMMENT 'Отчество',
	`IND_COD` VARCHAR(10) COMMENT 'Идентификационный номер',
	`PSP_SER` VARCHAR(6) COMMENT 'Серия паспорта',
	`PSP_NUM` VARCHAR(6) COMMENT 'Номер паспорта',
	`OZN` INT(1) COMMENT 'Признак владельца',
	`CM_AREA` DECIMAL(10,2) COMMENT 'Общая площадь',
	`HEAT_AREA` DECIMAL(10,2) COMMENT 'Обогреваемая площадь',
	`OWN_FRM` INT(6) COMMENT 'Форма собственности',
	`HOSTEL` INT(2) COMMENT 'Количество комнат',
	`PRIV_CAT` INT(3) COMMENT 'Категория льготы на платежи',
	`ORD_FAM` INT(2) COMMENT 'Порядок семьи льготников для расчета платежей',
	`OZN_SQ_ADD` INT(1) COMMENT 'Признак учета дополнительной площади',
	`OZN_ABS` INT(1) COMMENT 'Признак отсутствия данных в базе ЖЭО',
	`RESERV1` DECIMAL(10,2) COMMENT 'Резерв',
	`RESERV2` VARCHAR(10) COMMENT 'Резерв',
	PRIMARY KEY (`id`),
	KEY `key_request_file_id` (`request_file_id`),
    KEY `key_account_number` (`account_number`),
    KEY `key_status` (`status`),
    KEY `key_F_NAM` (`F_NAM`),
    KEY `key_M_NAM` (`M_NAM`),
    KEY `key_SUR_NAM` (`SUR_NAM`),   
    KEY `key_OWN_NUM_SR` (`OWN_NUM_SR`),
    CONSTRAINT `fk_benefit__request_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Tarif
-- ------------------------------
DROP TABLE IF EXISTS `tarif`;

CREATE TABLE `tarif` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `request_file_id` BIGINT(20) NOT NULL,
    `status` INTEGER NULL,

	`T11_DATA_T` VARCHAR(10),
    `T11_DATA_E` VARCHAR(10),
    `T11_DATA_R` VARCHAR(10),
    `T11_MARK` INT(3),
    `T11_TARN` INT(6),
    `T11_CODE1` INT(3),
    `T11_CODE2` INT(6),
    `T11_COD_NA` VARCHAR(40),
    `T11_CODE3` INT(6),
    `T11_NORM_U` DECIMAL(19, 10),
    `T11_NOR_US` DECIMAL(19, 10),
    `T11_CODE_N` INT(3),
    `T11_COD_ND` INT(3),
    `T11_CD_UNI` INT(3),
    `T11_CS_UNI` DECIMAL (19, 10),
    `T11_NORM` DECIMAL (19, 10),
    `T11_NRM_DO` DECIMAL (19, 10),
    `T11_NRM_MA` DECIMAL (19, 10),
    `T11_K_NADL` DECIMAL (19, 10),
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ------------------------------
-- Person account
-- ------------------------------
DROP TABLE IF EXISTS `person_account`;

CREATE TABLE `person_account` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `first_name`VARCHAR(100) NOT NULL,
    `middle_name` VARCHAR(100) NOT NULL,
    `last_name` VARCHAR(100) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `street` VARCHAR(100) NOT NULL,
    `building_num` VARCHAR(100) NOT NULL,
    `building_corp` VARCHAR(100) NULL,
    `apartment` VARCHAR(100) NOT NULL,
    `account_number` VARCHAR(100) NOT NULL,
    `own_num_sr` VARCHAR(15) NOT NULL,
    `oszn_id` bigint(20) NOT NULL,
    `calc_center_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Corrections
-- ------------------------------
DROP TABLE IF EXISTS `entity_type_correction`;

CREATE TABLE `entity_type_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `organization_id` BIGINT(20) NOT NULL,
    `type` VARCHAR(100) NOT NULL,
    `entity_type_id` BIGINT(20) NOT NULL,
    `organization_type_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    KEY `key_entity_type_id` (`entity_type_id`),
    KEY `key_type` (`type`),
    CONSTRAINT `fk_entity_type_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_entity_type_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_entity_type_correction__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_correction`;

CREATE TABLE `city_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(100) NOT NULL,
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_city_correction__city` FOREIGN KEY (`object_id`) REFERENCES `city` (`object_id`),
    CONSTRAINT `fk_city_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_city_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `district_correction`;

CREATE TABLE `district_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(100) NOT NULL,
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_district_correction__district` FOREIGN KEY (`object_id`) REFERENCES `district` (`object_id`),
    CONSTRAINT `fk_district_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_district_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_correction`;

CREATE TABLE `street_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(100) NOT NULL,
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_street_correction` FOREIGN KEY (`object_id`) REFERENCES `street` (`object_id`),
    CONSTRAINT `fk_street_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_street_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_correction`;

CREATE TABLE `building_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(20) NOT NULL,
    `correction_corp` VARCHAR(20),
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_building_correction` FOREIGN KEY (`object_id`) REFERENCES `building` (`object_id`),
    CONSTRAINT `fk_building_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_building_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `apartment_correction`;

CREATE TABLE `apartment_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(100) NOT NULL,
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_apartment_correction` FOREIGN KEY (`object_id`) REFERENCES `apartment` (`object_id`),
    CONSTRAINT `fk_apartment_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_apartment_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ownership_correction`;

CREATE TABLE `ownership_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(100) NOT NULL,
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_ownership_correction` FOREIGN KEY (`object_id`) REFERENCES `ownership` (`object_id`),
    CONSTRAINT `fk_ownership_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_ownership_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `privilege_correction`;

CREATE TABLE `privilege_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(100) NOT NULL,
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_privilege_correction` FOREIGN KEY (`object_id`) REFERENCES `privilege` (`object_id`),
    CONSTRAINT `fk_privilege_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_privilege_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- calculation_center_preference --
DROP TABLE IF EXISTS `calculation_center_preference`;

CREATE TABLE `calculation_center_preference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `calculation_center_id` BIGINT(20) NOT NULL,
    `adapter_class` VARCHAR(100) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `key_calculation_center_id` (`calculation_center_id`),
    CONSTRAINT `fk_calculation_center_preference__organization` FOREIGN KEY (`calculation_center_id`)
      REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ------------------------------
-- Config
-- ------------------------------

DROP TABLE IF EXISTS `config`;

CREATE TABLE `config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(64) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Status descriptions. Read only, use only for reports.
-- ------------------------------

DROP TABLE IF EXISTS `status_description`;

CREATE TABLE `status_description` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `code` INTEGER NOT NULL,
    `name` VARCHAR(500) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
