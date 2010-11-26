-- Script for deletion of dublicates of streets and building addresses.

-- remove dublicate streets

CREATE TABLE `street_dublicate`(
    `id1` BIGINT(20) NOT NULL,
    `id2` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id1`, `id2`)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `street_dublicate`(`id1`,`id2`) SELECT s1.`object_id`, s2.`object_id` 
	FROM `street` s1
	JOIN `street_attribute` a1 ON (s1.`object_id` = a1.`object_id` AND a1.`status` = 'ACTIVE' AND a1.`attribute_type_id` = 300)
	JOIN `street_attribute` t1 ON (s1.`object_id` = t1.`object_id` AND t1.`status` = 'ACTIVE' AND t1.`attribute_type_id` = 301)
	JOIN `street_string_culture` sc1 ON (a1.`value_id` = sc1.`id` AND sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `street_string_culture` sc2 ON (sc1.`value` = sc2.`value` AND sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `street_attribute` a2 ON (a2.`value_id` = sc2.`id` AND a2.`status` = 'ACTIVE' AND a2.`attribute_type_id` = 300)
	JOIN `street` s2 ON s2.`object_id` = a2.`object_id`
	JOIN `street_attribute` t2 ON (t1.`value_id` = t2.`value_id` AND s2.`object_id` = t2.`object_id` AND t2.`status` = 'ACTIVE' AND t2.`attribute_type_id` = 301)
	WHERE s1.`object_id` < s2.`object_id` AND 
	((s1.`parent_id` IS NULL AND s1.`parent_entity_id` IS NULL AND s2.`parent_id` IS NULL AND s2.`parent_entity_id` IS NULL) 
		OR (s1.`parent_id` = s2.`parent_id` AND s1.`parent_entity_id` = s2.`parent_entity_id`));

DELIMITER /
CREATE PROCEDURE `remove_street_dublicate`()
BEGIN
    DECLARE l_id1 BIGINT(20);
    DECLARE l_id2 BIGINT(20);
    DECLARE done INT;
    DECLARE street_cursor CURSOR FOR SELECT s.`id1`, s.`id2` FROM `street_dublicate` s ORDER BY s.`id1` DESC;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN street_cursor;
    street_loop: LOOP
        FETCH street_cursor INTO l_id1, l_id2;
        IF done = 1 THEN
            LEAVE street_loop;
        END IF;
        
	UPDATE `building_address` SET `parent_id` = l_id1 WHERE `parent_id` = l_id2;
	UPDATE `payment` SET `internal_street_id` = l_id1 WHERE `internal_street_id` = l_id2;
	DELETE FROM `building_correction` WHERE `parent_id` IN (SELECT `id` FROM `street_correction` WHERE `object_id` = l_id2);
	DELETE FROM `street_correction` WHERE `object_id` = l_id2;
	DELETE FROM `street_string_culture` WHERE `id` IN (SELECT a.`value_id` FROM `street_attribute` a WHERE a.`object_id` = l_id2 AND a.`attribute_type_id` = 300);
	DELETE FROM `street_attribute` WHERE `object_id` = l_id2;
	DELETE FROM `street` WHERE `object_id` = l_id2;
    END LOOP street_loop;
    CLOSE street_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `remove_street_dublicate`();
 
DROP PROCEDURE `remove_street_dublicate`;
DROP TABLE IF EXISTS `street_dublicate`;

-- remove dublicate building addresses

CREATE TABLE `building_address_dublicate`(
    `id1` BIGINT(20) NOT NULL,
    `id2` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id1`, `id2`)
)ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `building_address_dublicate`(`id1`,`id2`) SELECT ad1.`object_id`, ad2.`object_id`
	FROM `building_address` ad1
	JOIN `street` s1 ON s1.`object_id` = ad1.`parent_id`
	JOIN `street_attribute` sa1 ON (sa1.`object_id` = s1.`object_id` AND sa1.`status` = 'ACTIVE' AND sa1.`attribute_type_id` = 300)
	JOIN `street_string_culture` s1_value ON (sa1.`value_id` = s1_value.`id` AND s1_value.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `building_address_attribute` num1 ON (ad1.`object_id` = num1.`object_id` AND num1.`status` = 'ACTIVE' AND num1.`attribute_type_id` = 1500)
	JOIN `building_address_string_culture` num_sc1 ON (num1.`value_id` = num_sc1.`id` AND num_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` corp1 ON (ad1.`object_id` = corp1.`object_id` AND corp1.`status` = 'ACTIVE' AND corp1.`attribute_type_id` = 1501)
	LEFT JOIN `building_address_string_culture` corp_sc1 ON (corp1.`value_id` = corp_sc1.`id` AND corp_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` st1 ON (ad1.`object_id` = st1.`object_id` AND st1.`status` = 'ACTIVE' AND st1.`attribute_type_id` = 1502)
	LEFT JOIN `building_address_string_culture` st_sc1 ON (st1.`value_id` = st_sc1.`id` AND st_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `building_address_string_culture` num_sc2 ON (num_sc1.`value` = num_sc2.`value` AND num_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `building_address_attribute` num2 ON (num2.`value_id` = num_sc2.`id` AND num2.`status` = 'ACTIVE' AND num2.`attribute_type_id` = 1500)
	JOIN `building_address` ad2 ON (ad2.`object_id` = num2.`object_id` AND ad2.`status` = 'ACTIVE')
	JOIN `street` s2 ON s2.`object_id` = ad2.`parent_id`
	JOIN `street_attribute` sa2 ON (sa2.`object_id` = s2.`object_id` AND sa2.`status` = 'ACTIVE' AND sa2.`attribute_type_id` = 300)
	JOIN `street_string_culture` s2_value ON (sa2.`value_id` = s2_value.`id` AND s2_value.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` corp2 ON (ad2.`object_id` = corp2.`object_id` AND corp2.`status` = 'ACTIVE' AND corp2.`attribute_type_id` = 1501)
	LEFT JOIN `building_address_string_culture` corp_sc2 ON (corp2.`value_id` = corp_sc2.`id` AND corp_sc2.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` st2 ON (ad2.`object_id` = st2.`object_id` AND st2.`status` = 'ACTIVE' AND st2.`attribute_type_id` = 1502)
	LEFT JOIN `building_address_string_culture` st_sc2 ON (st2.`value_id` = st_sc2.`id` AND st_sc2.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	WHERE ad1.`status` = 'ACTIVE' AND ad1.`object_id` < ad2.`object_id` 
	AND ad1.`parent_id` = ad2.`parent_id` AND ad1.`parent_entity_id` = ad2.`parent_entity_id`
	AND ((corp_sc1.`value` IS NULL AND corp_sc2.`value` IS NULL) OR corp_sc1.`value` = corp_sc2.`value`) 
	AND ((st_sc1.`value` IS NULL AND st_sc2.`value` IS NULL) OR st_sc1.`value` = st_sc2.`value`);

DELIMITER /
CREATE PROCEDURE `remove_building_address_dublicate`()
BEGIN
    DECLARE l_id1 BIGINT(20);
    DECLARE l_id2 BIGINT(20);
    DECLARE l_building_id BIGINT(20);
    DECLARE l_count BIGINT(20);
    DECLARE l_building_parent_id BIGINT(20);
    DECLARE done INT;
    DECLARE address_cursor CURSOR FOR SELECT ad.`id1`, ad.`id2` FROM `building_address_dublicate` ad ORDER BY ad.`id1` DESC;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    OPEN address_cursor;
    address_loop: LOOP
        FETCH address_cursor INTO l_id1, l_id2;
        IF done = 1 THEN
            LEAVE address_loop;
        END IF;
	
	SELECT COUNT(*) INTO l_count FROM `building_address` ad WHERE ad.`object_id` = l_id2;
	IF l_count != 0 THEN
		UPDATE `payment` SET `internal_building_id` = l_id1 WHERE `internal_building_id` = (SELECT b.`object_id` FROM `building` b WHERE b.`parent_id` = l_id2);
		DELETE FROM `building_correction` WHERE `object_id` = (SELECT b.`object_id` FROM `building` b WHERE b.`parent_id` = l_id2);
		DELETE FROM `building_attribute` WHERE `object_id` = (SELECT b.`object_id` FROM `building` b WHERE b.`parent_id` = l_id2);
		DELETE FROM `building` WHERE `parent_id` = l_id2;
					
		DELETE FROM `building_address_string_culture` WHERE `id` IN (SELECT a.`value_id` FROM `building_address_attribute` a WHERE a.`object_id` = l_id2 AND a.`attribute_type_id` IN (1500, 1501, 1502));
		DELETE FROM `building_address_attribute` WHERE `object_id` = l_id2;
		DELETE FROM `building_address` WHERE `object_id` = l_id2;
	END IF;	
    END LOOP address_loop;
    CLOSE address_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `remove_building_address_dublicate`();
 
DROP PROCEDURE `remove_building_address_dublicate`;
DROP TABLE IF EXISTS `building_address_dublicate`;

INSERT INTO `update` (`version`) VALUE ('20101126_395');

