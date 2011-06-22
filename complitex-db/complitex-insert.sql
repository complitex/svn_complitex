-- --------------------------------
-- Users
-- --------------------------------
INSERT INTO USER (`id`, `login`, `password`) VALUE (1, 'admin', '21232f297a57a5a743894a0e4a801fc3');
INSERT INTO usergroup (`id`, `login`, `group_name`) VALUE (1, 'admin', 'ADMINISTRATORS');
INSERT INTO usergroup (`id`, `login`, `group_name`) VALUE (2, 'admin', 'EMPLOYEES');
INSERT INTO usergroup (`id`, `login`, `group_name`) VALUE (3, 'admin', 'EMPLOYEES_CHILD_VIEW');
INSERT INTO USER (`id`, `login`, `password`)  VALUE (2, 'ANONYMOUS', 'ANONYMOUS');

-- --------------------------------
-- Locale
-- --------------------------------

INSERT INTO `locales`(`id`, `locale`, `system`) VALUES (1, 'ru', 1);
INSERT INTO `locales`(`id`, `locale`, `system`) VALUES (2, 'uk', 0);

-- --------------------------------
-- Sequence
-- --------------------------------

INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES
('string_culture',1),
('apartment',1), ('apartment_string_culture',1),
('building',1), ('building_string_culture',1),
('building_address',1), ('building_address_string_culture',1),
('country',1), ('country_string_culture',1),
('district',1), ('district_string_culture',1),
('city',1), ('city_string_culture',1),
('city_type',1), ('city_type_string_culture',1),
('region',1), ('region_string_culture',1),
('room',1), ('room_string_culture',1),
('street',1), ('street_string_culture',1),
('street_type',1), ('street_type_string_culture',1),
('organization',1), ('organization_string_culture',1),
('user_info', 1), ('user_info_string_culture', 1);

-- Permission
INSERT INTO `permission` (`permission_id`, `table`, `entity`, `object_id`) VALUE (0, 'ALL', 'ALL', 0);
INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES ('permission', 1);

-- --------------------------------
-- Apartment
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (100, 1, 'Квартира'), (100, 2, 'Квартира');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (100, 'apartment', 100, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (101, 1, UPPER('Наименование квартиры')), (101, 2, UPPER('Найменування квартири'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (100, 100, 1, 101, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (100, 100, UPPER('string_culture'));

-- --------------------------------
-- Room
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (200, 1, 'Комната'), (200, 2, 'Кімната');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (200, 'room', 200, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (201, 1, UPPER('Наименование комнаты')), (201, 2, UPPER('Найменування кімнати'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (200, 200, 1, 201, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (200, 200, UPPER('string_culture'));

-- --------------------------------
-- Street
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (300, 1, 'Улица'), (300, 2, 'Вулиця');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (300, 'street', 300, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (301, 1, UPPER('Наименование улицы')), (301, 2, UPPER('Найменування вулиці'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (300, 300, 1, 301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (300, 300, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (302, 1, UPPER('Тип улицы')),(302, 2, UPPER('Тип улицы'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (301, 300, 1, 302, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (301, 301, 'street_type');

-- --------------------------------
-- Street Type
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1400, 1, 'Тип улицы'), (1400, 2, 'Тип улицы');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1400, 'street_type', 1400, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1401, 1, UPPER('Краткое название')), (1401, 2, UPPER('Краткое название'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1400, 1400, 1, 1401, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1400, 1400, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1402, 1, UPPER('Название')), (1402, 2, UPPER('Название'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1401, 1400, 1, 1402, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1401, 1401, UPPER('string_culture'));

-- --------------------------------
-- City
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (400, 1, 'Населенный пункт'), (400, 2, 'Населений пункт');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (400, 'city', 400, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (401, 1, UPPER('Наименование населенного пункта')), (401, 2, UPPER('Найменування населеного пункту'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (400, 400, 1, 401, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (400, 400, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (402, 1, UPPER('Тип населенного пункта')), (402, 2, UPPER('Тип населенного пункта'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (401, 400, 1, 402, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (401, 401, 'city_type');

-- --------------------------------
-- City Type
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1300, 1, 'Тип нас. пункта'), (1300, 2, 'Тип населенного пункта');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1300, 'city_type', 1300, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1301, 1, UPPER('Краткое название')), (1301, 2, UPPER('Краткое название'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1300, 1300, 1, 1301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1300, 1300, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1302, 1, UPPER('Название')), (1302, 2, UPPER('Название'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1301, 1300, 1, 1302, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1301, 1301, UPPER('string_culture'));

-- --------------------------------
-- Building
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (500, 1, 'Дом'), (500, 2, 'Будинок');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (500, 'building', 500, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (501, 1, UPPER('Район')), (501, 2, UPPER('Район'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (500, 500, 0, 501, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (500, 500, 'district');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (502, 1, UPPER('Альтернативный адрес')), (502, 2, UPPER('Альтернативный адрес'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (501, 500, 0, 502, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (501, 501, 'building_address');


-- --------------------------------
-- Building Address
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1500, 1, 'Адрес здания'), (1500, 2, 'Адрес здания');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1500, 'building_address', 1500, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1501, 1, UPPER('Номер дома')), (1501, 2, UPPER('Номер будинку'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1500, 1500, 1, 1501, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1502, 1, UPPER('Корпус')), (1502, 2, UPPER('Корпус'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1501, 1500, 0, 1502, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1503, 1, UPPER('Строение')), (1503, 2, UPPER('Будова'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1502, 1500, 0, 1503, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1500, 1500, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1501, 1501, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1502, 1502, UPPER('string_culture'));

-- --------------------------------
-- District
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (600, 1, 'Район'), (600, 2, 'Район');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (600, 'district', 600, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (601, 1, UPPER('Наименование района')), (601, 2, UPPER('Найменування району'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (600, 600, 1, 601, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (600, 600, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (602, 1, UPPER('Код района')), (602, 2, UPPER('Код району'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (601, 600, 1, 602, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (601, 601, UPPER('string'));

-- --------------------------------
-- Region
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (700, 1, 'Регион'), (700, 2, 'Регіон');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (700, 'region', 700, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (701, 1, UPPER('Наименование региона')), (701, 2, UPPER('Найменування регіону'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (700, 700, 1, 701, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (700, 700, UPPER('string_culture'));

-- --------------------------------
-- Country
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (800, 1, 'Страна'), (800, 2, 'Країна');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (800, 'country', 800, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (801, 1, UPPER('Наименование страны')), (801, 2, UPPER('Найменування країни'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (800, 800, 1, 801, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (800, 800, UPPER('string_culture'));


-- --------------------------------
-- User
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1000, 1, 'Пользователь'), (1000, 2, 'Користувач');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1000, 'user_info', 1000, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1001, 1, UPPER('Фамилия')), (1001, 2, UPPER('Прізвище'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1000, 1000, 1, 1001, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1000, 1000, 'last_name');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1002, 1, UPPER('Имя')), (1002, 2, UPPER('Ім\'я'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1001, 1000, 1, 1002, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1001, 1001, 'first_name');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (1003, 1, UPPER('Отчество')), (1003, 2, UPPER('По батькові'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1002, 1000, 1, 1003, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1002, 1002, 'middle_name');


-- --------------------------------
-- Organization
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (900, 1, 'Организация'), (900, 2, 'Організація');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (900, 'organization', 900, '');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (901, 1, UPPER('Наименование организации')), (901, 2, UPPER('Найменування організації'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (900, 900, 1, 901, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (902, 1, UPPER('Уникальный код организации')), (902, 2, UPPER('Унікальний код організації'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (901, 900, 1, 902, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (903, 1, UPPER('Район')), (903, 2, UPPER('Район'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (902, 900, 0, 903, 1);
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (907, 1, UPPER('Принадлежит')), (907, 2, UPPER('Принадлежит'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (903, 900, 0, 907, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (900, 900, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (901, 901, UPPER('string'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (902, 902, 'district');
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (903, 903, 'organization');
INSERT INTO `string_culture`(`id`, `locale_id`, `value`) VALUES (906, 1, UPPER('Организации пользователей')), (906, 2, UPPER('Организации пользователей'));
INSERT INTO `entity_type`(`id`, `entity_id`, `entity_type_name_id`) VALUES (902, 900, 906);
