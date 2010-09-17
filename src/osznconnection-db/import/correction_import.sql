set @organization_id = 2;
set @organization_code = 1;

-- street type
insert into `entity_type_correction` (`object_id`, `correction`,  `organization_id`, `organization_code`)
  select
    et.`id`, tst.`name`,  @organization_id, @organization_code
  from
    `tmp_street_types` tst inner join `entity_type` et on tst.`object_id` = et.`import_object_id`; 

-- district
insert into `district_correction` (`object_id`, `correction`,  `organization_id`, `organization_code`)
  select
    d.`object_id`, td.`name`,  @organization_id, @organization_code
  from
    `tmp_districts` td inner join `district` d on td.`object_id` = d.`import_object_id`;

-- street
insert into `street_correction` (`object_id`, `correction`, `organization_id`, `organization_code`)
  select
    s.`object_id`, ts.`name`, @organization_id, @organization_code
  from
    `tmp_streets` ts inner join `street` s on ts.`object_id` = s.`import_object_id`;

-- house
insert into `building_correction` (`object_id`, `correction`, `organization_id`, `organization_code`)
  select
    b.`object_id`, th.`name`, @organization_id, @organization_code
  from
    `tmp_houses` th  inner join `building` b on th.`object_id` = b.`import_object_id`;  