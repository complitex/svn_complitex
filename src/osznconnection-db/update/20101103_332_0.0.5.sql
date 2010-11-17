ALTER TABLE `apartment` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `building` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `city` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `country` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `district` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `room` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `region` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `street` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `apartment_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `building_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `city_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `country_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `district_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `room_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `region_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;
ALTER TABLE `street_attribute` CHANGE COLUMN `id` `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `apartment` DROP COLUMN `import_object_id`;
ALTER TABLE `building` DROP COLUMN `import_object_id`;
ALTER TABLE `district` DROP COLUMN `import_object_id`;
ALTER TABLE `room` DROP COLUMN `import_object_id`;
ALTER TABLE `street` DROP COLUMN `import_object_id`;

CREATE TABLE `street_type` (
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
  CONSTRAINT `fk_street_type__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_street_type__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

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

CREATE TABLE `street_type_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY `unique_id__locale` (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_street_type_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1400, 'ru', 'Тип улицы'), (1400, 'uk', 'Тип улицы');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1400, 'street_type', 1400, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1401, 'ru', UPPER('Название')), (1401, 'uk', UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1400, 1400, 1, 1401, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1400, 1400, UPPER('string_culture'));

INSERT INTO `street_type_string_culture`(`id`, `locale`, `value`) VALUES (10000,'ru','Б-Р'), (10001,'ru','М'), (10002,'ru','М-Н'),
(10003,'ru','ПЕР'), (10004,'ru','ПЛ'), (10005,'ru','П'), (10006,'ru','ПОС'), (10007,'ru','ПР-Д'), (10008,'ru','ПРОСП'), (10009,'ru','СП'),
(10010,'ru','Т'), (10011,'ru','ТУП'), (10012,'ru','УЛ'), (10013,'ru','ШОССЕ'), (10014,'ru','НАБ'), (10015,'ru','В-Д'), (10016,'ru','СТ');

INSERT INTO `street_type` (`object_id`) VALUES (10000), (10001), (10002), (10003), (10004), (10005), (10006), (10007), (10008), (10009), (10010),
(10011), (10012), (10013), (10014), (10015), (10016);
INSERT INTO `street_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,10000,1400,10000,1400),
(1,10001,1400,10001,1400), (1,10002,1400,10002,1400), (1,10003,1400,10003,1400), (1,10004,1400,10004,1400), (1,10005,1400,10005,1400),
(1,10006,1400,10006,1400), (1,10007,1400,10007,1400), (1,10008,1400,10008,1400), (1,10009,1400,10009,1400), (1,10010,1400,10010,1400),
(1,10011,1400,10011,1400), (1,10012,1400,10012,1400), (1,10013,1400,10013,1400), (1,10014,1400,10014,1400), (1,10015,1400,10015,1400)
, (1,10016,1400,10016,1400);

INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('street_type_string_culture', 10017), ('street_type', 10017);

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (302, 'ru', UPPER('Тип улицы')),(302, 'uk', UPPER('Тип улицы'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (301, 300, 1, 302, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (301, 301, 'street_type');

CREATE TABLE `street_type_correction` (
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
    CONSTRAINT `fk_street_type_correction__street_type` FOREIGN KEY (`object_id`) REFERENCES `street_type` (`object_id`),
    CONSTRAINT `fk_street_type_correction__internal_organization` FOREIGN KEY (`internal_organization_id`) REFERENCES `organization` (`object_id`),
    CONSTRAINT `fk_street_type_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `street_type_correction`(`object_id`, `correction`, `organization_id`, `organization_code`, `internal_organization_id`) VALUES
(10000,'Б-Р',1,'1',0), (10001,'М',1,'1',0), (10002,'М-Н',1,'1',0), (10003,'ПЕР',1,'1',0), (10004,'ПЛ',1,'1',0), (10005,'П',1,'1',0)
, (10006,'ПОС',1,'1',0), (10007,'ПР-Д',1,'1',0), (10008,'ПРОСП',1,'1',0), (10009,'СП',1,'1',0), (10010,'Т',1,'1',0), (10011,'ТУП',1,'1',0)
, (10012,'УЛ',1,'1',0), (10013,'ШОССЕ',1,'1',0), (10014,'НАБ',1,'1',0), (10015,'В-Д',1,'1',0), (10016,'СТ',1,'1',0);

DELETE FROM `entity_type_correction` WHERE `entity_type_id` >= 10000 OR `entity_type_id` <= 10016;

DELIMITER /
CREATE PROCEDURE `street_type_update`()
BEGIN
    DECLARE l_object_id BIGINT(20);
    DECLARE l_entity_type_id BIGINT(20);
    DECLARE done INT;
    DECLARE street_cursor CURSOR FOR SELECT `object_id`, `entity_type_id` FROM `street`;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN street_cursor;
    street_loop: LOOP
        FETCH street_cursor INTO l_object_id, l_entity_type_id;
        IF done = 1 THEN
            LEAVE street_loop;
        END IF;
        INSERT INTO `street_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
            (1,l_object_id,301,l_entity_type_id,301);
    END LOOP street_loop;
    CLOSE street_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `street_type_update`();

DROP PROCEDURE `street_type_update`;

UPDATE `street` SET `entity_type_id` = NULL;

DELETE FROM `entity_type` WHERE `entity_id` = 300;
DELETE FROM `string_culture` WHERE `id` BETWEEN 10000 AND 10016;

ALTER TABLE `payment` ADD KEY `key_internal_street_type_id`(`internal_street_type_id`);
ALTER TABLE `payment` ADD CONSTRAINT `fk_payment__street_type` FOREIGN KEY (`internal_street_type_id`) REFERENCES `street_type` (`object_id`);

INSERT INTO `update` (`version`) VALUE ('20101103_332');

