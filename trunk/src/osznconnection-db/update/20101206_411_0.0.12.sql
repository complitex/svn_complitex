-- Script adds INVALID_FORMAT request status.

UPDATE `status_description` SET `name` = 'Неверный формат данных' WHERE `code` = 219;

INSERT INTO `update` (`version`) VALUE ('20101206_411');

