set @organization_id = 1;

-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, organization_code) values
(@organization_id,UPPER('мiсцевих Рад'),1,'1'),
(@organization_id,UPPER('кооперативна'),2,'4'),
(@organization_id,UPPER('приватна'),5,'5'),
(@organization_id,UPPER('приватизована'),6,'6'),
(@organization_id,UPPER('громадська'),4,'3'),
(@organization_id,UPPER('вiдомча'),3,'2');
