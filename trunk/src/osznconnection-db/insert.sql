insert into `locales`(`locale`, `system`) values ('ru', 1);
insert into `locales`(`locale`, `system`) values ('en', 0);

insert into `sequence` (`sequence_name`, `sequence_value`) values
('string_culture',1),
('apartment',1), ('apartment_string_culture',1), ('building',1), ('building_string_culture',1),
('country',1), ('country_string_culture',1), ('district',1), ('district_string_culture',1),
('city',1), ('city_string_culture',1), ('region',1), ('region_string_culture',1),
('room',1), ('room_string_culture',1), ('street',1), ('street_string_culture',1),
('organization',1), ('organization_string_culture',1), ('user_info', 1), ('user_info_string_culture', 1);

insert into `string_culture`(`id`, `locale`, `value`) values (100, 'ru', 'Квартира'), (100, 'en', 'Apartment');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (100, 'apartment', 100, '');
insert into `string_culture`(`id`, `locale`, `value`) values (101, 'ru', UPPER('Наименование квартиры')), (101, 'en', UPPER('Apartment name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (100, 100, 1, 101, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (100, 100, UPPER('string_culture'));

insert into `string_culture`(`id`, `locale`, `value`) values (200, 'ru', 'Комната'), (200, 'en', 'Room');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (200, 'room', 200, '');
insert into `string_culture`(`id`, `locale`, `value`) values (201, 'ru', UPPER('Наименование комнаты')), (201, 'en', UPPER('Room name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (200, 200, 1, 201, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (200, 200, UPPER('string_culture'));

insert into `string_culture`(`id`, `locale`, `value`) values (300, 'ru', 'Улица'), (300, 'en', 'Street');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (300, 'street', 300, '');
insert into `string_culture`(`id`, `locale`, `value`) values (301, 'ru', UPPER('Наименование улицы')), (301, 'en', UPPER('Street name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (300, 300, 1, 301, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (300, 300, UPPER('string_culture'));
insert into `string_culture`(`id`, `locale`, `value`) values
(302, 'ru', UPPER('улица')), (302, 'en', UPPER('street')),
(303, 'ru', UPPER('проспект')), (303, 'en', UPPER('avenue')),
(304, 'ru', UPPER('переулок')), (304, 'en', UPPER('alley'));
insert into `entity_type` (`id`, `entity_id`, `entity_type_name_id`) values
(300, 300, 302), (301, 300, 303), (302, 300, 304);

insert into `string_culture`(`id`, `locale`, `value`) values (400, 'ru', 'Населенный пункт'), (400, 'en', 'City');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (400, 'city', 400, '');
insert into `string_culture`(`id`, `locale`, `value`) values (401, 'ru', UPPER('Наименование населенного пункта')), (401, 'en', UPPER('City name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (400, 400, 1, 401, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (400, 400, UPPER('string_culture'));
insert into `string_culture`(`id`, `locale`, `value`) values
(402, 'ru', UPPER('город')), (402, 'en', UPPER('city')),
(403, 'ru', UPPER('деревня')), (403, 'en', UPPER('village'));
insert into `entity_type`(`id`, `entity_id`, `entity_type_name_id`) values (400, 400, 402), (401, 400, 403);

insert into `string_culture`(`id`, `locale`, `value`) values (500, 'ru', 'Дом'), (500, 'en', 'Building');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (500, 'building', 500, '');
insert into `string_culture`(`id`, `locale`, `value`) values (501, 'ru', UPPER('Номер дома')), (501, 'en', UPPER('Building number'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (500, 500, 1, 501, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (502, 'ru', UPPER('Корпус')), (502, 'en', UPPER('Building corps.'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (501, 500, 0, 502, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (503, 'ru', UPPER('Строение')), (503, 'en', UPPER('Building structure'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (502, 500, 0, 503, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (504, 'ru', UPPER('Улица')), (504, 'en', UPPER('Street'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (503, 500, 0, 504, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (505, 'ru', UPPER('Район')), (505, 'en', UPPER('District'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (504, 500, 0, 505, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (500, 500, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (501, 501, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (502, 502, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (503, 503, 'street');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (504, 504, 'district');

insert into `string_culture`(`id`, `locale`, `value`) values (600, 'ru', 'Район'), (600, 'en', 'District');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (600, 'district', 600, '');
insert into `string_culture`(`id`, `locale`, `value`) values (601, 'ru', UPPER('Наименование района')), (601, 'en', UPPER('District name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (600, 600, 1, 601, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (600, 600, UPPER('string_culture'));

insert into `string_culture`(`id`, `locale`, `value`) values (700, 'ru', 'Регион'), (700, 'en', 'Region');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (700, 'region', 700, '');
insert into `string_culture`(`id`, `locale`, `value`) values (701, 'ru', UPPER('Наименование региона')), (701, 'en', UPPER('Region name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (700, 700, 1, 701, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (700, 700, UPPER('string_culture'));

insert into `string_culture`(`id`, `locale`, `value`) values (800, 'ru', 'Страна'), (800, 'en', 'Country');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (800, 'country', 800, '');
insert into `string_culture`(`id`, `locale`, `value`) values (801, 'ru', UPPER('Наименование страны')), (801, 'en', UPPER('Country name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (800, 800, 1, 801, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (800, 800, UPPER('string_culture'));

insert into `string_culture`(`id`, `locale`, `value`) values (900, 'ru', 'Организация'), (900, 'en', 'Organization');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (900, 'organization', 900, '');
insert into `string_culture`(`id`, `locale`, `value`) values (901, 'ru', UPPER('Наименование')), (901, 'en', UPPER('Name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (900, 900, 1, 901, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (902, 'ru', UPPER('Код района')), (902, 'en', UPPER('District''s code'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (901, 900, 0, 902, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (903, 'ru', UPPER('Уникальный код организации')), (903, 'en', UPPER('Unique organization code'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (902, 900, 1, 903, 1);

insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (900, 900, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (901, 901, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (902, 902, UPPER('integer'));
insert into `string_culture`(`id`, `locale`, `value`) values
(904, 'ru', UPPER('ОСЗН')), (904, 'en', UPPER('ОСЗН')),
(905, 'ru', UPPER('ПУ')), (905, 'en', UPPER('ПУ')),
(906, 'ru', UPPER('Обслуживающая организация')), (906, 'en', UPPER('Обслуживающая организация'));
insert into `entity_type`(`id`, `entity_id`, `entity_type_name_id`) values (900, 900, 904), (901, 900, 905), (902, 900, 906);

insert into `string_culture`(`id`, `locale`, `value`) values (1000, 'ru', 'Пользователь'), (1000, 'en', 'User');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (1000, 'user_info', 1000, '');
insert into `string_culture`(`id`, `locale`, `value`) values (1001, 'ru', UPPER('Фамилия')), (1001, 'en', UPPER('Last Name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1000, 1000, 1, 1001, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1000, 1000, UPPER('string'));
insert into `string_culture`(`id`, `locale`, `value`) values (1002, 'ru', UPPER('Имя')), (1002, 'en', UPPER('First name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1001, 1000, 1, 1002, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1001, 1001, UPPER('string'));
insert into `string_culture`(`id`, `locale`, `value`) values (1003, 'ru', UPPER('Отчество')), (1003, 'en', UPPER('Middle name'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1002, 1000, 1, 1003, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1002, 1002, UPPER('string'));


update `sequence` set `sequence_value` = 2000 where `sequence_name` = 'string_culture';