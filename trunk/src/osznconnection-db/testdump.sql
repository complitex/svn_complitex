
-- Rooms
insert into room(object_id, parent_id, parent_entity_id) values (1,1,100), (2,1,100), (3,2,100), (4,2,100);
insert into room_string_culture(id, locale, value) values (1, 'ru', UPPER('1а')), (1, 'en', UPPER('1a')), (2, 'ru', UPPER('1б')), (2, 'en', UPPER('1b')),
(3, 'ru', UPPER('2а')), (3, 'en', UPPER('2a')), (4, 'ru', UPPER('2б')), (4, 'en', UPPER('2b'));
insert into room_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,200,1,200), (1,2,200,2,200), (1,3,200,3,200), (1,4,200,4,200);
update sequence set sequence_value = 5 where sequence_name = 'room';
update sequence set sequence_value = 5 where sequence_name = 'room_string_culture';

-- Apartments
insert into apartment(object_id, parent_id, parent_entity_id) values (1,1,500), (2,1,500);
insert into apartment_string_culture(id, locale, value) values (1, 'ru', UPPER('10')), (1, 'en', UPPER('10')), (2, 'ru', UPPER('20')), (2, 'en', UPPER('20'));
insert into apartment_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,100,1,100), (1,2,100,2,100);
update sequence set sequence_value = 3 where sequence_name = 'apartment';
update sequence set sequence_value = 3 where sequence_name = 'apartment_string_culture';

-- Buildings
insert into building(object_id, parent_id, parent_entity_id) values (1,1,400), (2,1,400), (3,1,400), (4,2,400), (5,2,400);
insert into building_string_culture(id, locale, value) values (1, 'ru', UPPER('10')), (2, 'ru', UPPER('20')), (3,'ru',UPPER('11')), (4,'ru',UPPER('12')), (5,'ru',UPPER('21')), (6,'ru',UPPER('100')), (7,'ru', null), (8,'ru', null), (9,'ru', null), (10,'ru', null), (11,'ru', null), (12,'ru', null), (13,'ru', null), (14,'ru', null), (15,'ru', null), (16,'ru', null), (17,'ru', null), (18,'ru', null),
                                                              (1, 'en', UPPER('10')), (2, 'en', UPPER('20')), (3,'en',UPPER('11')), (4,'en',UPPER('12')), (5,'en',UPPER('21')), (6,'en',UPPER('100')), (7,'en', null), (8,'en', null), (9,'en', null), (10,'en', null), (11,'en', null), (12,'en', null), (13,'en', null), (14,'en', null), (15,'en', null), (16,'en', null), (17,'en', null), (18,'en', null);
insert into building_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,500,1,500),
(1,1,501,7,501),
(1,1,502,8,502),
(1,1,503,1,503),
(1,1,504,2,504),
(2,1,500,6,500),
(2,1,501,9,501),
(2,1,502,10,502),
(2,1,503,3,503),
(1,2,500,2,500),
(1,2,501,11,501),
(1,2,502,12,502),
(1,2,503,1,503),
(1,2,504,2,504),
(1,3,500,3,500),
(1,3,501,13,501),
(1,3,502,14,502),
(1,3,503,1,503),
(1,3,504,2,504),
(1,4,500,4,500),
(1,4,501,15,501),
(1,4,502,16,502),
(1,4,503,2,503),
(1,4,504,1,504),
(1,5,500,5,500),
(1,5,501,17,501),
(1,5,502,18,502),
(1,5,503,2,503),
(1,5,504,1,504);
update sequence set sequence_value = 19 where sequence_name = 'building_string_culture';
update sequence set sequence_value = 6 where sequence_name = 'building';

-- Streets
insert into street_string_culture(id, locale, value) values (1, 'ru', UPPER('Терешковой')), (1,'en',UPPER('Tereshkovoy')),
                                                            (2, 'ru', UPPER('Ленина')), (2,'en',UPPER('Lenina')),
                                                            (3, 'ru', UPPER('Морской')), (3,'en', UPPER('Morskoy'));
insert into street(object_id, parent_id, parent_entity_id) values (1,1,400), (2,2,400), (3,1,400);
insert into street_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,300,1,300),
(1,2,300,2,300),
(1,3,300,3,300);
update sequence set sequence_value = 4 where sequence_name = 'street_string_culture';
update sequence set sequence_value = 4 where sequence_name = 'street';

-- Districts
insert into district_string_culture(id, locale, value) values (1, 'ru', UPPER('Ленинский')), (1, 'en',UPPER('Leninsky')),
                                                              (2, 'ru', UPPER('Советский')), (2, 'en', UPPER('Sovetsky'));
insert into district(object_id, parent_id, parent_entity_id) values (1,1,400), (2,1,400);
insert into district_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,600,1,600),
(1,2,600,2,600);
update sequence set sequence_value = 3 where sequence_name = 'district_string_culture';
update sequence set sequence_value = 3 where sequence_name = 'district';

-- Cities
insert into city_string_culture(id, locale, value) values (1, 'ru', UPPER('Новосибирск')), (1,'en',UPPER('Novosibirsk')),
                                                          (2, 'ru', UPPER('Москва')), (2,'en',UPPER('Moscow'));
insert into city(object_id, parent_id, parent_entity_id) values (1,1,700), (2,2,700);
insert into city_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,400,1,400),
(1,2,400,2,400);
update sequence set sequence_value = 3 where sequence_name = 'city_string_culture';
update sequence set sequence_value = 3 where sequence_name = 'city';

-- Regions
insert into region_string_culture(id, locale, value) values (1, 'ru', UPPER('Новосибирская обл.')), (1,'en',UPPER('Novosibirsk''s region')),
                                                            (2, 'ru', UPPER('Московская обл.')), (2,'en',UPPER('Moscow''s region'));
insert into region(object_id, parent_id, parent_entity_id) values (1,1,800), (2,1,800);
insert into region_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,700,1,700),
(1,2,700,2,700);
update sequence set sequence_value = 3 where sequence_name = 'region_string_culture';
update sequence set sequence_value = 3 where sequence_name = 'region';

-- Countries
insert into country_string_culture(id, locale, value) values (1, 'ru', UPPER('Россия')), (1,'en',UPPER('Russia')),
                                                            (2, 'ru', UPPER('Украина')), (2,'en',UPPER('Ukraine'));
insert into country(object_id) values (1), (2);
insert into country_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,800,1,800),
(1,2,800,2,800);
update sequence set sequence_value = 3 where sequence_name = 'country_string_culture';
update sequence set sequence_value = 3 where sequence_name = 'country';

-- Users
insert into user value (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', null);
insert into usergroup value (1, 'admin', 'ADMINISTRATORS');
insert into user value (2, 'ANONYMOUS', 'ANONYMOUS', null);

-- Organizations
insert into organization(object_id, parent_id, parent_entity_id, entity_type_id) values (1,null,null,900), (2,1,900,901);
insert into organization_string_culture(id, locale, value) values (1, 'ru', UPPER('ОСЗН 1')), (1,'en',UPPER('ОСЗН 1')),
                                                            (2, 'ru', UPPER('LE')), (3, 'ru', UPPER('1234')), (4, 'ru', UPPER('ПУ 1')), (4,'en',UPPER('ПУ 1')), (5, 'ru', UPPER('3456'));
insert into organization_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,900,1,900), (1,1,901,2,901), (1,1,902,3,902),
(1,2,900,4,900), (1,2,902,5,902);

update sequence set sequence_value = 6 where sequence_name = 'organization_string_culture';
update sequence set sequence_value = 3 where sequence_name = 'organization';

-- Files
insert into `request_file`(id, organization_object_id, `name`, `date`) values (1,1,'A_123405.xml', CURRENT_TIMESTAMP),
                                (2,1,'AF123405.xml', CURRENT_TIMESTAMP);

-- Request payments
insert into request_payment(own_num_sr, f_nam, m_nam, sur_nam, n_name, vul_name, bld_num, flat, city_id, street_id, building_id, apartment_id, request_file_id)
values (1,'Иван', 'Иванович', 'Иванов', 'Новосибирск', 'ул. Терешковой', 'д. 10', 'кв. 10', 1,null,null,null,1);

-- Requset benefit
insert into request_benefit(own_num_sr, f_nam, m_nam, sur_nam, request_file_id)
values (1,'Иван1', 'Иванович1', 'Иванов1',2), (1,'Иван2', 'Иванович2', 'Иванов2',2);

