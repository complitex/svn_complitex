/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

DROP TABLE IF EXISTS `sequence`;
CREATE TABLE `sequence`(
   `sequence_name` VARCHAR(100) NOT NULL COMMENT 'Название таблицы сущности',
   `sequence_value` bigint UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Значение идентификатора',
   PRIMARY KEY (`sequence_name`)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Последовательность генерации идентификаторов объектов';

DROP TABLE IF EXISTS `locales`;
CREATE TABLE `locales` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор локали',
  `locale` VARCHAR(2) NOT NULL COMMENT 'Код локали',
  `system` TINYINT(1) NOT NULL default 0 COMMENT 'Является ли локаль системной',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_locale` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локаль';

DROP TABLE IF EXISTS `string_culture`;
CREATE TABLE `string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`, `locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация';

DROP TABLE IF EXISTS `entity`;

CREATE TABLE `entity` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор сущности',
  `entity_table` VARCHAR(100) NOT NULL COMMENT 'Название таблицы сущности',
  `entity_name_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации названия сущности',
  `strategy_factory` VARCHAR(100) NOT NULL COMMENT 'Не используется',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `unique_entity_table` (`entity_table`),
  KEY `key_entity_name_id` (`entity_name_id`),
  CONSTRAINT `fk_entity__string_culture` FOREIGN KEY (`entity_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Сущность';

DROP TABLE IF EXISTS `entity_type`;

CREATE TABLE `entity_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор типа сущности',
  `entity_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор сущности',
  `entity_type_name_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации названия типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия типа сущности',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия типа сущности',
  PRIMARY KEY (`id`),
  KEY `key_entity_id` (`entity_id`),
  KEY `key_entity_type_name_id` (`entity_type_name_id`),
  CONSTRAINT `fk_entity_type__entity` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_entity_type__string_culture` FOREIGN KEY (`entity_type_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Тип сущности';

DROP TABLE IF EXISTS `entity_attribute_type`;

CREATE TABLE `entity_attribute_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор типа атрибута',
  `entity_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор сущности',
  `mandatory` TINYINT(1) default 0 NOT NULL COMMENT 'Является ли атрибут обязательным',
  `attribute_type_name_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации названия атрибута',
  `start_date` TIMESTAMP NOT NULL default CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия типа атрибута',
  `end_date` TIMESTAMP NULL default NULL COMMENT 'Дата окончания периода действия типа атрибута',
  `system` TINYINT(1) default 0 NOT NULL COMMENT 'Является ли тип атрибута системным',
  PRIMARY KEY (`id`),
  KEY `key_entity_id` (`entity_id`),
  KEY `key_attribute_type_name_id` (`attribute_type_name_id`),
  CONSTRAINT `fk_entity_attribute_type__entity` FOREIGN KEY (`entity_id`) REFERENCES `entity` (`id`),
  CONSTRAINT `fk_entity_attribute_type__string_culture` FOREIGN KEY (`attribute_type_name_id`) REFERENCES `string_culture` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Тип атрибута сущности';

DROP TABLE IF EXISTS `entity_attribute_value_type`;

CREATE TABLE `entity_attribute_value_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор типа значения атрибута',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `attribute_value_type` VARCHAR(100) NOT NULL COMMENT 'Тип значения атрибута',
  PRIMARY KEY  (`id`),
  KEY `key_attribute_type_id` (`attribute_type_id`),
  CONSTRAINT `fk_entity_attribute_value_type` FOREIGN KEY (`attribute_type_id`) REFERENCES `entity_attribute_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Тип значения атрибута';

/* Entities */

-- ------------------------------
-- Apartment
-- ------------------------------
DROP TABLE IF EXISTS `apartment`;

CREATE TABLE `apartment` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус объекта. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Квартира';

DROP TABLE IF EXISTS `apartment_attribute`;

CREATE TABLE `apartment_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус атрибута. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты квартиры';

DROP TABLE IF EXISTS `apartment_string_culture`;

CREATE TABLE `apartment_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_apartment_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов квартиры';

-- ------------------------------
-- Room --
-- ------------------------------
DROP TABLE IF EXISTS `room`;

CREATE TABLE `room` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Начало действия значений параметров объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Комната';

DROP TABLE IF EXISTS `room_attribute`;

CREATE TABLE `room_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты комнаты';

DROP TABLE IF EXISTS `room_string_culture`;

CREATE TABLE `room_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_room_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов комнаты';

-- ------------------------------
-- Street
-- ------------------------------
DROP TABLE IF EXISTS `street`;

CREATE TABLE `street` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Улица';

DROP TABLE IF EXISTS `street_attribute`;

CREATE TABLE `street_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты улицы';

DROP TABLE IF EXISTS `street_string_culture`;

CREATE TABLE `street_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_street_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов улицы';

-- ------------------------------
-- Street Type
-- ------------------------------

DROP TABLE IF EXISTS `street_type`;

CREATE TABLE `street_type` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Тип улицы';

DROP TABLE IF EXISTS `street_type_attribute`;

CREATE TABLE `street_type_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты типа улицы';

DROP TABLE IF EXISTS `street_type_string_culture`;

CREATE TABLE `street_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_street_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов типа улицы';

-- ------------------------------
-- City
-- ------------------------------
DROP TABLE IF EXISTS `city`;

CREATE TABLE `city` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Населенный пункт';

DROP TABLE IF EXISTS `city_attribute`;

CREATE TABLE `city_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты населенного пункта';

DROP TABLE IF EXISTS `city_string_culture`;

CREATE TABLE `city_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_city_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов населенного пункта';

-- ------------------------------
-- City Type
-- ------------------------------

DROP TABLE IF EXISTS `city_type`;

CREATE TABLE `city_type` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Тип населенного пункта';

DROP TABLE IF EXISTS `city_type_attribute`;

CREATE TABLE `city_type_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты типа населенного пункта';

DROP TABLE IF EXISTS `city_type_string_culture`;

CREATE TABLE `city_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_city_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов типа населенного пункта';

-- ------------------------------
-- Building Address
-- ------------------------------
DROP TABLE IF EXISTS `building_address`;

CREATE TABLE `building_address` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Адрес дома';

DROP TABLE IF EXISTS `building_address_attribute`;

CREATE TABLE `building_address_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты адреса дома';

DROP TABLE IF EXISTS `building_address_string_culture`;

CREATE TABLE `building_address_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_building_address_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов адреса дома';

-- ------------------------------
-- Building
-- ------------------------------
DROP TABLE IF EXISTS `building`;

CREATE TABLE `building` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Дом';

DROP TABLE IF EXISTS `building_attribute`;

CREATE TABLE `building_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты дома';

DROP TABLE IF EXISTS `building_string_culture`;

CREATE TABLE `building_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_building_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов дома ';

-- ------------------------------
-- District --
-- ------------------------------
DROP TABLE IF EXISTS `district`;

CREATE TABLE `district` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Район';

DROP TABLE IF EXISTS `district_attribute`;

CREATE TABLE `district_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты района';

DROP TABLE IF EXISTS `district_string_culture`;

CREATE TABLE `district_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_district_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов района';

-- ------------------------------
-- Region
-- ------------------------------
DROP TABLE IF EXISTS `region`;

CREATE TABLE `region` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Регион';

DROP TABLE IF EXISTS `region_attribute`;

CREATE TABLE `region_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты региона';

DROP TABLE IF EXISTS `region_string_culture`;

CREATE TABLE `region_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_region_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов региона';

-- ------------------------------
-- Country
-- ------------------------------
DROP TABLE IF EXISTS `country`;

CREATE TABLE `country` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Страна';

DROP TABLE IF EXISTS `country_attribute`;

CREATE TABLE `country_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты страны';

DROP TABLE IF EXISTS `country_string_culture`;

CREATE TABLE `country_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_country_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов страны';

-- ------------------------------
-- Organization
-- ------------------------------
DROP TABLE IF EXISTS `organization`;

CREATE TABLE `organization` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Организация';

DROP TABLE IF EXISTS `organization_attribute`;

CREATE TABLE `organization_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты организации';

DROP TABLE IF EXISTS `organization_string_culture`;

CREATE TABLE `organization_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_organization_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов организации';


-- ------------------------------
-- User info
-- ------------------------------
DROP TABLE IF EXISTS `user_info`;

CREATE TABLE `user_info` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `parent_id` BIGINT(20) COMMENT 'Идентификатор родительского объекта',
  `parent_entity_id` BIGINT(20) COMMENT 'Идентификатор сущности родительского объекта',
  `entity_type_id` BIGINT(20) COMMENT 'Идентификатор типа сущности',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия объекта',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия объекта',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
  `permission_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT 'Ключ прав доступа к объекту',
  `external_id` BIGINT(20) COMMENT 'Внешний идентификатор импорта записи',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Информация о пользователе';

DROP TABLE IF EXISTS `user_info_attribute`;

CREATE TABLE `user_info_attribute` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `attribute_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор атрибута',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
  `attribute_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа атрибута',
  `value_id` BIGINT(20) COMMENT 'Идентификатор значения',
  `value_type_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор типа значения атрибута',
  `start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата начала периода действия атрибута',
  `end_date` TIMESTAMP NULL DEFAULT NULL COMMENT 'Дата окончания периода действия атрибута',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Статус. См. класс StatusType',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Атрибуты информации о пользователе';

DROP TABLE IF EXISTS `user_info_string_culture`;

CREATE TABLE `user_info_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
  `id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локализации',
  `locale_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор локали',
  `value` VARCHAR(1000) COMMENT 'Текстовое значение',
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale_id`),
  KEY `key_locale` (`locale_id`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_user_info_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локализация атрибутов информации о пользователе';

-- ------------------------------
-- User
-- ------------------------------
DROP TABLE IF EXISTS `user`;

CREATE TABLE  `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор пользователя',
  `login` VARCHAR(45) NOT NULL COMMENT 'Имя пользователя',
  `password` VARCHAR(45) NOT NULL COMMENT 'MD5 хэш пароля',
  `user_info_object_id` BIGINT(20) COMMENT 'Идентификатор объекта информация о пользователе',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_login` (`login`),
  KEY `key_user_info_object_id` (`user_info_object_id`),
  CONSTRAINT `fk_user__user_info` FOREIGN KEY (`user_info_object_id`) REFERENCES `user_info` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Пользователь';

-- ------------------------------
-- Usergroup
-- ------------------------------
DROP TABLE IF EXISTS `usergroup`;

CREATE TABLE  `usergroup` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор группы пользователей',
  `login` VARCHAR(45) NOT NULL COMMENT 'Имя пользователя',
  `group_name` VARCHAR(45) NOT NULL COMMENT 'Название группы',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_login__group_name` (`login`, `group_name`),
  CONSTRAINT `fk_usergroup__user` FOREIGN KEY (`login`) REFERENCES `user` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Группа пользователей';

-- ------------------------------
-- User Organization
-- ------------------------------
DROP TABLE IF EXISTS `user_organization`;

CREATE TABLE `user_organization` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор организации пользователей',
    `user_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор пользователя',
    `organization_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
    `main` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Является ли организация основной',
    PRIMARY KEY (`id`),
    UNIQUE KEY `key_unique` (`user_id`, `organization_object_id`),
    CONSTRAINT `fk_user_organization__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_user_organization__organization` FOREIGN KEY (`organization_object_id`)
      REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Организация пользователей';

-- ------------------------------
-- Log
-- ------------------------------
DROP TABLE IF EXISTS `log`;

CREATE TABLE  `log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор записи журнала событий',
  `date` DATETIME COMMENT 'Дата',
  `login` VARCHAR(45) COMMENT 'Имя пользователя',
  `module` VARCHAR(100) COMMENT 'Название модуля системы',
  `object_id` BIGINT(20) COMMENT 'Идентификатор объекта',
  `controller` VARCHAR(100) COMMENT 'Название класса обработчика',
  `model` VARCHAR(100) COMMENT 'Название класса модели данных',
  `event` VARCHAR(100) COMMENT 'Название события',
  `status` VARCHAR(100) COMMENT 'Статус',
  `description` VARCHAR(255) COMMENT 'Описание',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Журнал событий';

-- ------------------------------
-- Log change
-- ------------------------------
DROP TABLE IF EXISTS `log_change`;

CREATE TABLE `log_change` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор изменения',
    `log_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор журнала событий',
    `attribute_id` BIGINT(20) COMMENT 'Идентификатор атрибута',
    `collection` VARCHAR(100) COMMENT 'Название группы параметров',
    `property` VARCHAR(100) COMMENT 'Свойство',
    `old_value` VARCHAR(500) COMMENT 'Предыдущее значение',
    `new_value` VARCHAR(500) COMMENT 'Новое значение',
    `locale` VARCHAR(2) COMMENT 'Код локали',
    PRIMARY KEY (`id`),
    KEY `key_log` (`log_id`),
    CONSTRAINT `fk_log_change__log` FOREIGN KEY (`log_id`) REFERENCES `log` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Изменения модели данных';

-- ------------------------------
-- Config
-- ------------------------------

DROP TABLE IF EXISTS `config`;

CREATE TABLE `config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор настройки',
    `name` VARCHAR(64) NOT NULL COMMENT 'Имя',
    `value` VARCHAR(255) NOT NULL COMMENT 'Значение',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Настройки';

-- ------------------------------
-- Update
-- ------------------------------

DROP TABLE IF EXISTS `update`;

CREATE TABLE `update` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор обновления',
    `version` VARCHAR(64) NOT NULL COMMENT 'Версия',
    `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата обновления',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Обновление базы данных';

-- ------------------------------
-- Preference
-- ------------------------------

DROP TABLE IF EXISTS `preference`;

CREATE TABLE `preference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор предпочтения',
    `user_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор пользователя',
    `page` VARCHAR(255) NOT NULL COMMENT 'Класс страницы',
    `key` VARCHAR(255) NOT NULL COMMENT 'Ключ',
    `value` VARCHAR(255) NOT NULL COMMENT 'Значение',
    PRIMARY KEY (`id`),
    KEY `key_user_id` (`user_id`),
    CONSTRAINT `fk_preference__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Предпочтения пользователя';

-- ------------------------------
-- Permission
-- ------------------------------

DROP TABLE IF EXISTS `permission`;

CREATE TABLE `permission` (
    `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT  COMMENT 'Суррогатный ключ',
    `permission_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор права доступа',
    `table` VARCHAR(64) NOT NULL COMMENT 'Таблица',
    `entity` VARCHAR(64) NOT NULL COMMENT 'Сущность',
    `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта',
    PRIMARY KEY (`pk_id`),
    UNIQUE KEY `key_unique` (`permission_id`, `entity`, `object_id`),
    KEY `key_permission_id` (`permission_id`),
    KEY `key_table` (`table`),
    KEY `key_entity` (`entity`),
    KEY `key_object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Права доступа';

-- ------------------------------
-- First Name
-- ------------------------------

DROP TABLE IF EXISTS `first_name`;

CREATE TABLE `first_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор имени',
  `name` VARCHAR(100) NOT NULL COMMENT 'Имя',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Имя';

-- ------------------------------
-- Middle Name
-- ------------------------------

DROP TABLE IF EXISTS `middle_name`;

CREATE TABLE `middle_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор отчества',
  `name` VARCHAR(100) NOT NULL COMMENT 'Отчество',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Отчество';

-- ------------------------------
-- Last Name
-- ------------------------------

DROP TABLE IF EXISTS `last_name`;

CREATE TABLE `last_name` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор фамилии',
  `name` VARCHAR(100) NOT NULL COMMENT 'Фамилия',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Фамилия';

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;