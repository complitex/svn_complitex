-- Set mysql user-defined variable - system locale id.
SELECT (@system_locale_id := `id`) FROM `locales` WHERE `system` = 1;

-- City Types
INSERT INTO `city_type_string_culture`(`id`, `locale_id`, `value`) VALUES (10000,1,'ГОРОД'), (10000,2,'МIСТО'), (10001,1,'ДЕРЕВНЯ'), (10001,2,'СЕЛО');
INSERT INTO `city_type` (`object_id`) VALUES (10000), (10001);
INSERT INTO `city_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,10000,1300,10000,1300),
(1,10001,1300,10001,1300);
INSERT INTO `city_type_string_culture`(`id`, `locale_id`, `value`) VALUES (10002,1,'ГОРОД'), (10002,2,'МIСТО'), (10003,1,'ДЕРЕВНЯ'), (10003,2,'СЕЛО');
INSERT INTO `city_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
(1,10000,1301,10002,1301),(1,10001,1301,10003,1301);

-- Street Types
INSERT INTO `street_type_string_culture`(`id`, `locale_id`, `value`) VALUES (10000,@system_locale_id,'Б-Р'), (10001,@system_locale_id,'М'), (10002,@system_locale_id,'М-Н'),
(10003,@system_locale_id,'ПЕР'), (10004,@system_locale_id,'ПЛ'), (10005,@system_locale_id,'П'), (10006,@system_locale_id,'ПОС'), (10007,@system_locale_id,'ПР-Д'), (10008,@system_locale_id,'ПРОСП'), (10009,@system_locale_id,'СП'),
(10010,@system_locale_id,'Т'), (10011,@system_locale_id,'ТУП'), (10012,@system_locale_id,'УЛ'), (10013,@system_locale_id,'ШОССЕ'), (10014,@system_locale_id,'НАБ'), (10015,@system_locale_id,'В-Д'), (10016,@system_locale_id,'СТ');
INSERT INTO `street_type` (`object_id`) VALUES (10000), (10001), (10002), (10003), (10004), (10005), (10006), (10007), (10008), (10009), (10010),
(10011), (10012), (10013), (10014), (10015), (10016);
INSERT INTO `street_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,10000,1400,10000,1400),
(1,10001,1400,10001,1400), (1,10002,1400,10002,1400), (1,10003,1400,10003,1400), (1,10004,1400,10004,1400), (1,10005,1400,10005,1400),
(1,10006,1400,10006,1400), (1,10007,1400,10007,1400), (1,10008,1400,10008,1400), (1,10009,1400,10009,1400), (1,10010,1400,10010,1400),
(1,10011,1400,10011,1400), (1,10012,1400,10012,1400), (1,10013,1400,10013,1400), (1,10014,1400,10014,1400), (1,10015,1400,10015,1400),
(1,10016,1400,10016,1400);
INSERT INTO `street_type_string_culture`(`id`, `locale_id`, `value`) VALUES (10017,@system_locale_id,'Б-Р'), (10018,@system_locale_id,'М'), (10019,@system_locale_id,'М-Н'),
(10020,@system_locale_id,'ПЕР'), (10021,@system_locale_id,'ПЛ'), (10022,@system_locale_id,'П'), (10023,@system_locale_id,'ПОС'), (10024,@system_locale_id,'ПР-Д'), (10025,@system_locale_id,'ПРОСП'), (10026,@system_locale_id,'СП'),
(10027,@system_locale_id,'Т'), (10028,@system_locale_id,'ТУП'), (10029,@system_locale_id,'УЛ'), (10030,@system_locale_id,'ШОССЕ'), (10031,@system_locale_id,'НАБ'), (10032,@system_locale_id,'В-Д'), (10033,@system_locale_id,'СТ');
INSERT INTO `street_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
(1,10000,1401,10017,1401), (1,10001,1401,10018,1401), (1,10002,1401,10019,1401), (1,10003,1401,10020,1401), (1,10004,1401,10021,1401),
(1,10005,1401,10022,1401), (1,10006,1401,10023,1401), (1,10007,1401,10024,1401), (1,10008,1401,10025,1401), (1,10009,1401,10026,1401),
(1,10010,1401,10027,1401), (1,10011,1401,10028,1401), (1,10012,1401,10029,1401), (1,10013,1401,10030,1401), (1,10014,1401,10031,1401),
(1,10015,1401,10032,1401), (1,10016,1401,10033,1401);

-- Rooms
insert into room(object_id, parent_id, parent_entity_id) values (1,1,100), (2,1,100), (3,2,100), (4,2,100);
insert into room_string_culture(id, locale_id, value) values (1,@system_locale_id, UPPER('1а')),(2,@system_locale_id, UPPER('1б')), (3,@system_locale_id, UPPER('2а')), (4,@system_locale_id, UPPER('2б'));
insert into room_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,200,1,200), (1,2,200,2,200), (1,3,200,3,200), (1,4,200,4,200);

-- Apartments
insert into apartment(object_id, parent_id, parent_entity_id) values (1,1,500), (2,1,500), (3,3,500), (4,2,500);
insert into apartment_string_culture(id, locale_id, value) values (1,@system_locale_id, UPPER('10')), (2,@system_locale_id, UPPER('20')), (3,@system_locale_id,UPPER('18')), (4,1,UPPER('28'));
insert into apartment_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,100,1,100), (1,2,100,2,100), (1,3,100,3,100),(1,4,100,4,100);

-- Building Addresses
insert into building_address(object_id, parent_id, parent_entity_id) values (1,1,300), (2,3,300), (3,1,300), (4,1,300), (5,2,300), (6,2,300),
(7,4,300), (8,5,300),
(9,6,300);
insert into building_address_string_culture(id, locale_id, value) values
(1, @system_locale_id, UPPER('8')), (2, @system_locale_id, UPPER('28')), (3,@system_locale_id,UPPER('18')), (4,@system_locale_id,UPPER('12')), (5,@system_locale_id,UPPER('21')), (6,@system_locale_id,UPPER('100')),
(7,@system_locale_id,UPPER('154А')), (8,@system_locale_id,UPPER('25А')),
(9,@system_locale_id,UPPER('26А'));
-- (10,@system_locale_id,UPPER('к1')), (10,@system_locale_id,UPPER('к1'));
insert into building_address_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,1500,1,1500),
(1,2,1500,6,1500),
(1,3,1500,2,1500),
(1,4,1500,3,1500),
(1,5,1500,4,1500),
(1,6,1500,5,1500),
(1,7,1500,7,1500),
(1,8,1500,8,1500),
(1,9,1500,9,1500);
--(1,9,1501,10,1501);

-- Buildings
insert into building(object_id, parent_id, parent_entity_id) values (1,1,1500), (2,3,1500), (3,4,1500), (4,5,1500), (5,6,1500), (6,7,1500), (7,8,1500),
(8,9,1500);
insert into building_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,500,2,500),(1,1,501,2,501),
(1,3,500,2,500),
(1,4,500,1,500),
(1,5,500,1,500),
(1,6,500,3,500),
(1,7,500,3,500),
(1,8,500,3,500);

-- Streets
insert into street_string_culture(id, locale_id, value) values (1,@system_locale_id, UPPER('Терешковой')),
                                                            (2,@system_locale_id, UPPER('Ленина')),
                                                            (3,@system_locale_id, UPPER('Морской')),
                                                            (4,@system_locale_id, UPPER('КОСИОРА')),
                                                            (5,@system_locale_id, UPPER('ФРАНТИШЕКА КРАЛА')),
                                                            (6,@system_locale_id, UPPER('ФРАНТИШЕКА КРАЛА1'));
insert into street(object_id, parent_id, parent_entity_id) values (1,1,400), (2,2,400), (3,1,400), (4,3,400), (5,3,400),
(6,3,400);
insert into street_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,300,1,300),(1,1,301,10012,301),
(1,2,300,2,300),(1,2,301,10012,301),
(1,3,300,3,300),(1,3,301,10008,301),
(1,4,300,4,300),(1,4,301,10012,301),
(1,5,300,5,300),(1,5,301,10012,301),
(1,6,300,6,300),(1,6,301,10008,301);

-- Districts
insert into district_string_culture(id, locale_id, value) values (1,@system_locale_id, UPPER('Ленинский')),
                                                              (2,@system_locale_id, UPPER('Советский')),
                                                              (3,@system_locale_id, UPPER('Центральный')),
                                                              (4,@system_locale_id, UPPER('LE')), (5,@system_locale_id, UPPER('SO')), (6,@system_locale_id, UPPER('CE')),
                                                              (7,@system_locale_id, UPPER('Другой район')), (8,@system_locale_id,UPPER('DR'));
insert into district(object_id, parent_id, parent_entity_id) values (1,2,400), (2,1,400), (3,3,400), (4,3,400);
insert into district_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,600,1,600),(1,1,601,4,601),
(1,2,600,2,600),(1,2,601,5,601),
(1,3,600,3,600),(1,3,601,6,601),
(1,4,600,7,600),(1,4,601,8,601);


-- Cities
insert into city_string_culture(id, locale_id, value) values (1,@system_locale_id, UPPER('Новосибирск')),
                                                          (2,@system_locale_id, UPPER('Москва')),
                                                          (3,@system_locale_id, UPPER('Харьков'));
insert into city(object_id, parent_id, parent_entity_id) values (1,1,700), (2,2,700), (3,3,700);
insert into city_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,400,1,400),(1,1,401,10000,401),
(1,2,400,2,400),(1,2,401,10000,401),
(1,3,400,3,400),(1,3,401,10000,401);

-- Regions
insert into region_string_culture(id, locale_id, value) values (1,@system_locale_id, UPPER('Новосибирская обл.')),
                                                            (2,@system_locale_id, UPPER('Московская обл.')),
                                                            (3,@system_locale_id, UPPER('Харьковская обл.(ТЕСТ)'));
insert into region(object_id, parent_id, parent_entity_id) values (1,1,800), (2,1,800), (3,2,800);
insert into region_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,700,1,700),
(1,2,700,2,700),
(1,3,700,3,700);

-- Countries
insert into country_string_culture(id, locale_id, value) values (1,@system_locale_id, UPPER('Россия')),
                                                            (2,@system_locale_id, UPPER('Украина(ТЕСТ)'));
insert into country(object_id) values (1), (2);
insert into country_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,800,1,800),
(1,2,800,2,800);