/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

-- ------------------------------
-- Corrections
-- ------------------------------
DROP TABLE IF EXISTS `city_correction`;

CREATE TABLE `city_correction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта населенного пункта',
  `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор объекта',
  `correction` VARCHAR(100) NOT NULL COMMENT 'Название населенного пункта',
  `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
  `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
  `user_organization_id` BIGINT(20),
  `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
  `status` INTEGER COMMENT 'Статус',
  PRIMARY KEY (`id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_correction` (`correction`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_user_organization_id` (`user_organization_id`),
  KEY `key_module_id` (`module_id`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_city_correction__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_city_correction__city` FOREIGN KEY (`object_id`) REFERENCES `city` (`object_id`),
  CONSTRAINT `fk_city_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция населенного пункта';

DROP TABLE IF EXISTS `city_type_correction`;

CREATE TABLE `city_type_correction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта типа населенного пункта',
  `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор объекта',
  `correction` VARCHAR(100) NOT NULL COMMENT 'Название типа населенного пункта',
  `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
  `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
  `user_organization_id` BIGINT(20),
  `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
  `status` INTEGER COMMENT 'Статус',
  PRIMARY KEY (`id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_correction` (`correction`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_user_organization_id` (`user_organization_id`),
  KEY `key_module_id` (`module_id`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_city_type__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_city_type_correction__city_type` FOREIGN KEY (`object_id`) REFERENCES `city_type` (`object_id`),
  CONSTRAINT `fk_city_type_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция типа населенного пункта';

DROP TABLE IF EXISTS `district_correction`;

CREATE TABLE `district_correction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
  `city_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта населенного пункта',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта района',
  `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор объекта',
  `correction` VARCHAR(100) NOT NULL COMMENT 'Название типа населенного пункта',
  `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
  `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
  `user_organization_id` BIGINT(20),
  `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
  `status` INTEGER COMMENT 'Статус',
  PRIMARY KEY (`id`),
  KEY `key_city_object_id` (`city_object_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_correction` (`correction`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_user_organization_id` (`user_organization_id`),
  KEY `key_module_id` (`module_id`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_district_correction__city` FOREIGN KEY (`city_object_id`) REFERENCES `city` (`object_id`),
  CONSTRAINT `fk_district_correction__district` FOREIGN KEY (`object_id`) REFERENCES `district` (`object_id`),
  CONSTRAINT `fk_district_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_district_correction__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция района';

DROP TABLE IF EXISTS `street_correction`;

CREATE TABLE `street_correction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
  `city_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта населенного пункта',
  `street_type_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта типа улицы',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта улицы',
  `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор объекта',
  `correction` VARCHAR(100) NOT NULL COMMENT 'Название типа населенного пункта',
  `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
  `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
  `user_organization_id` BIGINT(20),
  `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
  `status` INTEGER COMMENT 'Статус',
  PRIMARY KEY (`id`),
  KEY `key_city_object_id` (`city_object_id`),
  KEY `key_street_type_object_id` (`street_type_object_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_correction` (`correction`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_user_organization_id` (`user_organization_id`),
  KEY `key_module_id` (`module_id`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_street_correction__city` FOREIGN KEY (`city_object_id`) REFERENCES `city` (`object_id`),
  CONSTRAINT `fk_street_correction__street_type` FOREIGN KEY (`street_type_object_id`) REFERENCES `street_type` (`object_id`),
  CONSTRAINT `fk_street_correction__street` FOREIGN KEY (`object_id`) REFERENCES `street` (`object_id`),
  CONSTRAINT `fk_street_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_street_correction__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция улицы';

DROP TABLE IF EXISTS `street_type_correction`;

CREATE TABLE `street_type_correction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта типа населенного пункта',
  `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор объекта',
  `correction` VARCHAR(100) NOT NULL COMMENT 'Название типа населенного пункта',
  `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
  `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
  `user_organization_id` BIGINT(20),
  `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
  `status` INTEGER COMMENT 'Статус',
  PRIMARY KEY (`id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_correction` (`correction`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_user_organization_id` (`user_organization_id`),
  KEY `key_module_id` (`module_id`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_street_type__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_street_type_correction__street_type` FOREIGN KEY (`object_id`) REFERENCES `street_type` (`object_id`),
  CONSTRAINT `fk_street_type_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция типа улицы';

DROP TABLE IF EXISTS `building_correction`;

CREATE TABLE `building_correction` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор коррекции',
  `street_object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта улица',
  `object_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор объекта дом',
  `external_id` VARCHAR(20) COMMENT 'Внешний идентификатор объекта',
  `correction` VARCHAR(100) NOT NULL COMMENT 'Номер дома',
  `correction_corp` VARCHAR(20) NOT NULL DEFAULT '' COMMENT 'Корпус дома',
  `begin_date` DATE NOT NULL DEFAULT '1970-01-01' COMMENT 'Дата начала актуальности соответствия',
  `end_date` DATE NOT NULL DEFAULT '2054-12-31' COMMENT 'Дата окончания актуальности соответствия',
  `organization_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор организации',
  `user_organization_id` BIGINT(20),
  `module_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор модуля',
  `status` INTEGER COMMENT 'Статус',
  PRIMARY KEY (`id`),
  KEY `key_street_object_id` (`street_object_id`),
  KEY `key_object_id` (`object_id`),
  KEY `key_correction` (`correction`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  KEY `key_organization_id` (`organization_id`),
  KEY `key_user_organization_id` (`user_organization_id`),
  KEY `key_module_id` (`module_id`),
  KEY `key_status` (`status`),
  CONSTRAINT `fk_building_correction__street` FOREIGN KEY (`street_object_id`) REFERENCES `street` (`object_id`),
  CONSTRAINT `fk_building_correction__building` FOREIGN KEY (`object_id`) REFERENCES `building` (`object_id`),
  CONSTRAINT `fk_building_correction__organization` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`object_id`),
  CONSTRAINT `fk_building_correction__user_organization` FOREIGN KEY (`user_organization_id`) REFERENCES `organization` (`object_id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Коррекция дома';

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;