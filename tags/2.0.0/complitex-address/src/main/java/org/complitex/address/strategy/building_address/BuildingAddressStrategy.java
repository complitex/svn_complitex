package org.complitex.address.strategy.building_address;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.address.resource.CommonResources;

import javax.ejb.Stateless;
import java.util.*;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Parameter;
import org.complitex.dictionary.entity.StatusType;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.SearchComponent;
import org.complitex.template.strategy.AbstractStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless(name = "Building_addressStrategy")
public class BuildingAddressStrategy extends AbstractStrategy {

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

    @Transactional
    @Override
    public void enable(DomainObject object) {
        object.setStatus(StatusType.ACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), object));
    }

    @Transactional
    @Override
    public void disable(DomainObject object) {
        object.setStatus(StatusType.INACTIVE);
        sqlSession().update(DOMAIN_OBJECT_NAMESPACE + "." + UPDATE_OPERATION, new Parameter(getEntityTable(), object));
    }
}
