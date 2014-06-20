/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;


-- drop schema junitTest;
-- create schema junitTest;
-- create user 'junitTest'@'localhost' identified by 'junitTest';
-- grant all privileges on junitTest.* to 'junitTest'@'localhost' WITH GRANT OPTION;
-- flush privileges;

-- ------------------------------
-- Registry number
-- ------------------------------

DROP TABLE IF EXISTS `test`;

CREATE TABLE `test` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Автоинкремент',
  `value` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT 'Используется для тестирования';

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;