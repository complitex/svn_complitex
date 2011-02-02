/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.web.component.permission;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.PermissionBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;

/**
 *
 * @author Artem
 */
public final class DomainObjectPermissionsPanel extends Panel {

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;
    private static final DomainObject VISIBLE_BY_ALL = new DomainObject();

    static {
        VISIBLE_BY_ALL.setId(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
    }

    public DomainObjectPermissionsPanel(String id, Set<Long> subjectIds) {
        super(id);
        init(subjectIds);
    }

    private void init(final Set<Long> subjectIds) {
        final IModel<List<DomainObject>> allSubjectsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                List<DomainObject> list = Lists.newArrayList();
                list.addAll(organizationStrategy.getUserOrganizations(getLocale()));
                list.add(0, VISIBLE_BY_ALL);
                return list;
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                if (object.getId().equals(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID)) {
                    return getString("visible_by_all");
                } else {
                    return organizationStrategy.displayDomainObject(object, getLocale());
                }
            }
        };

        IModel<List<DomainObject>> subjectsModel = new IModel<List<DomainObject>>() {

            List<DomainObject> selectedSubjects = Lists.newArrayList(Iterables.transform(subjectIds, new Function<Long, DomainObject>() {

                @Override
                public DomainObject apply(final Long id) {
                    return Iterables.find(allSubjectsModel.getObject(), new Predicate<DomainObject>() {

                        @Override
                        public boolean apply(DomainObject input) {
                            return id.equals(input.getId());
                        }
                    });
                }
            }));

            @Override
            public List<DomainObject> getObject() {
                return selectedSubjects;
            }

            @Override
            public void setObject(List<DomainObject> subjects) {
                if (subjects != null && !subjects.isEmpty()) {
                    subjectIds.clear();
                    subjectIds.addAll(Lists.newArrayList(Iterables.transform(subjects, new Function<DomainObject, Long>() {

                        @Override
                        public Long apply(DomainObject subject) {
                            return subject.getId();
                        }
                    })));
                    normalizeSubjectIds(subjectIds);
                }
            }

            @Override
            public void detach() {
            }
        };

        ListMultipleChoice<DomainObject> subjects = new ListMultipleChoice<DomainObject>("subjects", subjectsModel, allSubjectsModel, renderer);
        add(subjects);
    }

    private void normalizeSubjectIds(Set<Long> subjectIds) {
        //check if visible-by-all subject has been selected along with some actual subjects(organizations)
        if (subjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) && subjectIds.size() > 1) {
            subjectIds.clear();
            subjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        }
    }
}
