package org.complitex.correction.service;

import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.AbstractBean;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 28.11.13 15:43
 */
public class OrganizationCorrectionBean extends AbstractBean{
    public final static String NS = OrganizationCorrectionBean.class.getName();

    public OrganizationCorrection geOrganizationCorrection(Long id){
        return sqlSession().selectOne(NS + ".selectOrganizationCorrection");
    }

    public List<OrganizationCorrection> getOrganizationCorrections(FilterWrapper<OrganizationCorrection> filterWrapper){
        return sqlSession().selectList(NS + ".selectOrganizationCorrections", filterWrapper);
    }

    public Integer getOrganizationCorrectionsCount(FilterWrapper<OrganizationCorrection> filterWrapper){
        return sqlSession().selectOne(NS + ".selectOrganizationCorrectionsCount", filterWrapper);
    }

}
