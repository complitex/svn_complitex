package org.complitex.correction.service;

import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.13 15:43
 */
@Stateless
public class OrganizationCorrectionBean extends AbstractBean{
    public final static String NS = OrganizationCorrectionBean.class.getName();
    private static final String NS_CORRECTION = Correction.class.getName();

    public OrganizationCorrection geOrganizationCorrection(Long id){
        return sqlSession().selectOne(NS + ".selectOrganizationCorrection", id);
    }

    public List<OrganizationCorrection> getOrganizationCorrections(FilterWrapper<OrganizationCorrection> filterWrapper){
        return sqlSession().selectList(NS + ".selectOrganizationCorrections", filterWrapper);
    }

    public Integer getOrganizationCorrectionsCount(FilterWrapper<OrganizationCorrection> filterWrapper){
        return sqlSession().selectOne(NS + ".selectOrganizationCorrectionsCount", filterWrapper);
    }

    public void save(OrganizationCorrection organizationCorrection){
        if (organizationCorrection.getId() == null) {
            sqlSession().insert(NS_CORRECTION + ".insertCorrection", organizationCorrection);
        } else{
            sqlSession().update(NS_CORRECTION + ".updateCorrection", organizationCorrection);
        }
    }

    @Transactional
    public void delete(OrganizationCorrection organizationCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", organizationCorrection);
    }
}
