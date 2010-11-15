ALTER TABLE `request_file_group` ADD COLUMN `status` INTEGER;

-- --------------
-- Migrate status
-- --------------

-- LOADED
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=112 WHERE f.`status`=5;

-- LOAD_ERROR
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=111 WHERE f.`status`=4;

-- LOADING
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=112 WHERE f.`status`=3;

-- BOUND
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=120 WHERE f.`status`=8;

-- BIND_ERROR
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=121 WHERE f.`status`=7;

-- BINDING
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=122 WHERE f.`status`=6;

-- FILLED
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=130 WHERE f.`status`=14;

-- FILL_ERROR
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=131 WHERE f.`status`=13;

-- FILLING
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=132 WHERE f.`status`=12;

-- SAVED
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=140 WHERE f.`status`=11;

-- SAVE_ERROR
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=141 WHERE f.`status`=10;

-- SAVING
UPDATE `request_file_group` g RIGHT JOIN `request_file` f ON g.`id`=f.`group_id` SET g.`status`=142 WHERE f.`status`=9;

ALTER TABLE `request_file` DROP COLUMN `status`;
ALTER TABLE `request_file` DROP COLUMN `status_detail`;

DELETE FROM `status_description` WHERE `code` < 200;
INSERT INTO `status_description` (`code`, `name`) VALUES
  (110,'Загружено'), (111,'Ошибка загрузки'), (112,'Загружается'),
  (120,'Связано'), (121,'Ошибка связывания'), (122,'Связывается'),
  (130,'Обработано'), (131,'Ошибка обработки'), (132,'Обрабатывается'),
  (140,'Выгружено'), (141,'Ошибка выгрузки'), (142,'Выгружается');