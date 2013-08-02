/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.room;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.resource.CommonResources;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.DomainObjectListPanel;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.template.strategy.TemplateStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.complitex.address.strategy.room.web.edit.RoomEdit;

/**
 *
 * @author Artem
 */
@Stateless
public class RoomStrategy extends TemplateStrategy {

    @EJB
    private StringCultureBean stringBean;

    /*
     * Attribute type ids
     */
    public static final Long NAME = 200L;

    @Override
    public String getEntityTable() {
        return "room";
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
    public List<String> getSearchFilters() {
        return ImmutableList.of("country", "region", "city", "street", "building", "apartment");
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return new SearchCallback();
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        configureExampleImpl(example, ids, searchTextInput);
    }

    private static void configureExampleImpl(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
        if (!Strings.isEmpty(searchTextInput)) {
            AttributeExample attrExample = example.getAttributeExample(NAME);
            if (attrExample == null) {
                attrExample = new AttributeExample(NAME);
                example.addAttributeExample(attrExample);
            }
            attrExample.setValue(searchTextInput);
        }
        Long apartmentId = ids.get("apartment");
        if (apartmentId != null && apartmentId > 0) {
            example.setParentId(apartmentId);
            example.setParentEntity("apartment");
        } else {
            Long buildingId = ids.get("building");
            if (buildingId != null && buildingId > 0) {
                example.setParentId(buildingId);
                example.setParentEntity("building");
            } else {
                example.setParentId(-1L);
                example.setParentEntity("");
            }
        }
    }

    private static class SearchCallback implements ISearchCallback, Serializable {

        @Override
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
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
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
            DomainObjectInputPanel inputPanel = component.findParent(DomainObjectInputPanel.class);
            DomainObject object = inputPanel.getObject();
            Long apartmentId = ids.get("apartment");
            if (apartmentId != null && apartmentId > 0) {
                object.setParentId(apartmentId);
                object.setParentEntityId(100L);
            } else {
                Long buildingId = ids.get("building");
                if (buildingId != null && buildingId > 0) {
                    object.setParentId(buildingId);
                    object.setParentEntityId(500L);
                } else {
                    object.setParentId(null);
                    object.setParentEntityId(null);
                }
            }
        }
    }

    @Override
    public String getPluralEntityLabel(Locale locale) {
        return ResourceUtil.getString(CommonResources.class.getName(), getEntityTable(), locale);
    }

    @Override
    public String[] getParents() {
        return new String[]{"apartment", "building"};
    }

    @Override
    public int getSearchTextFieldSize() {
        return 5;
    }

    @Override
    public boolean allowProceedNextSearchFilter() {
        return true;
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_EDIT, SecurityRole.ROOM_EDIT};
    }

    @Override
    public String[] getListRoles() {
        return new String[]{SecurityRole.ADDRESS_MODULE_VIEW};
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        return RoomEdit.class;
    }

    @Override
    protected void extendOrderBy(DomainObjectExample example) {
        if (example.getOrderByAttributeTypeId() != null
                && example.getOrderByAttributeTypeId().equals(NAME)) {
            example.setOrderByNumber(true);
        }
    }
}
