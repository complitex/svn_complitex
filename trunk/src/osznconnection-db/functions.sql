DELIMITER /

DROP FUNCTION IF EXISTS `TO_CYRILLIC`/

CREATE FUNCTION `TO_CYRILLIC`(str VARCHAR(1000)) RETURNS VARCHAR(1000)
    NO SQL
    BEGIN
    	DECLARE result VARCHAR(1000);
	DECLARE i INT;
	DECLARE curr VARCHAR(1);
	DECLARE rpl VARCHAR(1);

	IF str IS NULL THEN
		RETURN NULL;
	END IF;

	SET result = '';

	SET i = 1;
	WHILE i <= CHAR_LENGTH(str) DO
		SET curr = SUBSTRING(str, i, 1);

		CASE curr
			 WHEN 'a' THEN SET rpl = 'а';
			 WHEN 'A' THEN SET rpl = 'А';
			 WHEN 'T' THEN SET rpl = 'Т';
			 WHEN 'x' THEN SET rpl = 'х';
			 WHEN 'X' THEN SET rpl = 'Х';
			 WHEN 'k' THEN SET rpl = 'к';
			 WHEN 'K' THEN SET rpl = 'К';
			 WHEN 'M' THEN SET rpl = 'М';
			 WHEN 'e' THEN SET rpl = 'е';
			 WHEN 'E' THEN SET rpl = 'Е';
                         WHEN 'o' THEN SET rpl = 'о';
                         WHEN 'O' THEN SET rpl = 'О';
                         WHEN 'p' THEN SET rpl = 'р';
                         WHEN 'P' THEN SET rpl = 'Р';
                         WHEN 'c' THEN SET rpl = 'с';
                         WHEN 'C' THEN SET rpl = 'С';
                         WHEN 'B' THEN SET rpl = 'В';
			 ELSE SET rpl = curr;
		END CASE;

		SET result = CONCAT(result,rpl);
		SET i = i+1;
	END WHILE;
	RETURN result;
    END/

    DELIMITER ;