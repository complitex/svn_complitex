package org.complitex.osznconnection.admin.strategy;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.dictionaryfw.entity.DomainObject;

import javax.ejb.Stateless;
import java.util.Locale;
import org.complitex.osznconnection.commons.strategy.AbstractStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.08.2010 14:43:55
 */
@Stateless(name = "User_infoStrategy")
public class UserInfoStrategy extends AbstractStrategy {

    @Override
    public String getEntityTable() {
        return "user_info";
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
    public String displayDomainObject(DomainObject object, Locale locale) {
        return null;
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
    public Class<? extends WebPage> getHistoryPage() {
        return null;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        return null;
    }
}
