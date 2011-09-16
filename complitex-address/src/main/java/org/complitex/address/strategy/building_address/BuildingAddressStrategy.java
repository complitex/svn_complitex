package org.complitex.address.strategy.building_address;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.resource.CommonResources;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Artem
 */
@Stateless
public class BuildingAddressStrategy extends TemplateStrategy {

    private static final String BUILDING_ADDRESS_NAMESPACE = BuildingAddressStrategy.class.getPackage().getName() + ".BuildingAddress";
    public static final long NUMBER = 1500;
    public static final long CORP = 1501;
    public static final long STRUCTURE = 1502;
    public static final long PARENT_STREET_ENTITY_ID = 300L;
    /**
     * It indicates sorting by combination of number, corp and structure
     */
    public static final long DEFAULT_ORDER_BY_ID = -1;
    @EJB
    private BuildingStrategy buildingStrategy;

    @Override
    public String getEntityTable() {
        return "building_address";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return null;
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample number = example.getAttributeExample(NUMBER);
            if (number == null) {
                number = new AttributeExample(NUMBER);
                example.addAttributeExample(number);
            }
            number.setValue(searchTextInput);
        }
        Long streetId = ids.get("street");
        if (streetId != null && streetId > 0) {
            example.setParentId(streetId);
            example.setParentEntity("street");
        } else {
            Long cityId = ids.get("city");
            if (cityId != null && cityId > 0) {
                example.setParentId(cityId);
                example.setParentEntity("city");
            } else {
                example.setParentId(-1L);
                example.setParentEntity("");
            }
        }
    }

    @Override
    public List<? extends DomainObject> find(DomainObjectExample example) {
        if (example.getId() != null && example.getId() <= 0) {
            return Collections.emptyList();
        }

        example.setTable(getEntityTable());
        prepareExampleForPermissionCheck(example);

        List<DomainObject> objects = sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + "." + FIND_OPERATION, example);
        for (DomainObject object : objects) {
            loadAttributes(object);
            //load subject ids
            object.setSubjectIds(loadSubjects(object.getPermissionId()));
        }
        return objects;
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return new ParentSearchCallback();
    }

    @Override
    public List<String> getParentSearchFilters() {
        return ImmutableList.of("country", "region", "city", "street");
    }

    private static class ParentSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(Component component, final Map<String, Long> ids, final AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            Long streetId = ids.get("street");
            if (streetId != null && streetId > 0) {
                inputPanel.getObject().setParentId(streetId);
                inputPanel.getObject().setParentEntityId(PARENT_STREET_ENTITY_ID);
            } else {
                Long cityId = ids.get("city");
                if (cityId != null && cityId > 0) {
                    inputPanel.getObject().setParentId(cityId);
                    inputPanel.getObject().setParentEntityId(400L);
                } else {
                    inputPanel.getObject().setParentId(null);
                    inputPanel.getObject().setParentEntityId(null);
                }
            }
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getRealChildren() {
        return new String[]{"building"};
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        return null;
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        return null;
    }

    @Override
    public Class<? extends WebPage> getListPage() {
        return null;
    }

    @Override
    public PageParameters getListPageParams() {
        return null;
    }

    @Override
    public String[] getParents() {
        return new String[]{"street"};
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        return null;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        return null;
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_EDIT};
    }

    @Transactional
    private List<DomainObjectPermissionInfo> findBuildingPermissionInfoByParent(long buildingAddressId) {
        return sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findBuildingPermissionInfoByParent", buildingAddressId);
    }

    @Transactional
    private List<DomainObjectPermissionInfo> findBuildingPermissionInfoByReference(long buildingAddressId) {
        return sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findBuildingPermissionInfoByReference", buildingAddressId);
    }

    @Transactional
    private List<DomainObjectPermissionInfo> findReferenceAddressPermissionInfo(long buildingId) {
        return sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findReferenceAddressPermissionInfo", buildingId);
    }

    @Transactional
    private List<DomainObjectPermissionInfo> findParentAddressPermissionInfo(long buildingId) {
        return sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findParentAddressPermissionInfo", buildingId);
    }

    @Transactional
    private Set<Long> findBuildingActivityInfoByParent(long buildingAddressId) {
        return Sets.newHashSet(sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findBuildingActivityInfoByParent", buildingAddressId));
    }

    @Transactional
    private Set<Long> findBuildingActivityInfoByReference(long buildingAddressId) {
        return Sets.newHashSet(sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findBuildingActivityInfoByReference", buildingAddressId));
    }

    @Transactional
    private Set<Long> findReferenceAddressActivityInfo(long buildingId) {
        return Sets.newHashSet(sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findReferenceAddressActivityInfo", buildingId));
    }

    @Transactional
    private Set<Long> findParentAddressActivityInfo(long buildingId) {
        return Sets.newHashSet(sqlSession().selectList(BUILDING_ADDRESS_NAMESPACE + ".findParentAddressActivityInfo", buildingId));
    }

    @Transactional
    @Override
    protected void replaceChildrenPermissions(long parentId, Set<Long> subjectIds) {
        long buildingAddressId = parentId;

        List<DomainObjectPermissionInfo> buildingPermissionInfoByParent = findBuildingPermissionInfoByParent(buildingAddressId);
        for (DomainObjectPermissionInfo buildingPermissionInfo : buildingPermissionInfoByParent) {
            long buildingId = buildingPermissionInfo.getId();
            List<DomainObjectPermissionInfo> referenceAddressPermissionInfos = findReferenceAddressPermissionInfo(buildingId);
            for (DomainObjectPermissionInfo referenceAddressPermissionInfo : referenceAddressPermissionInfos) {
                replaceObjectPermissions(referenceAddressPermissionInfo, subjectIds);
            }
            buildingStrategy.replacePermissions(buildingPermissionInfo, subjectIds);
        }

        List<DomainObjectPermissionInfo> buildingPermissionInfoByReference = findBuildingPermissionInfoByReference(buildingAddressId);
        for (DomainObjectPermissionInfo buildingPermissionInfo : buildingPermissionInfoByReference) {
            long buildingId = buildingPermissionInfo.getId();
            List<DomainObjectPermissionInfo> parentAddressPermissionInfos = findParentAddressPermissionInfo(buildingId);
            for (DomainObjectPermissionInfo parentAddressPermissionInfo : parentAddressPermissionInfos) {
                replaceObjectPermissions(parentAddressPermissionInfo, subjectIds);
            }
            buildingStrategy.replacePermissions(buildingPermissionInfo, subjectIds);
        }
    }

    @Transactional
    @Override
    protected void changeChildrenPermissions(long parentId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds) {
        long buildingAddressId = parentId;

        List<DomainObjectPermissionInfo> buildingPermissionInfoByParent = findBuildingPermissionInfoByParent(buildingAddressId);
        for (DomainObjectPermissionInfo buildingPermissionInfo : buildingPermissionInfoByParent) {
            long buildingId = buildingPermissionInfo.getId();
            List<DomainObjectPermissionInfo> referenceAddressPermissionInfos = findReferenceAddressPermissionInfo(buildingId);
            for (DomainObjectPermissionInfo referenceAddressPermissionInfo : referenceAddressPermissionInfos) {
                changeObjectPermissions(referenceAddressPermissionInfo, addSubjectIds, removeSubjectIds);
            }
            buildingStrategy.changePermissions(buildingPermissionInfo, addSubjectIds, removeSubjectIds);
        }

        List<DomainObjectPermissionInfo> buildingPermissionInfoByReference = findBuildingPermissionInfoByReference(buildingAddressId);
        for (DomainObjectPermissionInfo buildingPermissionInfo : buildingPermissionInfoByReference) {
            long buildingId = buildingPermissionInfo.getId();
            List<DomainObjectPermissionInfo> parentAddressPermissionInfos = findParentAddressPermissionInfo(buildingId);
            for (DomainObjectPermissionInfo parentAddressPermissionInfo : parentAddressPermissionInfos) {
                changeObjectPermissions(parentAddressPermissionInfo, addSubjectIds, removeSubjectIds);
            }
            buildingStrategy.changePermissions(buildingPermissionInfo, addSubjectIds, removeSubjectIds);
        }
    }

    @Transactional
    public void updateBuildingAddressActivity(long addressId, boolean enabled) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("addressId", addressId);
        params.put("enabled", enabled);
        params.put("status", enabled ? StatusType.INACTIVE : StatusType.ACTIVE);
        sqlSession().update(BUILDING_ADDRESS_NAMESPACE + ".updateBuildingAddressActivity", params);
    }

    @Transactional
    @Override
    public void changeChildrenActivity(long parentId, boolean enable) {
        long buildingAddressId = parentId;

        Set<Long> buildingActivityInfoByParent = findBuildingActivityInfoByParent(buildingAddressId);
        for (long buildingId : buildingActivityInfoByParent) {
            Set<Long> referenceAddressActivityInfo = findReferenceAddressActivityInfo(buildingId);
            for (long referenceAddressId : referenceAddressActivityInfo) {
                updateBuildingAddressActivity(referenceAddressId, !enable);
            }
            buildingStrategy.changeChildrenActivity(buildingId, enable);
        }
        updateChildrenActivity(buildingAddressId, "building", !enable);

        Set<Long> buildingActivityInfoByReference = findBuildingActivityInfoByReference(buildingAddressId);
        for (long buildingId : buildingActivityInfoByReference) {
            Set<Long> parentAddressActivityInfo = findParentAddressActivityInfo(buildingId);
            for (long parentAddressId : parentAddressActivityInfo) {
                updateBuildingAddressActivity(parentAddressId, !enable);
            }
            buildingStrategy.changeChildrenActivity(buildingId, enable);
            buildingStrategy.updateBuildingActivity(buildingId, !enable);
        }
    }
}
