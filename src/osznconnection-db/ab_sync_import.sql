DELIMITER $$

DROP PROCEDURE IF EXISTS `osznconnection`.`ad_import` $$
CREATE PROCEDURE `osznconnection`.`ad_import` ()
BEGIN

DECLARE locale0 VARCHAR(2) DEFAULT 'ru';
DECLARE locale1 VARCHAR(2) DEFAULT 'en';

DECLARE done, _index INT DEFAULT 0;

DECLARE record_date0 DATETIME;
DECLARE old_value0, current_value0 VARCHAR(50);
DECLARE object_type0, object_id0, field0, action_type0 DOUBLE;

DECLARE seqObjectId, tmpObjectId, seqValueId bigint(20);

DECLARE cur_ab_sync_changes CURSOR FOR
  SELECT `record_date`, `old_value`, `current_value`, `object_type`, `object_id`, `field`, `action_type`
  FROM `ab_sync_changes_tbl`;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

OPEN cur_ab_sync_changes;

-- remove empty changes
-- DELETE FROM `ab_sync_changes_tbl` where `action_type` = 1 and `old_value` = `current_value`;

SET autocommit = 0;

REPEAT
  FETCH cur_ab_sync_changes INTO record_date0, old_value0, current_value0, object_type0, object_id0, field0, action_type0;
  IF NOT done THEN

    SET _index = _index + 1;

    IF (_index % 1000 = 0) THEN
      select _index;
      COMMIT;
    END IF;

    CASE object_type0
      -- ------------------------------------------------------
      -- DISTRICT
      -- ------------------------------------------------------
      WHEN 1 THEN

        -- action type: {create: 0, change: 1, delete 2}
        IF (action_type0 = 0 and (select count(*) = 0 from `district` where `import_object_id` = object_id0)) THEN
          -- get sequence domain object id
          SET seqObjectId = (select sequence_value from sequence where sequence_name = 'district');
          update sequence set sequence_value = sequence_value+1 where sequence_name = 'district';

          -- save domain object
          insert into `district`
            (`status`, `object_id`, `import_object_id`, `start_date`, `parent_id`, `parent_entity_id`, `entity_type_id`)
          value
            ('ACTIVE', seqObjectId, object_id0, now(), 3, 400, null);

          -- district field
          IF (field0 = 0) THEN
            -- get sequence string culture id
            SET seqValueId = (select sequence_value from sequence where sequence_name = 'district_string_culture');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'district_string_culture';

            -- save value to string culture
            insert into `district_string_culture`
              (`id`, `locale`, `value`)
            value
              (seqValueId, locale0, current_value0);

            -- save attribute
            insert into `district_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 600, seqValueId, 600, now(), 'ACTIVE');
          END IF;

        -- update field
        ELSEIF (action_type0 = 1 and field0 = 0) THEN
          
          update `district_string_culture` sc set sc.`value` = current_value0
          where sc.`locale` = locale0 and sc.`id` = (select da.`value_id` from `district_attribute` da
            where da.`object_id` = (select d.`object_id` from `district` d where d.`import_object_id` = object_id0 limit 1));

        -- delete domain object cascade
        ELSEIF (action_type0 = 2) THEN

          delete sc from `district` d left join `district_attribute` da on d.`object_id` = da.`object_id` 
            left join `district_string_culture` sc on sc.`id` = da.`value_id`
             where d.`import_object_id` = object_id0;

          delete da from `district` d left join `district_attribute` da on d.`object_id` = da.`object_id`
             where d.`import_object_id` = object_id0;

          delete d from `district` d where d.`import_object_id` = object_id0;

        END IF;

      -- ------------------------------------------------------
      -- STREET TYPE
      -- ------------------------------------------------------
      WHEN 4 THEN
        -- create
        IF (action_type0 = 0 and (select count(*) = 0 from `entity_type` where `import_object_id` = object_id0)) THEN
          SET seqValueId = (select sequence_value from sequence where sequence_name = 'string_culture');
          update sequence set sequence_value = sequence_value+1 where sequence_name = 'string_culture';

          insert into `string_culture` (`id`, `locale`, `value`) value (seqValueId, locale0, current_value0);

          insert into `entity_type`
            (`entity_id`, `entity_type_name_id`, `start_date`, `import_object_id`)
          value
            (300, seqValueId, now(), object_id0);

          -- change
          ELSEIF (action_type0 = 1 and field0 = 1) THEN
            update `string_culture` sc set sc.`value` = current_value0
              where sc.`locale` = locale0 and sc.`id` = (select et.`entity_type_name_id` from `entity_type` et
                where et.`import_object_id` = object_id0 limit 1);

          -- delete
          ELSEIF (action_type0 = 2) THEN
            delete sc from `entity_type` et left join `string_culture` sc on et.`entity_type_name_id` = sc.`id`
               where et.`import_object_id` = object_id0;

            delete from `entity_type` where et.`import_object_id` = object_id0;
        END IF;

      -- ------------------------------------------------------
      -- STREET
      -- ------------------------------------------------------
      WHEN 2 THEN
            
        IF (action_type0 = 0) THEN
          -- create domain object if not exit
          IF (select count(*) = 0 from `street` where `import_object_id` = object_id0) THEN
            -- get sequence domain object id
            SET seqObjectId = (select sequence_value from sequence where sequence_name = 'street');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'street';

            -- save domain object
            insert into `street`
              (`status`, `object_id`, `import_object_id`, `start_date`, `parent_id`, `parent_entity_id`, `entity_type_id`)
            value
              ('ACTIVE', seqObjectId, object_id0, now(), 3, 400, null);
          END IF;

          -- street type field
          IF (field0 = 9) THEN
            update
              `street`
            set
              `entity_type_id` = (select et.`id` from `entity_type` et where et.`import_object_id` = current_value0)
            where
              `import_object_id` = object_id0;

          ELSEIF (field0 = 2) THEN
            -- get sequence string culture id
            SET seqValueId = (select sequence_value from sequence where sequence_name = 'street_string_culture');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'street_string_culture';

            -- save value to string culture
            insert into `street_string_culture`
              (`id`, `locale`, `value`)
            value
              (seqValueId, locale0, current_value0);

            -- save attribute
            SET seqObjectId = (select `object_id` from `street` where `import_object_id` = object_id0);
            
            insert into `street_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 300, seqValueId, 300, now(), 'ACTIVE');
          END IF;

        -- update field
        ELSEIF (action_type0 = 1) THEN
          -- Street Type
          IF (field0 = 9) THEN
           update
              `street`
            set
              `entity_type_id` = (select et.`id` from `entity_type` et where et.`import_object_id` = current_value0)
            where
              `import_object_id` = object_id0;
          -- Street
          ELSEIF (field0 = 2) THEN
            update
              `street` o left join `street_attribute` a on o.`object_id` = a.`object_id`
                left join `street_string_culture` sc on sc.`id` = a.`value_id`
            set
              sc.`value` = current_value0
            where
              o.`import_object_id` = object_id0 and a.`attribute_type_id` = 300;

          END IF;

        -- delete domain object cascade
        ELSEIF (action_type0 = 2) THEN

          delete sc from `street` o left join `street_attribute` a on o.`object_id` = a.`object_id`
            left join `street_string_culture` sc on sc.`id` = a.`value_id`
             where o.`import_object_id` = object_id0;

          delete a from `street` o left join `street_attribute` a on o.`object_id` = a.`object_id`
             where o.`import_object_id` = object_id0;

          delete o from `street` o where o.`object_id` = object_id0;

        END IF;

      -- ------------------------------------------------------
      -- BUILDING
      -- ------------------------------------------------------
      WHEN 3 THEN
        -- create
        IF (action_type0 = 0) THEN
          -- create domain object if not exit
          IF (select count(*) = 0 from `building` where `import_object_id` = object_id0) THEN
            -- get sequence domain object id
            SET seqObjectId = (select sequence_value from sequence where sequence_name = 'building');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'building';

            -- save domain object
            insert into `building`
              (`status`, `object_id`, `import_object_id`, `start_date`, `parent_id`, `parent_entity_id`, `entity_type_id`)
            value
              ('ACTIVE', seqObjectId, object_id0, now(), 3, 400, null);

            -- insert empty structure
            SET seqValueId = (select sequence_value from sequence where sequence_name = 'building_string_culture');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'building_string_culture';

            -- save value to string culture
            insert into `building_string_culture`
              (`id`, `locale`, `value`)
            value
              (seqValueId, locale0, null);

            -- save attribute
            insert into `building_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 502, seqValueId, 502, now(), 'ACTIVE');
          END IF;

          -- HouseNum
          IF (field0 = 3) THEN
            -- get sequence string culture id
            SET seqValueId = (select sequence_value from sequence where sequence_name = 'building_string_culture');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'building_string_culture';

            -- save value to string culture
            insert into `building_string_culture`
              (`id`, `locale`, `value`)
            value
              (seqValueId, locale0, current_value0);

            -- save attribute
            SET seqObjectId = (select `object_id` from `building` where `import_object_id` = object_id0);

            insert into `building_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 500, seqValueId, 500, now(), 'ACTIVE');

          -- PartNum
          ELSEIF (field0 = 4) THEN
            -- get sequence string culture id
            SET seqValueId = (select sequence_value from sequence where sequence_name = 'building_string_culture');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'building_string_culture';

            -- save value to string culture
            insert into `building_string_culture`
              (`id`, `locale`, `value`)
            value
              (seqValueId, locale0, current_value0);

            -- save attribute
            SET seqObjectId = (select `object_id` from `building` where `import_object_id` = object_id0);

            insert into `building_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 501, seqValueId, 501, now(), 'ACTIVE');

          -- DistrictID
          ELSEIF (field0 = 6) THEN
            SET tmpObjectId = (select `object_id` from `district` where `import_object_id` = current_value0);

            insert into `building_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 504, tmpObjectId, 504, now(), 'ACTIVE');

          -- StreetID
          ELSEIF (field0 = 7) THEN
            SET tmpObjectId = (select `object_id` from `street` where `import_object_id` = current_value0);

            insert into `building_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 503, tmpObjectId, 503, now(), 'ACTIVE');

          END IF; -- field

        -- update field
        ELSEIF (action_type0 = 1) THEN
          -- HouseNum
          IF (field0 = 3) THEN
            update
              `building` o left join `building_attribute` a on o.`object_id` = a.`object_id`
                left join `building_string_culture` sc on sc.`id` = a.`value_id`
            set
              sc.`value` = current_value0
            where
              o.`import_object_id` = object_id0 and a.`attribute_type_id` = 500;

          -- PartNum
          ELSEIF (field0 = 4) THEN
            update
              `building` o left join `building_attribute` a on o.`object_id` = a.`object_id`
                left join `building_string_culture` sc on sc.`id` = a.`value_id`
            set
              sc.`value` = current_value0
            where
              o.`import_object_id` = object_id0 and a.`attribute_type_id` = 501;

          -- DistrictID
          ELSEIF (field0 = 6) THEN
            SET tmpObjectId = (select `object_id` from `district` 
              where `import_object_id` = IF(CHAR_LENGTH(current_value0) = 0, null, current_value0));

            update
              `building` o left join `building_attribute` a on o.`object_id` = a.`object_id`
            set
              a.`value_id` = tmpObjectId
            where
              o.`import_object_id` = object_id0 and a.`attribute_type_id` = 504;

          -- StreetID
          ELSEIF (field0 = 7) THEN
            SET tmpObjectId = (select `object_id` from `street`
              where `import_object_id` = IF(CHAR_LENGTH(current_value0) = 0, null, current_value0));

            update
              `building` o left join `building_attribute` a on o.`object_id` = a.`object_id`
            set
              a.`value_id` = tmpObjectId
            where
              o.`import_object_id` = object_id0 and a.`attribute_type_id` = 503;

          END IF; -- field

        -- delete domain object cascade
        ELSEIF (action_type0 = 2) THEN

          delete sc from `building` o left join `building_attribute` a on o.`object_id` = a.`object_id`
            left join `building_string_culture` sc on sc.`id` = a.`value_id`
             where o.`import_object_id` = object_id0;

          delete a from `building` o left join `building_attribute` a on o.`object_id` = a.`object_id`
             where o.`import_object_id` = object_id0;

          delete o from `building` o where o.`object_id` = object_id0;

        END IF; -- action

      -- ------------------------------------------------------
      -- APARTMENT
      -- ------------------------------------------------------
      WHEN 5 THEN
        -- create
        IF (action_type0 = 0) THEN
          -- create domain object if not exit
          IF (select count(*) = 0 from `apartment` where `import_object_id` = object_id0) THEN
            -- get sequence domain object id
            SET seqObjectId = (select sequence_value from sequence where sequence_name = 'apartment');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'apartment';

            -- save domain object
            insert into `apartment`
              (`status`, `object_id`, `import_object_id`, `start_date`, `parent_id`, `parent_entity_id`, `entity_type_id`)
            value
              ('ACTIVE', seqObjectId, object_id0, now(), -1, 500, null);
          END IF;

          -- Apartment
          IF (field0 = 5) THEN
            -- get sequence string culture id
            SET seqValueId = (select sequence_value from sequence where sequence_name = 'apartment_string_culture');
            update sequence set sequence_value = sequence_value+1 where sequence_name = 'apartment_string_culture';

            -- save value to string culture
            insert into `apartment_string_culture`
              (`id`, `locale`, `value`)
            value
              (seqValueId, locale0, current_value0);

            -- save attribute
            SET seqObjectId = (select `object_id` from `apartment` where `import_object_id` = object_id0);

            insert into `apartment_attribute`
              (`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`, `start_date`, `status`)
            value
              (1, seqObjectId, 100, seqValueId, 100, now(), 'ACTIVE');

          -- BuildingID
          ELSEIF (field0 = 8) THEN
              SET tmpObjectId = (select `object_id` from `building`
                where `import_object_id` =  IF(CHAR_LENGTH(current_value0) = 0, null, current_value0));

              update `apartment` set `parent_id` = tmpObjectId where `import_object_id` = object_id0;

          END IF; -- field

        -- update field
        ELSEIF (action_type0 = 1) THEN
          -- Apartment
          IF (field0 = 5) THEN
            update
              `apartment` o left join `apartment_attribute` a on o.`object_id` = a.`object_id`
                left join `apartment_string_culture` sc on sc.`id` = a.`value_id`
            set
              sc.`value` = current_value0
            where
              o.`import_object_id` = object_id0 and a.`attribute_type_id` = 100;

          -- BuildingID
          ELSEIF (field0 = 8) THEN
            SET tmpObjectId = (select `object_id` from `building`
              where `import_object_id` = IF(CHAR_LENGTH(current_value0) = 0, null, current_value0));          

            update `apartment` set `parent_id` = tmpObjectId where `import_object_id` = object_id0;

          END IF; -- field

        -- delete domain object cascade
        ELSEIF (action_type0 = 2) THEN

          delete sc from `apartment` o left join `apartment_attribute` a on o.`object_id` = a.`object_id`
            left join `apartment_string_culture` sc on sc.`id` = a.`value_id`
             where o.`import_object_id` = object_id0;

          delete a from `apartment` o left join `apartment_attribute` a on o.`object_id` = a.`object_id`
             where o.`import_object_id` = object_id0;

          delete o from `apartment` o where o.`object_id` = object_id0;

        END IF; -- action
    END CASE;

  END IF;

UNTIL done END REPEAT;

select _index;
COMMIT;

CLOSE cur_ab_sync_changes;

END $$

DELIMITER ;