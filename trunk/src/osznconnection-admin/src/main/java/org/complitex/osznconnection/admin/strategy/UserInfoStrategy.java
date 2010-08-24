package org.complitex.osznconnection.admin.strategy;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.dictionaryfw.dao.aop.SqlSessionInterceptor;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.description.EntityAttributeType;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.08.2010 14:43:55
 */
@Stateless(name = "User_infoStrategy")
@Interceptors({SqlSessionInterceptor.class})
public class UserInfoStrategy extends Strategy {

    @Override
    public String getEntityTable() {
        return "user_info";
    }

    @Override
    public boolean isSimpleAttributeType(EntityAttributeType attributeType) {
        return true;
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
    public List<String> getSearchFilters() {
        return null;
    }

    @Override
    public ISearchCallback getSearchCallback() {
        return null;
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {

        return null;
    }

    @Override
    public void configureExample(DomainObjectExample example, Map<String, Long> ids, String searchTextInput) {
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
    public ISearchCallback getParentSearchCallback() {
        return null;
    }

    @Override
    public String[] getChildrenEntities() {
        return new String[0];
    }

    @Override
    public String[] getParents() {
        return null;
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        return null;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        return null;
    }
}
