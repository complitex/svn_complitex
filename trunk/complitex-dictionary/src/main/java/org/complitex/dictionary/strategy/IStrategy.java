/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.History;
import org.complitex.dictionary.entity.description.Entity;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;

import java.util.*;
import org.apache.wicket.Page;

/**
 *
 * @author Artem
 */
public interface IStrategy {

    String ARCHIVE_ATTRIBUTES_OPERATION = "archiveAttributes";
    String ATTRIBUTE_NAMESPACE = "org.complitex.dictionary.entity.Attribute";
    String COUNT_OPERATION = "count";
    String DOMAIN_OBJECT_NAMESPACE = "org.complitex.dictionary.entity.DomainObject";
    String FIND_BY_ID_OPERATION = "findById";
    String FIND_HISTORY_ATTRIBUTES_OPERATION = "findHistoryAttributes";
    String FIND_HISTORY_OBJECT_OPERATION = "findHistoryObject";
    String FIND_OPERATION = "find";
    String FIND_PARENT_IN_SEARCH_COMPONENT_OPERATION = "findParentInSearchComponent";
    String HAS_HISTORY_OPERATION = "hasHistory";
    String INSERT_OPERATION = "insert";
    String UPDATE_OPERATION = "update";
    String FIND_CHILDREN_PERMISSION_INFO_OPERATION = "findChildrenPermissionInfo";
    String FIND_CHILDREN_ACTIVITY_INFO_OPERATION = "findChildrenActivityInfo";
    String UPDATE_CHILDREN_ACTIVITY_OPERATION = "updateChildrenActivity";
    String DELETE_OPERATION = "delete";

    public static class DomainObjectPermissionInfo {

        private Long id;
        private Long permissionId;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getPermissionId() {
            return permissionId;
        }

        public void setPermissionId(Long permissionId) {
            this.permissionId = permissionId;
        }
    }

    @Transactional
    void archive(DomainObject object, Date endDate);

    @Transactional
    void archiveAttributes(Collection<Long> attributeTypeIds, Date endDate);

    void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput);

    @Transactional
    int count(DomainObjectExample example);

    void disable(DomainObject object);

    String displayDomainObject(DomainObject object, Locale locale);

    String displayAttribute(Attribute attribute, Locale locale);

    void enable(DomainObject object);

    @Transactional
    List<? extends DomainObject> find(DomainObjectExample example);

    @Transactional
    DomainObject findById(long id, boolean runAsAdmin);

    public Long getObjectId(final Long externalId);

    @Transactional
    DomainObject findHistoryObject(long objectId, Date date);

    public static class SimpleObjectInfo {

        private String entityTable;
        private Long id;

        public SimpleObjectInfo(String entityTable, Long id) {
            this.entityTable = entityTable;
            this.id = id;
        }

        public String getEntityTable() {
            return entityTable;
        }

        public Long getId() {
            return id;
        }
    }

    @Transactional
    SimpleObjectInfo findParentInSearchComponent(long id, Date date);

    String getAttributeLabel(Attribute attribute, Locale locale);

    String[] getRealChildren();

    String[] getLogicalChildren();

    Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelBeforeClass();

    Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelAfterClass();

    long getDefaultOrderByAttributeId();

    Class<? extends WebPage> getEditPage();

    PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity);

    Entity getEntity();

    String getEntityTable();

    @Transactional
    List<History> getHistory(long objectId);

    @Transactional
    TreeSet<Date> getHistoryDates(long objectId);

    Class<? extends WebPage> getHistoryPage();

    PageParameters getHistoryPageParams(long objectId);

    List<EntityAttributeType> getListColumns();
    
    long getDefaultSortAttributeTypeId();

    Class<? extends WebPage> getListPage();

    PageParameters getListPageParams();

    ISearchCallback getParentSearchCallback();

    List<String> getParentSearchFilters();

    String[] getParents();

    String getPluralEntityLabel(Locale locale);

    ISearchCallback getSearchCallback();

    @Transactional
    SearchComponentState getSearchComponentStateForParent(Long parentId, String parentEntity, Date date);

    List<String> getSearchFilters();

    int getSearchTextFieldSize();
    
    boolean allowProceedNextSearchFilter();

    IValidator getValidator();

    @Transactional
    void insert(DomainObject object, Date insertDate);

    boolean isSimpleAttribute(final Attribute attribute);

    boolean isSimpleAttributeType(EntityAttributeType entityAttributeType);

    DomainObject newInstance();

    @Transactional
    Long performDefaultValidation(DomainObject object, Locale locale);

    @Transactional
    void update(DomainObject oldObject, DomainObject newObject, Date updateDate);

    @Transactional
    void updateAndPropagate(DomainObject oldObject, DomainObject newObject, Date updateDate);

    @Transactional
    void replacePermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> subjectIds);

    boolean isNeedToChangePermission(Set<Long> oldSubjectIds, Set<Long> newSubjectIds);

    String[] getListRoles();

    String[] getEditRoles();

    @Transactional
    void changePermissions(DomainObjectPermissionInfo objectPermissionInfo, Set<Long> addSubjectIds, Set<Long> removeSubjectIds);

    void changePermissionsInDistinctThread(long objectId, long permissionId, Set<Long> addSubjectIds, Set<Long> removeSubjectIds);

    @Transactional
    void changeChildrenActivity(long parentId, boolean enable);

    boolean canPropagatePermissions(DomainObject object);

    @Transactional
    void delete(long objectId, Locale locale) throws DeleteException;

    String[] getDescriptionRoles();

    Page getObjectNotFoundPage();
}
