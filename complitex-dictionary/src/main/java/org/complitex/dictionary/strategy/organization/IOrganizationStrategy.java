package org.complitex.dictionary.strategy.organization;

import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    long NAME = 900;
    long CODE = 901;
    long DISTRICT = 902;
    long USER_ORGANIZATION_PARENT = 903;
    long ORGANIZATION_TYPE = 905;

    @Transactional
    List<? extends DomainObject> getUserOrganizations(Locale locale, Long... excludeOrganizationsId);

    @Transactional
    List<? extends DomainObject> getAllOrganizations(Locale locale, Long... excludeOrganizationsId);

    @Transactional
    Set<Long> getTreeChildrenOrganizationIds(long parentOrganizationId);

    String getDistrictCode(DomainObject organization);

    boolean isUserOrganization(DomainObject organization);

    @Transactional
    Long validateCode(Long id, String code);

    @Transactional
    Long validateName(Long id, String name, Locale locale);
}
