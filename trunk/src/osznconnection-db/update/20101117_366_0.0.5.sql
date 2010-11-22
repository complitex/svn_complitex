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
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__start_date` (`object_id`,`start_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_parent_id` (`parent_id`),
  KEY `key_parent_entity_id` (`parent_entity_id`),
  KEY `key_entity_type_id` (`entity_type_id`),
  KEY `key_start_date` (`start_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_building_address__entity_type` FOREIGN KEY (`entity_type_id`) REFERENCES `entity_type` (`id`),
  CONSTRAINT `fk_building_address__entity` FOREIGN KEY (`parent_entity_id`) REFERENCES `entity` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

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
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `building_address_string_culture`;

CREATE TABLE `building_address_string_culture` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `id` BIGINT(20) NOT NULL,
  `locale` VARCHAR(2) NOT NULL,
  `value` VARCHAR(1000),
  PRIMARY KEY (`pk_id`),
  UNIQUE KEY  (`id`,`locale`),
  KEY `key_locale` (`locale`),
  KEY `key_value` (`value`),
  CONSTRAINT `fk_building_address_string_culture__locales` FOREIGN KEY (`locale`) REFERENCES `locales` (`locale`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1500, 'ru', 'Адрес здания'), (1500, 'uk', 'Адрес здания');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1500, 'building_address', 1500, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1501, 'ru', UPPER('Номер дома')), (1501, 'uk', UPPER('Номер будинку'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1500, 1500, 1, 1501, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1502, 'ru', UPPER('Корпус')), (1502, 'uk', UPPER('Корпус'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1501, 1500, 0, 1502, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1503, 'ru', UPPER('Строение')), (1503, 'uk', UPPER('Будова'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1502, 1500, 0, 1503, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1500, 1500, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1501, 1501, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1502, 1502, UPPER('string_culture'));

INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('building_address', 10000), ('building_address_string_culture', 10000);

-- delete "wrong" buildings
DELETE FROM `building` WHERE `object_id` IN (12507,13210,13813,13835,14278,14518,15129,15882,17195,17271,17287);
DELETE FROM `building_correction` WHERE `object_id` IN (10000,10002,10150,10601,10817);
DELETE FROM `building_attribute` WHERE `object_id` IN (10000,10002,10150,10601,10817);
DELETE FROM `building` WHERE `object_id` IN (10000,10002,10150,10601,10817);


SET @old_log_bin_trust_routine_creators = @@global.log_bin_trust_routine_creators;
SET GLOBAL log_bin_trust_routine_creators = 1;
DELIMITER /
CREATE FUNCTION `copy_building_strings`(l_value_id BIGINT(20)) RETURNS BIGINT(20)
BEGIN
    DECLARE l_value VARCHAR(1000);
    DECLARE l_locale VARCHAR(2);
    DECLARE l_inserted INT;
    DECLARE building_strings_seq BIGINT(20);
    DECLARE building_strings_done INT;
    DECLARE building_strings_cursor CURSOR FOR SELECT `locale`, `value` FROM `building_string_culture` WHERE `id` = l_value_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET building_strings_done = 1;

    SET building_strings_done = 0;
    SET l_inserted = 0;
    SELECT `sequence_value` INTO building_strings_seq FROM `sequence` WHERE `sequence_name` = 'building_address_string_culture';
    OPEN building_strings_cursor;
    building_strings_loop: LOOP
        FETCH building_strings_cursor INTO l_locale, l_value;
        IF building_strings_done = 1 THEN
            LEAVE building_strings_loop;
        END IF;
        IF ((l_value IS NOT NULL) AND (TRIM(l_value) != '')) THEN
	    INSERT INTO `building_address_string_culture`(`id`, `locale`, `value`) VALUES (building_strings_seq, l_locale, l_value);
	    SET l_inserted = 1;
        END IF;
    END LOOP building_strings_loop;
    SET building_strings_done = 0;
    CLOSE building_strings_cursor;
    IF l_inserted = 1 THEN
	SET building_strings_seq = building_strings_seq+1;
	UPDATE `sequence` SET `sequence_value` = building_strings_seq WHERE `sequence_name` = 'building_address_string_culture';
	RETURN building_strings_seq;
    ELSE 
	RETURN NULL;
    END IF;
END/
DELIMITER ;
SET GLOBAL log_bin_trust_routine_creators = @old_log_bin_trust_routine_creators;

DELIMITER /
CREATE PROCEDURE `building_structure_update`()
BEGIN
    DECLARE l_object_id BIGINT(20);
    DECLARE l_city_id BIGINT(20);

    DECLARE l_attr_id INT;
    DECLARE building_address_seq BIGINT(20);
    DECLARE l_parent_id BIGINT(20);
    DECLARE l_parent_entity_id BIGINT(20);
    DECLARE l_street_id BIGINT(20);
    DECLARE l_address_count INT;
    DECLARE l_number BIGINT(20);
    DECLARE l_corp BIGINT(20);
    DECLARE l_structure BIGINT(20);
        
    DECLARE building_done INT;
    DECLARE building_cursor CURSOR FOR SELECT `object_id`, `parent_id` FROM `building`;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET building_done = 1;    
    
    SET building_done = 0;
    OPEN building_cursor;
    building_loop: LOOP
        FETCH building_cursor INTO l_object_id, l_city_id;
        IF building_done = 1 THEN
            LEAVE building_loop;
        END IF;      
            
            SET l_attr_id = 1;            

            SELECT (CASE COUNT(*) 
			WHEN 0 THEN 0
			ELSE t.`amount` 
	           END) INTO l_address_count 
	        FROM (SELECT COUNT(*) amount FROM `building_attribute` a WHERE a.`object_id` = l_object_id GROUP BY a.`attribute_type_id` HAVING a.`attribute_type_id` = 500) t;

            WHILE l_attr_id <= l_address_count DO
                SELECT a.`value_id` INTO l_street_id FROM `building_attribute` a WHERE a.`object_id` = l_object_id
                AND a.`attribute_id` = l_attr_id AND a.`attribute_type_id` = 503;
                IF l_street_id IS NOT NULL THEN
                    SET l_parent_id = l_street_id;
                    SET l_parent_entity_id = 300;
                ELSE
                    SET l_parent_id = l_city_id;
                    SET l_parent_entity_id = 400;
                END IF;

                SELECT `sequence_value` INTO building_address_seq FROM `sequence` WHERE `sequence_name` = 'building_address';
                INSERT INTO `building_address` (`object_id`, `parent_id`, `parent_entity_id`) VALUES (building_address_seq, l_parent_id, l_parent_entity_id);
                UPDATE `sequence` SET `sequence_value` = (building_address_seq+1) WHERE `sequence_name` = 'building_address';

                -- number
                SELECT a.`value_id` INTO l_number FROM `building_attribute` a WHERE a.`object_id` = l_object_id AND a.`attribute_id` = l_attr_id AND a.`attribute_type_id` = 500;
                IF l_number IS NOT NULL THEN
		    SELECT copy_building_strings(l_number) INTO l_number;
                    INSERT INTO `building_address_attribute` (`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (building_address_seq, 1, 1500, l_number, 1500);
                END IF;

                -- corp
                SELECT a.`value_id` INTO l_corp FROM `building_attribute` a WHERE a.`object_id` = l_object_id AND a.`attribute_id` = l_attr_id AND a.`attribute_type_id` = 501;
                IF l_corp IS NOT NULL THEN
                    SELECT copy_building_strings(l_corp) INTO l_corp;
                    INSERT INTO `building_address_attribute` (`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (building_address_seq, 1, 1501, l_corp, 1501);
                END IF;
                
                -- structure 
                SELECT a.`value_id` INTO l_structure FROM `building_attribute` a WHERE a.`object_id` = l_object_id AND a.`attribute_id` = l_attr_id AND a.`attribute_type_id` = 502;
                IF l_structure IS NOT NULL THEN
                    SELECT copy_building_strings(l_structure) INTO l_structure;
                    INSERT INTO `building_address_attribute` (`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (building_address_seq, 1, 1502, l_structure, 1502);
                END IF;

                DELETE FROM `building_attribute` WHERE `object_id` = l_object_id AND `attribute_id` = l_attr_id AND `attribute_type_id` IN (500,501,502,503);                
                UPDATE `building_attribute` SET `attribute_type_id` = 500, `value_type_id` = 500 WHERE `object_id` = l_object_id AND `attribute_type_id` = 504;

                IF l_attr_id = 1 THEN	            
                    UPDATE `building` SET `parent_id` = building_address_seq, `parent_entity_id` = 1500 WHERE `object_id` = l_object_id;
                ELSE
                    INSERT INTO `building_attribute` (`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (l_object_id, l_attr_id-1, 501, building_address_seq, 501);
                END IF;

                SET l_attr_id = l_attr_id+1;
            END WHILE;
                    
    END LOOP building_loop;
    CLOSE building_cursor;
    SET building_done = 0;
END/
DELIMITER ;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
DELETE FROM `building` WHERE `status` = 'ARCHIVE';
DELETE FROM `building_attribute` WHERE `status` = 'ARCHIVE';
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;

CALL `building_structure_update`();

DELETE FROM `building_string_culture`;
UPDATE `sequence` SET `sequence_value` = 1 WHERE `sequence_name` = 'building_string_culture';

DROP PROCEDURE `building_structure_update`;
DROP FUNCTION `copy_building_strings`;

DELETE FROM `entity_attribute_value_type` WHERE `id` IN (502,503,504);
DELETE FROM `entity_attribute_type` WHERE `id` IN (502,503,504);

DELETE FROM `string_culture` WHERE `id` IN (503,504,505);
UPDATE `string_culture` SET `value` = 'Район' WHERE `id` = 501;
UPDATE `string_culture` SET `value` = 'Альтернативный адрес' WHERE `id` = 502;
UPDATE `entity_attribute_type` SET `mandatory` = 0 WHERE `id` = 500;
UPDATE `entity_attribute_value_type` SET `attribute_value_type` = 'district' WHERE `id` = 500;
UPDATE `entity_attribute_value_type` SET `attribute_value_type` = 'building_address' WHERE `id` = 501;

UPDATE `city` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `city_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `city_type` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `city_type_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `country` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `country_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `region` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `region_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `district` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `district_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `street` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `street_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `street_type` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `street_type_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `building` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `building_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `building_address` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `building_address_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `apartment` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `apartment_attribute` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `room` SET `start_date` = '2010-09-27 13:00:00';
UPDATE `room_attribute` SET `start_date` = '2010-09-27 13:00:00';

INSERT INTO `update` (`version`) VALUE ('20101117_366');

