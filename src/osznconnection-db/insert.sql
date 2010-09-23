-- --------------------------------
-- Users
-- --------------------------------
insert into user value (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', null);
insert into usergroup value (1, 'admin', 'ADMINISTRATORS');
insert into user value (2, 'ANONYMOUS', 'ANONYMOUS', null);

-- --------------------------------
-- Locale
-- --------------------------------

insert into `locales`(`locale`, `system`) values ('ru', 1);
insert into `locales`(`locale`, `system`) values ('uk', 0);

-- --------------------------------
-- Sequence
-- --------------------------------

insert into `sequence` (`sequence_name`, `sequence_value`) values
('string_culture',1),
('apartment',1), ('apartment_string_culture',1),
('building',1), ('building_string_culture',1),
('country',1), ('country_string_culture',1),
('district',1), ('district_string_culture',1),
('city',1), ('city_string_culture',1),
('region',1), ('region_string_culture',1),
('room',1), ('room_string_culture',1),
('street',1), ('street_string_culture',1),
('organization',1), ('organization_string_culture',1),
('user_info', 1), ('user_info_string_culture', 1),
('ownership',1), ('ownership_string_culture',1);

-- --------------------------------
-- Apartment
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (100, 'ru', 'Квартира'), (100, 'uk', 'Квартира');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (100, 'apartment', 100, '');
insert into `string_culture`(`id`, `locale`, `value`) values (101, 'ru', UPPER('Наименование квартиры')), (101, 'uk', UPPER('Найменування квартири'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (100, 100, 1, 101, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (100, 100, UPPER('string_culture'));

-- --------------------------------
-- Room
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (200, 'ru', 'Комната'), (200, 'uk', 'Кімната');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (200, 'room', 200, '');
insert into `string_culture`(`id`, `locale`, `value`) values (201, 'ru', UPPER('Наименование комнаты')), (201, 'uk', UPPER('Найменування кімнати'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (200, 200, 1, 201, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (200, 200, UPPER('string_culture'));

-- --------------------------------
-- Street
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (300, 'ru', 'Улица'), (300, 'uk', 'Вулиця');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (300, 'street', 300, '');
insert into `string_culture`(`id`, `locale`, `value`) values (301, 'ru', UPPER('Наименование улицы')), (301, 'uk', UPPER('Найменування вулиці'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (300, 300, 1, 301, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (300, 300, UPPER('string_culture'));

-- --------------------------------
-- City
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (400, 'ru', 'Населенный пункт'), (400, 'uk', 'Населений пункт');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (400, 'city', 400, '');
insert into `string_culture`(`id`, `locale`, `value`) values (401, 'ru', UPPER('Наименование населенного пункта')), (401, 'uk', UPPER('Найменування населеного пункту'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (400, 400, 1, 401, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (400, 400, UPPER('string_culture'));
insert into `string_culture`(`id`, `locale`, `value`) values
(402, 'ru', UPPER('город')), (402, 'uk', UPPER('місто')),
(403, 'ru', UPPER('деревня')), (403, 'uk', UPPER('село'));
insert into `entity_type`(`id`, `entity_id`, `entity_type_name_id`) values (400, 400, 402), (401, 400, 403);

-- --------------------------------
-- Building
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (500, 'ru', 'Дом'), (500, 'uk', 'Будинок');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (500, 'building', 500, '');
insert into `string_culture`(`id`, `locale`, `value`) values (501, 'ru', UPPER('Номер дома')), (501, 'uk', UPPER('Номер будинку'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (500, 500, 1, 501, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (502, 'ru', UPPER('Корпус')), (502, 'uk', UPPER('Корпус'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (501, 500, 0, 502, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (503, 'ru', UPPER('Строение')), (503, 'uk', UPPER('Будова'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (502, 500, 0, 503, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (504, 'ru', UPPER('Улица')), (504, 'uk', UPPER('Вулиця'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (503, 500, 0, 504, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (505, 'ru', UPPER('Район')), (505, 'uk', UPPER('Район'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (504, 500, 0, 505, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (500, 500, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (501, 501, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (502, 502, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (503, 503, 'street');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (504, 504, 'district');

-- --------------------------------
-- District
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (600, 'ru', 'Район'), (600, 'uk', 'Район');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (600, 'district', 600, '');
insert into `string_culture`(`id`, `locale`, `value`) values (601, 'ru', UPPER('Наименование района')), (601, 'uk', UPPER('Найменування району'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (600, 600, 1, 601, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (600, 600, UPPER('string_culture'));
insert into `string_culture`(`id`, `locale`, `value`) values (602, 'ru', UPPER('Код района')), (602, 'uk', UPPER('Код району'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (601, 600, 1, 602, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (601, 601, UPPER('string'));

-- --------------------------------
-- Region
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (700, 'ru', 'Регион'), (700, 'uk', 'Регіон');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (700, 'region', 700, '');
insert into `string_culture`(`id`, `locale`, `value`) values (701, 'ru', UPPER('Наименование региона')), (701, 'uk', UPPER('Найменування регіону'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (700, 700, 1, 701, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (700, 700, UPPER('string_culture'));

-- --------------------------------
-- Country
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (800, 'ru', 'Страна'), (800, 'uk', 'Країна');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (800, 'country', 800, '');
insert into `string_culture`(`id`, `locale`, `value`) values (801, 'ru', UPPER('Наименование страны')), (801, 'uk', UPPER('Найменування країни'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (800, 800, 1, 801, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (800, 800, UPPER('string_culture'));

-- --------------------------------
-- Organization
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (900, 'ru', 'Организация'), (900, 'uk', 'Організація');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (900, 'organization', 900, '');
insert into `string_culture`(`id`, `locale`, `value`) values (901, 'ru', UPPER('Наименование организации')), (901, 'uk', UPPER('Найменування організації'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (900, 900, 1, 901, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (902, 'ru', UPPER('Уникальный код организации')), (902, 'uk', UPPER('Унікальний код організації'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (901, 900, 1, 902, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (903, 'ru', UPPER('Район')), (903, 'uk', UPPER('Район'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (902, 900, 0, 903, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (900, 900, UPPER('string_culture'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (901, 901, UPPER('string'));
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (902, 902, 'district');
insert into `string_culture`(`id`, `locale`, `value`) values
(904, 'ru', UPPER('ОСЗН')), (904, 'uk', UPPER('ОСЗН')),
(905, 'ru', UPPER('Центр начислений')), (905, 'uk', UPPER('Центр нарахувань'));
insert into `entity_type`(`id`, `entity_id`, `entity_type_name_id`) values (900, 900, 904), (901, 900, 905);

-- --------------------------------
-- User
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (1000, 'ru', 'Пользователь'), (1000, 'uk', 'Користувач');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (1000, 'user_info', 1000, '');
insert into `string_culture`(`id`, `locale`, `value`) values (1001, 'ru', UPPER('Фамилия')), (1001, 'uk', UPPER('Прізвище'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1000, 1000, 1, 1001, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1000, 1000, UPPER('string'));
insert into `string_culture`(`id`, `locale`, `value`) values (1002, 'ru', UPPER('Имя')), (1002, 'uk', UPPER('Ім\'я'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1001, 1000, 1, 1002, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1001, 1001, UPPER('string'));
insert into `string_culture`(`id`, `locale`, `value`) values (1003, 'ru', UPPER('Отчество')), (1003, 'uk', UPPER('По батькові'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1002, 1000, 1, 1003, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1002, 1002, UPPER('string'));

-- --------------------------------
-- Ownership
-- --------------------------------

insert into `string_culture`(`id`, `locale`, `value`) values (1100, 'ru', 'Форма собственности'), (1100, 'uk', 'Форма власності');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (1100, 'ownership', 1100, '');
insert into `string_culture`(`id`, `locale`, `value`) values (1101, 'ru', UPPER('Название')), (1101, 'uk', UPPER('Назва'));
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1100, 1100, 1, 1101, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1100, 1100, UPPER('string_culture'));

-- Forms of ownerships
insert into ownership(object_id) values (1),(2),(3),(4),(5),(6);
insert into ownership_string_culture(id, locale, value) values (1, 'ru', UPPER('мiсцевих Рад')), (1,'uk',UPPER('мiсцевих Рад')),
(2, 'ru', UPPER('кооперативна')), (2, 'uk', UPPER('кооперативна')), (3, 'ru', UPPER('вiдомча')), (3,'uk',UPPER('вiдомча')),
(4, 'ru', UPPER('громадська')), (4,'uk',UPPER('громадська')), (5, 'ru', UPPER('приватна')), (5,'uk',UPPER('приватна')),
(6, 'ru', UPPER('приватизована')), (6,'uk',UPPER('приватизована'));
insert into ownership_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) values
(1,1,1100,1,1100),(1,2,1100,2,1100),(1,3,1100,3,1100),(1,4,1100,4,1100),(1,5,1100,5,1100),(1,6,1100,6,1100);