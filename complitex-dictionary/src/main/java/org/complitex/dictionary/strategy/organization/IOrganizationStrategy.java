package org.complitex.dictionary.strategy.organization;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public interface IOrganizationStrategy extends IStrategy {

    /**
     * Attribute type ids
     */
    public long NAME = 900;
    public long CODE = 901;
    public long DISTRICT = 902;
    public long USER_ORGANIZATION_PARENT = 903;
    public long ORGANIZATION_TYPE = 905;

    @Transactional
    List<? extends DomainObject> getUserOrganizations(Locale locale, Long... excludeOrganizationsId);

    @Transactional
    Set<Long> getTreeChildrenOrganizationIds(long parentOrganizationId);

    Attribute getDistrictAttribute(DomainObject organization);

    Attribute getParentAttribute(DomainObject organization);

    String getDistrictCode(DomainObject organization);

    String getCode(DomainObject organization);

    String getName(DomainObject organization, Locale locale);

    boolean isUserOrganization(DomainObject organization);

    @Transactional
    Long validateCode(Long id, String code, Long parentId, Long parentEntityId);

    @Transactional
    Long validateName(Long id, String name, Long parentId, Long parentEntityId, Locale locale);
}
