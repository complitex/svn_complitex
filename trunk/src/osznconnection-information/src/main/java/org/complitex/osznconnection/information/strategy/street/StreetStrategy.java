/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.street;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.dao.StringCultureBean;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.Attribute;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.example.AttributeExample;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.web.DomainObjectListPanel;
import org.complitex.dictionaryfw.util.ResourceUtil;
import org.complitex.dictionaryfw.web.component.DomainObjectInputPanel;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.osznconnection.commons.web.pages.DomainObjectEdit;
import org.complitex.osznconnection.commons.web.pages.DomainObjectList;
import org.complitex.osznconnection.commons.web.pages.HistoryPage;
import org.complitex.osznconnection.information.resource.CommonResources;

/**
 *
 * @author Artem
 */
@Stateless
@Interceptors({SqlSessionInterceptor.class})
public class StreetStrategy extends Strategy {

    @EJB
    private StringCultureBean stringBean;

    private static final long NAME_ATTRIBUTE_TYPE_ID = 300L;

    @Override
    public String getEntityTable() {
        return "street";
    }

    @Override
    public List<DomainObject> find(DomainObjectExample example) {
        example.setTable(getEntityTable());
        return session.selectList("org.complitex.pspoffice.information.strategy.street.Street." + FIND_OPERATION, example);
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType attributeDescription) {
        return attributeDescription.getId() >= NAME_ATTRIBUTE_TYPE_ID;
    }

    @Override
    public List<EntityAttributeType> getListColumns() {
        return Lists.newArrayList(Iterables.filter(getEntity().getEntityAttributeTypes(), new Predicate<EntityAttributeType>() {

            @Override
            public boolean apply(EntityAttributeType attr) {
                return attr.getId().equals(NAME_ATTRIBUTE_TYPE_ID);
            }
        }));
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        return stringBean.displayValue(Iterables.find(object.getAttributes(), new Predicate<Attribute>() {

            @Override
            public boolean apply(Attribute attr) {
                return attr.getAttributeTypeId().equals(NAME_ATTRIBUTE_TYPE_ID);
            }
        }).getLocalizedValues(), locale);
    }

    @Override
    public List<String> getSearchFilters() {
        return ImmutableList.of("country", "region", "city");
    }

    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = null;
            try {
                attrExample = Iterables.find(example.getAttributeExamples(), new Predicate<AttributeExample>() {

                    @Override
                    public boolean apply(AttributeExample attrExample) {
                        return attrExample.getAttributeTypeId().equals(NAME_ATTRIBUTE_TYPE_ID);
                    }
                });
            } catch (NoSuchElementException e) {
                attrExample = new AttributeExample(NAME_ATTRIBUTE_TYPE_ID);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
        Long districtId = ids.get("district");
        if (districtId != null) {
            example.addAdditionalParam("district", districtId);
        }
        Long cityId = ids.get("city");
        example.setParentId(cityId);
        example.setParentEntity("city");
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    private static class SearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectListPanel list = component.findParent(DomainObjectListPanel.class);
            configureExampleImpl(list.getExample(), ids, null);
            list.refreshContent(target);
        }
    }

    @Override
    public ISearchCallback getParentSearchCallback() {
        return new ParentSearchCallback();
    }

    private static class ParentSearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
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

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[]{"building"};
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        return DomainObjectEdit.class;
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        PageParameters params = new PageParameters();
        params.put(DomainObjectEdit.ENTITY, getEntityTable());
        params.put(DomainObjectEdit.OBJECT_ID, objectId);
        params.put(DomainObjectEdit.PARENT_ID, parentId);
        params.put(DomainObjectEdit.PARENT_ENTITY, parentEntity);
        return params;
    }

    @Override
    public Class<? extends WebPage> getListPage() {
        return DomainObjectList.class;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters params = new PageParameters();
        params.put(DomainObjectList.ENTITY, getEntityTable());
        return params;
    }

    @Override
    public String[] getParents() {
        return new String[]{"city"};
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        return HistoryPage.class;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        PageParameters params = new PageParameters();
        params.put(HistoryPage.ENTITY, getEntityTable());
        params.put(HistoryPage.OBJECT_ID, objectId);
        return params;
    }
}
