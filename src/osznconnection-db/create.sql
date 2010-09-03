/*
SQLyog Enterprise - MySQL GUI v8.14 
MySQL - 5.0.88-community-nt-log : Database - passport_office_2
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `entity` */

/* System and helper tables */

DROP TABLE IF EXISTS `sequence`;
create table `sequence`(
   `sequence_name` varchar(100) NOT NULL COMMENT 'Наименование генератора',
   `sequence_value` bigint UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Значение генератора',
   PRIMARY KEY (`sequence_name`)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Вспомогательная таблица для хранения значений, генерируемых табличными генераторами';

DROP TABLE IF EXISTS `locales`;

CREATE TABLE `locales` (
  `locale` varchar(2) NOT NULL,
  `system` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `string_culture`;

CREATE TABLE `string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_string_culture_locale` (`locale`),
  CONSTRAINT `FK_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity`;

CREATE TABLE `entity` (
  `id` bigint(20) NOT NULL auto_increment,
  `entity_table` varchar(100) NOT NULL,
  `entity_name_id` bigint(20) NOT NULL,
  `strategy_factory` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `entity_table` (`entity_table`),
  KEY `FK_entity_name` (`entity_name_id`),
  CONSTRAINT `FK_entity_name` FOREIGN KEY (`entity_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity_type`;

CREATE TABLE `entity_type` (
  `id` bigint(20) NOT NULL auto_increment,
  `entity_id` bigint(20) NOT NULL,
  `entity_type_name_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_entity_type_entity` (`entity_id`),
  CONSTRAINT `FK_entity_type_entity` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`),
  KEY `FK_entity_type_name` (`entity_type_name_id`),
  CONSTRAINT `FK_entity_type_name` FOREIGN KEY (`entity_type_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity_attribute_type`;

CREATE TABLE `entity_attribute_type` (
  `id` bigint(20) NOT NULL auto_increment,
  `entity_id` bigint(20) NOT NULL,
  `mandatory` tinyint(1) default 0 NOT NULL,
  `attribute_type_name_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `system` tinyint(1) default 0 NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_entity_attribute_type_entity` (`entity_id`),
  CONSTRAINT `FK_entity_attribute_type_entity` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`),
  KEY `FK_entity_attribute_type_attribute_type_name` (`attribute_type_name_id`),
  CONSTRAINT `FK_entity_attribute_type_attribute_type_name` FOREIGN KEY (`attribute_type_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity_attribute_value_type`;

CREATE TABLE `entity_attribute_value_type` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_type_id` bigint(20) NOT NULL,
  `attribute_value_type` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_entity_attribute_value_type_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_entity_attribute_value_type_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Entities */

-- apartment --
DROP TABLE IF EXISTS `apartment`;

CREATE TABLE `apartment` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_apartment_type` (`entity_type_id`),
  CONSTRAINT `FK_apartment_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_apartment_parent` (`parent_entity_id`),
  CONSTRAINT `FK_apartment_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `apartment_attribute`;

CREATE TABLE `apartment_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_apartment_object_id` (`object_id`),
  CONSTRAINT `FK_apartment_object_id` FOREIGN KEY (`object_id`) REFERENCES `apartment`(`object_id`),
  KEY `FK_apartment_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_apartment_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_apartment_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_apartment_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `apartment_string_culture`;

CREATE TABLE `apartment_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_apartment_string_culture_locale` (`locale`),
  CONSTRAINT `FK_apartment_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- room --
DROP TABLE IF EXISTS `room`;

CREATE TABLE `room` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_room_type` (`entity_type_id`),
  CONSTRAINT `FK_room_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_room_parent` (`parent_entity_id`),
  CONSTRAINT `FK_room_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `room_attribute`;

CREATE TABLE `room_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_room_object_id` (`object_id`),
  CONSTRAINT `FK_room_object_id` FOREIGN KEY (`object_id`) REFERENCES `room`(`object_id`),
  KEY `FK_room_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_room_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_v_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_room_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `room_string_culture`;

CREATE TABLE `room_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_room_string_culture_locale` (`locale`),
  CONSTRAINT `FK_room_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- street --
DROP TABLE IF EXISTS `street`;

CREATE TABLE `street` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_street_type` (`entity_type_id`),
  CONSTRAINT `FK_street_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_street_parent` (`parent_entity_id`),
  CONSTRAINT `FK_street_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_attribute`;

CREATE TABLE `street_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_street_object_id` (`object_id`),
  CONSTRAINT `FK_street_object_id` FOREIGN KEY (`object_id`) REFERENCES `street`(`object_id`),
  KEY `FK_street_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_street_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_street_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_street_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_string_culture`;

CREATE TABLE `street_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_street_string_culture_locale` (`locale`),
  CONSTRAINT `FK_street_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- city --
DROP TABLE IF EXISTS `city`;

CREATE TABLE `city` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_city_type` (`entity_type_id`),
  CONSTRAINT `FK_city_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_city_parent` (`parent_entity_id`),
  CONSTRAINT `FK_city_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_attribute`;

CREATE TABLE `city_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_city_object_id` (`object_id`),
  CONSTRAINT `FK_city_object_id` FOREIGN KEY (`object_id`) REFERENCES `city`(`object_id`),
  KEY `FK_city_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_city_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_city_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_city_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_string_culture`;

CREATE TABLE `city_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_city_string_culture_locale` (`locale`),
  CONSTRAINT `FK_city_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- building --
DROP TABLE IF EXISTS `building`;

CREATE TABLE `building` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_building_type` (`entity_type_id`),
  CONSTRAINT `FK_building_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_building_parent` (`parent_entity_id`),
  CONSTRAINT `FK_building_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_attribute`;

CREATE TABLE `building_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_building_object_id` (`object_id`),
  CONSTRAINT `FK_building_object_id` FOREIGN KEY (`object_id`) REFERENCES `building`(`object_id`),
  KEY `FK_building_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_building_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_building_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_building_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_string_culture`;

CREATE TABLE `building_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_building_string_culture_locale` (`locale`),
  CONSTRAINT `FK_building_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- district --
DROP TABLE IF EXISTS `district`;

CREATE TABLE `district` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_district_type` (`entity_type_id`),
  CONSTRAINT `FK_district_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_district_parent` (`parent_entity_id`),
  CONSTRAINT `FK_district_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `district_attribute`;

CREATE TABLE `district_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_district_object_id` (`object_id`),
  CONSTRAINT `FK_district_object_id` FOREIGN KEY (`object_id`) REFERENCES `district`(`object_id`),
  KEY `FK_district_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_district_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_district_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_district_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `district_string_culture`;

CREATE TABLE `district_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_district_string_culture_locale` (`locale`),
  CONSTRAINT `FK_district_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- region --
DROP TABLE IF EXISTS `region`;

CREATE TABLE `region` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_region_type` (`entity_type_id`),
  CONSTRAINT `FK_region_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_region_parent` (`parent_entity_id`),
  CONSTRAINT `FK_region_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `region_attribute`;

CREATE TABLE `region_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_region_object_id` (`object_id`),
  CONSTRAINT `FK_region_object_id` FOREIGN KEY (`object_id`) REFERENCES `region`(`object_id`),
  KEY `FK_region_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_region_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_region_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_region_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `region_string_culture`;

CREATE TABLE `region_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_region_string_culture_locale` (`locale`),
  CONSTRAINT `FK_region_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- country --
DROP TABLE IF EXISTS `country`;

CREATE TABLE `country` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_country_type` (`entity_type_id`),
  CONSTRAINT `FK_country_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_country_parent` (`parent_entity_id`),
  CONSTRAINT `FK_country_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `country_attribute`;

CREATE TABLE `country_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_country_object_id` (`object_id`),
  CONSTRAINT `FK_country_object_id` FOREIGN KEY (`object_id`) REFERENCES `country`(`object_id`),
  KEY `FK_country_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_country_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_country_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_country_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `country_string_culture`;

CREATE TABLE `country_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  CONSTRAINT `FK_country_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- organization --
DROP TABLE IF EXISTS `organization`;

CREATE TABLE `organization` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_organization_type` (`entity_type_id`),
  CONSTRAINT `FK_organization_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_organization_parent` (`parent_entity_id`),
  CONSTRAINT `FK_organization_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organization_attribute`;

CREATE TABLE `organization_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_organization_object_id` (`object_id`),
  CONSTRAINT `FK_organization_object_id` FOREIGN KEY (`object_id`) REFERENCES `organization`(`object_id`),
  KEY `FK_organization_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_organization_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_organization_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_organization_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `organization_string_culture`;

CREATE TABLE `organization_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_organization_string_culture_locale` (`locale`),
  CONSTRAINT `FK_organization_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- user info --
DROP TABLE IF EXISTS `user_info`;

CREATE TABLE `user_info` (
  `id` bigint(20) NOT NULL auto_increment,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  `object_id` bigint(20) NOT NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `parent_id` bigint(20) default NULL,
  `parent_entity_id` bigint(20) default NULL,
  `entity_type_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`object_id`,`start_date`),
  KEY `FK_user_info_type` (`entity_type_id`),
  CONSTRAINT `FK_user_info_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  KEY `FK_user_info_parent` (`parent_entity_id`),
  CONSTRAINT `FK_user_info_parent` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_info_attribute`;

CREATE TABLE `user_info_attribute` (
  `id` bigint(20) NOT NULL auto_increment,
  `attribute_id` bigint(20) NOT NULL,
  `object_id` bigint(20) NOT NULL,
  `attribute_type_id` bigint(20) NOT NULL,
  `value_id` bigint(20) default NULL,
  `value_type_id` bigint(20) default NULL,
  `start_date` timestamp NOT NULL default CURRENT_TIMESTAMP,
  `end_date` timestamp NULL default NULL,
  `status` varchar(20) NOT NULL default 'ACTIVE',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `ID` (`attribute_id`,`object_id`,`attribute_type_id`, `start_date`),
  KEY `FK_user_info_object_id` (`object_id`),
  CONSTRAINT `FK_user_info_object_id` FOREIGN KEY (`object_id`) REFERENCES `user_info`(`object_id`),
  KEY `FK_user_info_attribute_attribute_type` (`attribute_type_id`),
  CONSTRAINT `FK_user_info_attribute_attribute_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`),
  KEY `FK_user_info_attribute_value_type` (`value_type_id`),
  CONSTRAINT `FK_user_info_attribute_value_type` FOREIGN KEY (`value_type_id`) REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_info_string_culture`;

CREATE TABLE `user_info_string_culture` (
  `pk_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `locale` varchar(2) NOT NULL,
  `value` varchar(1000) default NULL,
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `FK_user_info_string_culture_locale` (`locale`),
  CONSTRAINT `FK_user_info_string_culture_locale` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- user --

DROP TABLE IF EXISTS `user`;

CREATE TABLE  `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `login` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `user_info_object_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_login` (`login`),
  KEY `fk_user_info_object` (`user_info_object_id`),
  CONSTRAINT `fk_user_info_object` FOREIGN KEY (`user_info_object_id`) REFERENCES `user_info` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- usergroup --

DROP TABLE IF EXISTS `usergroup`;

CREATE TABLE  `usergroup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `login` varchar(45) NOT NULL,
  `group_name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fk_login_usergroup` (`login`, `group_name`),
  CONSTRAINT `fk_user_login` FOREIGN KEY (`login`) REFERENCES `user` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- log --

DROP TABLE IF EXISTS `log`;

CREATE TABLE  `log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` datetime DEFAULT NULL,
  `login` varchar(45) DEFAULT NULL,
  `module` varchar(100) DEFAULT NULL,
  `object_id` bigint(20) DEFAULT NULL,  
  `controller` varchar(100) DEFAULT NULL,
  `model` varchar(100) DEFAULT NULL,
  `event` varchar(100) DEFAULT NULL,
  `status` varchar(100) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_login` (`login`),
  KEY `index_date` (`date`),
  KEY `index_controller` (`controller`),
  KEY `index_model` (`model`),
  KEY `index_event` (`event`),
  KEY `index_module` (`module`),
  KEY `index_status` (`status`),
  KEY `index_description` (`description`),
  CONSTRAINT `fk_login` FOREIGN KEY (`login`) REFERENCES `user` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_change`;

CREATE TABLE `log_change` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `log_id` bigint(20) NOT NULL,
    `attribute_id` bigint(20) DEFAULT NULL,
    `collection` varchar(100) DEFAULT NULL,
    `property` varchar(100) DEFAULT NULL,    
    `old_value` varchar(500) DEFAULT NULL,
    `new_value` varchar(500) DEFAULT NULL,
    `locale` varchar(2) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_log` (`log_id`),
    CONSTRAINT `fk_log` FOREIGN KEY (`log_id`) REFERENCES `log` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `request_file`;

CREATE TABLE `request_file` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `loaded` datetime NOT NULL,
    `name` varchar(20) NOT NULL,
    `organization_object_id` bigint(20) NOT NULL,
    `date` date NOT NULL,
    `dbf_record_count` bigint(20) NOT NULL, 
    `length` bigint(20),
    `check_sum` varchar(32),      
    `status` varchar(20),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `payment`;

CREATE TABLE `payment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `request_file_id` bigint(20) NULL,
    `account_number` varchar(100) NULL,

    `internal_city_id` bigint(20) NULL,
    `internal_street_id` bigint(20) NULL,
    `internal_street_type_id` bigint(20) NULL,
    `internal_building_id` bigint(20) NULL,
    `internal_apartment_id` bigint(20) NULL,

    `outgoing_city` varchar(100) NULL,
    `outgoing_street` varchar(100) NULL,
    `outgoing_street_type` varchar(100) NULL,
    `outgoing_building_number` varchar(100) NULL,
    `outgoing_building_corp` varchar(100) NULL,
    `outgoing_apartment` varchar(100) NULL,

    `status` varchar(50) NOT NULL default 'CITY_UNRESOLVED_LOCALLY',

    `OWN_NUM` varchar (15) NOT NULL COMMENT 'Номер дела',
    `REE_NUM` int(2) COMMENT 'Номер реестра',
    `OPP` varchar(8) COMMENT 'Признаки наличия услуг',
	`NUMB` int(2) COMMENT 'Общее число зарегистрированных',
	`MARK` int(2) COMMENT 'К-во людей, которые пользуются льготами',
	`CODE` int(4) COMMENT 'Код ЖЭО',
	`ENT_COD` int(10) COMMENT 'Код ЖЭО ОКПО',
	`FROG`  double(5,1) COMMENT 'Процент льгот',
    `FL_PAY` int(2) COMMENT 'Общая плата',
	`NM_PAY` double(9,2) COMMENT 'Плата в пределах норм потребления',
	`DEBT` double(9,2) COMMENT 'Сумма долга',
	`CODE2_1` int(6) COMMENT 'Оплата жилья',
	`CODE2_2` int(6) COMMENT 'система',
	`CODE2_3` int(6) COMMENT 'Горячее водоснабжение',
	`CODE2_4` int(6) COMMENT 'Холодное водоснабжение',
	`CODE2_5` int(6) COMMENT 'Газоснабжение',
	`CODE2_6` int(6) COMMENT 'Электроэнергия',
	`CODE2_7` int(6) COMMENT 'Вывоз мусора',
	`CODE2_8` int(6) COMMENT 'Водоотведение',
	`NORM_F_1` double(10,4) COMMENT 'Общая площадь (оплата жилья)',
	`NORM_F_2` double(10,4) COMMENT 'Объемы потребления (отопление)',
	`NORM_F_3` double(10,4) COMMENT 'Объемы потребления (горячего водо.)',
	`NORM_F_4` double(10,4) COMMENT 'Объемы потребления (холодное водо.)',
	`NORM_F_5` double(10,4) COMMENT 'Объемы потребления (газоснабжение)',
	`NORM_F_6` double(10,4) COMMENT 'Объемы потребления (электроэнергия)',
	`NORM_F_7` double(10,4) COMMENT 'Объемы потребления (вывоз мусора)',
	`NORM_F_8` double(10,4) COMMENT 'Объемы потребления (водоотведение)',
	`OWN_NUM_SR` varchar(15) COMMENT 'Лицевой счет в обслуж. организации',
	`DAT1` DATE COMMENT 'Дата начала действия субсидии',
	`DAT2` DATE COMMENT 'Дата формирования запроса',
	`OZN_PRZ` int(1) COMMENT 'Признак (0 - автоматическое назначение, 1-для ручного расчета)',
	`DAT_F_1` DATE COMMENT 'Дата начала для факта',
	`DAT_F_2` DATE COMMENT 'Дата конца для факта',
	`DAT_FOP_1` DATE COMMENT 'Дата начала для факта отопления',
	`DAT_FOP_2` DATE COMMENT 'Дата конца для факта отопления',
	`ID_RAJ` varchar(5) COMMENT 'Код района',
	`SUR_NAM` varchar(30) COMMENT 'Фамилия',
	`F_NAM` varchar(15) COMMENT 'Имя',
	`M_NAM` varchar(20) COMMENT 'Отчество',
	`IND_COD` varchar(10) COMMENT 'Идентификационный номер',
	`INDX` varchar(6) COMMENT 'Индекс почтового отделения',
	`N_NAME` varchar(30) COMMENT 'Название населенного пункта',
	`VUL_NAME` varchar(30) COMMENT 'Название улицы',
	`BLD_NUM` varchar(7) COMMENT 'Номер дома',
	`CORP_NUM` varchar(2) COMMENT 'Номер корпуса',
	`FLAT` varchar(9) COMMENT 'Номер квартиры',
	`CODE3_1` int(6) COMMENT 'Код тарифа оплаты жилья',
	`CODE3_2` int(6) COMMENT 'Код тарифа отопления',
	`CODE3_3` int(6) COMMENT 'Код тарифа горячего водоснабжения',
	`CODE3_4` int(6) COMMENT 'Код тарифа холодного водоснабжения',
	`CODE3_5` int(6) COMMENT 'Код тарифа - газоснабжение',
	`CODE3_6` int(6) COMMENT 'Код тарифа-электроэнергии',
	`CODE3_7` int(6) COMMENT 'Код тарифа - вывоз мусора',
	`CODE3_8` int(6) COMMENT 'Код тарифа - водоотведение',
	`OPP_SERV` varchar(8) COMMENT 'Резерв',
	`RESERV1` int(10) COMMENT 'Резерв',
	`RESERV2` varchar(10) COMMENT 'Резер',
    PRIMARY KEY (`id`),
    KEY `FK_payment_file` (`request_file_id`),
    CONSTRAINT `FK_payment_file` FOREIGN KEY (`request_file_id`) REFERENCES `request_file` (`id`),
    KEY `FK_payment_city` (`internal_city_id`),
    CONSTRAINT `FK_payment_city` FOREIGN KEY (`internal_city_id`) REFERENCES `city` (`id`),
    KEY `FK_payment_street` (`internal_street_id`),
    CONSTRAINT `FK_payment_street` FOREIGN KEY (`internal_street_id`) REFERENCES `street` (`id`),
    KEY `FK_payment_building` (`internal_building_id`),
    CONSTRAINT `FK_payment_building` FOREIGN KEY (`internal_building_id`) REFERENCES `building` (`id`),
    KEY `FK_payment_apartment` (`internal_apartment_id`),
    CONSTRAINT `FK_payment_apartment` FOREIGN KEY (`internal_apartment_id`) REFERENCES `apartment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `benefit`;

CREATE TABLE `benefit` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `request_file_id` bigint(20) NULL,
    `account_number` varchar(100) NULL,
    `city_id` bigint(20) NULL,
    `street_id` bigint(20) NULL,
    `building_id` bigint(20) NULL,
    `apartment_id` bigint(20) NULL,
    `status` varchar(50) NOT NULL default 'CITY_UNRESOLVED_LOCALLY',

	`OWN_NUM` varchar(15) COMMENT 'Номер дела',
	`REE_NUM` int(2) COMMENT 'Номер реестра',
	`OWN_NUM_SR` varchar(15) COMMENT 'Лицевой счет в обслуж. организации',
	`FAM_NUM` int(2) COMMENT 'Номер члена семьи',
	`SUR_NAM` varchar(30) COMMENT 'Фамилия',
	`F_NAM` varchar(15) COMMENT 'Имя',
	`M_NAM` varchar(20) COMMENT 'Отчество',
	`IND_COD` varchar(10) COMMENT 'Идентификационный номер',
	`PSP_SER` varchar(6) COMMENT 'Серия паспорта',
	`PSP_NUM` varchar(6) COMMENT 'Номер паспорта',
	`OZN` int(1) COMMENT 'Признак владельца',
	`CM_AREA` double(10,2) COMMENT 'Общая площадь',
	`HEAT_AREA` double(10,2) COMMENT 'Обогреваемая площадь',
	`OWN_FRM` int(6) COMMENT 'Форма собственности',
	`HOSTEL` int(2) COMMENT 'Количество комнат',
	`PRIV_CAT` int(3) COMMENT 'Категория льготы на платежи',
	`ORD_FAM` int(2) COMMENT 'Порядок семьи льготников для расчета платежей',
	`OZN_SQ_ADD` int(1) COMMENT 'Признак учета дополнительной площади',
	`OZN_ABS` int(1) COMMENT 'Признак отсутствия данных в базе ЖЭО',
	`RESERV1` double(10,2) COMMENT 'Резерв',
	`RESERV2` varchar(10) COMMENT 'Резерв',
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `person_account`;

CREATE TABLE `person_account` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `first_name` varchar(30) NOT NULL,
    `middle_name` varchar(30) NOT NULL,
    `last_name` varchar(30) NOT NULL,
    `city_id` bigint(20) NOT NULL,
    `street_id` bigint(20) NOT NULL,
    `building_id` bigint(20) NOT NULL,
    `apartment_id` bigint(20) NOT NULL,
    `account_number` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_person_account_city` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`),
    KEY `FK_person_account_street` (`street_id`),
    CONSTRAINT `FK_fileperson_account_street` FOREIGN KEY (`street_id`) REFERENCES `street` (`id`),
    KEY `FK_person_account_building` (`building_id`),
    CONSTRAINT `FK_person_account_building` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`),
    KEY `FK_person_account_apartment` (`apartment_id`),
    CONSTRAINT `FK_person_account_apartment` FOREIGN KEY (`apartment_id`) REFERENCES `apartment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `entity_type_correction`;

CREATE TABLE `entity_type_correction` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `organization_id` bigint(20) NOT NULL,
    `type` varchar(100) NOT NULL,
    `entity_type_id` bigint(20) NOT NULL,
    `organization_type_code` bigint(20) NULL,
    PRIMARY KEY (`id`),
    KEY `FK_entity_type_correction_organization` (`organization_id`),
    CONSTRAINT `FK_entity_type_correction_organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`),
    KEY `FK_entity_type_correction_type` (`entity_type_id`),
    CONSTRAINT `FK_entity_type_correction_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_correction`;

CREATE TABLE `city_correction` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `organization_id` bigint(20) NOT NULL,
    `city` varchar(100) NOT NULL,
    `city_id` bigint(20) NOT NULL,
    `organization_city_code` bigint(20) NULL,
    PRIMARY KEY (`id`),
    KEY `FK_city_correction_organization` (`organization_id`),
    CONSTRAINT `FK_city_correction_organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_correction`;

CREATE TABLE `street_correction` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `organization_id` bigint(20) NOT NULL,
    `street` varchar(100) NOT NULL,
    `street_id` bigint(20) NOT NULL,
    `organization_street_code` bigint(20) NULL,
    PRIMARY KEY (`id`),
    KEY `FK_street_correction_organization` (`organization_id`),
    CONSTRAINT `FK_street_correction_organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_correction`;

CREATE TABLE `building_correction` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `organization_id` bigint(20) NOT NULL,
    `building_num` varchar(100) NOT NULL,
    `building_corp` varchar(100) NULL,
    `building_id` bigint(20) NOT NULL,
    `organization_building_code` bigint(20) NULL,
    PRIMARY KEY (`id`),
    KEY `FK_building_correction_organization` (`organization_id`),
    CONSTRAINT `FK_building_correction_organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `apartment_correction`;

CREATE TABLE `apartment_correction` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `organization_id` bigint(20) NOT NULL,
    `apartment` varchar(100) NOT NULL,
    `apartment_id` bigint(20) NOT NULL,
    `organization_apartment_code` bigint(20) NULL,
    PRIMARY KEY (`id`),
    KEY `FK_apartment_correction_organization` (`organization_id`),
    CONSTRAINT `FK_apartment_correction_organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `calculation_center_preference`;

CREATE TABLE `calculation_center_preference` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `calculation_center_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_calculation_center_preference_organization` (`calculation_center_id`),
    CONSTRAINT `FK_calculation_center_preference_organization` FOREIGN KEY (`calculation_center_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
