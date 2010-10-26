CREATE TABLE `update` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `version` VARCHAR(64) NOT NULL,
    `date` TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `update` (`version`) value ('20101020_300');

ALTER TABLE `city_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;

ALTER TABLE `district_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `district_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `district_correction` ADD CONSTRAINT `fk_district_correction__city_correction`
  FOREIGN KEY (`parent_id`) REFERENCES `city_correction` (`id`);

ALTER TABLE `street_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `street_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `street_correction` ADD CONSTRAINT `fk_street_correction__city_correction`
  FOREIGN KEY (`parent_id`) REFERENCES `city_correction` (`id`);

UPDATE `street_correction` sc SET sc.`parent_id` = (SELECT cc.`id` FROM `city_correction` cc
   WHERE cc.object_id = (SELECT s.`parent_id` FROM `street` s WHERE s.`object_id` = sc.`object_id`)
    AND cc.`organization_id` = sc.`organization_id` LIMIT 1);

ALTER TABLE `building_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `building_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `building_correction` ADD CONSTRAINT `fk_building_correction__street_correction`
  FOREIGN KEY (`parent_id`) REFERENCES `street_correction` (`id`);

UPDATE `building_correction` bc SET bc.`parent_id` = (SELECT sc.`id` FROM `street_correction` sc
   WHERE sc.`object_id` = (SELECT ba.`value_id` FROM `building_attribute` ba WHERE ba.`object_id` = bc.`object_id`
    AND ba.`attribute_type_id` = 503) AND bc.`organization_id` = sc.`organization_id` LIMIT 1);

ALTER TABLE `ownership_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `privilege_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;