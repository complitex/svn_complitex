-- City Types
INSERT INTO `city_type_string_culture`(`id`, `locale_id`, `value`) VALUES (10000,1,'ГОРОД'), (10000,2,'МIСТО'), (10001,1,'ДЕРЕВНЯ'), (10001,2,'СЕЛО');
INSERT INTO `city_type` (`object_id`) VALUES (10000), (10001);
INSERT INTO `city_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,10000,1300,10000,1300),
(1,10001,1300,10001,1300);

-- Street Types
INSERT INTO `street_type_string_culture`(`id`, `locale_id`, `value`) VALUES (10000,1,'Б-Р'), (10001,1,'М'), (10002,1,'М-Н'),
(10003,1,'ПЕР'), (10004,1,'ПЛ'), (10005,1,'П'), (10006,1,'ПОС'), (10007,1,'ПР-Д'), (10008,1,'ПРОСП'), (10009,1,'СП'),
(10010,1,'Т'), (10011,1,'ТУП'), (10012,1,'УЛ'), (10013,1,'ШОССЕ'), (10014,1,'НАБ'), (10015,1,'В-Д'), (10016,1,'СТ');

INSERT INTO `street_type` (`object_id`) VALUES (10000), (10001), (10002), (10003), (10004), (10005), (10006), (10007), (10008), (10009), (10010),
(10011), (10012), (10013), (10014), (10015), (10016);
INSERT INTO `street_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,10000,1400,10000,1400),
(1,10001,1400,10001,1400), (1,10002,1400,10002,1400), (1,10003,1400,10003,1400), (1,10004,1400,10004,1400), (1,10005,1400,10005,1400),
(1,10006,1400,10006,1400), (1,10007,1400,10007,1400), (1,10008,1400,10008,1400), (1,10009,1400,10009,1400), (1,10010,1400,10010,1400),
(1,10011,1400,10011,1400), (1,10012,1400,10012,1400), (1,10013,1400,10013,1400), (1,10014,1400,10014,1400), (1,10015,1400,10015,1400)
, (1,10016,1400,10016,1400);


-- Rooms
insert into room(object_id, parent_id, parent_entity_id) values (1,1,100), (2,1,100), (3,2,100), (4,2,100);
insert into room_string_culture(id, locale_id, value) values (1, 1, UPPER('1а')), (1, 2, UPPER('1a')), (2, 1, UPPER('1б')), (2, 2, UPPER('1b')),
(3, 1, UPPER('2а')), (3, 2, UPPER('2a')), (4, 1, UPPER('2б')), (4, 2, UPPER('2b'));
insert into room_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,200,1,200), (1,2,200,2,200), (1,3,200,3,200), (1,4,200,4,200);

-- Apartments
insert into apartment(object_id, parent_id, parent_entity_id) values (1,1,500), (2,1,500), (3,6,500), (4,7,500), (5,7,500);
insert into apartment_string_culture(id, locale_id, value) values (1, 1, UPPER('10')), (1, 2, UPPER('10')), (2, 1, UPPER('20')), (2, 2, UPPER('20')),
                                                                (3, 1, UPPER('1')), (3, 2, UPPER('1')),
                                                                (4, 1, UPPER('40')), (4, 2, UPPER('40')),
                                                                (5, 1, UPPER('19')), (5, 2, UPPER('19'));
insert into apartment_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,100,1,100), (1,2,100,2,100), (1,3,100,3,100), (1,4,100,4,100), (1,5,100,5,100);

-- Building Addresses
insert into building_address(object_id, parent_id, parent_entity_id) values (1,1,300), (2,3,300), (3,1,300), (4,1,300), (5,2,300), (6,2,300),
(7,4,300), (8,5,300);
insert into building_address_string_culture(id, locale_id, value) values
(1, 1, UPPER('8')), (2, 1, UPPER('28')), (3,1,UPPER('18')), (4,1,UPPER('12')), (5,1,UPPER('21')), (6,1,UPPER('100')),
(1, 2, UPPER('8')), (2, 2, UPPER('28')), (3,2,UPPER('18')), (4,2,UPPER('12')), (5,2,UPPER('21')), (6,2,UPPER('100')),
(7,1,UPPER('154А')), (7,2,UPPER('154А')),(8,1,UPPER('25А')), (8,2,UPPER('25А'));
insert into building_address_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,1500,1,1500),
(1,2,1500,6,1500),
(1,3,1500,2,1500),
(1,4,1500,3,1500),
(1,5,1500,4,1500),
(1,6,1500,5,1500),
(1,7,1500,7,1500),
(1,8,1500,8,1500);

-- Buildings
insert into building(object_id, parent_id, parent_entity_id) values (1,1,1500), (2,3,1500), (3,4,1500), (4,5,1500), (5,6,1500), (6,7,1500), (7,8,1500);
insert into building_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,500,2,500),(1,1,501,2,501),
(1,3,500,2,500),
(1,4,500,1,500),
(1,5,500,1,500),
(1,6,500,3,500),
(1,7,500,3,500);

-- Streets
insert into street_string_culture(id, locale_id, value) values (1, 1, UPPER('Терешковой')), (1,2,UPPER('Tereshkovoy')),
                                                            (2, 1, UPPER('Ленина')), (2,2,UPPER('Lenina')),
                                                            (3, 1, UPPER('Морской')), (3,2, UPPER('Morskoy')),
                                                            (4, 1, UPPER('КОСИОРА')), (4,2, UPPER('КОСИОРА')),
                                                            (5, 1, UPPER('ФРАНТИШЕКА КРАЛА')), (5,2, UPPER('ФРАНТИШЕКА КРАЛА'));
insert into street(object_id, parent_id, parent_entity_id) values (1,1,400), (2,2,400), (3,1,400), (4,3,400), (5,3,400);
insert into street_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,300,1,300),(1,1,301,10008,301),
(1,2,300,2,300),(1,2,301,10012,301),
(1,3,300,3,300),(1,3,301,10008,301),
(1,4,300,4,300),(1,4,301,10012,301),
(1,5,300,5,300),(1,5,301,10012,301);

-- Districts
insert into district_string_culture(id, locale_id, value) values (1, 1, UPPER('Ленинский')), (1, 2,UPPER('Leninsky')),
                                                              (2, 1, UPPER('Советский')), (2, 2, UPPER('Sovetsky')),
                                                              (3, 1, UPPER('Центральный')), (3, 2, UPPER('Центральный')),
                                                              (4, 1, UPPER('LE')), (5, 1, UPPER('SO')), (6, 1, UPPER('CE'));
insert into district(object_id, parent_id, parent_entity_id) values (1,1,400), (2,1,400), (3,3,400);
insert into district_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,600,1,600),
(1,1,601,4,601),
(1,2,600,2,600),
(1,2,601,5,601),
(1,3,600,3,600),
(1,3,601,6,601);

-- Cities
insert into city_string_culture(id, locale_id, value) values (1, 1, UPPER('Новосибирск')), (1,2,UPPER('Novosibirsk')),
                                                          (2, 1, UPPER('Москва')), (2,2,UPPER('Moscow')),
                                                          (3, 1, UPPER('Харьков')), (3,2,UPPER('Харьков'));
insert into city(object_id, parent_id, parent_entity_id) values (1,1,700), (2,2,700), (3,3,700);
insert into city_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,400,1,400),(1,1,401,10000,401),
(1,2,400,2,400),(1,2,401,10000,401),
(1,3,400,3,400),(1,3,401,10000,401);

-- Regions
insert into region_string_culture(id, locale_id, value) values (1, 1, UPPER('Новосибирская обл.')), (1,2,UPPER('Novosibirsk''s region')),
                                                            (2, 1, UPPER('Московская обл.')), (2,2,UPPER('Moscow''s region')),
                                                            (3, 1, UPPER('Харьковская обл.(ТЕСТ)')), (3,2,UPPER('Харьковская обл.(ТЕСТ)'));
insert into region(object_id, parent_id, parent_entity_id) values (1,1,800), (2,1,800), (3,2,800);
insert into region_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,700,1,700),
(1,2,700,2,700),
(1,3,700,3,700);

-- Countries
insert into country_string_culture(id, locale_id, value) values (1, 1, UPPER('Россия')), (1,2,UPPER('Russia')),
                                                            (2, 1, UPPER('Украина(ТЕСТ)')), (2,2,UPPER('Ukraine(ТЕСТ)'));
insert into country(object_id) values (1), (2);
insert into country_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,800,1,800),
(1,2,800,2,800);

-- Organizations
insert into organization(object_id, parent_id, parent_entity_id, entity_type_id) values (1,null,null,900), (2,null,null,901);
insert into organization_string_culture(id, locale_id, value) values (3, 1, UPPER('ОСЗН 1')), (3,2,UPPER('ОСЗН 1')), (4, 1, UPPER('1234')),
(5, 1, UPPER('Центр начислений №1')), (5, 2, UPPER('Центр начислений №1')), (6, 1, UPPER('1234'));
insert into organization_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,900,3,900), (1,1,901,4,901), (1,1,902,3,902),
(1,2,900,5,900), (1,2,901,6,902);

-- Files
insert into request_file_group(id) values (1);
insert into `request_file`(id, group_id, organization_id, `name`, `directory`, `registry`, `month`, `year`, `loaded`, `type`) values
(1,1,1,'A_123405.dbf', 'AB', 1, 10, 2010, CURRENT_TIMESTAMP, 'PAYMENT'),
(2,1,1,'AF123405.dbf', 'CE', 1, 10, 2010, CURRENT_TIMESTAMP, 'BENEFIT'),
(3,null,1,'TARIF12.dbf', 'MO', 1, 10, 2010, CURRENT_TIMESTAMP, 'TARIF');

-- Benefit
insert into benefit(own_num_sr, OZN, f_nam, m_nam, sur_nam, request_file_id, IND_COD, PSP_NUM)
values
-- (1, 1, 'Иван', 'Иванович', 'Иванов',2),
-- (1, 0, 'Иван2', 'Иванович2', 'Иванов2',2, '0000000', null),
 (4, 0, 'Петр0','Петрович0','Петров0',2, '11111111', null),
(4, 1, 'Петр','Петрович','Петров',2, '2142426430', null);

-- Payments
insert into payment(own_num_sr, f_nam, m_nam, sur_nam, n_name, vul_name, bld_num, corp_num, flat, DAT1, request_file_id)
values
-- (1,'Иван', 'Иванович', 'Иванов', 'Новосибирск', 'Терешковой', '8','', 'кв. 10', '2010-09-08',1),
-- (2,'Сидор', 'Сидорович', 'Сидоров', 'Новосибирск', 'ул. Терешковой', 'д. 11','', 'кв. 11', '2010-09-08',1),
-- (3,'Петр', 'Петрович', 'Петров', 'Харьков', 'Косиора', '154A','', '1', '2010-09-08',1),
-- (3,'Петр1', 'Петрович1', 'Петров1', 'Харьков', 'Kоcиорa', '154A','', '1', '2010-09-08',1),
-- (3,'Матвей1', 'Матвеевич1', 'Матвеев1', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '25А','', '40', '2010-09-09',1),
(4,'Матвей', 'Матвеевич', 'Матвеев', 'Харьков', 'ФРАНТИШЕКА КРАЛА1', '  2 5А','  ', '19', '2010-09-09',1);
-- (4,'Матвей', 'Матвеевич', 'Матвеев', 'Новосибирск', 'Терешковой', '25','  ', '19', '2010-09-09',1);

--insert into `person_account` (`first_name`, `middle_name`, `last_name`, `city`, `street`, `building_num`, `building_corp`, `apartment`, `account_number`, `own_num_sr`, `oszn_id`, `calc_center_id`) values('Матвей','Матвеевич','Матвеев','Харьков','ФРАНТИШЕКА КРАЛА','  2 5А','  ','19','1000001108','4','1','2');

-- Address corrections
INSERT INTO `street_type_correction`(`object_id`, `correction`, `organization_id`, `organization_code`, `internal_organization_id`) VALUES
(10000,'Б-Р',2,'1',0), (10001,'М',2,'1',0), (10002,'М-Н',2,'1',0), (10003,'ПЕР',2,'1',0), (10004,'ПЛ',2,'1',0), (10005,'П',2,'1',0)
, (10006,'ПОС',2,'1',0), (10007,'ПР-Д',2,'1',0), (10008,'ПРОСП',2,'1',0), (10009,'СП',2,'1',0), (10010,'Т',2,'1',0), (10011,'ТУП',2,'1',0)
, (10012,'УЛ',2,'1',0), (10013,'ШОССЕ',2,'1',0), (10014,'НАБ',2,'1',0), (10015,'В-Д',2,'1',0), (10016,'СТ',2,'1',0);

insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (1,2,UPPER('Новосибирск'),1,0);
insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id) values (1,2,UPPER('Терешковой'),1,0,1);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (2,'10','1',3,0,1);

insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (2,2,UPPER('Харьков'),3,0);
insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id) values (2,2,UPPER('Косиора'),4,0,2);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (2,'154А','',6,0,2);

insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id) values (3,2,UPPER('ФРАНТИШЕКА КРАЛА'),5,0,2);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (2,'25А','',7,0,3);

insert into district_correction(organization_id, correction, object_id, internal_organization_id, parent_id) values (2,UPPER('Центральный'),3,0,2);

-- insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (3,1,UPPER('Харьков'),3,0);
-- insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id) values (4,1,UPPER('ФРАНТИШЕКА КРАЛА1'),5,0,3);

/* Corrections for testing situations where correction's building belongs another street than local address base' building */

--insert into city_correction(id, organization_id, correction, object_id, internal_organization_id) values (3,1,UPPER('Новосибирск'),1,0);
--insert into street_correction(id, organization_id, correction, object_id, internal_organization_id, parent_id) values (4,1,UPPER('Терешковой'),1,0,3);
--insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id, parent_id) values (1,'25','',7,0,4);


-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, organization_code, internal_organization_id) values
(1,UPPER('мiсцевих Рад'),1,'1',0), (2,UPPER('ГОС'),1,'1',0),
(1,UPPER('кооперативна'),2,'1',0), (2,UPPER('КООП'),2,'2',0),
(1,UPPER('приватна'),5,'5',0), (2,UPPER('ВЫК'),5,'5',0),
(1,UPPER('приватизована'),6,'6',0), (2,UPPER('ЧАС'),6,'6',0);

-- Privileges corrections
insert into privilege_correction(organization_id, correction, object_id, organization_code, internal_organization_id) values
(2,'ПЕНСИОНЕР ПО ВОЗРАСТУ',15,'34',0),
(1,'ПЕНСИОНЕР ПО ВОЗРАСТУ',15,'1000',0);

-- Tarif
insert into tarif(`T11_CS_UNI`, `T11_CODE2`, `request_file_id`, `T11_CODE1`) values (0,123,3,1);

-- calculation center info
insert into calculation_center_preference(calculation_center_id, adapter_class) values (2, 'org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter');
