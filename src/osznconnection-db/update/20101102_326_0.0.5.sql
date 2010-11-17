CREATE TABLE `city_type` (
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
  CONSTRAINT `fk_city_type__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_city_type__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

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
) ENGINE=INNODB DEFAULT CHARSET=utf8;

CREATE TABLE `city_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_city_type_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1300, 'ru', 'Тип населенного пункта'), (1300, 'uk', 'Тип населенного пункта');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1300, 'city_type', 1300, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1301, 'ru', UPPER('Название')), (1301, 'uk', UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1300, 1300, 1, 1301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1300, 1300, UPPER('string_culture'));

INSERT INTO `city_type_string_culture`(`id`, `locale`, `value`) VALUES (10000,'ru','ГОРОД'), (10000,'uk','МIСТО'), (10001,'ru','ДЕРЕВНЯ'), (10001,'uk','СЕЛО');
INSERT INTO `city_type` (`object_id`) VALUES (10000), (10001);
INSERT INTO `city_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,10000,1300,10000,1300),
(1,10001,1300,10001,1300);

INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('city_type_string_culture', 10002), ('city_type', 10002);

DELETE FROM `entity_type` WHERE `id` IN (400, 401);
DELETE FROM `string_culture` WHERE `id` IN (402, 403);

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (402, 'ru', UPPER('Тип населенного пункта')),
                                                             (402, 'uk', UPPER('Тип населенного пункта'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (401, 400, 1, 402, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (401, 401, 'city_type');

CREATE TABLE `city_type_correction` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `parent_id` BIGINT(20),
    `object_id` BIGINT(20) NOT NULL,
    `correction` VARCHAR(100) NOT NULL,
    `organization_id` BIGINT(20) NOT NULL,
    `organization_code` VARCHAR(100),
    `internal_organization_id` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `key_object_id` (`object_id`),
    KEY `key_parent_id` (`parent_id`),
    KEY `key_correction` (`correction`),
    KEY `key_organization_id` (`organization_id`),
    KEY `key_internal_organization_id` (`internal_organization_id`),
    CONSTRAINT `fk_city_type_correction__city_type` FOREIGN KEY (`object_id`) REFERENCES `city_type` (`object_id`),
    CONSTRAINT `fk_city_type_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_city_type_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `city_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,10000,401,10000,401);
INSERT INTO `update` (`version`) VALUE ('20101102_326');

