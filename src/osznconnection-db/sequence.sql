-- sequence update

update sequence set sequence_value = (select max(`object_id`) from `room`)+1 where sequence_name = 'room';
update sequence set sequence_value = (select max(`id`) from `room_string_culture`)+1 where sequence_name = 'room_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `apartment`)+1 where sequence_name = 'apartment';
update sequence set sequence_value = (select max(`id`) from `apartment_string_culture`)+1 where sequence_name = 'apartment_string_culture';
update sequence set sequence_value = (select max(`id`) from `building_string_culture`)+1 where sequence_name = 'building_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `building`)+1 where sequence_name = 'building';
update sequence set sequence_value = (select max(`id`) from `street_string_culture`)+1 where sequence_name = 'street_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `street`)+1 where sequence_name = 'street';
update sequence set sequence_value = (select max(`id`) from `district_string_culture`)+1 where sequence_name = 'district_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `district`)+1 where sequence_name = 'district';
update sequence set sequence_value = (select max(`id`) from `city_string_culture`)+1 where sequence_name = 'city_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `city`)+1 where sequence_name = 'city';
update sequence set sequence_value = (select max(`id`) from `region_string_culture`)+1 where sequence_name = 'region_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `region`)+1 where sequence_name = 'region';
update sequence set sequence_value = (select max(`id`) from `country_string_culture`)+1 where sequence_name = 'country_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `country`)+1 where sequence_name = 'country';
update sequence set sequence_value = (select max(`id`) from `organization_string_culture`)+1 where sequence_name = 'organization_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `organization`)+1 where sequence_name = 'organization';
update sequence set sequence_value = (select max(`id`) from `ownership_string_culture`)+1 where sequence_name = 'ownership_string_culture';
update sequence set sequence_value = (select max(`object_id`) from `ownership`)+1 where sequence_name = 'ownership';