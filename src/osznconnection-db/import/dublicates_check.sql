 -- find dublicate country. Suitable for region, district, apartment and room entities.
SELECT c1.`object_id` c1, c2.`object_id` c2 FROM `country` c1
	JOIN `country_attribute` a1 ON (c1.`object_id` = a1.`object_id` AND a1.`status` = 'ACTIVE' AND a1.`attribute_type_id` = 800)
	JOIN `country_string_culture` sc1 ON (a1.`value_id` = sc1.`id` AND sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `country_string_culture` sc2 ON (sc1.`value` = sc2.`value` AND sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `country_attribute` a2 ON (a2.`value_id` = sc2.`id` AND a2.`status` = 'ACTIVE' AND a2.`attribute_type_id` = 800)
	JOIN `country` c2 ON (c2.`object_id` = a2.`object_id` AND c2.`status` = 'ACTIVE')
	WHERE c1.`status` = 'ACTIVE' AND c1.`object_id` < c2.`object_id` AND 
	((c1.`parent_id` IS NULL AND c1.`parent_entity_id` IS NULL AND c2.`parent_id` IS NULL AND c2.`parent_entity_id` IS NULL) 
		OR (c1.`parent_id` = c2.`parent_id` AND c1.`parent_entity_id` = c2.`parent_entity_id`)) AND 
	((c1.`entity_type_id` IS NULL AND c2.`entity_type_id` IS NULL) OR c1.`entity_type_id` = c2.`entity_type_id`);

 -- find dublicate streets 
SELECT 
-- count(*)
s1.`object_id` s1, sc1.`value` value1, s2.`object_id` s2, sc2.`value` value2 
	FROM `street` s1
	JOIN `street_attribute` a1 ON (s1.`object_id` = a1.`object_id` AND a1.`status` = 'ACTIVE' AND a1.`attribute_type_id` = 300)
	JOIN `street_attribute` t1 ON (s1.`object_id` = t1.`object_id` AND t1.`status` = 'ACTIVE' AND t1.`attribute_type_id` = 301)
	JOIN `street_string_culture` sc1 ON (a1.`value_id` = sc1.`id` AND sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `street_string_culture` sc2 ON (sc1.`value` = sc2.`value` AND sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `street_attribute` a2 ON (a2.`value_id` = sc2.`id` AND a2.`status` = 'ACTIVE' AND a2.`attribute_type_id` = 300)
	JOIN `street` s2 ON (s2.`object_id` = a2.`object_id` AND s2.`status` = 'ACTIVE')
	JOIN `street_attribute` t2 ON (t1.`value_id` = t2.`value_id` AND s2.`object_id` = t2.`object_id` AND t2.`status` = 'ACTIVE' AND t2.`attribute_type_id` = 301)
	WHERE s1.`status` = 'ACTIVE' AND s1.`object_id` < s2.`object_id` AND 
	((s1.`parent_id` IS NULL AND s1.`parent_entity_id` IS NULL AND s2.`parent_id` IS NULL AND s2.`parent_entity_id` IS NULL) 
		OR (s1.`parent_id` = s2.`parent_id` AND s1.`parent_entity_id` = s2.`parent_entity_id`));
		
 -- find dublicate building addresses
SELECT COUNT(*) FROM `building_address` WHERE parent_entity_id = 400;
SELECT 
COUNT(*)
-- ad1.`object_id` ad1, num_sc1.`value` num1, corp_sc1.`value` corp1, st_sc1.`value` st1, ad1.`parent_id` parent1, s1_value.`value` street1,
-- ad2.`object_id` ad2, num_sc2.`value` num2, corp_sc2.`value` corp2, st_sc2.`value` st2, ad2.`parent_id` parent2, s2_value.`value` street2
	FROM `building_address` ad1
	JOIN `street` s1 ON s1.`object_id` = ad1.`parent_id`
	JOIN `street_attribute` sa1 ON (sa1.`object_id` = s1.`object_id` AND sa1.`status` = 'ACTIVE' AND sa1.`attribute_type_id` = 300)
	JOIN `street_string_culture` s1_value ON (sa1.`value_id` = s1_value.`id` AND s1_value.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `building_address_attribute` num1 ON (ad1.`object_id` = num1.`object_id` AND num1.`status` = 'ACTIVE' AND num1.`attribute_type_id` = 1500)
	JOIN `building_address_string_culture` num_sc1 ON (num1.`value_id` = num_sc1.`id` AND num_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` corp1 ON (ad1.`object_id` = corp1.`object_id` AND corp1.`status` = 'ACTIVE' AND corp1.`attribute_type_id` = 1501)
	LEFT JOIN `building_address_string_culture` corp_sc1 ON (corp1.`value_id` = corp_sc1.`id` AND corp_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` st1 ON (ad1.`object_id` = st1.`object_id` AND st1.`status` = 'ACTIVE' AND st1.`attribute_type_id` = 1502)
	LEFT JOIN `building_address_string_culture` st_sc1 ON (st1.`value_id` = st_sc1.`id` AND st_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `building_address_string_culture` num_sc2 ON (num_sc1.`value` = num_sc2.`value` AND num_sc1.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	JOIN `building_address_attribute` num2 ON (num2.`value_id` = num_sc2.`id` AND num2.`status` = 'ACTIVE' AND num2.`attribute_type_id` = 1500)
	JOIN `building_address` ad2 ON (ad2.`object_id` = num2.`object_id` AND ad2.`status` = 'ACTIVE')
	JOIN `street` s2 ON s2.`object_id` = ad2.`parent_id`
	JOIN `street_attribute` sa2 ON (sa2.`object_id` = s2.`object_id` AND sa2.`status` = 'ACTIVE' AND sa2.`attribute_type_id` = 300)
	JOIN `street_string_culture` s2_value ON (sa2.`value_id` = s2_value.`id` AND s2_value.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` corp2 ON (ad2.`object_id` = corp2.`object_id` AND corp2.`status` = 'ACTIVE' AND corp2.`attribute_type_id` = 1501)
	LEFT JOIN `building_address_string_culture` corp_sc2 ON (corp2.`value_id` = corp_sc2.`id` AND corp_sc2.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	LEFT JOIN `building_address_attribute` st2 ON (ad2.`object_id` = st2.`object_id` AND st2.`status` = 'ACTIVE' AND st2.`attribute_type_id` = 1502)
	LEFT JOIN `building_address_string_culture` st_sc2 ON (st2.`value_id` = st_sc2.`id` AND st_sc2.`locale` = (SELECT l.`locale` FROM `locales` l WHERE l.`system` = 1))
	WHERE ad1.`status` = 'ACTIVE' AND ad1.`object_id` < ad2.`object_id` 
	AND ad1.`parent_id` = ad2.`parent_id` AND ad1.`parent_entity_id` = ad2.`parent_entity_id`
	AND ((corp_sc1.`value` IS NULL AND corp_sc2.`value` IS NULL) OR corp_sc1.`value` = corp_sc2.`value`) 
	AND ((st_sc1.`value` IS NULL AND st_sc2.`value` IS NULL) OR st_sc1.`value` = st_sc2.`value`);
	
 -- find buildings pointing onto one address
SELECT `object_id` FROM `building_attribute` WHERE `attribute_type_id` = 501;

SELECT b1.`object_id` b1, b2.`object_id` b2 FROM `building_address` ad
	JOIN `building` b1 ON b1.`parent_id` = ad.`object_id`
	JOIN `building` b2 ON b2.`parent_id` = ad.`object_id`
        WHERE b1.`object_id` < b2.`object_id`;
                
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
	