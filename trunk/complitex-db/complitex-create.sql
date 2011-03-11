/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

DROP TABLE IF EXISTS `sequence`;
CREATE TABLE `sequence`(
   `sequence_name` VARCHAR(100) NOT NULL,
   `sequence_value` bigint UNSIGNED NOT NULL DEFAULT '0',
   PRIMARY KEY (`sequence_name`)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `locales`;
CREATE TABLE `locales` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `locale` VARCHAR(2) NOT NULL,
  `system` TINYINT(1) NOT NULL default 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_locale` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `string_culture`;
CREATE TABLE `string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`, `locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (object_id),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_apartment__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_apartment__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_apartment__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_apartment_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_room__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_room__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_room__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_room_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_street__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_street__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_street__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_street_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Street Type
-- ------------------------------

DROP TABLE IF EXISTS `street_type`;

CREATE TABLE `street_type` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (object_id),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_street_type__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_street_type__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_street_type__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_type_attribute`;

CREATE TABLE `street_type_attribute` (
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
  CONSTRAINT `fk_street_type_attribute__street_type` FOREIGN KEY (`object_id`) REFERENCES `street_type`(`object_id`),
  CONSTRAINT `fk_street_type_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_street_type_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `street_type_string_culture`;

CREATE TABLE `street_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_street_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_city__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `ft_city__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_city__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_city_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- City Type
-- ------------------------------

DROP TABLE IF EXISTS `city_type`;

CREATE TABLE `city_type` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (object_id),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_city_type__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_city_type__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_city_type__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_type_attribute`;

CREATE TABLE `city_type_attribute` (
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
  CONSTRAINT `fk_city_type_attribute__city_type` FOREIGN KEY (`object_id`) REFERENCES `city_type`(`object_id`),
  CONSTRAINT `fk_city_type_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_city_type_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_type_string_culture`;

CREATE TABLE `city_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_city_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Building Address
-- ------------------------------
DROP TABLE IF EXISTS `building_address`;

CREATE TABLE `building_address` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `parent_id` BIGINT(20),
  `parent_entity_id` BIGINT(20),
  `entity_type_id` BIGINT(20),
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_building_address__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_building_address__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_building_address__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_address_attribute`;

CREATE TABLE `building_address_attribute` (
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
  CONSTRAINT `fk_building_address_attribute__building_address` FOREIGN KEY (`object_id`) REFERENCES `building_address`(`object_id`),
  CONSTRAINT `fk_building_address_attribute__entity_attribute_type` FOREIGN KEY (`attribute_type_id`)
    REFERENCES `entity_attribute_type` (`id`),
  CONSTRAINT `fk_building_address_attribute__entity_attribute_value_type` FOREIGN KEY (`value_type_id`)
    REFERENCES `entity_attribute_value_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_address_string_culture`;

CREATE TABLE `building_address_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_building_address_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_building__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_building__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_building__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_building_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_district__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_district__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_district__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_district_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_region__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_region__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_region__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_region_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_country__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_country__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_country__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_country_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_organization__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_organization__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_organization__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_organization_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0,
  `external_id` BIGINT(20),
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  UNIQUE KEY `unique_external_id` (`external_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  KEY `key_permission_id` (`permission_id`),
  CONSTRAINT `fk_user_info__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_user_info__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_user_info__permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
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
  `locale_id` BIGINT(20) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_user_info_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
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
-- User Organization
-- ------------------------------
DROP TABLE IF EXISTS `user_organization`;

CREATE TABLE `user_organization` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT(20) NOT NULL,
    `organization_object_id` BIGINT(20) NOT NULL,
    `main` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_unique` (`user_id`, `organization_object_id`),
    CONSTRAINT `fk_user_organization__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_user_organization__organization` FOREIGN KEY (`organization_object_id`)
      REFERENCES `organization` (`object_id`)
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
-- Update
-- ------------------------------

DROP TABLE IF EXISTS `update`;

CREATE TABLE `update` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` VARCHAR(64) NOT NULL,
    `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Preference
-- ------------------------------

DROP TABLE IF EXISTS `preference`;

CREATE TABLE `preference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT(20) NOT NULL,
    `page` VARCHAR(255) NOT NULL,
    `key` VARCHAR(255) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `key_user_id` (`user_id`),
    CONSTRAINT `fk_preference__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Permission
-- ------------------------------

DROP TABLE IF EXISTS `permission`;

CREATE TABLE `permission` (
    `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `permission_id` BIGINT(20) NOT NULL,
    `table` VARCHAR(64) NOT NULL,
    `entity` VARCHAR(64) NOT NULL,
    `object_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`pk_id`),
    UNIQUE KEY `key_unique` (`permission_id`, `entity`, `object_id`),
    KEY `key_permission_id` (`permission_id`),
    KEY `key_table` (`table`),
    KEY `key_entity` (`entity`),
    KEY `key_object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- First Name
-- ------------------------------

DROP TABLE IF EXISTS `first_name`;

CREATE TABLE `first_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Middle Name
-- ------------------------------

DROP TABLE IF EXISTS `middle_name`;

CREATE TABLE `middle_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ------------------------------
-- Last Name
-- ------------------------------

DROP TABLE IF EXISTS `last_name`;

CREATE TABLE `last_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;