set @organization_id = 2;

-- Ownership corrections
insert into ownership_correction(organization_id, correction, object_id, organization_code) values
(@organization_id,UPPER('ГОС'),1,'6513'),
(@organization_id,UPPER('КООП'),2,'45188713'),
(@organization_id,UPPER('ВЫК'),5,'3769903'),
(@organization_id,UPPER('ЧАС'),6,'6514');
