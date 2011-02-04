/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.strategy;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
    String FIND_CHILDREN_OPERATION = "findChildren";

    void archive(DomainObject object);

    @Transactional
    void archiveAttributes(Collection<Long> attributeTypeIds, Date endDate);

    void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput);

    @Transactional
    int count(DomainObjectExample example);

    @Transactional
    void disable(DomainObject object);

    String displayDomainObject(DomainObject object, Locale locale);

    @Transactional
    void enable(DomainObject object);

    @Transactional
    List<? extends DomainObject> find(DomainObjectExample example);

    @Transactional
    DomainObject findById(Long id);

    @Transactional
    DomainObject findHistoryObject(long objectId, Date date);

    public static class RestrictedObjectInfo {

        private String entityTable;
        private Long id;

        public RestrictedObjectInfo(String entityTable, Long id) {
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
    RestrictedObjectInfo findParentInSearchComponent(long id, Date date);

    String getAttributeLabel(Attribute attribute, Locale locale);

    String[] getChildrenEntities();

    Class<? extends AbstractComplexAttributesPanel> getComplexAttributesPanelClass();

    long getDefaultOrderByAttributeId();

    Class<? extends WebPage> getEditPage();

    PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity);

    Entity getEntity();

    String getEntityTable();

    @Transactional
    List<History> getHistory(long objectId);

    TreeSet<Date> getHistoryDates(long objectId);

    Class<? extends WebPage> getHistoryPage();

    PageParameters getHistoryPageParams(long objectId);

    List<EntityAttributeType> getListColumns();

    Class<? extends WebPage> getListPage();

    PageParameters getListPageParams();

    ISearchCallback getParentSearchCallback();

    List<String> getParentSearchFilters();

    String[] getParents();

    String getPluralEntityLabel(Locale locale);

    ISearchCallback getSearchCallback();

    SearchComponentState getSearchComponentStateForParent(Long parentId, String parentEntity, Date date);

    List<String> getSearchFilters();

    int getSearchTextFieldSize();

    IValidator getValidator();

    @Transactional
    void insert(DomainObject object);

    boolean isSimpleAttribute(final Attribute attribute);

    boolean isSimpleAttributeType(EntityAttributeType entityAttributeType);

    DomainObject newInstance();

    Long performDefaultValidation(DomainObject object, Locale locale);

    @Transactional
    void update(DomainObject oldObject, DomainObject newObject, Date updateDate);

    @Transactional
    void updateAndPropagate(DomainObject oldObject, DomainObject newObject, Date updateDate);

    @Transactional
    void changeChildrenPermission(long parentId, Set<Long> subjectIds);

    boolean isNeedToChangePermission(Set<Long> oldSubjectIds, Set<Long> newSubjectIds);

    @Transactional
    Set<Long> loadSubjects(long permissionId);

    @Transactional
    void updatePermissionId(long objectId, long permissionId);

    @Transactional
    Long getNewPermissionId(Set<Long> newSubjectIds);

    @Transactional
    List<? extends DomainObjectPermissionInfo> findChildren(long parentId, String childEntity, int start, int size);

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

    String[] getEditRoles();
}
