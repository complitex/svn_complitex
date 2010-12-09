-- Script adds request warnings structure.

DROP TABLE IF EXISTS `request_warning`;

CREATE TABLE `request_warning` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `request_id` BIGINT(20) NOT NULL,
    `request_file_type` VARCHAR(50) NOT NULL,
    `status` BIGINT(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `request_warning_parameter`;

CREATE TABLE `request_warning_parameter` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `request_warning_id` BIGINT(20) NOT NULL,
    `order` INTEGER NOT NULL,
    `type` VARCHAR(100) NULL,
    `value` VARCHAR(500) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_warning_parameter` (`request_warning_id`, `order`),
    CONSTRAINT `fk_request_warning_parameter__request_warning` FOREIGN KEY (`request_warning_id`) REFERENCES `request_warning` (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

INSERT INTO `status_description`(`code`, `name`) VALUES (300, 'Тариф не найден'), (301, 'Объект формы собственности не найден в таблице коррекций для ц.н.'),
(302, 'Код формы собственности не найден в таблице коррекций для ОСЗН'), (303, 'Не числовой код формы собственности в коррекции для ОСЗН'),
(304, 'Объект льготы не найден в таблице коррекций для ц.н.'), (305, 'Код льготы не найден в таблице коррекций для ОСЗН'),
(306, 'Не числовой код льготы в коррекции для ОСЗН'), (307, 'Не числовой порядок льготы');
UPDATE `status_description` SET `name` = 'Код тарифа на оплату жилья не найден в справочнике тарифов' WHERE `code` = 216;

DELIMITER /
CREATE PROCEDURE `transform_tarif_warning`()
BEGIN
    DECLARE l_payment_id BIGINT(20);
    DECLARE l_calc_center_code2_1 DOUBLE;
    DECLARE l_calc_center_id BIGINT(20);
    DECLARE l_warning_id BIGINT(20);
    DECLARE done INT;
    DECLARE payment_cursor CURSOR FOR SELECT `id`, `calc_center_code2_1` FROM `payment` WHERE `calc_center_code2_1` IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SET done = 0;
    SELECT `calculation_center_id` INTO l_calc_center_id FROM `calculation_center_preference`;
    OPEN payment_cursor;
    payment_loop: LOOP
        FETCH payment_cursor INTO l_payment_id, l_calc_center_code2_1;
        IF done = 1 THEN
            LEAVE payment_loop;
        END IF;
	
        INSERT INTO `request_warning` (`request_id`, `request_file_type`, `status`) VALUES (l_payment_id, 'PAYMENT', 300);
        SELECT LAST_INSERT_ID() INTO l_warning_id FROM DUAL;
        INSERT INTO `request_warning_parameter` (`request_warning_id`, `order`, `type`, `value`) VALUES (l_warning_id, 0, NULL, FORMAT(l_calc_center_code2_1, 3));
        INSERT INTO `request_warning_parameter` (`request_warning_id`, `order`, `type`, `value`) VALUES (l_warning_id, 1, 'organization', FORMAT(l_calc_center_id, 0));
       
    END LOOP payment_loop;
    CLOSE payment_cursor;
    SET done = 0;
END/
DELIMITER ;

CALL `transform_tarif_warning`();

DROP PROCEDURE `transform_tarif_warning`;

ALTER TABLE `payment` DROP COLUMN `calc_center_code2_1`;

INSERT INTO `update` (`version`) VALUE ('20101209_417');

