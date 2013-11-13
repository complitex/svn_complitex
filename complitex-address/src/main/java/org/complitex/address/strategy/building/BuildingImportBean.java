package org.complitex.address.strategy.building;

import com.google.common.collect.ImmutableMap;
import org.complitex.address.strategy.building.entity.BuildingImport;
import org.complitex.address.strategy.building.entity.BuildingSegmentImport;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;

@Stateless
public class BuildingImportBean extends AbstractBean {
    private static final String MAPPING_NAMESPACE = BuildingImportBean.class.getName();

    @Transactional
    public void insert(long buildingSegmentId, long distrId, long streetId, String num, String part, Long gekId, String code) {
        BuildingImport b = new BuildingImport(distrId, streetId, num, part);

        sqlSession().insert(MAPPING_NAMESPACE + ".insertBuildingImport", b);

        addBuildingSegment(buildingSegmentId, gekId, code, b.getId());
    }

    public Long findId(long streetId, String num, String part) {
        return sqlSession().selectOne(MAPPING_NAMESPACE + ".getBuildingImportId",
                ImmutableMap.of("streetId", streetId, "num", num, "part", part));
    }

    @Transactional
    public void saveOrUpdate(long buildingSegmentId, long distrId, long streetId, String num, String part,
            Long gekId, String code) {
        if (part == null) {
            part = "";
        }
        Long id = findId(streetId, num, part);
        if (id == null) {
            insert(buildingSegmentId, distrId, streetId, num, part, gekId, code);
        } else {
            addBuildingSegment(buildingSegmentId, gekId, code, id);
        }
    }

    @Transactional
    public void addBuildingSegment(long buildingPartId, Long gekId, String code, long buildingImportId) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertBuildingSegmentImport",
                new BuildingSegmentImport(buildingPartId, gekId, code, buildingImportId));
    }

    @Transactional
    public void markProcessed(long buildingImportId) {
        sqlSession().update(MAPPING_NAMESPACE + ".markProcessed", buildingImportId);
    }

    @Transactional
    public void markProcessed(List<BuildingImport> buildings) {
        for (BuildingImport b : buildings) {
            markProcessed(b.getId());
        }
    }

    public List<BuildingImport> getBuildingImports(int count) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".getBuildingImports", count);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void delete() {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteBuildingSegmentImport");
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteBuildingImport");
    }
}
