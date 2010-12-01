-- Script updates locale related structures.

-- string_culture
ALTER TABLE `string_culture` DROP FOREIGN KEY `fk_string_culture__locale`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `string_culture` DROP KEY `unique_id__locale`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- apartment_string_culture
ALTER TABLE `apartment_string_culture` DROP FOREIGN KEY `fk_apartment_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `apartment_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `apartment_string_culture` DROP KEY `unique_id__locale`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- room_string_culture
ALTER TABLE `room_string_culture` DROP FOREIGN KEY `fk_room_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `room_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `room_string_culture` DROP KEY `unique_id__locale`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- street_string_culture
ALTER TABLE `street_string_culture` DROP FOREIGN KEY `fk_street_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `street_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `street_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- street_type_string_culture
ALTER TABLE `street_type_string_culture` DROP FOREIGN KEY `fk_street_type_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `street_type_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `street_type_string_culture` DROP KEY `unique_id__locale`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- city_string_culture
ALTER TABLE `city_string_culture` DROP FOREIGN KEY `fk_city_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `city_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `city_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- city_type_string_culture
ALTER TABLE `city_type_string_culture` DROP FOREIGN KEY `fk_city_type_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `city_type_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `city_type_string_culture` DROP KEY `unique_id__locale`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- building_address_string_culture
ALTER TABLE `building_address_string_culture` DROP FOREIGN KEY `fk_building_address_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `building_address_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `building_address_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- building_string_culture
ALTER TABLE `building_string_culture` DROP FOREIGN KEY `fk_building_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `building_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `building_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- district_string_culture
ALTER TABLE `district_string_culture` DROP FOREIGN KEY `fk_district_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `district_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `district_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- region_string_culture
ALTER TABLE `region_string_culture` DROP FOREIGN KEY `fk_region_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `region_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `region_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- country_string_culture
ALTER TABLE `country_string_culture` DROP FOREIGN KEY `fk_country_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `country_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `country_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- organization_string_culture
ALTER TABLE `organization_string_culture` DROP FOREIGN KEY `fk_organization_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `organization_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `organization_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- ownership_string_culture
ALTER TABLE `ownership_string_culture` DROP FOREIGN KEY `fk_ownership_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `ownership_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `ownership_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- privilege_string_culture
ALTER TABLE `privilege_string_culture` DROP FOREIGN KEY `fk_privilege_string_culture__locales`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `privilege_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `privilege_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

-- user_info_string_culture
ALTER TABLE `user_info_string_culture` DROP FOREIGN KEY `FK_user_info_string_culture_locale`, DROP KEY `key_locale`, 
ADD COLUMN `locale_id` BIGINT(20) AFTER `id`;
UPDATE `user_info_string_culture` SET `locale_id` = (CASE `locale` WHEN 'ru' THEN 1 WHEN 'uk' THEN 2 END);
ALTER TABLE `user_info_string_culture` DROP KEY `id`, ADD UNIQUE KEY `unique_id__locale`(`id`, `locale_id`), DROP COLUMN `locale`, 
ADD KEY `key_locale`(`locale_id`), MODIFY COLUMN `locale_id` BIGINT(20) NOT NULL;

DROP TABLE IF EXISTS `locales`;

CREATE TABLE `locales` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `locale` VARCHAR(2) NOT NULL,
  `system` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_locale` (`locale`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `locales`(`id`, `locale`, `system`) VALUES (1, 'ru', 1);
INSERT INTO `locales`(`id`, `locale`, `system`) VALUES (2, 'uk', 0);

ALTER TABLE `string_culture` ADD CONSTRAINT `fk_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `apartment_string_culture` ADD CONSTRAINT `fk_apartment_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `room_string_culture` ADD CONSTRAINT `fk_room_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `street_string_culture` ADD CONSTRAINT `fk_street_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `street_type_string_culture` ADD CONSTRAINT `fk_street_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `city_string_culture` ADD CONSTRAINT `fk_city_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `city_type_string_culture` ADD CONSTRAINT `fk_city_type_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `building_address_string_culture` ADD CONSTRAINT `fk_building_address_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `building_string_culture` ADD CONSTRAINT `fk_building_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `district_string_culture` ADD CONSTRAINT `fk_district_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `region_string_culture` ADD CONSTRAINT `fk_region_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `country_string_culture` ADD CONSTRAINT `fk_country_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `organization_string_culture` ADD CONSTRAINT `fk_organization_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `ownership_string_culture` ADD CONSTRAINT `fk_ownership_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `privilege_string_culture` ADD CONSTRAINT `fk_privilege_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);
ALTER TABLE `user_info_string_culture` ADD CONSTRAINT `fk_user_info_string_culture__locales` FOREIGN KEY (`locale_id`) REFERENCES `locales` (`id`);

INSERT INTO `update` (`version`) VALUE ('20101201_403');

