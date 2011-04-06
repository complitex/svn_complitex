package org.complitex.organization.strategy;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.district.DistrictStrategy;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.DeleteException;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.Numbers;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.organization.strategy.web.edit.OrganizationValidator;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.*;
import org.complitex.dictionary.service.PermissionBean;

/**
 *
 * @author Artem
 */
@Stateless
public class OrganizationStrategy extends TemplateStrategy implements IOrganizationStrategy {

    private static final Logger log = LoggerFactory.getLogger(OrganizationStrategy.class);
    private static final String ORGANIZATION_NAMESPACE = OrganizationStrategy.class.getPackage().getName() + ".Organization";
    private static final String RESOURCE_BUNDLE = OrganizationStrategy.class.getName();
    @EJB
    private StringCultureBean stringBean;
    @EJB
    private DistrictStrategy districtStrategy;
    @EJB
    private LocaleBean localeBean;
    @EJB
    private PermissionBean permissionBean;

    @Override
    public String getEntityTable() {
        return "organization";
    }

    @Override
    protected List<Long> getListAttributeTypes() {
        return Lists.newArrayList(NAME);
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return stringBean.displayValue(object.getAttribute(NAME).getLocalizedValues(), locale);
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
    public boolean canPropagatePermissions(DomainObject object) {
        Long entityTypeId = object.getEntityTypeId();
        return entityTypeId != null && entityTypeId.equals(USER_ORGANIZATION);
    }

    @Transactional
    @Override
    public void insert(DomainObject object, Date insertDate) {
        super.insert(object, insertDate);
        changeDistrictPermissions(object);
    }

    @Transactional
    protected void changeDistrictPermissions(DomainObject newOrganization) {
        Long entityTypeId = newOrganization.getEntityTypeId();
        if (entityTypeId != null && entityTypeId.equals(USER_ORGANIZATION)) {
            Attribute districtAttribute = getDistrictAttribute(newOrganization);
            Long districtId = districtAttribute.getValueId();
            if (districtId != null) {
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
        Long entityTypeId = newOrganization.getEntityTypeId();
        if (entityTypeId != null && entityTypeId.equals(USER_ORGANIZATION)) {
            long organizationId = newOrganization.getId();
            Set<Long> subjectIds = Sets.newHashSet(organizationId);
            Attribute oldDistrictAttribute = getDistrictAttribute(oldOrganization);
            Attribute newDistrictAttribute = getDistrictAttribute(newOrganization);
            Long oldDistrictId = oldDistrictAttribute.getValueId();
            Long newDistrictId = newDistrictAttribute.getValueId();
            if (!Numbers.isEqual(oldDistrictId, newDistrictId)) {
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
            replaceObjectPermissions(childPermissionInfo, subjectIds);
        }
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    protected List<DomainObjectPermissionInfo> getTreeChildrenPermissionInfo(long parentId) {
        List<DomainObjectPermissionInfo> childrenPermissionInfo = sqlSession().selectList(ORGANIZATION_NAMESPACE
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

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public List<? extends DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        List<DomainObject> objects = sqlSession().selectList(ORGANIZATION_NAMESPACE + "." + FIND_OPERATION, example);

        for (DomainObject object : objects) {
            loadAttributes(object);
        }
        return objects;
    }

    @Transactional
    @Override
    public int count(DomainObjectExample example) {
        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);
        return (Integer) sqlSession().selectOne(DOMAIN_OBJECT_NAMESPACE + "." + COUNT_OPERATION, example);
    }

    @Override
    public Attribute getDistrictAttribute(DomainObject organization) {
        return organization.getAttribute(DISTRICT);
    }

    @Override
    public Attribute getParentAttribute(DomainObject organization) {
        return organization.getAttribute(USER_ORGANIZATION_PARENT);
    }

    @Override
    public String getDistrictCode(DomainObject organization) {
        String districtCode = null;
        Attribute districtAttribute = getDistrictAttribute(organization);
        if (districtAttribute != null) {
            districtCode = districtStrategy.getDistrictCode(districtAttribute.getValueId());
        }
        return districtCode;
    }

    @Transactional
    @Override
    public DomainObject getItselfOrganization() {
        DomainObjectExample example = new DomainObjectExample(ITSELF_ORGANIZATION_OBJECT_ID);
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        return find(example).get(0);
    }

    @Override
    public String getCode(DomainObject organization) {
        return stringBean.getSystemStringCulture(organization.getAttribute(CODE).getLocalizedValues()).getValue();
    }

    @Override
    public String getName(DomainObject organization, Locale locale) {
        return stringBean.displayValue(organization.getAttribute(NAME).getLocalizedValues(), locale);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public Long validateCode(Long id, String code, Long parentId, Long parentEntityId) {
        List<Long> results = sqlSession().selectList(ORGANIZATION_NAMESPACE + ".validateCode", code);
        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public Long validateName(Long id, String name, Long parentId, Long parentEntityId, Locale locale) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", name);
        params.put("localeId", localeBean.convert(locale).getId());
        List<Long> results = sqlSession().selectList(ORGANIZATION_NAMESPACE + ".validateName", params);
        for (Long result : results) {
            if (!result.equals(id)) {
                return result;
            }
        }
        return null;
    }

    @Transactional
    @Override
    public List<? extends DomainObject> getUserOrganizations(Locale locale, Long... excludeOrganizationsId) {
        DomainObjectExample example = new DomainObjectExample();
        example.setEntityTypeId(USER_ORGANIZATION);
        if (locale != null) {
            example.setOrderByAttributeTypeId(NAME);
            example.setLocaleId(localeBean.convert(locale).getId());
            example.setAsc(true);
        }
        configureExample(example, ImmutableMap.<String, Long>of(), null);
        List<? extends DomainObject> userOrganizations = find(example);
        if (excludeOrganizationsId == null) {
            return userOrganizations;
        }

        List<DomainObject> finalUserOrganizations = Lists.newArrayList();
        Set<Long> excludeSet = Sets.newHashSet(excludeOrganizationsId);
        for (DomainObject userOrganization : userOrganizations) {
            if (!excludeSet.contains(userOrganization.getId())) {
                finalUserOrganizations.add(userOrganization);
            }
        }
        return finalUserOrganizations;
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    @Override
    public Set<Long> getTreeChildrenOrganizationIds(long parentOrganizationId) {
        Set<Long> childrenIds = Sets.newHashSet(sqlSession().selectList(ORGANIZATION_NAMESPACE + ".findOrganizationChildrenObjectIds",
                parentOrganizationId));
        Set<Long> treeChildren = Sets.newHashSet(childrenIds);

        for (Long childId : childrenIds) {
            treeChildren.addAll(getTreeChildrenOrganizationIds(childId));
        }

        return treeChildren;
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ORGANIZATION_MODULE_EDIT};
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
        sqlSession().update(ORGANIZATION_NAMESPACE + "." + UPDATE_CHILDREN_ACTIVITY_OPERATION, params);
    }

    @Transactional
    @Override
    protected void deleteChecks(long objectId) throws DeleteException {
        if (permissionBean.isOrganizationPermissionExists(getEntityTable(), objectId)) {
            throw new DeleteException();
        }
        super.deleteChecks(objectId);
    }
}
