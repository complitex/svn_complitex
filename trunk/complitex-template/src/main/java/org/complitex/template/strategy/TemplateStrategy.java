package org.complitex.template.strategy;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.dictionary.strategy.web.validate.DefaultValidator;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.template.web.pages.DomainObjectEdit;
import org.complitex.template.web.pages.DomainObjectList;
import org.complitex.template.web.pages.HistoryPage;
import org.complitex.template.web.pages.ObjectNotFoundPage;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
public abstract class TemplateStrategy extends Strategy {

    public static final String ENTITY = "entity";
    public static final String STRATEGY = "strategy";
    public static final String OBJECT_ID = "object_id";
    public static final String PARENT_ID = "parent_id";
    public static final String PARENT_ENTITY = "parent_entity";
    public static final String BACK_INFO_SESSION_KEY = "back_info_session_key";

    @Override
    public Class<? extends WebPage> getListPage() {
        return DomainObjectList.class;
    }

    @Override
    public PageParameters getListPageParams() {
        PageParameters params = new PageParameters();
        params.put(ENTITY, getEntityTable());
        return params;
    }

    @Override
    public Class<? extends WebPage> getEditPage() {
        return DomainObjectEdit.class;
    }

    @Override
    public PageParameters getEditPageParams(Long objectId, Long parentId, String parentEntity) {
        PageParameters params = new PageParameters();
        params.put(ENTITY, getEntityTable());
        params.put(OBJECT_ID, objectId);
        params.put(PARENT_ID, parentId);
        params.put(PARENT_ENTITY, parentEntity);
        return params;
    }

    @Override
    public Class<? extends WebPage> getHistoryPage() {
        return HistoryPage.class;
    }

    @Override
    public PageParameters getHistoryPageParams(long objectId) {
        PageParameters params = new PageParameters();
        params.put(ENTITY, getEntityTable());
        params.put(OBJECT_ID, objectId);
        return params;
    }

    @Override
    public IValidator getValidator() {
        return new DefaultValidator(getEntityTable());
    }

    @Override
    public Page getObjectNotFoundPage() {
        return new ObjectNotFoundPage(getListPage(), getListPageParams());
    }

    @Override
    public String[] getListRoles() {
        return new String[]{SecurityRole.AUTHORIZED};
    }
}
