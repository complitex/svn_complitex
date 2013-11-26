package org.complitex.organization.service;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.organization.entity.OrganizationImport;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;


@Stateless
public class OrganizationImportBean extends AbstractBean {
    private static final String NS = OrganizationImportBean.class.getName();

    @Transactional
    public void importOrganization(OrganizationImport importOrganization) {
        sqlSession().insert(NS + ".insertOrganizationImport", importOrganization);
    }

    public List<OrganizationImport> find(Long parentOrganizationId) {
        return sqlSession().selectList(NS + ".selectOrganizationImports", parentOrganizationId);
    }

    public OrganizationImport findById(long organizationId) {
        return sqlSession().selectOne(NS + ".selectOrganizationImport", organizationId);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void delete() {
        sqlSession().delete(NS + ".deleteOrganizationImport");
    }
}
