/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

DROP TABLE IF EXISTS `locales`;
CREATE TABLE `locales` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор локали',
  `locale` VARCHAR(2) NOT NULL COMMENT 'Код локали',
  `system` TINYINT(1) NOT NULL default 0 COMMENT 'Является ли локаль системной',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_locale` (`locale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Локаль';

-- ------------------------------
-- User
-- ------------------------------
DROP TABLE IF EXISTS `user`;

CREATE TABLE  `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор пользователя',
  `login` VARCHAR(45) NOT NULL COMMENT 'Имя пользователя',
  `password` VARCHAR(45) NOT NULL COMMENT 'MD5 хэш пароля',
  `user_info_object_id` BIGINT(20) COMMENT 'Идентификатор объекта информация о пользователе',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key_login` (`login`),
  KEY `key_user_info_object_id` (`user_info_object_id`),
  CONSTRAINT `fk_user__user_info` FOREIGN KEY (`user_info_object_id`) REFERENCES `user_info` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Пользователь';

-- ------------------------------
-- Usergroup
-- ------------------------------
DROP TABLE IF EXISTS `usergroup`;

CREATE TABLE  `usergroup` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор группы пользователей',
  `login` VARCHAR(45) NOT NULL COMMENT 'Имя пользователя',
  `group_name` VARCHAR(45) NOT NULL COMMENT 'Название группы',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_login__group_name` (`login`, `group_name`),
  CONSTRAINT `fk_usergroup__user` FOREIGN KEY (`login`) REFERENCES `user` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Группа пользователей';

-- ------------------------------
-- Log
-- ------------------------------
DROP TABLE IF EXISTS `log`;

CREATE TABLE  `log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор записи журнала событий',
  `date` DATETIME COMMENT 'Дата',
  `login` VARCHAR(45) COMMENT 'Имя пользователя',
  `module` VARCHAR(100) COMMENT 'Название модуля системы',
  `object_id` BIGINT(20) COMMENT 'Идентификатор объекта',
  `controller` VARCHAR(100) COMMENT 'Название класса обработчика',
  `model` VARCHAR(100) COMMENT 'Название класса модели данных',
  `event` VARCHAR(100) COMMENT 'Название события',
  `status` VARCHAR(100) COMMENT 'Статус',
  `description` VARCHAR(255) COMMENT 'Описание',
  PRIMARY KEY (`id`),
  KEY `key_login` (`login`),
  KEY `key_date` (`date`),
  KEY `key_controller` (`controller`),
  KEY `key_model` (`model`),
  KEY `key_event` (`event`),
  KEY `key_module` (`module`),
  KEY `key_status` (`status`),
  KEY `key_description` (`description`),
  CONSTRAINT `fk_log__user` FOREIGN KEY (`login`) REFERENCES `user` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Журнал событий';

-- ------------------------------
-- Log change
-- ------------------------------
DROP TABLE IF EXISTS `log_change`;

CREATE TABLE `log_change` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор изменения',
    `log_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор журнала событий',
    `attribute_id` BIGINT(20) COMMENT 'Идентификатор атрибута',
    `collection` VARCHAR(100) COMMENT 'Название группы параметров',
    `property` VARCHAR(100) COMMENT 'Свойство',
    `old_value` VARCHAR(500) COMMENT 'Предыдущее значение',
    `new_value` VARCHAR(500) COMMENT 'Новое значение',
    `locale` VARCHAR(2) COMMENT 'Код локали',
    PRIMARY KEY (`id`),
    KEY `key_log` (`log_id`),
    CONSTRAINT `fk_log_change__log` FOREIGN KEY (`log_id`) REFERENCES `log` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Изменения модели данных';

-- ------------------------------
-- Config
-- ------------------------------

DROP TABLE IF EXISTS `config`;

CREATE TABLE `config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор настройки',
    `name` VARCHAR(64) NOT NULL COMMENT 'Имя',
    `value` VARCHAR(255) NOT NULL COMMENT 'Значение',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Настройки';

-- ------------------------------
-- Update
-- ------------------------------

DROP TABLE IF EXISTS `update`;

CREATE TABLE `update` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор обновления',
    `version` VARCHAR(64) NOT NULL COMMENT 'Версия',
    `date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Дата обновления',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Обновление базы данных';

-- ------------------------------
-- Preference
-- ------------------------------

DROP TABLE IF EXISTS `preference`;

CREATE TABLE `preference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Идентификатор предпочтения',
    `user_id` BIGINT(20) NOT NULL COMMENT 'Идентификатор пользователя',
    `page` VARCHAR(255) NOT NULL COMMENT 'Класс страницы',
    `key` VARCHAR(255) NOT NULL COMMENT 'Ключ',
    `value` VARCHAR(255) NOT NULL COMMENT 'Значение',
    PRIMARY KEY (`id`),
    KEY `key_user_id` (`user_id`),
    CONSTRAINT `fk_preference__user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT 'Предпочтения пользователя';

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;