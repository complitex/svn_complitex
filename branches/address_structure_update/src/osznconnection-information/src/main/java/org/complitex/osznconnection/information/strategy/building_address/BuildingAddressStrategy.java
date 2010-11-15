package org.complitex.osznconnection.information.strategy.building_address;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Serializable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.osznconnection.information.resource.CommonResources;

import javax.ejb.Stateless;
import java.util.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.InsertParameter;
import org.complitex.dictionaryfw.entity.StatusType;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless(name = "Building_addressStrategy")
public class BuildingAddressStrategy extends Strategy {

    private static final Logger log = LoggerFactory.getLogger(BuildingAddressStrategy.class);

    public static final long NUMBER = 1500;

    public static final long CORP = 1501;

    public static final long STRUCTURE = 1502;

    @Override
    public String getEntityTable() {
        return "building_address";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return null;
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return null;
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    @Override
    public List<String> getSearchFilters() {
        return null;
    }

//    public DomainObjectExample createExample(String number, String corp, String structure, Long streetId, Long cityId) {
//        DomainObjectExample buildingAddressExample = new DomainObjectExample();
//        AttributeExample numberAttrEx = new AttributeExample(NUMBER);
//        numberAttrEx.setValue(number);
//        buildingAddressExample.addAttributeExample(numberAttrEx);
//        AttributeExample corpAttrEx = new AttributeExample(CORP);
//        corpAttrEx.setValue(corp);
//        buildingAddressExample.addAttributeExample(corpAttrEx);
//        AttributeExample structureAttrEx = new AttributeExample(STRUCTURE);
//        structureAttrEx.setValue(structure);
//        buildingAddressExample.addAttributeExample(structureAttrEx);
//        if (streetId != null && streetId > 0) {
//            buildingAddressExample.setParentId(streetId);
//            buildingAddressExample.setParentEntity("street");
//        } else {
//            buildingAddressExample.setParentId(cityId);
//            buildingAddressExample.setParentEntity("city");
//        }
//        return buildingAddressExample;
//    }
    @Override
    public DomainObject findById(Long id) {
        return super.findById(id);
    }

    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample number = null;
            try {
                number = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {

                    @Override
                    public boolean apply(AttributeExample attrExample) {
                        return attrExample.getAttributeTypeId().equals(NUMBER);
                    }
                });
            } catch (NoSuchElementException e) {
                number = new AttributeExample(NUMBER);
                example.addAttributeExample(number);
            }
            number.setValue(searchTextInput);
        }
        Long streetId = ids.get("street");
        if (streetId != null) {
            example.setParentId(streetId);
            example.setParentEntity("street");
        } else {
            Long cityId = ids.get("city");
            if (cityId != null) {
                example.setParentId(cityId);
                example.setParentEntity("city");
            } else {
                example.setParentId(null);
                example.setParentEntity(null);
            }
        }

    }

//    private static class SearchCallback implements ISearchCallback, Serializable {
//
//        @Override
//        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
//            DomainObjectListPanel list = component.findParent(DomainObjectListPanel.class);
//            DomainObjectExample example = list.getExample();
//            configureExampleImpl(example, ids, null);
//            list.refreshContent(target);
//        }
//    }
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
        public void found(SearchComponent component, final Map<String, Long> ids, final AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            Long streetId = ids.get("street");
            if (streetId != null && streetId > 0) {
                inputPanel.getObject().setParentId(streetId);
                inputPanel.getObject().setParentEntityId(300L);
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

//            Long cityId = ids.get("city");
//            if (cityId != null && cityId > 0) {
//                inputPanel.getObject().setParentId(cityId);
//                inputPanel.getObject().setParentEntityId(400L);
//            } else {
//                inputPanel.getObject().setParentId(null);
//                inputPanel.getObject().setParentEntityId(null);
//            }
//
//            component.getPage().visitChildren(SearchComponent.class, new IVisitor<SearchComponent>() {
//
//                @Override
//                public Object component(SearchComponent searchComponent) {
//                    if (target != null) {
//                        target.addComponent(searchComponent);
//                    }
//                    return CONTINUE_TRAVERSAL;
//                }
//            });
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[]{"building"};
    }

//    @Override
//    public RestrictedObjectInfo findParentInSearchComponent(long id, Date date) {
//        RestrictedObjectInfo info = super.findParentInSearchComponent(id, date);
//        log.info("INFO: entity: {}, id: {}", info.getEntityTable(), info.getId());
//        return info;
//    }
//    @Override
//    public SearchComponentState getSearchComponentStateForParent(Long parentId, String parentEntity, Date date) {
//        SearchComponentState state = super.getSearchComponentStateForParent(parentId, parentEntity, date);
//        log.info("PARENT ID: {}, PARENT ENTITY: {}, STATE: {}", new Object[]{parentId, parentEntity, state});
//        return state;
//    }
//    @Override
//    @Transactional
//    public RestrictedObjectInfo findParentInSearchComponent(long id, Date startDate) {
//        DomainObjectExample example = new DomainObjectExample();
//        example.setTable(getEntityTable());
//        example.setId(id);
//        Long streetId = (Long) sqlSession().selectOne(BUILDING_ADDRESS_NAMESPACE + ".findStreetInSearchComponent", example);
//        if (streetId != null) {
//            return new RestrictedObjectInfo("street", streetId);
//        } else {
//            return super.findParentInSearchComponent(id, startDate);
//        }
//    }
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
    public int getSearchTextFieldSize() {
        return 5;
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        return null;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        return null;
    }

    @Transactional
    @Override
    public void enable(DomainObject object) {
        object.setStatus(StatusType.ACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), object));
    }

    @Transactional
    @Override
    public void disable(DomainObject object) {
        object.setStatus(StatusType.INACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new InsertParameter(getEntityTable(), object));
    }
}
