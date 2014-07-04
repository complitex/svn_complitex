package org.complitex.organization.strategy;

import com.google.common.collect.*;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.SqlSessionFactoryBean;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.PermissionBean;
import org.complitex.dictionary.service.SequenceBean;
import org.complitex.dictionary.strategy.DeleteException;
import org.complitex.dictionary.strategy.DomainObjectPermissionInfo;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.util.Numbers;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.organization.strategy.web.edit.OrganizationEdit;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.organization.strategy.web.edit.OrganizationValidator;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.*;

/**
 *
 * @author Artem
 */
public abstract class AbstractOrganizationStrategy<T extends DomainObject> extends TemplateStrategy
        implements IOrganizationStrategy<T> {
    private static final String NS = AbstractOrganizationStrategy.class.getPackage().getName() + ".Organization";
    private static final String RESOURCE_BUNDLE = AbstractOrganizationStrategy.class.getName();

    @EJB
    private LocaleBean localeBean;

    @EJB
    private PermissionBean permissionBean;

    @EJB
    private SequenceBean sequenceBean;

    @EJB
    private StrategyFactory strategyFactory;

    @Override
    public String getEntityTable() {
        return "organization";
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME, CODE, USER_ORGANIZATION_PARENT);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return AttributeUtil.getStringCultureValue(object, NAME, locale);
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
                attrExample = new AttributeExample(NAME);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, getEntityTable(), locale);
    }

    @Override
    public boolean canPropagatePermissions(DomainObject organization) {
        return isUserOrganization(organization) && organization.getId() != null
                && !getTreeChildrenOrganizationIds(organization.getId()).isEmpty();
    }

    @Override
    public boolean isUserOrganization(DomainObject organization) {
        List<Long> organizationTypeIds = getOrganizationTypeIds(organization);
        return organizationTypeIds != null && organizationTypeIds.contains(OrganizationTypeStrategy.USER_ORGANIZATION_TYPE);
    }

    protected List<Long> getOrganizationTypeIds(DomainObject organization) {
        List<Long> organizationTypeIds = Lists.newArrayList();
        List<Attribute> organizationTypeAttributes = organization.getAttributes(ORGANIZATION_TYPE);
        if (organizationTypeAttributes != null && !organizationTypeAttributes.isEmpty()) {
            for (Attribute attribute : organizationTypeAttributes) {
                if (attribute.getValueId() != null) {
                    organizationTypeIds.add(attribute.getValueId());
                }
            }
        }
        return organizationTypeIds;
    }

    @Transactional
    @Override
    public void insert(DomainObject object, Date insertDate) {
        object.setId(sequenceBean.nextId(getEntityTable()));

        if (!object.getSubjectIds().contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID)) {
            object.getSubjectIds().add(object.getId());
        }

        object.setPermissionId(getNewPermissionId(object.getSubjectIds()));
        insertDomainObject(object, insertDate);
        for (Attribute attribute : object.getAttributes()) {
            attribute.setObjectId(object.getId());
            attribute.setStartDate(insertDate);
            insertAttribute(attribute);
        }

        changeDistrictPermissions(object);
    }

    @Transactional
    protected void changeDistrictPermissions(DomainObject newOrganization) {
        if (isUserOrganization(newOrganization)) {
            Attribute districtAttribute = newOrganization.getAttribute(DISTRICT);
            Long districtId = districtAttribute.getValueId();
            if (districtId != null) {
                IStrategy districtStrategy = strategyFactory.getStrategy("district");

                DomainObject districtObject = districtStrategy.findById(districtId, false);
                if (districtObject != null) {
                    Set<Long> addSubjectIds = Sets.newHashSet(newOrganization.getId());
                    districtStrategy.changePermissionsInDistinctThread(districtId, districtObject.getPermissionId(), addSubjectIds, null);
                }
            }
        }
    }

    @Transactional
    @Override
    public void update(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        super.update(oldObject, newObject, updateDate);
        changeDistrictPermissions(oldObject, newObject);
    }

    @Transactional
    protected void changeDistrictPermissions(DomainObject oldOrganization, DomainObject newOrganization) {
        if (isUserOrganization(newOrganization)) {
            long organizationId = newOrganization.getId();
            Set<Long> subjectIds = Sets.newHashSet(organizationId);
            Attribute oldDistrictAttribute = oldOrganization.getAttribute(DISTRICT);
            Attribute newDistrictAttribute = newOrganization.getAttribute(DISTRICT);
            Long oldDistrictId = oldDistrictAttribute != null ? oldDistrictAttribute.getValueId() : null;
            Long newDistrictId = newDistrictAttribute != null ? newDistrictAttribute.getValueId() : null;
            if (!Numbers.isEqual(oldDistrictId, newDistrictId)) {
                IStrategy districtStrategy = strategyFactory.getStrategy("district");

                //district reference has changed
                if (oldDistrictId != null) {
                    long oldDistrictPermissionId = districtStrategy.findById(oldDistrictId, true).getPermissionId();
                    districtStrategy.changePermissionsInDistinctThread(oldDistrictId, oldDistrictPermissionId, null, subjectIds);
                }

                if (newDistrictId != null) {
                    long newDistrictPermissionId = districtStrategy.findById(newDistrictId, true).getPermissionId();
                    districtStrategy.changePermissionsInDistinctThread(newDistrictId, newDistrictPermissionId, subjectIds, null);
                }
            }
        }
    }

    @Transactional
    @Override
    public void updateAndPropagate(DomainObject oldObject, DomainObject newObject, Date updateDate) {
        super.updateAndPropagate(oldObject, newObject, updateDate);
        changeDistrictPermissions(oldObject, newObject);
    }

    @Transactional
    @Override
    public void replaceChildrenPermissions(long parentId, Set<Long> subjectIds) {
        for (DomainObjectPermissionInfo childPermissionInfo : getTreeChildrenPermissionInfo(parentId)) {

            Set<Long> childSubjectIds = Sets.newHashSet(subjectIds);
            if (!childSubjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID)) {
                childSubjectIds.add(childPermissionInfo.getId());
            }

            replaceObjectPermissions(childPermissionInfo, childSubjectIds);
        }
    }

    @Transactional
    protected List<DomainObjectPermissionInfo> getTreeChildrenPermissionInfo(long parentId) {
        List<DomainObjectPermissionInfo> childrenPermissionInfo = sqlSession().selectList(NS
                + ".findOrganizationChildrenPermissionInfo", parentId);
        List<DomainObjectPermissionInfo> treeChildrenPermissionInfo = Lists.newArrayList(childrenPermissionInfo);
        for (DomainObjectPermissionInfo childPermissionInfo : childrenPermissionInfo) {
            treeChildrenPermissionInfo.addAll(getTreeChildrenPermissionInfo(childPermissionInfo.getId()));
        }
        return treeChildrenPermissionInfo;
    }

    @Override
    public void changePermissionsInDistinctThread(long objectId, long permissionId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changePermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IValidator getValidator() {
        return new OrganizationValidator(localeBean.getSystemLocale());
    }

    @Override
    public Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelAfterClass() {
        return OrganizationEditComponent.class;
    }

    @Transactional
    @Override
    public List<T> find(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return Collections.emptyList();
        }

        example.setTable(getEntityTable());
        if (!example.isAdmin()) {
            prepareExampleForPermissionCheck(example);
        }
        extendOrderBy(example);

        List<T> organizations = sqlSession().selectList(NS + "." + FIND_OPERATION, example);

        for (DomainObject object : organizations) {
            loadAttributes(object);
            //load subject ids
            object.setSubjectIds(loadSubjects(object.getPermissionId()));
        }

        return organizations;
    }

    @Transactional
    @Override
    public int count(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return 0;
        }
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);
        return sqlSession().selectOne(NS + "." + COUNT_OPERATION, example);
    }

    @Transactional
    @Override
    public Long validateCode(Long id, String code) {
        List<Long> results = sqlSession().selectList(NS + ".validateCode", code);
        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }
        return null;
    }

    @Transactional
    @Override
    public Long validateName(Long id, String name, Locale locale) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", name);
        params.put("localeId", localeBean.convert(locale).getId());
        List<Long> results = sqlSession().selectList(NS + ".validateName", params);
        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }
        return null;
    }

    @Transactional
    @Override
    public List<T> getUserOrganizations(Locale locale, Long... excludeOrganizationsId) {
        DomainObjectExample example = new DomainObjectExample();
        example.addAdditionalParam(ORGANIZATION_TYPE_PARAMETER, ImmutableList.of(OrganizationTypeStrategy.USER_ORGANIZATION_TYPE));
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        List<T> userOrganizations = find(example);

        if (excludeOrganizationsId == null) {
            return userOrganizations;
        }

        List<T> finalUserOrganizations = Lists.newArrayList();

        Set<Long> excludeSet = Sets.newHashSet(excludeOrganizationsId);

        for (T userOrganization : userOrganizations) {
            if (!excludeSet.contains(userOrganization.getId())) {
                finalUserOrganizations.add(userOrganization);
            }
        }

        return finalUserOrganizations;
    }

    @Transactional
    @Override
    public Set<Long> getTreeChildrenOrganizationIds(long parentOrganizationId) {
        List<Long> results = sqlSession().selectList(NS + ".findOrganizationChildrenObjectIds",
                parentOrganizationId);
        Set<Long> childrenIds = Sets.newHashSet(results);
        Set<Long> treeChildrenIds = Sets.newHashSet(childrenIds);

        for (Long childId : childrenIds) {
            treeChildrenIds.addAll(getTreeChildrenOrganizationIds(childId));
        }

        return Collections.unmodifiableSet(treeChildrenIds);
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ORGANIZATION_MODULE_EDIT};
    }

    @Override
    public String[] getListRoles() {
        return new String[]{SecurityRole.ORGANIZATION_MODULE_VIEW};
    }

    @Transactional
    @Override
    public void changeChildrenActivity(long parentId, boolean enable) {
        Set<Long> childrenIds = getTreeChildrenOrganizationIds(parentId);
        if (!childrenIds.isEmpty()) {
            updateChildrenActivity(childrenIds, !enable);
        }
    }

    @Transactional
    private void updateChildrenActivity(Set<Long> childrenIds, boolean enabled) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("childrenIds", childrenIds);
        params.put("enabled", enabled);
        params.put("status", enabled ? StatusType.INACTIVE : StatusType.ACTIVE);
        sqlSession().update(NS + "." + UPDATE_CHILDREN_ACTIVITY_OPERATION, params);
    }

    @Transactional
    @Override
    protected void deleteChecks(long objectId, Locale locale) throws DeleteException {
        if (permissionBean.isOrganizationPermissionExists(getEntityTable(), objectId)) {
            throw new DeleteException();
        }
        super.deleteChecks(objectId, locale);
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        return OrganizationEdit.class;
    }

    @Override
    public String getCode(DomainObject organization) {
        return AttributeUtil.getStringValue(organization, CODE);
    }

    @Override
    public String getCode(long organizationId) {
        DomainObject organization = findById(organizationId, true);
        return organization != null ? getCode(organization) : null;
    }

    @Override
    public Long getObjectId(String externalId) {
        return sqlSession().selectOne(NS + ".selectOrganizationObjectId", externalId);
    }

    public Long getObjectIdByCode(String code) {
        return sqlSession().selectOne(NS + ".selectOrganizationObjectIdByCode", code);
    }

    @Override
    @Transactional
    public List<T> getAllOuterOrganizations(Locale locale) {
        return null;
    }

    @Override
    public List<T> getOrganizations(List<Long> types, Locale locale) {
        DomainObjectExample example = new DomainObjectExample();

        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }

        example.addAdditionalParam(ORGANIZATION_TYPE_PARAMETER, types);

        configureExample(example, ImmutableMap.<String, Long>of(), null);

        return find(example);
    }

    public String displayShortNameAndCode(DomainObject organization, Locale locale) {
        final String fullName = AttributeUtil.getStringCultureValue(organization, NAME, locale);
        final String shortName = AttributeUtil.getStringCultureValue(organization, SHORT_NAME, locale);
        final String code = getCode(organization);
        final String name = !com.google.common.base.Strings.isNullOrEmpty(shortName) ? shortName : fullName;
        return name + " (" + code + ")";
    }

    @Override
    public String displayShortNameAndCode(Long organizationId, Locale locale) {
        if (organizationId == null){
            return "";
        }

        return displayShortNameAndCode(findById(organizationId, true), locale);
    }

    @Override
    public void setSqlSessionFactoryBean(SqlSessionFactoryBean sqlSessionFactoryBean) {
        super.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        localeBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        permissionBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
        sequenceBean.setSqlSessionFactoryBean(sqlSessionFactoryBean);
    }
}
