-- Script renames WRONG_ACCOUNT_NUMBER status description

UPDATE `status_description` SET `name` = 'Не сопоставлен носитель льготы' WHERE `code` = 217;

INSERT INTO `update` (`version`) VALUE ('20101210_429');

