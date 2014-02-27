package org.complitex.dictionary.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.entity.UserGroup.GROUP_NAME;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.exception.WrongCurrentPasswordException;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.DictionaryFwSession;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 19:00
 */
@Stateless
@DeclareRoles(SessionBean.CHILD_ORGANIZATION_VIEW_ROLE)
public class SessionBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = SessionBean.class.getName();
    private static final String ORGANIZATION_ENTITY = "organization";
    public static final String CHILD_ORGANIZATION_VIEW_ROLE = "CHILD_ORGANIZATION_VIEW";

    @Resource
    private SessionContext sessionContext;

    @EJB
    private IUserProfileBean userProfileBean;

    @EJB
    private StrategyFactory strategyFactory;

    @EJB(lookup = IOrganizationStrategy.BEAN_LOOKUP)
    private IOrganizationStrategy<DomainObject> organizationStrategy;

    public boolean isAdmin() {
        final Set<GROUP_NAME> userGroups = getCurrentUserGroups();
        return userGroups.contains(GROUP_NAME.ADMINISTRATORS);
    }

    private Set<GROUP_NAME> getCurrentUserGroups() {
        final String login = sessionContext.getCallerPrincipal().getName();
        List<String> ugs = sqlSession().selectList(MAPPING_NAMESPACE + ".getUserGroups", login);
        if (ugs != null && !ugs.isEmpty()) {
            final Set<GROUP_NAME> userGroups = EnumSet.noneOf(GROUP_NAME.class);
            for (String ug : ugs) {
                GROUP_NAME group;
                try {
                    group = GROUP_NAME.valueOf(ug);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("User with login `" + login + "` is member of unknown user group: `" + ug + "`", e);
                }
                userGroups.add(group);
            }
            return ImmutableSet.copyOf(userGroups);
        }
        throw new IllegalStateException("User with login `" + login + "` is not member of any user group.");
    }

    public Long getCurrentUserId() {
        return sqlSession().selectOne(MAPPING_NAMESPACE + ".selectUserId", getCurrentUserLogin());
    }

    public String getCurrentUserLogin() {
        return sessionContext.getCallerPrincipal().getName();
    }

    public List<Long> getUserOrganizationObjectIds() {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationObjectIds", getCurrentUserLogin());
    }

    private List<Long> getOrganizationChildrenObjectId(Long parentObjectId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectOrganizationChildrenObjectIds", parentObjectId);
    }

    public List<Long> getUserOrganizationTreeObjectIds() {
        List<Long> objectIds = new ArrayList<>();

        for (Long objectId : getUserOrganizationObjectIds()) {
            addChildOrganizations(objectIds, objectId);
        }

        return objectIds;
    }

    private void addChildOrganizations(List<Long> objectIds, Long objectId) {
        objectIds.add(objectId);

        for (Long id : getOrganizationChildrenObjectId(objectId)) {
            if (!objectIds.contains(id)) {
                addChildOrganizations(objectIds, id);
            }
        }
    }

    private List<Long> getUserOrganizationTreePermissionIds(String table) {
        Map<String, String> parameter = new HashMap<>();
        parameter.put("table", table);
        parameter.put("organizations", getUserOrganizationTreeString());

        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectUserOrganizationTreePermissionIds", parameter);
    }

    public String getUserOrganizationTreeString(){
        String s = "";
        String d = "";

        for (Long p : getUserOrganizationTreeObjectIds()) {
            s += d + p;
            d = ", ";
        }

        return !Strings.isEmpty(s) ? "(" + s + ")" : "(0)";
    }

    public String getOrganizationString(Long organizationId){
        String s = "";
        String d = "";

        List<Long> ids = new ArrayList<>();
        addChildOrganizations(ids, organizationId);

        for (Long p : ids) {
            s += d + p;
            d = ", ";
        }

        return !Strings.isEmpty(s) ? "(" + s + ")" : "(0)";
    }

    /**
     * Loads main user's organization id from database.
     *
     * @return main organization id
     */
    private Long getMainUserOrganizationObjectId() {
            return sqlSession().selectOne(MAPPING_NAMESPACE + ".selectMainOrganizationObjectId", getCurrentUserLogin());
    }

    private List<Long> getUserOrganizationPermissionIds(final String table) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectUserOrganizationPermissionIds",
                new HashMap<String, String>() {

                    {
                        put("table", table);
                        put("login", getCurrentUserLogin());
                    }
                });
    }

    public String getPermissionString(String table) {
        List<Long> permissions = sessionContext.isCallerInRole(CHILD_ORGANIZATION_VIEW_ROLE)
                ? getUserOrganizationTreePermissionIds(table)
                : getUserOrganizationPermissionIds(table);
        permissions.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);

        String s = "";
        String d = "";

        for (Long p : permissions) {
            s += d + p;
            d = ", ";
        }

        return "(" + s + ")";
    }

    public String getCurrentUserFullName(Locale locale) {
        return userProfileBean.getFullName(getCurrentUserId(), locale);
    }

    public String getMainUserOrganizationName(DictionaryFwSession session) {
        try {
            IStrategy strategy = strategyFactory.getStrategy(ORGANIZATION_ENTITY);
            DomainObject mainUserOrganization = getMainUserOrganization(session);
            return mainUserOrganization != null && mainUserOrganization.getId() != null
                    ? strategy.displayDomainObject(mainUserOrganization, session.getLocale()) : "";
        } catch (Exception e) {
            return "[NA]";
        }
    }

    /**
     * Loads main user's organization from database.
     *
     * @return
     */
    public DomainObject loadMainUserOrganization() {
        IStrategy strategy = strategyFactory.getStrategy(ORGANIZATION_ENTITY);
        Long oId = getMainUserOrganizationObjectId();

        return oId != null ? strategy.findById(oId, true) : null;
    }

    @Transactional
    public void updatePassword(String currentPassword, final String password) throws WrongCurrentPasswordException {
        userProfileBean.updatePassword(currentPassword, password);
    }

    public boolean isBlockedUser(String login) {
        int userGroupCount = (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".getUserGroupCount", login);
        return userGroupCount == 0;
    }

    /**
     * Loads main user's organization from session at first and if it doesn't find then fallbacks to loading from database.
     *
     * @param session
     * @return
     */
    public DomainObject getMainUserOrganization(DictionaryFwSession session) {
        DomainObject sessionOrganization = session.getMainUserOrganization();

        if (sessionOrganization != null && sessionOrganization.getId() != null) {
            return sessionOrganization;
        } else {
            DomainObject mainUserOrganization = loadMainUserOrganization();
            session.setMainUserOrganization(mainUserOrganization);

            return mainUserOrganization;
        }
    }

    /**
     * Updates main user's organization in database and session.
     */
    @Transactional
    public void updateMainUserOrganization(DictionaryFwSession session, DomainObject mainUserOrganization) {
        userProfileBean.updateMainUserOrganization(mainUserOrganization.getId());
        session.setMainUserOrganization(mainUserOrganization);
    }

    public String getAllOuterOrganizationsString() {
        String s = "";
        String d = "";

        for (long id : getAllOuterOrganizationObjectIds()) {
            s += d + id;
            d = ",";
        }

        return "(" + s + ")";
    }

    private List<Long> getAllOuterOrganizationObjectIds() {
        List<Long> objectIds = new ArrayList<>();

        for (DomainObject o : organizationStrategy.getAllOuterOrganizations(null)) {
            objectIds.add(o.getId());
        }

        return objectIds;
    }

    private boolean hasOuterOrganization(Long objectId) {
        return getAllOuterOrganizationObjectIds().contains(objectId);
    }

    public boolean isAuthorized(Long outerOrganizationObjectId, Long userOrganizationId) {
        return isAdmin()
                || (hasOuterOrganization(outerOrganizationObjectId) && isUserOrganizationVisibleToCurrentUser(userOrganizationId));
    }

    /**
     * Returns main user's organization by means
     *  of {@link SessionBean#getMainUserOrganization(DictionaryFwSession)}
     *
     * @param session dictionary session.
     * @return
     */
    public Long getCurrentUserOrganizationId(DictionaryFwSession session) {
        DomainObject mainUserOrganization = getMainUserOrganization(session);

        return mainUserOrganization != null && mainUserOrganization.getId() != null
                && mainUserOrganization.getId() > 0 ? mainUserOrganization.getId() : null;
    }

    public void prepareFilterForPermissionCheck(AbstractFilter filter) {
        filter.setAdmin(isAdmin());

        if (!isAdmin()) {
            //filter.setOuterOrganizationsString(getAllOuterOrganizationsString());
            filter.setUserOrganizationsString(getCurrentUserOrganizationsString());
        }
    }

    public void prepareFilterForPermissionCheck(FilterWrapper filter) {
        filter.setAdmin(isAdmin());

        if (!isAdmin()) {
            filter.setOuterOrganizationsString(getAllOuterOrganizationsString());
            filter.setUserOrganizationsString(getCurrentUserOrganizationsString());
        }
    }

    public String getCurrentUserOrganizationsString() {
        return getUserOrganizationsString(getUserOrganizationIdsVisibleToCurrentUser());
    }

    private String getUserOrganizationsString(Set<Long> userOrganizationIds) {
        String s = "";
        String d = "";

        for (long p : userOrganizationIds) {
            s += d + p;
            d = ", ";
        }

        return "(" + s + ")";
    }

    private Set<Long> getUserOrganizationIdsVisibleToCurrentUser() {
        return sessionContext.isCallerInRole(SessionBean.CHILD_ORGANIZATION_VIEW_ROLE)
                ? Sets.newHashSet(getUserOrganizationTreeObjectIds())
                : Sets.newHashSet(getUserOrganizationObjectIds());
    }

    public String getMainUserOrganizationString(Long userOrganizationId) {
        return getUserOrganizationsString(Sets.newHashSet(userOrganizationId));
    }

    public String getMainUserOrganizationString(Long userOrganizationId, boolean nullOnAdmin) {
        return nullOnAdmin && isAdmin() ? null : getUserOrganizationsString(Sets.newHashSet(userOrganizationId));
    }

    private boolean isUserOrganizationVisibleToCurrentUser(Long userOrganizationId) {
        return userOrganizationId == null || getUserOrganizationIdsVisibleToCurrentUser().contains(userOrganizationId);
    }

}
