-- Script restores buildings that were in incorrect format.

DELIMITER /
CREATE PROCEDURE `insert_building`(number VARCHAR(1000), street_id BIGINT(20), start_date DATETIME, district_id BIGINT(20))
BEGIN
	DECLARE l_string_id BIGINT(20);
	DECLARE l_address_id BIGINT(20);
	DECLARE l_object_id BIGINT(20);
	DECLARE l_address_parent_id BIGINT(20);
	DECLARE l_address_parent_entity_id BIGINT(20);
	DECLARE l_parent_id BIGINT(20);
	DECLARE l_parent_entity_id BIGINT(20);

	SELECT `sequence_value` INTO l_address_id FROM `sequence` WHERE `sequence_name` = 'building_address';
	INSERT INTO `building_address`(`object_id`, `parent_id`, `parent_entity_id`, `start_date`) VALUES (l_address_id, street_id, 300, start_date);
	UPDATE `sequence` SET `sequence_value` = (l_address_id+1) WHERE `sequence_name` = 'building_address';

	-- number
	SELECT `sequence_value` INTO l_string_id FROM `sequence` WHERE `sequence_name` = 'building_address_string_culture';
	INSERT INTO `building_address_string_culture` (`id`, `locale`, `value`) VALUES (l_string_id, 'ru', number);
	INSERT INTO `building_address_attribute`(`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`) VALUES
		(l_address_id, 1, 1500, l_string_id, 1500, start_date);
	UPDATE `sequence` SET `sequence_value` = (l_string_id+1) WHERE `sequence_name` = 'building_address_string_culture';

	-- building
	SELECT `sequence_value` INTO l_object_id FROM `sequence` WHERE `sequence_name` = 'building';
	INSERT INTO `building`(`object_id`, `parent_id`, `parent_entity_id`, `start_date`) VALUES (l_object_id, l_address_id, 1500, start_date);
	INSERT INTO `building_attribute`(`object_id`, `attribute_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`) VALUES
	(l_object_id, 1, 500, district_id, 500, start_date);
	UPDATE `sequence` SET `sequence_value` = (l_object_id+1) WHERE `sequence_name` = 'building';

END /
DELIMITER ;

CALL `insert_building`('25А',10283,NOW(),10000);
CALL `insert_building`('45',10283,NOW(),10000);
CALL `insert_building`('1',10074,NOW(),10006);
CALL `insert_building`('5',11752,NOW(),10007);
CALL `insert_building`('33',10023,NOW(),10002);

DROP PROCEDURE `insert_building`;

INSERT INTO `update` (`version`) VALUE ('20101129_399');

