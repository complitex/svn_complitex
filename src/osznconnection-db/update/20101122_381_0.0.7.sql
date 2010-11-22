DELETE FROM `building_address_attribute` WHERE `value_id` IS NULL AND `attribute_type_id` IN (1501,1502);
DELETE FROM `building_attribute` WHERE `attribute_type_id` = 500 AND `value_id` IS NULL;
DELETE FROM `organization_attribute` WHERE `attribute_type_id` = 902 AND `value_id` IS NULL;
UPDATE `organization_attribute` SET `start_date` = '2010-10-11 13:53:11' WHERE `object_id` = 0 AND `status` = 'ACTIVE';

INSERT INTO `update` (`version`) VALUE ('20101122_381');