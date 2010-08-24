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
insert into `string_culture`(`id`, `locale`, `value`) values (101, 'ru', 'Наименование квартиры'), (101, 'en', 'Apartment name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (100, 100, 1, 101, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (100, 100, 'string_culture');

insert into `string_culture`(`id`, `locale`, `value`) values (200, 'ru', 'Комната'), (200, 'en', 'Room');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (200, 'room', 200, '');
insert into `string_culture`(`id`, `locale`, `value`) values (201, 'ru', 'Наименование комнаты'), (201, 'en', 'Room name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (200, 200, 1, 201, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (200, 200, 'string_culture');

insert into `string_culture`(`id`, `locale`, `value`) values (300, 'ru', 'Улица'), (300, 'en', 'Street');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (300, 'street', 300, '');
insert into `string_culture`(`id`, `locale`, `value`) values (301, 'ru', 'Наименование улицы'), (301, 'en', 'Street name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (300, 300, 1, 301, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (300, 300, 'string_culture');
insert into `string_culture`(`id`, `locale`, `value`) values
(302, 'ru', 'улица'), (302, 'en', 'street'),
(303, 'ru', 'проспект'), (303, 'en', 'avenue'),
(304, 'ru', 'переулок'), (304, 'en', 'alley');
insert into `entity_type` (`id`, `entity_id`, `entity_type_name_id`) values
(300, 300, 302), (301, 300, 303), (302, 300, 304);

insert into `string_culture`(`id`, `locale`, `value`) values (400, 'ru', 'Населенный пункт'), (400, 'en', 'City');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (400, 'city', 400, '');
insert into `string_culture`(`id`, `locale`, `value`) values (401, 'ru', 'Наименование населенного пункта'), (401, 'en', 'City name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (400, 400, 1, 401, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (400, 400, 'string_culture');
insert into `string_culture`(`id`, `locale`, `value`) values
(402, 'ru', 'город'), (402, 'en', 'city'),
(403, 'ru', 'деревня'), (403, 'en', 'village');
insert into `entity_type`(`id`, `entity_id`, `entity_type_name_id`) values (400, 400, 402), (401, 400, 403);

insert into `string_culture`(`id`, `locale`, `value`) values (500, 'ru', 'Дом'), (500, 'en', 'Building');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (500, 'building', 500, '');
insert into `string_culture`(`id`, `locale`, `value`) values (501, 'ru', 'Номер дома'), (501, 'en', 'Building number');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (500, 500, 1, 501, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (502, 'ru', 'Корпус'), (502, 'en', 'Building corps.');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (501, 500, 0, 502, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (503, 'ru', 'Строение'), (503, 'en', 'Building structure');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (502, 500, 0, 503, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (504, 'ru', 'Улица'), (504, 'en', 'Street');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (503, 500, 0, 504, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (505, 'ru', 'Район'), (505, 'en', 'District');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (504, 500, 0, 505, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (500, 500, 'string_culture');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (501, 501, 'string_culture');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (502, 502, 'string_culture');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (503, 503, 'street');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (504, 504, 'district');

insert into `string_culture`(`id`, `locale`, `value`) values (600, 'ru', 'Район'), (600, 'en', 'District');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (600, 'district', 600, '');
insert into `string_culture`(`id`, `locale`, `value`) values (601, 'ru', 'Наименование района'), (601, 'en', 'District name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (600, 600, 1, 601, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (600, 600, 'string_culture');

insert into `string_culture`(`id`, `locale`, `value`) values (700, 'ru', 'Регион'), (700, 'en', 'Region');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (700, 'region', 700, '');
insert into `string_culture`(`id`, `locale`, `value`) values (701, 'ru', 'Наименование региона'), (701, 'en', 'Region name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (700, 700, 1, 701, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (700, 700, 'string_culture');

insert into `string_culture`(`id`, `locale`, `value`) values (800, 'ru', 'Страна'), (800, 'en', 'Country');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (800, 'country', 800, '');
insert into `string_culture`(`id`, `locale`, `value`) values (801, 'ru', 'Наименование страны'), (801, 'en', 'Country name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (800, 800, 1, 801, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (800, 800, 'string_culture');

insert into `string_culture`(`id`, `locale`, `value`) values (900, 'ru', 'Организация'), (900, 'en', 'Organization');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (900, 'organization', 900, '');
insert into `string_culture`(`id`, `locale`, `value`) values (901, 'ru', 'Наименование'), (901, 'en', 'Name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (900, 900, 1, 901, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (902, 'ru', 'Короткий код района, в котором расположена организация'), (902, 'en', 'Short name of organization''s district');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (901, 900, 1, 902, 1);
insert into `string_culture`(`id`, `locale`, `value`) values (903, 'ru', 'Уникальный код организации'), (903, 'en', 'Unique organization code');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (902, 900, 1, 903, 1);

insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (900, 900, 'string_culture');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (901, 900, 'string');
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (902, 900, 'string');
insert into `string_culture`(`id`, `locale`, `value`) values
(904, 'ru', 'Отдел субсидий ОСЗН'), (904, 'en', 'Отдел субсидий ОСЗН'),
(905, 'ru', 'ПУ'), (905, 'en', 'ПУ'),
(906, 'ru', 'Обслуживающая организация'), (906, 'en', 'Обслуживающая организация');
insert into `entity_type`(`id`, `entity_id`, `entity_type_name_id`) values (900, 900, 904), (901, 900, 905), (902, 900, 906);

insert into `string_culture`(`id`, `locale`, `value`) values (1000, 'ru', 'Пользователь'), (1000, 'en', 'User');
insert into `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) values (1000, 'user_info', 1000, '');
insert into `string_culture`(`id`, `locale`, `value`) values (1001, 'ru', 'Фамилия'), (1001, 'en', 'Last Name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1000, 1000, 1, 1001, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1000, 1000, 'string');
insert into `string_culture`(`id`, `locale`, `value`) values (1002, 'ru', '�?мя'), (1002, 'en', 'First name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1001, 1000, 1, 1002, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1001, 1001, 'string');
insert into `string_culture`(`id`, `locale`, `value`) values (1003, 'ru', 'Отчество'), (1003, 'en', 'Middle name');
insert into `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) values (1002, 1000, 1, 1003, 1);
insert into `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) values (1002, 1002, 'string');


update `sequence` set `sequence_value` = 2000 where `sequence_name` = 'string_culture';