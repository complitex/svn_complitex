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
ALTER TABLE `district_correction` ADD CONSTRAINT `fk_district_correction__city` FOREIGN KEY (`parent_id`) REFERENCES `city` (`object_id`);

ALTER TABLE `street_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `street_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `street_correction` ADD CONSTRAINT `fk_street_correction__district` FOREIGN KEY (`parent_id`) REFERENCES `district` (`object_id`);

ALTER TABLE `building_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `building_correction` ADD KEY `key_parent_id` (`parent_id`);
ALTER TABLE `building_correction` ADD CONSTRAINT `fk_building_correction__street` FOREIGN KEY (`parent_id`) REFERENCES `street` (`object_id`);

ALTER TABLE `ownership_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;
ALTER TABLE `privilege_correction` ADD COLUMN `parent_id` BIGINT(20) AFTER `object_id`;