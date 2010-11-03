-- Street types
insert into `string_culture`(`id`, `locale`, `value`) values
(302, 'ru', UPPER('ул')), (302, 'uk', UPPER('вулиця')),
(303, 'ru', UPPER('пр-т')), (303, 'uk', UPPER('проспект')),
(304, 'ru', UPPER('пер-к')), (304, 'uk', UPPER('провулок'));
insert into `entity_type` (`id`, `entity_id`, `entity_type_name_id`) values
(300, 300, 302), (301, 300, 303), (302, 300, 304);


-- Rooms
insert into room(object_id, parent_id, parent_entity_id) values (1,1,100), (2,1,100), (3,2,100), (4,2,100);
insert into room_string_culture(id, locale, value) values (1, 'ru', UPPER('1а')), (1, 'uk', UPPER('1a')), (2, 'ru', UPPER('1б')), (2, 'uk', UPPER('1b')),
(3, 'ru', UPPER('2а')), (3, 'uk', UPPER('2a')), (4, 'ru', UPPER('2б')), (4, 'uk', UPPER('2b'));
insert into room_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,200,1,200), (1,2,200,2,200), (1,3,200,3,200), (1,4,200,4,200);

-- Apartments
insert into apartment(object_id, parent_id, parent_entity_id) values (1,1,500), (2,1,500), (3,6,500), (4,7,500), (5,7,500);
insert into apartment_string_culture(id, locale, value) values (1, 'ru', UPPER('10')), (1, 'uk', UPPER('10')), (2, 'ru', UPPER('20')), (2, 'uk', UPPER('20')),
                                                                (3, 'ru', UPPER('1')), (3, 'uk', UPPER('1')),
                                                                (4, 'ru', UPPER('40')), (4, 'uk', UPPER('40')),
                                                                (5, 'ru', UPPER('19')), (5, 'uk', UPPER('19'));
insert into apartment_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,100,1,100), (1,2,100,2,100), (1,3,100,3,100), (1,4,100,4,100), (1,5,100,5,100);

-- Buildings
insert into building(object_id, parent_id, parent_entity_id) values (1,1,400), (2,1,400), (3,1,400), (4,2,400), (5,2,400), (6,3,400), (7,3,400);
insert into building_string_culture(id, locale, value) values
(1, 'ru', UPPER('8')), (2, 'ru', UPPER('28')), (3,'ru',UPPER('18')), (4,'ru',UPPER('12')), (5,'ru',UPPER('21')), (6,'ru',UPPER('100')),
(1, 'uk', UPPER('8')), (2, 'uk', UPPER('28')), (3,'uk',UPPER('18')), (4,'uk',UPPER('12')), (5,'uk',UPPER('21')), (6,'uk',UPPER('100')),
(19,'ru',UPPER('154A')), (19,'uk',UPPER('154А')),
(22,'ru',UPPER('25A')), (22,'uk',UPPER('25А'));
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
(1,5,504,1,504),
(1,6,500,19,500),
(1,6,501,20,501),
(1,6,502,21,502),
(1,6,503,4,503),
(1,6,504,3,504),
(1,7,500,22,500),
(1,7,501,23,501),
(1,7,502,24,502),
(1,7,503,5,503),
(1,7,504,3,504);

-- Streets
insert into street_string_culture(id, locale, value) values (1, 'ru', UPPER('Терешковой')), (1,'uk',UPPER('Tereshkovoy')),
                                                            (2, 'ru', UPPER('Ленина')), (2,'uk',UPPER('Lenina')),
                                                            (3, 'ru', UPPER('Морской')), (3,'uk', UPPER('Morskoy')),
                                                            (4, 'ru', UPPER('КОСИОРА')), (4,'uk', UPPER('КОСИОРА')),
                                                            (5, 'ru', UPPER('ФРАНТИШЕКА КРАЛА')), (5,'uk', UPPER('ФРАНТИШЕКА КРАЛА'));
insert into street(object_id, parent_id, parent_entity_id, entity_type_id) values (1,1,400,300), (2,2,400,300), (3,1,400,300), (4,3,400,301), (5,3,400,300);
insert into street_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,300,1,300),
(1,2,300,2,300),
(1,3,300,3,300),
(1,4,300,4,300),
(1,5,300,5,300);

-- Districts
insert into district_string_culture(id, locale, value) values (1, 'ru', UPPER('Ленинский')), (1, 'uk',UPPER('Leninsky')),
                                                              (2, 'ru', UPPER('Советский')), (2, 'uk', UPPER('Sovetsky')),
                                                              (3, 'ru', UPPER('Центральный')), (3, 'uk', UPPER('Центральный')),
                                                              (4, 'ru', UPPER('LE')), (5, 'ru', UPPER('SO')), (6, 'ru', UPPER('CE'));
insert into district(object_id, parent_id, parent_entity_id) values (1,1,400), (2,1,400), (3,3,400);
insert into district_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,600,1,600),
(1,1,601,4,601),
(1,2,600,2,600),
(1,2,601,5,601),
(1,3,600,3,600),
(1,3,601,6,601);

-- Cities
insert into city_string_culture(id, locale, value) values (1, 'ru', UPPER('Новосибирск')), (1,'uk',UPPER('Novosibirsk')),
                                                          (2, 'ru', UPPER('Москва')), (2,'uk',UPPER('Moscow')),
                                                          (3, 'ru', UPPER('Харьков')), (3,'uk',UPPER('Харьков'));
insert into city(object_id, parent_id, parent_entity_id) values (1,1,700), (2,2,700), (3,3,700);
insert into city_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,400,1,400),(1,1,401,1,401),
(1,2,400,2,400),(1,2,401,1,401),
(1,3,400,3,400),(1,3,401,1,401);

-- Regions
insert into region_string_culture(id, locale, value) values (1, 'ru', UPPER('Новосибирская обл.')), (1,'uk',UPPER('Novosibirsk''s region')),
                                                            (2, 'ru', UPPER('Московская обл.')), (2,'uk',UPPER('Moscow''s region')),
                                                            (3, 'ru', UPPER('Харьковская обл.(ТЕСТ)')), (3,'uk',UPPER('Харьковская обл.(ТЕСТ)'));
insert into region(object_id, parent_id, parent_entity_id) values (1,1,800), (2,1,800), (3,2,800);
insert into region_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,700,1,700),
(1,2,700,2,700),
(1,3,700,3,700);

-- Countries
insert into country_string_culture(id, locale, value) values (1, 'ru', UPPER('Россия')), (1,'uk',UPPER('Russia')),
                                                            (2, 'ru', UPPER('Украина(ТЕСТ)')), (2,'uk',UPPER('Ukraine(ТЕСТ)'));
insert into country(object_id) values (1), (2);
insert into country_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,800,1,800),
(1,2,800,2,800);

-- Organizations
insert into organization(object_id, parent_id, parent_entity_id, entity_type_id) values (1,null,null,900), (2,null,null,901);
insert into organization_string_culture(id, locale, value) values (3, 'ru', UPPER('ОСЗН 1')), (3,'uk',UPPER('ОСЗН 1')), (4, 'ru', UPPER('1234')),
(5, 'ru', UPPER('Центр начислений №1')), (5, 'uk', UPPER('Центр начислений №1')), (6, 'ru', UPPER('1234'));
insert into organization_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,900,3,900), (1,1,901,4,901), (1,1,902,3,902),
(1,2,900,5,900), (1,2,901,6,902), (1,2,902,null,902);

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
-- (1, 0, 'Иван2', 'Иванович2', 'Иванов2',2),
 (4, 0, 'Петр','Петрович','Петров',2, '11111111', null),
(4, 1, 'Петр','Петрович','Петров',2, '2142426432', null);

-- Payments
insert into payment(own_num_sr, f_nam, m_nam, sur_nam, n_name, vul_name, bld_num, corp_num, flat, DAT1, request_file_id)
values
-- (1,'Иван', 'Иванович', 'Иванов', 'Новосибирск', 'Терешковой', '8','', 'кв. 10', '2010-09-08',1),
-- (2,'Сидор', 'Сидорович', 'Сидоров', 'Новосибирск', 'ул. Терешковой', 'д. 11','', 'кв. 11', '2010-09-08',1),
-- (3,'Петр', 'Петрович', 'Петров', 'Харьков', 'Косиора', '154A','', '1', '2010-09-08',1),
-- (3,'Петр1', 'Петрович1', 'Петров1', 'Харьков', 'Kоcиорa', '154A','', '1', '2010-09-08',1);
-- (3,'Матвей1', 'Матвеевич1', 'Матвеев1', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '25А','', '40', '2010-09-09',1),
(4,'Матвей', 'Матвеевич', 'Матвеев', 'Харьков', 'ФРАНТИШЕКА КРАЛА', '  2 5А','  ', '19', '2010-09-09',1);

-- Address corrections
insert into entity_type_correction(organization_id, `type`, entity_type_id, internal_organization_id) values (2,UPPER('ул'),300,0);
insert into entity_type_correction(organization_id, `type`, entity_type_id, internal_organization_id) values (2,UPPER('пр-т'),301,0);

insert into city_correction(organization_id, correction, object_id, internal_organization_id) values (2,UPPER('Новосибирск'),1,0);
insert into street_correction(organization_id, correction, object_id, internal_organization_id) values (2,UPPER('Терешковой'),1,0);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id) values (2,'10','1',1,0);

insert into city_correction(organization_id, correction, object_id, internal_organization_id) values (2,UPPER('Харьков'),3,0);
insert into street_correction(organization_id, correction, object_id, internal_organization_id) values (2,UPPER('Косиора'),4,0);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id) values (2,'154А','',6,0);

insert into street_correction(organization_id, correction, object_id, internal_organization_id) values (2,UPPER('ФРАНТИШЕКА КРАЛА'),5,0);
insert into building_correction(organization_id, correction, correction_corp, object_id, internal_organization_id) values (2,'25А','',7,0);

insert into district_correction(organization_id, correction, object_id, internal_organization_id) values (2,UPPER('Центральный'),3,0);

-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, organization_code, internal_organization_id) values
(1,UPPER('мiсцевих Рад'),1,'1',0), (2,UPPER('ГОС'),1,'1',0),
(1,UPPER('кооперативна'),2,'1',0), (2,UPPER('КООП'),2,'2',0),
(1,UPPER('приватна'),5,'5',0), (2,UPPER('ВЫК'),5,'5',0),
(1,UPPER('приватизована'),6,'6',0), (2,UPPER('ЧАС'),6,'6',0);

-- Privileges corrections
insert into privilege_correction(organization_id, correction, object_id, organization_code, internal_organization_id) values
(2,'ПЕНСИОНЕР ПО ВОЗРАСТУ',15,34,0),
(1,'ПЕНСИОНЕР ПО ВОЗРАСТУ',15,1000,0);

-- Tarif
insert into tarif(`T11_CS_UNI`, `T11_CODE2`, `request_file_id`, `T11_CODE1`) values (0,123,3,1);

-- calculation center info
insert into calculation_center_preference(calculation_center_id, adapter_class) values (2, 'org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter');
