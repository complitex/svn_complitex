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
INSERT INTO `string_culture`(`id`, `locale`, `value`) VALUES
(402, 'ru', UPPER('город')), (402, 'uk', UPPER('місто')),
(403, 'ru', UPPER('деревня')), (403, 'uk', UPPER('село'));
INSERT INTO `entity_type`(`id`, `entity_id`, `entity_type_name_id`) VALUES (400, 400, 402), (401, 400, 403);

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

-- Privileges
INSERT INTO privilege(object_id) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
(31),(32),(33),(34),(35),(36),(37),(38),(39),(40),(41),(42),(43),(44),(45),(46),(47),(48),(49),(50),(51),(52),(53),(54),(55),(56),(57),(58),(59),(60),
(61),(62),(63),(64),(65),(66),(67),(68),(69),(70),(71),(72),(73),(74),(75),(76),(77),(78),(79),(80),(81),(82),(83),(84),(85),(86),(87),(88),(89),(90),
(91),(92),(93),(94),(95),(96),(97),(98),(99),(100),(101),(102),(103),(104);
INSERT INTO privilege_string_culture(id, locale, VALUE) VALUES
(1,'ru',UPPER('УЧАСТНИК БОЕВЫХ ДЕЙСТВИЙ')), (1,'uk',UPPER('УЧАСТНИК БОЕВЫХ ДЕЙСТВИЙ')),
(2,'ru',UPPER('УЧАСТНИК ВОЙНЫ')), (2,'uk',UPPER('УЧАСТНИК ВОЙНЫ')),
(3,'ru',UPPER('ЧЛЕН СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО ВЕТЕРАНА ВОЙНЫ')), (3,'uk',UPPER('ЧЛЕН СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО ВЕТЕРАНА ВОЙНЫ')),
(4,'ru',UPPER('ИНВАЛИД ВОЙНЫ ПЕРВОЙ ГРУППЫ')), (4,'uk',UPPER('ИНВАЛИД ВОЙНЫ ПЕРВОЙ ГРУППЫ')),
(5,'ru',UPPER('ИНВАЛИД ВОЙНЫ ВТОРОЙ ГРУППЫ')), (5,'uk',UPPER('ИНВАЛИД ВОЙНЫ ВТОРОЙ ГРУППЫ')),
(6,'ru',UPPER('ИНВАЛИД ВОЙНЫ ТРЕТЬЕЙ ГРУППЫ')), (6,'uk',UPPER('ИНВАЛИД ВОЙНЫ ТРЕТЬЕЙ ГРУППЫ')),
(7,'ru',UPPER('РЕБЕНОК ВОЙНЫ')), (7,'uk',UPPER('РЕБЕНОК ВОЙНЫ')),
(8,'ru',UPPER('ЛИЦО С ОСОБЫМИ ЗАСЛУГАМИ')), (8,'uk',UPPER('ЛИЦО С ОСОБЫМИ ЗАСЛУГАМИ')),
(9,'ru',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (9,'uk',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')),
(10,'ru',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')), (10,'uk',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ЗАСЛУГАМИ')),
(11,'ru',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (11,'uk',UPPER('ВДОВА/ВДОВЕЦ ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')),
(12,'ru',UPPER('ЛИЦО С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (12,'uk',UPPER('ЛИЦО С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')),
(13,'ru',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')), (13,'uk',UPPER('РОДИТЕЛИ УМЕРШЕГО ЛИЦА С ОСОБЫМИ ТРУДОВЫМИ ЗАСЛУГАМИ')),
(14,'ru',UPPER('ВЕТЕРАН ТРУДА')), (14,'uk',UPPER('ВЕТЕРАН ТРУДА')),
(15,'ru',UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ')), (15,'uk',UPPER('ПЕНСИОНЕР ПО ВОЗРАСТУ')),
(16,'ru',UPPER('МНОГОДЕТНЫЕ СЕМЬИ')), (16,'uk',UPPER('МНОГОДЕТНЫЕ СЕМЬИ')),
(17,'ru',UPPER('ЧЛЕН  МНОГОДЕТНОЙ СЕМЬИ')), (17,'uk',UPPER('ЧЛЕН  МНОГОДЕТНОЙ СЕМЬИ')),
(18,'ru',UPPER('ВЕТЕРАН СЛУЖБЫ ГРАЖДАНСКОЙ ЗИЩИТЫ')), (18,'uk',UPPER('ВЕТЕРАН СЛУЖБЫ ГРАЖДАНСКОЙ ЗИЩИТЫ')),
(19,'ru',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ КРИМ.-ИСПОЛНИТЕЛЬНОЙ СЛУЖБЫ')), (19,'uk',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ КРИМ.-ИСПОЛНИТЕЛЬНОЙ СЛУЖБЫ')),
(20,'ru',UPPER('СЕЛЬСКИЙ ПЕДАГОГ НА ПЕНСИИ')), (20,'uk',UPPER('СЕЛЬСКИЙ ПЕДАГОГ НА ПЕНСИИ')),
(21,'ru',UPPER('СЕЛЬСКИЙ БИБЛИОТЕКАРЬ НА ПЕНСИИ')), (21,'uk',UPPER('СЕЛЬСКИЙ БИБЛИОТЕКАРЬ НА ПЕНСИИ')),
(22,'ru',UPPER('СЕЛЬСКИЙ СПЕЦИАЛИСТ ПО ЗАЩИТЕ РАСТЕНИЙ НА ПЕНСИИ')), (22,'uk',UPPER('СЕЛЬСКИЙ СПЕЦИАЛИСТ ПО ЗАЩИТЕ РАСТЕНИЙ НА ПЕНСИИ')),
(23,'ru',UPPER('СЕЛЬСКИЙ МЕДИК НА ПЕНСИИ')), (23,'uk',UPPER('СЕЛЬСКИЙ МЕДИК НА ПЕНСИИ')),
(24,'ru',UPPER('СУДЬЯ В ОТСТАВКЕ')), (24,'uk',UPPER('СУДЬЯ В ОТСТАВКЕ')),
(25,'ru',UPPER('СЛЕДОВАТЕЛЬ ПРОКУРАТУРЫ НА ПЕНСИИ')), (25,'uk',UPPER('СЛЕДОВАТЕЛЬ ПРОКУРАТУРЫ НА ПЕНСИИ')),
(26,'ru',UPPER('НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (26,'uk',UPPER('НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')),
(27,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (27,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО НАЛОГОВОГО МИЛИЦИОНЕРА')),
(28,'ru',UPPER('СЕЛЬСКИЙ НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (28,'uk',UPPER('СЕЛЬСКИЙ НАЛОГОВЫЙ МИЛИЦИОНЕР НА ПЕНСИИ')),
(29,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ СЕЛЬСКОГО НАЛОГОВОГО МИЛИЦИОНЕРА')), (29,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ СЕЛЬСКОГО НАЛОГОВОГО МИЛИЦИОНЕРА')),
(30,'ru',UPPER('ВОЕННОСЛУЖАЩИЙ СБУ НА ПЕНСИИ')), (30,'uk',UPPER('ВОЕННОСЛУЖАЩИЙ СБУ НА ПЕНСИИ')),
(31,'ru',UPPER('ЛИЦО (ЧАЭС) - 1 КАТЕГОРИЯ')), (31,'uk',UPPER('ЛИЦО (ЧАЭС) - 1 КАТЕГОРИЯ')),
(32,'ru',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ЛИКВИДАТОР')), (32,'uk',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ЛИКВИДАТОР')),
(33,'ru',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ПОТЕРПЕВШИЙ')), (33,'uk',UPPER('ЛИЦО (ЧАЭС) - 2 КАТЕГОРИЯ - ПОТЕРПЕВШИЙ')),
(34,'ru',UPPER('ЛИЦО (ЧАЭС) - 3 КАТЕГОРИЯ')), (34,'uk',UPPER('ЛИЦО (ЧАЭС) - 3 КАТЕГОРИЯ')),
(35,'ru',UPPER('ЛИЦО (ЧАЭС) - 4 КАТЕГОРИЯ')), (35,'uk',UPPER('ЛИЦО (ЧАЭС) - 4 КАТЕГОРИЯ')),
(36,'ru',UPPER('ЖЕНА/МУЖ (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (36,'uk',UPPER('ЖЕНА/МУЖ (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')),
(37,'ru',UPPER('РЕБЕНОК (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')), (37,'uk',UPPER('РЕБЕНОК (ЧАЭС) УМЕРШЕГО ГРАЖДАНИНА')),
(38,'ru',UPPER('РЕБЕНОК (ЧАЭС) ПОТЕРПЕВШЕГО')), (38,'uk',UPPER('РЕБЕНОК (ЧАЭС) ПОТЕРПЕВШЕГО')),
(39,'ru',UPPER('РЕБЕНОК (ЧАЭС) - ИНВАЛИД')), (39,'uk',UPPER('РЕБЕНОК (ЧАЭС) - ИНВАЛИД')),
(40,'ru',UPPER('ЛИЦО (ЧАЭС), РАБОТАВШЕЕ ЗА ПРЕДЕЛАМИ ЗОНЫ ОТЧУЖДЕНИЯ (ЛИКВИДАЦИЯ ПОСЛЕДСТВИЙ АВАРИИ)')), (40,'uk',UPPER('ЛИЦО (ЧАЭС), РАБОТАВШЕЕ ЗА ПРЕДЕЛАМИ ЗОНЫ ОТЧУЖДЕНИЯ (ЛИКВИДАЦИЯ ПОСЛЕДСТВИЙ АВАРИИ)')),
(41,'ru',UPPER('СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (41,'uk',UPPER('СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')),
(42,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (42,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')),
(43,'ru',UPPER('СЕЛЬСКИЙ СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')), (43,'uk',UPPER('СЕЛЬСКИЙ СОТРУДНИК УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ НА ПЕНСИИ')),
(44,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')), (44,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО СОТРУДНИКА УГОЛОВНО-ИСПОЛНИТЕЛЬНОЙ СИСТЕМЫ')),
(45,'ru',UPPER('МИЛИЦИОНЕР НА ПЕНСИИ')), (45,'uk',UPPER('МИЛИЦИОНЕР НА ПЕНСИИ')),
(46,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО МИЛИЦИОНЕРА')), (46,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО МИЛИЦИОНЕРА')),
(47,'ru',UPPER('СЕЛЬСКИЙ МИЛИЦИОНЕР НА ПЕНСИИ')), (47,'uk',UPPER('СЕЛЬСКИЙ МИЛИЦИОНЕР НА ПЕНСИИ')),
(48,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО МИЛИЦИОНЕРА')), (48,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО СЕЛЬСКОГО МИЛИЦИОНЕРА')),
(49,'ru',UPPER('ВЕТЕРАН ВОИНСКОЙ СЛУЖБЫ')), (49,'uk',UPPER('ВЕТЕРАН ВОИНСКОЙ СЛУЖБЫ')),
(50,'ru',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ВОИНСКОЙ СЛУЖБЫ')), (50,'uk',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ВОИНСКОЙ СЛУЖБЫ')),
(51,'ru',UPPER('ЧЛЕН СЕМЬИ ВОЕННОСЛУЖАЩЕГО, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ СТАВШЕГО ИНВАЛИДОМ')), (51,'uk',UPPER('ЧЛЕН СЕМЬИ ВОЕННОСЛУЖАЩЕГО, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ СТАВШЕГО ИНВАЛИДОМ')),
(52,'ru',UPPER('РОДИТЕЛИ ВОЕННОСЛУЖАЩЕГО, СТАВШЕГО ИНВАЛИДОМ')), (52,'uk',UPPER('РОДИТЕЛИ ВОЕННОСЛУЖАЩЕГО, СТАВШЕГО ИНВАЛИДОМ')),
(53,'ru',UPPER('ВДОВА/ВДОВЕЦ ВОЕННОСЛУЖАЩЕГО И ЕГО ДЕТИ')), (53,'uk',UPPER('ВДОВА/ВДОВЕЦ ВОЕННОСЛУЖАЩЕГО И ЕГО ДЕТИ')),
(54,'ru',UPPER('ЖЕНА/МУЖ ВОЕННОСЛУЖАЩЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ')), (54,'uk',UPPER('ЖЕНА/МУЖ ВОЕННОСЛУЖАЩЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ')),
(55,'ru',UPPER('РОДИТЕЛИ ПОГИБШЕГО ВОЕННОСЛУЖАЩЕГО')), (55,'uk',UPPER('РОДИТЕЛИ ПОГИБШЕГО ВОЕННОСЛУЖАЩЕГО')),
(56,'ru',UPPER('ИНВАЛИД ВОИНСКОЙ СЛУЖБЫ')), (56,'uk',UPPER('ИНВАЛИД ВОИНСКОЙ СЛУЖБЫ')),
(57,'ru',UPPER('ВЕТЕРАН ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (57,'uk',UPPER('ВЕТЕРАН ОРГАНОВ ВНУТРЕННИХ ДЕЛ')),
(58,'ru',UPPER('ВДОВА/ВДОВЕЦ, ВЕТЕРАНА ОРГАНОВ ВНУТРЕННИХ ДЕЛ')), (58,'uk',UPPER('ВДОВА/ВДОВЕЦ, ВЕТЕРАНА ОРГАНОВ ВНУТРЕННИХ ДЕЛ')),
(59,'ru',UPPER('ПОЖАРНЫЙ НА ПЕНСИИ')), (59,'uk',UPPER('ПОЖАРНЫЙ НА ПЕНСИИ')),
(60,'ru',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО ПОЖАРНОГО')), (60,'uk',UPPER('НЕТРУДОСПОСОБНЫЙ ЧЛЕН СЕМЬИ ПОГИБШЕГО ПОЖАРНОГО')),
(61,'ru',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (61,'uk',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')),
(62,'ru',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')), (62,'uk',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ ПОЖАРНОЙ ОХРАНЫ')),
(63,'ru',UPPER('РЕАБИЛИТИРОВАННЫЕ, СТАВШИЕ ИНВАЛИДАМИ ВСЛЕДСТВИИ РЕПРЕССИЙ, ЛИБО ЯВЛЯЮЩИЕСЯ ПЕНСИОНЕРАМИ, ИМЕЮЩИМИ П')), (63,'uk',UPPER('РЕАБИЛИТИРОВАННЫЕ, СТАВШИЕ ИНВАЛИДАМИ ВСЛЕДСТВИИ РЕПРЕССИЙ, ЛИБО ЯВЛЯЮЩИЕСЯ ПЕНСИОНЕРАМИ, ИМЕЮЩИМИ П')),
(64,'ru',UPPER('РЕБЕНОК-ИНВАЛИД')), (64,'uk',UPPER('РЕБЕНОК-ИНВАЛИД')),
(65,'ru',UPPER('ИНВАЛИД 1 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (65,'uk',UPPER('ИНВАЛИД 1 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')),
(66,'ru',UPPER('ИНВАЛИД 2 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')), (66,'uk',UPPER('ИНВАЛИД 2 ГРУППЫ ПО ЗРЕНИЮ ИЛИ С ВРАЖДЕННЫМ ОРА')),
(67,'ru',UPPER('ИНВАЛИД 1 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (67,'uk',UPPER('ИНВАЛИД 1 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')),
(68,'ru',UPPER('ИНВАЛИД 2 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')), (68,'uk',UPPER('ИНВАЛИД 2 ГРУППЫ, КРОМЕ ИНВАЛИДОВ ПО ЗРЕНИЮ ИЛИ С ВРОЖДЕННЫМ ОРА')),
(69,'ru',UPPER('ИНВАЛИД 3 ГРУППЫ')), (69,'uk',UPPER('ИНВАЛИД 3 ГРУППЫ')),
(70,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(1)')), (70,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(1)')),
(71,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 1 ГРУППЫ')), (71,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 1 ГРУППЫ')),
(72,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 2 ГРУППЫ')), (72,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 2 ГРУППЫ')),
(73,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 3 ГРУППЫ')), (73,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(2) - ИНВАЛИД 3 ГРУППЫ')),
(74,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(3)')), (74,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(3)')),
(75,'ru',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(4)')), (75,'uk',UPPER('ЖЕРТВА НАЦИСТСКИХ ПРЕСЛЕДОВАНИЙ, СТ. 6(4)')),
(76,'ru',UPPER('ГОРНЯКИ - НЕТРУДОСПОСОБНЫЕ РАБОТНИКИ')), (76,'uk',UPPER('ГОРНЯКИ - НЕТРУДОСПОСОБНЫЕ РАБОТНИКИ')),
(77,'ru',UPPER('ГОРНЯКИ - НЕРАБОТАЮЩИЕ ПЕНСИОНЕРЫ')), (77,'uk',UPPER('ГОРНЯКИ - НЕРАБОТАЮЩИЕ ПЕНСИОНЕРЫ')),
(78,'ru',UPPER('ГОРНЯКИ - ИНВАЛИДЫ')), (78,'uk',UPPER('ГОРНЯКИ - ИНВАЛИДЫ')),
(79,'ru',UPPER('ГОРНЯКИ - СЕМЬИ ПОГИБШИХ ТРУЖЕНИКОВ')), (79,'uk',UPPER('ГОРНЯКИ - СЕМЬИ ПОГИБШИХ ТРУЖЕНИКОВ')),
(80,'ru',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (80,'uk',UPPER('ВЕТЕРАН ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')),
(81,'ru',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (81,'uk',UPPER('ВДОВА/ВДОВЕЦ ВЕТЕРАНА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')),
(82,'ru',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО СОТРУДНИКА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')), (82,'uk',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ ПОГИБШЕГО/УМЕРШЕГО СОТРУДНИКА ГОСУДАРСТВЕННОЙ СЛУЖБЫ СПЕЦ.СВЯЗИ')),
(83,'ru',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ СОТРУДНИКА ГРАЖДАНСКОЙ ОБОРОНЫ, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ')), (83,'uk',UPPER('РОДИТЕЛИ И ЧЛЕНЫ СЕМЬИ СОТРУДНИКА ГРАЖДАНСКОЙ ОБОРОНЫ, ПОГИБШЕГО, УМЕРШЕГО, ПРОПАВШЕГО БЕЗ ВЕСТИ ИЛИ')),
(84,'ru',UPPER('МАТЕРИ-ГЕРОИНИ')), (84,'uk',UPPER('МАТЕРИ-ГЕРОИНИ')),
(85,'ru',UPPER('ДЕТИ-ИНВАЛИДЫ, ПРИКОВАННЫЕ К КРОВАТИ')), (85,'uk',UPPER('ДЕТИ-ИНВАЛИДЫ, ПРИКОВАННЫЕ К КРОВАТИ')),
(86,'ru',UPPER('ДЕТИ-ИНВАЛИДЫ ДО 18 ЛЕТ, ГДЕ ОБА РОДИТЕЛИ ИНВАЛИДЫ')), (86,'uk',UPPER('ДЕТИ-ИНВАЛИДЫ ДО 18 ЛЕТ, ГДЕ ОБА РОДИТЕЛИ ИНВАЛИДЫ')),
(87,'ru',UPPER('МНОГОДЕТНЫЕ СЕМЬИ (3 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (87,'uk',UPPER('МНОГОДЕТНЫЕ СЕМЬИ (3 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')),
(88,'ru',UPPER('МАТЕРИ-ОДИНОЧКИ (2 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')), (88,'uk',UPPER('МАТЕРИ-ОДИНОЧКИ (2 И БОЛЕЕ ДЕТЕЙ ДО 18 ЛЕТ)')),
(89,'ru',UPPER('ДЕТИ-ИНВАЛИДЫ С ОНКОЛОГИЧЕСКИМИ ЗАБОЛЕВАНИЯМИ ДО 18 ЛЕТ')), (89,'uk',UPPER('ДЕТИ-ИНВАЛИДЫ С ОНКОЛОГИЧЕСКИМИ ЗАБОЛЕВАНИЯМИ ДО 18 ЛЕТ')),
(90,'ru',UPPER('ИНВАЛИДЫ 1 ГРУППЫ, ПОЛУЧАЮЩИЕ СОЦИАЛЬНУЮ ПЕНСИЮ ИЛИ ГОСУДАРСТВЕННУЮ ПОМОЩЬ')), (90,'uk',UPPER('ИНВАЛИДЫ 1 ГРУППЫ, ПОЛУЧАЮЩИЕ СОЦИАЛЬНУЮ ПЕНСИЮ ИЛИ ГОСУДАРСТВЕННУЮ ПОМОЩЬ')),
(91,'ru',UPPER('НЕРАБОТАЮЩИЕ РОДИТЕЛИ, ОСУЩЕСТВЛЯЮЩИЕ УХОД ЗА РЕБЕНКОМ-ИНВАЛИДОМ ДО 18 ЛЕТ')), (91,'uk',UPPER('НЕРАБОТАЮЩИЕ РОДИТЕЛИ, ОСУЩЕСТВЛЯЮЩИЕ УХОД ЗА РЕБЕНКОМ-ИНВАЛИДОМ ДО 18 ЛЕТ')),
(92,'ru',UPPER('ИНВАЛИДЫ 1, 2 ГРУППЫ ПО ЗРЕНИЮ')), (92,'uk',UPPER('ИНВАЛИДЫ 1, 2 ГРУППЫ ПО ЗРЕНИЮ')),
(93,'ru',UPPER('СЕМЬИ ДЕТЕЙ ДО 18 ЛЕТ, БОЛЬНЫХ ДЦП')), (93,'uk',UPPER('СЕМЬИ ДЕТЕЙ ДО 18 ЛЕТ, БОЛЬНЫХ ДЦП')),
(94,'ru',UPPER('ГРАЖДАНЕ, РЕАБИЛИТИРОВАННЫЕ СОГЛАСНО')), (94,'uk',UPPER('ГРАЖДАНЕ, РЕАБИЛИТИРОВАННЫЕ СОГЛАСНО')),
(95,'ru',UPPER('СЕМЬИ ПОГИБШИХ (РЯДОВОЙ СОСТАВ) ПРИ ПРОХОЖДЕНИИ СРОЧНОЙ ВОИНСКОЙ СЛУЖБЫ, ИСПОЛНЯВШИХ СВОЙ ДОЛГ В МИ')), (95,'uk',UPPER('СЕМЬИ ПОГИБШИХ (РЯДОВОЙ СОСТАВ) ПРИ ПРОХОЖДЕНИИ СРОЧНОЙ ВОИНСКОЙ СЛУЖБЫ, ИСПОЛНЯВШИХ СВОЙ ДОЛГ В МИ')),
(96,'ru',UPPER('ПРИЕМНЫЕ СЕМЬИ')), (96,'uk',UPPER('ПРИЕМНЫЕ СЕМЬИ')),
(97,'ru',UPPER('ДВОРНИКИ')), (97,'uk',UPPER('ДВОРНИКИ')),
(98,'ru',UPPER('АВАРИЙНО-ДИСПЕТЧЕРСКАЯ СЛУЖБА')), (98,'uk',UPPER('АВАРИЙНО-ДИСПЕТЧЕРСКАЯ СЛУЖБА')),
(99,'ru',UPPER('ПРИЕМНЫЕ СЕМЬИ')), (99,'uk',UPPER('ПРИЕМНЫЕ СЕМЬИ')),
(100,'ru',UPPER('СОЦИАЛЬНЫЕ РАБОЧИЕ')), (100,'uk',UPPER('СОЦИАЛЬНЫЕ РАБОЧИЕ')),
(101,'ru',UPPER('УХОД ЗА ИНВАЛИДОМ 1 ГРУППЫ ВОВ')), (101,'uk',UPPER('УХОД ЗА ИНВАЛИДОМ 1 ГРУППЫ ВОВ')),
(102,'ru',UPPER('РАБОТНИКИ ХКП "ГОРЭЛЕКТРОТРАНС"')), (102,'uk',UPPER('РАБОТНИКИ ХКП "ГОРЭЛЕКТРОТРАНС"')),
(103,'ru',UPPER('АФГАНИСТАН')), (103,'uk',UPPER('АФГАНИСТАН')),
(104,'ru',UPPER('ВЕТЕРАН НАЛОГОВОЙ МИЛИЦИИ')), (104,'uk',UPPER('ВЕТЕРАН НАЛОГОВОЙ МИЛИЦИИ'));
INSERT INTO privilege_attribute(attribute_id, object_id, attribute_type_id, value_id, value_type_id) VALUES
(1,1,1200,1,1200),(1,2,1200,2,1200),(1,3,1200,3,1200),(1,4,1200,4,1200),(1,5,1200,5,1200),(1,6,1200,6,1200),(1,7,1200,7,1200),(1,8,1200,8,1200)
,(1,9,1200,9,1200),(1,10,1200,10,1200),(1,11,1200,11,1200),(1,12,1200,12,1200),(1,13,1200,13,1200),(1,14,1200,14,1200),(1,15,1200,15,1200),
(1,16,1200,16,1200),(1,17,1200,17,1200),(1,18,1200,18,1200),(1,19,1200,19,1200),(1,20,1200,20,1200),(1,21,1200,21,1200),(1,22,1200,22,1200),
(1,23,1200,23,1200),(1,24,1200,24,1200),(1,25,1200,25,1200),(1,26,1200,26,1200),(1,27,1200,27,1200),(1,28,1200,28,1200),(1,29,1200,29,1200),
(1,30,1200,30,1200),(1,31,1200,31,1200),(1,32,1200,32,1200),(1,33,1200,33,1200),(1,34,1200,34,1200),(1,35,1200,35,1200),(1,36,1200,36,1200),
(1,37,1200,37,1200),(1,38,1200,38,1200),(1,39,1200,39,1200),(1,40,1200,40,1200),(1,41,1200,41,1200),(1,42,1200,42,1200),(1,43,1200,43,1200),
(1,44,1200,44,1200),(1,45,1200,45,1200),(1,46,1200,46,1200),(1,47,1200,47,1200),(1,48,1200,48,1200),(1,49,1200,49,1200),(1,50,1200,50,1200),
(1,51,1200,51,1200),(1,52,1200,52,1200),(1,53,1200,53,1200),(1,54,1200,54,1200),(1,55,1200,55,1200),(1,56,1200,56,1200),(1,57,1200,57,1200),
(1,58,1200,58,1200),(1,59,1200,59,1200),(1,60,1200,60,1200),(1,61,1200,61,1200),(1,62,1200,62,1200),(1,63,1200,63,1200),(1,64,1200,64,1200),
(1,65,1200,65,1200),(1,66,1200,66,1200),(1,67,1200,67,1200),(1,68,1200,68,1200),(1,69,1200,69,1200),(1,70,1200,70,1200),(1,71,1200,71,1200),
(1,72,1200,72,1200),(1,73,1200,73,1200),(1,74,1200,74,1200),(1,75,1200,75,1200),(1,76,1200,76,1200),(1,77,1200,77,1200),(1,78,1200,78,1200),
(1,79,1200,79,1200),(1,80,1200,80,1200),(1,81,1200,81,1200),(1,82,1200,82,1200),(1,83,1200,83,1200),(1,84,1200,84,1200),(1,85,1200,85,1200),
(1,86,1200,86,1200),(1,87,1200,87,1200),(1,88,1200,88,1200),(1,89,1200,89,1200),(1,90,1200,90,1200),(1,91,1200,91,1200),(1,92,1200,92,1200),
(1,93,1200,93,1200),(1,94,1200,94,1200),(1,95,1200,95,1200),(1,96,1200,96,1200),(1,97,1200,97,1200),(1,98,1200,98,1200),(1,99,1200,99,1200),
(1,100,1200,100,1200),(1,101,1200,101,1200),(1,102,1200,102,1200),(1,103,1200,103,1200),(1,104,1200,104,1200);

