ALTER TABLE `person_account` MODIFY COLUMN `building_corp` VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE `person_account` ADD UNIQUE KEY `person_account_unique_key` (`first_name`,`middle_name`,`last_name`,`city`,`street`,`building_num`,`building_corp`,`apartment`,`own_num_sr`,`oszn_id`,`calc_center_id`);
ALTER TABLE `person_account` DROP COLUMN `dat1`;

INSERT INTO `update` (`version`) VALUE ('20101118_372');