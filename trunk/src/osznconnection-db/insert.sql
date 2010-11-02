-- --------------------------------
-- Users
-- --------------------------------
INSERT INTO USER VALUE (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', NULL);
INSERT INTO usergroup VALUE (1, 'admin', 'ADMINISTRATORS');
INSERT INTO USER VALUE (2, 'ANONYMOUS', 'ANONYMOUS', NULL);

-- --------------------------------
-- Locale
-- --------------------------------

INSERT INTO `locales`(`locale`, `system`) VALUES ('ru', 1);
INSERT INTO `locales`(`locale`, `system`) VALUES ('uk', 0);

-- --------------------------------
-- Sequence
-- --------------------------------

INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES
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
('ownership',1), ('ownership_string_culture',1),
('privilege',1), ('privilege_string_culture',1);

-- --------------------------------
-- Apartment
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (100, 'ru', 'Квартира'), (100, 'uk', 'Квартира');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (100, 'apartment', 100, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (101, 'ru', UPPER('Наименование квартиры')), (101, 'uk', UPPER('Найменування квартири'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (100, 100, 1, 101, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (100, 100, UPPER('string_culture'));

-- --------------------------------
-- Room
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (200, 'ru', 'Комната'), (200, 'uk', 'Кімната');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (200, 'room', 200, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (201, 'ru', UPPER('Наименование комнаты')), (201, 'uk', UPPER('Найменування кімнати'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (200, 200, 1, 201, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (200, 200, UPPER('string_culture'));

-- --------------------------------
-- Street
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (300, 'ru', 'Улица'), (300, 'uk', 'Вулиця');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (300, 'street', 300, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (301, 'ru', UPPER('Наименование улицы')), (301, 'uk', UPPER('Найменування вулиці'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (300, 300, 1, 301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (300, 300, UPPER('string_culture'));

-- --------------------------------
-- City
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (400, 'ru', 'Населенный пункт'), (400, 'uk', 'Населений пункт');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (400, 'city', 400, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (401, 'ru', UPPER('Наименование населенного пункта')), (401, 'uk', UPPER('Найменування населеного пункту'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (400, 400, 1, 401, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (400, 400, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (402, 'ru', UPPER('Тип населенного пункта')),
                                                             (402, 'uk', UPPER('Тип населенного пункта'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (401, 400, 1, 402, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (401, 401, 'city_type');

-- --------------------------------
-- City Type
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1300, 'ru', 'Тип нас. пункта'), (1300, 'uk', 'Тип населенного пункта');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1300, 'city_type', 1300, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1301, 'ru', UPPER('Название')), (1301, 'uk', UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1300, 1300, 1, 1301, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1300, 1300, UPPER('string_culture'));

INSERT INTO `city_type_string_culture`(`id`, `locale`, `value`) VALUES (1,'ru','ГОРОД'), (1,'uk','МIСТО'), (2,'ru','ДЕРЕВНЯ'), (2,'uk','СЕЛО');
INSERT INTO `city_type` (`object_id`) VALUES (1), (2);
INSERT INTO `city_type_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES (1,1,1300,1,1300),
(1,2,1300,2,1300);

-- --------------------------------
-- Building
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (500, 'ru', 'Дом'), (500, 'uk', 'Будинок');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (500, 'building', 500, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (501, 'ru', UPPER('Номер дома')), (501, 'uk', UPPER('Номер будинку'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (500, 500, 1, 501, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (502, 'ru', UPPER('Корпус')), (502, 'uk', UPPER('Корпус'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (501, 500, 0, 502, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (503, 'ru', UPPER('Строение')), (503, 'uk', UPPER('Будова'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (502, 500, 0, 503, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (504, 'ru', UPPER('Улица')), (504, 'uk', UPPER('Вулиця'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (503, 500, 0, 504, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (505, 'ru', UPPER('Район')), (505, 'uk', UPPER('Район'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (504, 500, 0, 505, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (500, 500, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (501, 501, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (502, 502, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (503, 503, 'street');
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (504, 504, 'district');

-- --------------------------------
-- District
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (600, 'ru', 'Район'), (600, 'uk', 'Район');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (600, 'district', 600, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (601, 'ru', UPPER('Наименование района')), (601, 'uk', UPPER('Найменування району'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (600, 600, 1, 601, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (600, 600, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (602, 'ru', UPPER('Код района')), (602, 'uk', UPPER('Код району'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (601, 600, 1, 602, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (601, 601, UPPER('string'));

-- --------------------------------
-- Region
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (700, 'ru', 'Регион'), (700, 'uk', 'Регіон');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (700, 'region', 700, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (701, 'ru', UPPER('Наименование региона')), (701, 'uk', UPPER('Найменування регіону'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (700, 700, 1, 701, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (700, 700, UPPER('string_culture'));

-- --------------------------------
-- Country
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (800, 'ru', 'Страна'), (800, 'uk', 'Країна');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (800, 'country', 800, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (801, 'ru', UPPER('Наименование страны')), (801, 'uk', UPPER('Найменування країни'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (800, 800, 1, 801, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (800, 800, UPPER('string_culture'));

-- --------------------------------
-- Organization
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (900, 'ru', 'Организация'), (900, 'uk', 'Організація');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (900, 'organization', 900, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (901, 'ru', UPPER('Наименование организации')), (901, 'uk', UPPER('Найменування організації'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (900, 900, 1, 901, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (902, 'ru', UPPER('Уникальный код организации')), (902, 'uk', UPPER('Унікальний код організації'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (901, 900, 1, 902, 1);
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (903, 'ru', UPPER('Район')), (903, 'uk', UPPER('Район'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (902, 900, 0, 903, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (900, 900, UPPER('string_culture'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (901, 901, UPPER('string'));
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (902, 902, 'district');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES
(904, 'ru', UPPER('ОСЗН')), (904, 'uk', UPPER('ОСЗН')),
(905, 'ru', UPPER('Центр начислений')), (905, 'uk', UPPER('Центр нарахувань'));
INSERT INTO `entity_type`(`id`, `entity_id`, `entity_type_name_id`) VALUES (900, 900, 904), (901, 900, 905);

-- --------------------------------
-- User
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1000, 'ru', 'Пользователь'), (1000, 'uk', 'Користувач');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1000, 'user_info', 1000, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1001, 'ru', UPPER('Фамилия')), (1001, 'uk', UPPER('Прізвище'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1000, 1000, 1, 1001, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1000, 1000, UPPER('string'));
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1002, 'ru', UPPER('Имя')), (1002, 'uk', UPPER('Ім\'я'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1001, 1000, 1, 1002, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1001, 1001, UPPER('string'));
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1003, 'ru', UPPER('Отчество')), (1003, 'uk', UPPER('По батькові'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1002, 1000, 1, 1003, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1002, 1002, UPPER('string'));

-- --------------------------------
-- Ownership
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1100, 'ru', 'Форма собственности'), (1100, 'uk', 'Форма власності');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1100, 'ownership', 1100, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1101, 'ru', UPPER('Название')), (1101, 'uk', UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1100, 1100, 1, 1101, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1100, 1100, UPPER('string_culture'));

-- Forms of ownerships
INSERT INTO ownership(object_id) VALUES (1),(2),(3),(4),(5),(6);
INSERT INTO ownership_string_culture(id, locale, VALUE) VALUES (1, 'ru', UPPER('мiсцевих Рад')), (1,'uk',UPPER('мiсцевих Рад')),
(2, 'ru', UPPER('кооперативна')), (2, 'uk', UPPER('кооперативна')), (3, 'ru', UPPER('вiдомча')), (3,'uk',UPPER('вiдомча')),
(4, 'ru', UPPER('громадська')), (4,'uk',UPPER('громадська')), (5, 'ru', UPPER('приватна')), (5,'uk',UPPER('приватна')),
(6, 'ru', UPPER('приватизована')), (6,'uk',UPPER('приватизована'));
INSERT INTO ownership_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) VALUES
(1,1,1100,1,1100),(1,2,1100,2,1100),(1,3,1100,3,1100),(1,4,1100,4,1100),(1,5,1100,5,1100),(1,6,1100,6,1100);

-- --------------------------------
-- Privilege
-- --------------------------------

INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1200, 'ru', 'Льгота'), (1200, 'uk', 'Привілей');
INSERT INTO `entity`(`id`, `entity_table`, `entity_name_id`, `strategy_factory`) VALUES (1200, 'privilege', 1200, '');
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1201, 'ru', UPPER('Название')), (1201, 'uk', UPPER('Назва'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1200, 1200, 1, 1201, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1200, 1200, UPPER('string_culture'));
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES (1202, 'ru', UPPER('Код')), (1202, 'uk', UPPER('Код'));
INSERT INTO `entity_attribute_type`(`id`, `entity_id`, `mandatory`, `attribute_type_name_id`, `system`) VALUES (1201, 1200, 1, 1202, 1);
INSERT INTO `entity_attribute_value_type`(`id`, `attribute_type_id`, `attribute_value_type`) VALUES (1201, 1201, UPPER('string'));

-- Privileges
INSERT INTO privilege(object_id) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),
(61),(62),(63),(64),(65),(66),(67),(68),(69),(70),(71),(72),(73),(74),(75),(76),(77),(78),(79),(80),(81),(82),(83),(84),(85),(86),(87),(88),(89),(90),
(91),(92),(93),(94),(95),(96),(97),(98),(99),(100),(101),(102),(103),(104);
INSERT INTO privilege_string_culture(id, locale, VALUE) VALUES
(1,'ru',UPPER('УЧАСТНИК БОЕВЫХ ДЕЙСТВИЙ')), (1,'uk',UPPER('УЧАСТНИК БОЕВЫХ ДЕЙСТВИЙ')), (2,'ru',UPPER('1')),
(3,'ru',UPPER('УЧАСТНИК ВОЙНЫ')), (3,'uk',UPPER('УЧАСТНИК ВОЙНЫ')), (4,'ru',UPPER('2')),
(5,'ru',UPPER('ЧЛЕН СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО ВЕТЕРАНА ВОЙНЫ')), (5,'uk',UPPER('ЧЛЕН СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО ВЕТЕРАНА ВОЙНЫ')), (6,'ru',UPPER('3')),
(7,'ru',UPPER('ИНВАЛИД ВОЙНЫ ПЕРВОЙ ГРУППЫ')), (7,'uk',UPPER('ИНВАЛИД ВОЙНЫ ПЕРВОЙ ГРУППЫ')), (8,'ru',UPPER('11')),
(9,'ru',UPPER('ИНВАЛИД ВОЙНЫ ВТОРОЙ ГРУППЫ')), (9,'uk',UPPER('ИНВАЛИД ВОЙНЫ ВТОРОЙ ГРУППЫ')), (10,'ru',UPPER('12')),
(11,'ru',UPPER('ИНВАЛИД ВОЙНЫ ТРЕТЬЕЙ ГРУППЫ')), (11,'uk',UPPER('ИНВАЛИД ВОЙНЫ ТРЕТЬЕЙ ГРУППЫ')), (12,'ru',UPPER('13')),
(13,'ru',UPPER('РЕБЕНОК ВОЙНЫ')), (13,'uk',UPPER('РЕБЕНОК ВОЙНЫ')), (14,'ru',UPPER('15')),
(15,'ru',UPPER('ЛИЦО С ОСОБЫМИ ЗАСЛУГАМИ')), (15,'uk',UPPER('ЛИЦО С ОСОБЫМИ ЗАСЛУГАМИ')), (16,'ru',UPPER('20')),
(17,'ru',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (17,'uk',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (18,'ru',UPPER('22')),
(19,'ru',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (19,'uk',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (20,'ru',UPPER('23')),
(21,'ru',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (21,'uk',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (22,'ru',UPPER('26')),
(23,'ru',UPPER('ЛИЦО С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (23,'uk',UPPER('ЛИЦО С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (24,'ru',UPPER('30')),
(25,'ru',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (25,'uk',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (26,'ru',UPPER('32')),
(27,'ru',UPPER('ВЕТЕРАН ТРУДА')), (27,'uk',UPPER('ВЕТЕРАН ТРУДА')), (28,'ru',UPPER('33')),
(29,'ru',UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ')), (29,'uk',UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ')), (30,'ru',UPPER('34')),
(31,'ru',UPPER('МНОГОДЕТНЫЕ СЕМЬИ')), (31,'uk',UPPER('МНОГОДЕТНЫЕ СЕМЬИ')), (32,'ru',UPPER('35')),
(33,'ru',UPPER('ЧЛЕН  МНОГОДЕТНОЙ СЕМЬИ')), (33,'uk',UPPER('ЧЛЕН  МНОГОДЕТНОЙ СЕМЬИ')), (34,'ru',UPPER('36')),
(35,'ru',UPPER('ВЕТЕРАН СЛУЖБЫ ГРАЖДАНСКОЙ ЗИЩИТЫ')), (35,'uk',UPPER('ВЕТЕРАН СЛУЖБЫ ГРАЖДАНСКОЙ ЗИЩИТЫ')), (36,'ru',UPPER('37')),
(37,'ru',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ КРИМ.-ИСПОЛНИТЕЛЬНОЙ СЛУЖБЫ')), (37,'uk',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ КРИМ.-ИСПОЛНИТЕЛЬНОЙ СЛУЖБЫ')), (38,'ru',UPPER('39')),
(39,'ru',UPPER('СЕЛЬСКИЙ ПЕДАГОГ НА ПЕНСИИ')), (39,'uk',UPPER('СЕЛЬСКИЙ ПЕДАГОГ НА ПЕНСИИ')), (40,'ru',UPPER('40')),
(41,'ru',UPPER('СЕЛЬСКИЙ БИБЛИОТЕКАРЬ НА ПЕНСИИ')), (41,'uk',UPPER('СЕЛЬСКИЙ БИБЛИОТЕКАРЬ НА ПЕНСИИ')), (42,'ru',UPPER('41')),
(43,'ru',UPPER('СЕЛЬСКИЙ СПЕЦИАЛИСТ ПО ЗАЩИТЕ РАСТЕНИЙ НА ПЕНСИИ')), (43,'uk',UPPER('СЕЛЬСКИЙ СПЕЦИАЛИСТ ПО ЗАЩИТЕ РАСТЕНИЙ НА ПЕНСИИ')), (44,'ru',UPPER('42')),
(45,'ru',UPPER('СЕЛЬСКИЙ МЕДИК НА ПЕНСИИ')), (45,'uk',UPPER('СЕЛЬСКИЙ МЕДИК НА ПЕНСИИ')), (46,'ru',UPPER('43')),
(47,'ru',UPPER('СУДЬЯ В ОТСТАВКЕ')), (47,'uk',UPPER('СУДЬЯ В ОТСТАВКЕ')), (48,'ru',UPPER('47')),
(49,'ru',UPPER('СЛЕДОВАТЕЛЬ ПРОКУРАТУРЫ НА ПЕНСИИ')), (49,'uk',UPPER('СЛЕДОВАТЕЛЬ ПРОКУРАТУРЫ НА ПЕНСИИ')), (50,'ru',UPPER('49')),
(51,'ru',UPPER('НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (51,'uk',UPPER('НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (52,'ru',UPPER('50')),
(53,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (53,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (54,'ru',UPPER('51')),
(55,'ru',UPPER('СЕЛЬСКИЙ НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (55,'uk',UPPER('СЕЛЬСКИЙ НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (56,'ru',UPPER('52')),
(57,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ СЕЛЬСКОГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (57,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ СЕЛЬСКОГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (58,'ru',UPPER('53')),
(59,'ru',UPPER('ВОЕННОСЛУЖАЩИЙ СБУ НА ПЕНСИИ')), (59,'uk',UPPER('ВОЕННОСЛУЖАЩИЙ СБУ НА ПЕНСИИ')), (60,'ru',UPPER('58')),
(61,'ru',UPPER('ЛИЦО (ЧАЭС) - 1 КАТЕГОРИЯ')), (61,'uk',UPPER('ЛИЦО (ЧАЭС) - 1 КАТЕГОРИЯ')), (62,'ru',UPPER('61')),
(63,'ru',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ЛИКВИДАТОР')), (63,'uk',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ЛИКВИДАТОР')), (64,'ru',UPPER('62')),
(65,'ru',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ПОТЕРПЕВШИЙ')), (65,'uk',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ПОТЕРПЕВШИЙ')), (66,'ru',UPPER('63')),
(67,'ru',UPPER('ЛИЦО (ЧАЭС) - 3 КАТЕГОРИЯ')), (67,'uk',UPPER('ЛИЦО (ЧАЭС) - 3 КАТЕГОРИЯ')), (68,'ru',UPPER('64')),
(69,'ru',UPPER('ЛИЦО (ЧАЭС) - 4 КАТЕГОРИЯ')), (69,'uk',UPPER('ЛИЦО (ЧАЭС) - 4 КАТЕГОРИЯ')), (70,'ru',UPPER('65')),
(71,'ru',UPPER('ЖЕНА/МУЖ (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (71,'uk',UPPER('ЖЕНА/МУЖ (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (72,'ru',UPPER('66')),
(73,'ru',UPPER('РЕБЕНОК (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (73,'uk',UPPER('РЕБЕНОК (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (74,'ru',UPPER('67')),
(75,'ru',UPPER('РЕБЕНОК (ЧАЭС) ПОТЕРПЕВШЕГО')), (75,'uk',UPPER('РЕБЕНОК (ЧАЭС) ПОТЕРПЕВШЕГО')), (76,'ru',UPPER('68')),
(77,'ru',UPPER('РЕБЕНОК (ЧАЭС) - ИНВАЛИД')), (77,'uk',UPPER('РЕБЕНОК (ЧАЭС) - ИНВАЛИД')), (78,'ru',UPPER('69')),
(79,'ru',UPPER('ЛИЦО (ЧАЭС), РАБОТАВШЕЕ ЗА ПРЕДЕЛАМИ ЗОНЫ ОТЧУЖДЕНИЯ (ЛИКВИДАЦИЯ ПОСЛЕДСТВИЙ АВАРИИ)')), (79,'uk',UPPER('ЛИЦО (ЧАЭС), РАБОТАВШЕЕ ЗА ПРЕДЕЛАМИ ЗОНЫ ОТЧУЖДЕНИЯ (ЛИКВИДАЦИЯ ПОСЛЕДСТВИЙ АВАРИИ)')), (80,'ru',UPPER('70')),
(81,'ru',UPPER('СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (81,'uk',UPPER('СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (82,'ru',UPPER('71')),
(83,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (83,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (84,'ru',UPPER('72')),
(85,'ru',UPPER('СЕЛЬСКИЙ СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (85,'uk',UPPER('СЕЛЬСКИЙ СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (86,'ru',UPPER('73')),
(87,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (87,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (88,'ru',UPPER('74')),
(89,'ru',UPPER('МИЛИЦИОНЕР НА ПЕНСИИ')), (89,'uk',UPPER('МИЛИЦИОНЕР НА ПЕНСИИ')), (90,'ru',UPPER('75')),
(91,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО МИЛИЦИОНЕРА')), (91,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО МИЛИЦИОНЕРА')), (92,'ru',UPPER('76')),
(93,'ru',UPPER('СЕЛЬСКИЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (93,'uk',UPPER('СЕЛЬСКИЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (94,'ru',UPPER('77')),
(95,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО МИЛИЦИОНЕРА')), (95,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО МИЛИЦИОНЕРА')), (96,'ru',UPPER('78')),
(97,'ru',UPPER('ВЕТЕРАН ВОИНСКОЙ СЛУЖБЫ')), (97,'uk',UPPER('ВЕТЕРАН ВОИНСКОЙ СЛУЖБЫ')), (98,'ru',UPPER('80')),
(99,'ru',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ВОИНСКОЙ СЛУЖБЫ')), (99,'uk',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ВОИНСКОЙ СЛУЖБЫ')), (100,'ru',UPPER('81')),
(101,'ru',UPPER('ЧЛЕН СЕМЬИ ВОЕННОСЛУЖАЩЕГО, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ СТАВШЕГО ИНВАЛИДОМ')), (101,'uk',UPPER('ЧЛЕН СЕМЬИ ВОЕННОСЛУЖАЩЕГО, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ СТАВШЕГО ИНВАЛИДОМ')), (102,'ru',UPPER('83')),
(103,'ru',UPPER('РОДИТЕЛИ ВОЕННОСЛУЖАЩЕГО, СТАВШЕГО ИНВАЛИДОМ')), (103,'uk',UPPER('РОДИТЕЛИ ВОЕННОСЛУЖАЩЕГО, СТАВШЕГО ИНВАЛИДОМ')), (104,'ru',UPPER('84')),
(105,'ru',UPPER('ВДОВА/ВДОВЕЦ ВОЕННОСЛУЖАЩЕГО И ЕГО ДЕТИ')), (105,'uk',UPPER('ВДОВА/ВДОВЕЦ ВОЕННОСЛУЖАЩЕГО И ЕГО ДЕТИ')), (106,'ru',UPPER('85')),
(107,'ru',UPPER('ЖЕНА/МУЖ ВОЕННОСЛУЖАЩЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ')), (107,'uk',UPPER('ЖЕНА/МУЖ ВОЕННОСЛУЖАЩЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ')), (108,'ru',UPPER('86')),
(109,'ru',UPPER('РОДИТЕЛИ ПОГИБШЕГО ВОЕННОСЛУЖАЩЕГО')), (109,'uk',UPPER('РОДИТЕЛИ ПОГИБШЕГО ВОЕННОСЛУЖАЩЕГО')), (110,'ru',UPPER('87')),
(111,'ru',UPPER('ИНВАЛИД ВОИНСКОЙ СЛУЖБЫ')), (111,'uk',UPPER('ИНВАЛИД ВОИНСКОЙ СЛУЖБЫ')), (112,'ru',UPPER('88')),
(113,'ru',UPPER('ВЕТЕРАН ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (113,'uk',UPPER('ВЕТЕРАН ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (114,'ru',UPPER('90')),
(115,'ru',UPPER('ВДОВА/ВДОВЕЦ, ВЕТЕРАНА ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (115,'uk',UPPER('ВДОВА/ВДОВЕЦ, ВЕТЕРАНА ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (116,'ru',UPPER('91')),
(117,'ru',UPPER('ПОЖАРНЫЙ НА ПЕНСИИ')), (117,'uk',UPPER('ПОЖАРНЫЙ НА ПЕНСИИ')), (118,'ru',UPPER('95')),
(119,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО ПОЖАРНОГО')), (119,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО ПОЖАРНОГО')), (120,'ru',UPPER('96')),
(121,'ru',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (121,'uk',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')),(122,'ru',UPPER('98')),
(123,'ru',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (123,'uk',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (124,'ru',UPPER('99')),
(125,'ru',UPPER('РЕАБИЛИТИРОВАННЫЕ, СТАВШИЕ ИНВАЛИДАМИ ВСЛЕДСТВИИ РЕПРЕССИЙ, ЛИБО ЯВЛЯЮЩИЕСЯ ПЕНСИОНЕРАМИ, ИМЕЮЩИМИ П')), (125,'uk',UPPER('РЕАБИЛИТИРОВАННЫЕ, СТАВШИЕ ИНВАЛИДАМИ ВСЛЕДСТВИИ РЕПРЕССИЙ, ЛИБО ЯВЛЯЮЩИЕСЯ ПЕНСИОНЕРАМИ, ИМЕЮЩИМИ П')), (126,'ru',UPPER('100')),
(127,'ru',UPPER('РЕБЕНОК-ИНВАЛИД')), (127,'uk',UPPER('РЕБЕНОК-ИНВАЛИД')), (128,'ru',UPPER('110')),
(129,'ru',UPPER('ИНВАЛИД 1 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (129,'uk',UPPER('ИНВАЛИД 1 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (130,'ru',UPPER('111')),
(131,'ru',UPPER('ИНВАЛИД 2 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (131,'uk',UPPER('ИНВАЛИД 2 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (132,'ru',UPPER('112')),
(133,'ru',UPPER('ИНВАЛИД 1 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (133,'uk',UPPER('ИНВАЛИД 1 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (134,'ru',UPPER('113')),
(135,'ru',UPPER('ИНВАЛИД 2 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (135,'uk',UPPER('ИНВАЛИД 2 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (136,'ru',UPPER('114')),
(137,'ru',UPPER('ИНВАЛИД 3 ГРУППЫ')), (137,'uk',UPPER('ИНВАЛИД 3 ГРУППЫ')), (138,'ru',UPPER('115')),
(139,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(1)')), (139,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(1)')), (140,'ru',UPPER('120')),
(141,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 1 ГРУППЫ')), (141,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 1 ГРУППЫ')), (142,'ru',UPPER('121')),
(143,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 2 ГРУППЫ')), (143,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 2 ГРУППЫ')), (144,'ru',UPPER('122')),
(145,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 3 ГРУППЫ')), (154,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 3 ГРУППЫ')), (146,'ru',UPPER('123')),
(147,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(3)')), (147,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(3)')), (148,'ru',UPPER('124')),
(149,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(4)')), (149,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(4)')), (150,'ru',UPPER('125')),
(151,'ru',UPPER('ГОРНЯКИ - НЕТРУДОСПОСОБНЫЕ РАБОТНИКИ')), (151,'uk',UPPER('ГОРНЯКИ - НЕТРУДОСПОСОБНЫЕ РАБОТНИКИ')), (152,'ru',UPPER('126')),
(153,'ru',UPPER('ГОРНЯКИ - НЕРАБОТАЮЩИЕ ПЕНСИОНЕРЫ')), (153,'uk',UPPER('ГОРНЯКИ - НЕРАБОТАЮЩИЕ ПЕНСИОНЕРЫ')), (154,'ru',UPPER('127')),
(155,'ru',UPPER('ГОРНЯКИ - ИНВАЛИДЫ')), (155,'uk',UPPER('ГОРНЯКИ - ИНВАЛИДЫ')), (156,'ru',UPPER('128')),
(157,'ru',UPPER('ГОРНЯКИ - СЕМЬИ ПОГИБШИХ ТРУЖЕНИКОВ')), (157,'uk',UPPER('ГОРНЯКИ - СЕМЬИ ПОГИБШИХ ТРУЖЕНИКОВ')), (158,'ru',UPPER('129')),
(159,'ru',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (159,'uk',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (160,'ru',UPPER('130')),
(161,'ru',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (161,'uk',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (162,'ru',UPPER('131')),
(163,'ru',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО СОТРУДНИКА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (163,'uk',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО СОТРУДНИКА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (164,'ru',UPPER('132')),
(165,'ru',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ СОТРУДНИКА ГРАЖДАНСКОЙ ОБОРОНЫ, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ')), (165,'uk',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ СОТРУДНИКА ГРАЖДАНСКОЙ ОБОРОНЫ, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ')), (166,'ru',UPPER('135')),
(167,'ru',UPPER('МАТЕРИ-ГЕРОИНИ')), (167,'uk',UPPER('МАТЕРИ-ГЕРОИНИ')), (168,'ru',UPPER('200')),
(169,'ru',UPPER('ДЕТИ-ИНВАЛИДЫ, ПРИКОВАННЫЕ К КРОВАТИ')), (169,'uk',UPPER('ДЕТИ-ИНВАЛИДЫ, ПРИКОВАННЫЕ К КРОВАТИ')), (170,'ru',UPPER('201')),
(171,'ru',UPPER('ДЕТИ-ИНВАЛИДЫ ДО 18 ЛЕТ, ГДЕ ОБА РОДИТЕЛИ ИНВАЛИДЫ')), (171,'uk',UPPER('ДЕТИ-ИНВАЛИДЫ ДО 18 ЛЕТ, ГДЕ ОБА РОДИТЕЛИ ИНВАЛИДЫ')), (172,'ru',UPPER('202')),
(173,'ru',UPPER('МНОГОДЕТНЫЕ СЕМЬИ (3 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (173,'uk',UPPER('МНОГОДЕТНЫЕ СЕМЬИ (3 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (174,'ru',UPPER('203')),
(175,'ru',UPPER('МАТЕРИ-ОДИНОЧКИ (2 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (175,'uk',UPPER('МАТЕРИ-ОДИНОЧКИ (2 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (176,'ru',UPPER('204')),
(177,'ru',UPPER('ДЕТИ-ИНВАЛИДЫ С ОНКОЛОГИЧЕСКИМИ ЗАБОЛЕВАНИЯМИ ДО 18 ЛЕТ')), (177,'uk',UPPER('ДЕТИ-ИНВАЛИДЫ С ОНКОЛОГИЧЕСКИМИ ЗАБОЛЕВАНИЯМИ ДО 18 ЛЕТ')), (178,'ru',UPPER('205')),
(179,'ru',UPPER('ИНВАЛИДЫ 1 ГРУППЫ, ПОЛУЧАЮЩИЕ СОЦИАЛЬНУЮ ПЕНСИЮ ИЛИ ГОСУДАРСТВЕННУЮ ПОМОЩЬ')), (179,'uk',UPPER('ИНВАЛИДЫ 1 ГРУППЫ, ПОЛУЧАЮЩИЕ СОЦИАЛЬНУЮ ПЕНСИЮ ИЛИ ГОСУДАРСТВЕННУЮ ПОМОЩЬ')), (180,'ru',UPPER('206')),
(181,'ru',UPPER('НЕРАБОТАЮЩИЕ РОДИТЕЛИ, ОСУЩЕСТВЛЯЮЩИЕ УХОД ЗА РЕБЕНКОМ-ИНВАЛИДОМ ДО 18 ЛЕТ')), (181,'uk',UPPER('НЕРАБОТАЮЩИЕ РОДИТЕЛИ, ОСУЩЕСТВЛЯЮЩИЕ УХОД ЗА РЕБЕНКОМ-ИНВАЛИДОМ ДО 18 ЛЕТ')), (182,'ru',UPPER('207')),
(183,'ru',UPPER('ИНВАЛИДЫ 1, 2 ГРУППЫ ПО ЗРЕНИЮ')), (183,'uk',UPPER('ИНВАЛИДЫ 1, 2 ГРУППЫ ПО ЗРЕНИЮ')), (184,'ru',UPPER('208')),
(185,'ru',UPPER('СЕМЬИ ДЕТЕЙ ДО 18 ЛЕТ, БОЛЬНЫХ ДЦП')), (185,'uk',UPPER('СЕМЬИ ДЕТЕЙ ДО 18 ЛЕТ, БОЛЬНЫХ ДЦП')), (186,'ru',UPPER('209')),
(187,'ru',UPPER('ГРАЖДАНЕ, РЕАБИЛИТИРОВАННЫЕ СОГЛАСНО')), (187,'uk',UPPER('ГРАЖДАНЕ, РЕАБИЛИТИРОВАННЫЕ СОГЛАСНО')), (188,'ru',UPPER('210')),
(189,'ru',UPPER('СЕМЬИ ПОГИБШИХ (РЯДОВОЙ СОСТАВ) ПРИ ПРОХОЖДЕНИИ СРОЧНОЙ ВОИНСКОЙ СЛУЖБЫ, ИСПОЛНЯВШИХ СВОЙ ДОЛГ В МИ')), (189,'uk',UPPER('СЕМЬИ ПОГИБШИХ (РЯДОВОЙ СОСТАВ) ПРИ ПРОХОЖДЕНИИ СРОЧНОЙ ВОИНСКОЙ СЛУЖБЫ, ИСПОЛНЯВШИХ СВОЙ ДОЛГ В МИ')), (190,'ru',UPPER('211')),
(191,'ru',UPPER('ПРИЕМНЫЕ СЕМЬИ')), (191,'uk',UPPER('ПРИЕМНЫЕ СЕМЬИ')), (192,'ru',UPPER('212')),
(193,'ru',UPPER('ДВОРНИКИ')), (193,'uk',UPPER('ДВОРНИКИ')), (194,'ru',UPPER('300')),
(195,'ru',UPPER('АВАРИЙНО-ДИСПЕТЧЕРСКАЯ СЛУЖБА')), (195,'uk',UPPER('АВАРИЙНО-ДИСПЕТЧЕРСКАЯ СЛУЖБА')), (196,'ru',UPPER('301')),
(197,'ru',UPPER('ПРИЕМНЫЕ СЕМЬИ')), (197,'uk',UPPER('ПРИЕМНЫЕ СЕМЬИ')), (198,'ru',UPPER('303')),
(199,'ru',UPPER('СОЦИАЛЬНЫЕ РАБОЧИЕ')), (199,'uk',UPPER('СОЦИАЛЬНЫЕ РАБОЧИЕ')), (200,'ru',UPPER('304')),
(201,'ru',UPPER('УХОД ЗА ИНВАЛИДОМ 1 ГРУППЫ ВОВ')), (201,'uk',UPPER('УХОД ЗА ИНВАЛИДОМ 1 ГРУППЫ ВОВ')), (202,'ru',UPPER('305')),
(203,'ru',UPPER('РАБОТНИКИ ХКП "ГОРЭЛЕКТРОТРАНС"')), (203,'uk',UPPER('РАБОТНИКИ ХКП "ГОРЭЛЕКТРОТРАНС"')), (204,'ru',UPPER('306')),
(205,'ru',UPPER('АФГАНИСТАН')), (205,'uk',UPPER('АФГАНИСТАН')), (206,'ru',UPPER('633')),
(207,'ru',UPPER('ВЕТЕРАН НАЛОГОВОЙ МИЛИЦИИ')), (207,'uk',UPPER('ВЕТЕРАН НАЛОГОВОЙ МИЛИЦИИ')), (208,'ru',UPPER('45'));
INSERT INTO privilege_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) VALUES
(1,1,1200,1,1200),(1,1,1201,2,1201),
(1,2,1200,3,1200),(1,2,1201,4,1201),
(1,3,1200,5,1200),(1,3,1201,6,1201),
(1,4,1200,7,1200),(1,4,1201,8,1201),
(1,5,1200,9,1200),(1,5,1201,10,1201),
(1,6,1200,11,1200),(1,6,1201,12,1201),
(1,7,1200,13,1200),(1,7,1201,14,1201),
(1,8,1200,15,1200),(1,8,1201,16,1201),
(1,9,1200,17,1200),(1,9,1201,18,1201),
(1,10,1200,19,1200),(1,10,1201,20,1201),
(1,11,1200,21,1200),(1,11,1201,22,1201),
(1,12,1200,23,1200),(1,12,1201,24,1201),
(1,13,1200,25,1200),(1,13,1201,26,1201),
(1,14,1200,27,1200),(1,14,1201,28,1201),
(1,15,1200,29,1200),(1,15,1201,30,1201),
(1,16,1200,31,1200),(1,16,1201,32,1201),
(1,17,1200,33,1200),(1,17,1201,34,1201),
(1,18,1200,35,1200),(1,18,1201,36,1201),
(1,19,1200,37,1200),(1,19,1201,38,1201),
(1,20,1200,39,1200),(1,20,1201,40,1201),
(1,21,1200,40,1200),(1,21,1201,42,1201),
(1,22,1200,43,1200),(1,22,1201,44,1201),
(1,23,1200,45,1200),(1,23,1201,46,1201),
(1,24,1200,47,1200),(1,24,1201,48,1201),
(1,25,1200,49,1200),(1,25,1201,50,1201),
(1,26,1200,51,1200),(1,26,1201,52,1201),
(1,27,1200,53,1200),(1,27,1201,54,1201),
(1,28,1200,55,1200),(1,28,1201,56,1201),
(1,29,1200,57,1200),(1,29,1201,58,1201),
(1,30,1200,59,1200),(1,30,1201,60,1201),
(1,31,1200,61,1200),(1,31,1201,62,1201),
(1,32,1200,63,1200),(1,32,1201,64,1201),
(1,33,1200,65,1200),(1,33,1201,66,1201),
(1,34,1200,67,1200),(1,34,1201,68,1201),
(1,35,1200,69,1200),(1,35,1201,70,1201),
(1,36,1200,71,1200),(1,36,1201,72,1201),
(1,37,1200,73,1200),(1,37,1201,74,1201),
(1,38,1200,75,1200),(1,38,1201,76,1201),
(1,39,1200,77,1200),(1,39,1201,78,1201),
(1,40,1200,79,1200),(1,40,1201,80,1201),
(1,41,1200,81,1200),(1,41,1201,82,1201),
(1,42,1200,83,1200),(1,42,1201,84,1201),
(1,43,1200,85,1200),(1,43,1201,86,1201),
(1,44,1200,87,1200),(1,44,1201,88,1201),
(1,45,1200,89,1200),(1,45,1201,90,1201),
(1,46,1200,91,1200),(1,46,1201,92,1201),
(1,47,1200,93,1200),(1,47,1201,94,1201),
(1,48,1200,95,1200),(1,48,1201,96,1201),
(1,49,1200,97,1200),(1,49,1201,98,1201),
(1,50,1200,99,1200),(1,50,1201,100,1201),
(1,51,1200,101,1200),(1,51,1201,102,1201),
(1,52,1200,103,1200),(1,52,1201,104,1201),
(1,53,1200,105,1200),(1,53,1201,106,1201),
(1,54,1200,107,1200),(1,54,1201,108,1201),
(1,55,1200,109,1200),(1,55,1201,110,1201),
(1,56,1200,111,1200),(1,56,1201,112,1201),
(1,57,1200,113,1200),(1,57,1201,114,1201),
(1,58,1200,115,1200),(1,58,1201,116,1201),
(1,59,1200,117,1200),(1,59,1201,118,1201),
(1,60,1200,119,1200),(1,60,1201,120,1201),
(1,61,1200,121,1200),(1,61,1201,122,1201),
(1,62,1200,123,1200),(1,62,1201,124,1201),
(1,63,1200,125,1200),(1,63,1201,126,1201),
(1,64,1200,127,1200),(1,64,1201,128,1201),
(1,65,1200,129,1200),(1,65,1201,130,1201),
(1,66,1200,131,1200),(1,66,1201,132,1201),
(1,67,1200,133,1200),(1,67,1201,134,1201),
(1,68,1200,135,1200),(1,68,1201,136,1201),
(1,69,1200,137,1200),(1,69,1201,138,1201),
(1,70,1200,139,1200),(1,70,1201,140,1201),
(1,71,1200,141,1200),(1,71,1201,142,1201),
(1,72,1200,143,1200),(1,72,1201,144,1201),
(1,73,1200,145,1200),(1,73,1201,146,1201),
(1,74,1200,147,1200),(1,74,1201,148,1201),
(1,75,1200,149,1200),(1,75,1201,150,1201),
(1,76,1200,151,1200),(1,76,1201,152,1201),
(1,77,1200,153,1200),(1,77,1201,154,1201),
(1,78,1200,155,1200),(1,78,1201,156,1201),
(1,79,1200,157,1200),(1,79,1201,158,1201),
(1,80,1200,159,1200),(1,80,1201,160,1201),
(1,81,1200,161,1200),(1,81,1201,162,1201),
(1,82,1200,163,1200),(1,82,1201,164,1201),
(1,83,1200,165,1200),(1,83,1201,166,1201),
(1,84,1200,167,1200),(1,84,1201,168,1201),
(1,85,1200,169,1200),(1,85,1201,170,1201),
(1,86,1200,171,1200),(1,86,1201,172,1201),
(1,87,1200,173,1200),(1,87,1201,174,1201),
(1,88,1200,175,1200),(1,88,1201,176,1201),
(1,89,1200,177,1200),(1,89,1201,178,1201),
(1,90,1200,179,1200),(1,90,1201,180,1201),
(1,91,1200,181,1200),(1,91,1201,182,1201),
(1,92,1200,183,1200),(1,92,1201,184,1201),
(1,93,1200,185,1200),(1,93,1201,186,1201),
(1,94,1200,187,1200),(1,94,1201,188,1201),
(1,95,1200,189,1200),(1,95,1201,190,1201),
(1,96,1200,191,1200),(1,96,1201,192,1201),
(1,97,1200,193,1200),(1,97,1201,194,1201),
(1,98,1200,195,1200),(1,98,1201,196,1201),
(1,99,1200,197,1200),(1,99,1201,198,1201),
(1,100,1200,199,1200),(1,100,1201,200,1201),
(1,101,1200,201,1200),(1,101,1201,202,1201),
(1,102,1200,203,1200),(1,102,1201,204,1201),
(1,103,1200,205,1200),(1,103,1201,206,1201),
(1,104,1200,207,1200),(1,104,1201,208,1201);

-- Status descriptions
INSERT INTO `status_description`(`code`, `name`) VALUES
(1,'Новый'), (2,'Пропущен'), (3,'Загружается'), (4,'Ошибка загрузки'), (5,'Загружен'), (6,'Связывается'), (7,'Связано с ошибками'), (8,'Связан'),
(9,'Выгружается'), (10,'Ошибка выгрузки'), (11,'Выгружен'), (12,'Обрабатывается'), (13,'Обработан с ошибками'), (14,'Обработан'),
(100,'Поле не найдено'), (101,'Недопустимый тип поля'), (102,'Недопустимый размер поля'), (103,'Файл уже загружен'), (104,'Отмена загрузки'),
(105,'Ошибка базы данных'), (106,'Неверный формат'), (107,'Критическая ошибка'), (108,'Отмена выгрузки'), (109,'Объединенный файл не найден'),
(200,'Населенный пункт не разрешен локально'), (201,'Улица не разрешена локально'), (202,'Дом не разрешен локально'),
(203,'Квартира не разрешена локально'), (204,'Адрес откорректирован'), (205,'Не найден населенный пункт в ц.н.'), (206,'Не найден район в ц.н.'),
(207,'Не найден тип улицы в ц.н.'), (208,'Не найдена улица в ц.н.'), (209,'Не найден дом в ц.н.'), (210,'Не найден корпус дома в ц.н.'),
(211,'Не найдена квартира в ц.н.'), (212,'Номер личного счета не разрешён'), (213,'Больше одного личного счета'), (214,'Номер личного счета разрешен'),
(215,'Запись обработана'), (216,'Тариф на оплату жилья не найден в справочнике тарифов'), (217,'Неверный номер личного счета'),
(218,'Льгота не найдена в таблице коррекций'), (219,'Запись выгружена');

-- Itself organization
INSERT INTO `organization`(`object_id`) VALUES (0);
INSERT INTO `organization_string_culture`(`id`, `locale`, `value`) VALUES
(1, 'ru', UPPER('Модуль №1')), (1,'uk',UPPER('Модуль №1')), (2, 'ru', UPPER('0'));
INSERT INTO `organization_attribute`(`attribute_id`, `object_id`, `attribute_type_id`, `value_id`, `value_type_id`) VALUES
(1,0,900,1,900), (1,0,901,2,901), (1,0,902,null,902);

