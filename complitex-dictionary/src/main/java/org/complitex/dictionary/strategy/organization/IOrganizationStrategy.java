package org.complitex.dictionary.strategy.organization;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.SqlSessionFactoryBean;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.IStrategy;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author Artem
 */
public interface IOrganizationStrategy<T extends DomainObject> extends IStrategy {
    String BEAN_NAME = "OrganizationStrategy";
    String BEAN_LOOKUP = "java:module/OrganizationStrategy";

    /**
     * Organization name.
     */
    long NAME = 900;

    /**
     * Organization's short name.
     */
    public final static long SHORT_NAME = 906;

    /**
     * Organization's code.
     */
    long CODE = 901;
    /**
     * District reference.
     */
    long DISTRICT = 902;
    /**
     * User organization's parent.
     */
    long USER_ORGANIZATION_PARENT = 903;
    /**
     * Organization type.
     */
    long ORGANIZATION_TYPE = 904;
    
    /**
     * Filter parameter to filter out organizations by organization types.
     */
    String ORGANIZATION_TYPE_PARAMETER = "organizationTypeIds";

    String BALANCE_HOLDER_PARAMETER = "balanceHolder";

    /**
     * Returns user organizations that current user can see (at least to read).
     * That is, it returns all user organizations that accessed by user's organizations.
     * 
     * @param locale Locale. It is used in sorting of user organizations by name.
     * @param excludeOrganizationsId Ids of user organizations that should be excluded from returned list.
     * @return User organizations that current user can see.
     */
    @Transactional
    List<T> getUserOrganizations(Locale locale, Long... excludeOrganizationsId);

    /**
     * Returns set of user organization object's ids that descendant of user organization with id of {@code parentOrganizationId}
     * regardless of visibility of organization objects to current user, i.e. it works for any user the same way as for admin user.
     * 
     * @param parentOrganizationId Id of given user organization object.
     * @return Ids of all descendant user organizations.
     */
    @Transactional
    Set<Long> getTreeChildrenOrganizationIds(long parentOrganizationId);

    /**
     * Calculates whether given {@code organization} play role of user organization, i.e. has user organization type
     * in set of organizations type attributes (recall that organization may play role of more one organization type at the same time).
     * 
     * @param organization Organization object.
     * @return true if {@code organization} is user organization.
     */
    boolean isUserOrganization(DomainObject organization);

    /**
     * Validates code of organization for existing. 
     * <p>
     * Note: 
     * If tested organization is new then {@code id} is <code>null</code>.
     * </p>
     * 
     * @param id Id of tested organization.
     * @param code Code of tested organization.
     * @return Id of any existing organization with the same code but another id if such exists and <code>null</code> otherwise.
     */
    @Transactional
    Long validateCode(Long id, String code);

    /**
     * Validates name of organization for existing.
     * 
     * @param id Id of tested organization.
     * @param name Name of tested organization.
     * @param locale Locale. It is used to validate only for names in given {@code locale}.
     * @return Id of any existing organization with the same name but another id if such exists and <code>null</code> otherwise.
     */
    @Transactional
    Long validateName(Long id, String name, Locale locale);

    /**
     * Returns code of {@code organization}.
     * 
     * @param organization Organization.
     * @return Organization's code.
     */
    String getCode(DomainObject organization);

    /**
     * Returns code of organization with {@code organizationId}.
     * 
     * @param organizationId Organization id.
     * @return Organization's code.
     */
    String getCode(long organizationId);

    /**
     * Return object id of organization by code
     * @param code Code
     * @return Organization object id
     */
    Long getObjectId(String code);

    /**
     * Figures out all outer (OSZNs and calculation centers) organizations visible to current user
     * and returns them sorted by organization's name in given {@code locale}.
     *
     * @param locale Locale. It is used in sorting of organizations by name.
     * @return All outer organizations visible to user.
     */
    @Transactional
    List<T> getAllOuterOrganizations(Locale locale);

    List<T> getOrganizations(List<Long> types,Locale locale);

    Long getModuleId();

    DomainObject getModule();

    String displayShortNameAndCode(DomainObject organization, Locale locale);

    String displayShortNameAndCode(Long organizationId, Locale locale);

    void setSqlSessionFactoryBean(SqlSessionFactoryBean sqlSessionFactoryBean);
}
