package org.complitex.dictionary.web.component.permission;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.PermissionBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.DisableAwareListMultipleChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.odlabs.wiquery.core.javascript.JsQuery;

import javax.ejb.EJB;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;

/**
 *
 * @author Artem
 */
public class DomainObjectPermissionsPanel extends AbstractDomainObjectPermissionPanel {

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    protected static final DomainObject VISIBLE_BY_ALL = new DomainObject();

    static {
        VISIBLE_BY_ALL.setId(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
    }
    private Set<Long> subjectIds;
    private Set<Long> parentSubjectIds;

    public DomainObjectPermissionsPanel(String id, DomainObjectPermissionParameters parameters) {
        super(id, parameters);
        setEnabled(parameters.isEnabled());
        this.subjectIds = parameters.getSubjectIds();
        this.parentSubjectIds = parameters.getParentSubjectIds();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        init();
    }

    private void init() {
        final IModel<List<DomainObject>> userSubjectsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                List<DomainObject> list = newArrayList();
                list.addAll(organizationStrategy.getUserOrganizations(getLocale()));
                list.add(0, VISIBLE_BY_ALL);
                return list;
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public String getDisplayValue(DomainObject object) {
                if (object.getId().equals(VISIBLE_BY_ALL.getId())) {
                    return getString("visible_by_all");
                } else {
                    return organizationStrategy.displayDomainObject(object, getLocale());
                }
            }
        };

        IModel<List<DomainObject>> subjectsModel = new IModel<List<DomainObject>>() {

            private List<DomainObject> selectedSubjects = newArrayList();

            {
                Set<Long> selectedSubjectIds = parentSubjectIds != null && !parentSubjectIds.isEmpty() ? parentSubjectIds
                        : subjectIds;
                for (DomainObject userSubject : userSubjectsModel.getObject()) {
                    for (long organizationId : selectedSubjectIds) {
                        if (userSubject.getId().equals(organizationId)) {
                            selectedSubjects.add(userSubject);
                        }
                    }
                }
            }

            @Override
            public List<DomainObject> getObject() {
                return selectedSubjects;
            }

            @Override
            public void setObject(List<DomainObject> subjects) {
                for (DomainObject userSubject : userSubjectsModel.getObject()) {
                    boolean selected = false;
                    for (DomainObject selectedSubject : subjects) {
                        if (selectedSubject.getId().equals(userSubject.getId())) {
                            selected = true;
                            break;
                        }
                    }
                    if (selected) {
                        subjectIds.add(userSubject.getId());
                    } else {
                        subjectIds.remove(userSubject.getId());
                    }
                }
                normalizeSubjectIds(subjectIds);
            }

            @Override
            public void detach() {
            }
        };

        DisableAwareListMultipleChoice<DomainObject> subjects =
                newSubjectsSelectComponent("subjects", subjectsModel, userSubjectsModel, renderer);
        add(subjects);
    }

    protected DisableAwareListMultipleChoice<DomainObject> newSubjectsSelectComponent(String id,
            IModel<List<DomainObject>> subjectsModel, IModel<List<DomainObject>> allSubjectsModel,
            DomainObjectDisableAwareRenderer renderer) {
        return new DisableAwareListMultipleChoice<DomainObject>(id, subjectsModel, allSubjectsModel, renderer) {

            @SuppressWarnings("unchecked")
            @Override
            protected void appendOptionHtml(AppendingStringBuffer buffer, DomainObject choice, int index, String selected) {
                /*
                 * Copy from source code for org.apache.wicket.markup.html.form.AbstractChoice.appendOptionHtml()
                 */
                String objectValue = (String) getChoiceRenderer().getDisplayValue(choice);

                String displayValue = "";
                if (objectValue != null) {
                    displayValue = objectValue.toString();
                }
                buffer.append("\n<option ");
                if (isSelected(choice, index, selected)) {
                    buffer.append("selected=\"selected\" ");
                }
                if (isDisabled(choice, index, selected)) {
                    buffer.append("disabled=\"disabled\" ");
                }

                enhanceOptionHtml(buffer, choice, index, selected);

                buffer.append("value=\"");
                buffer.append(Strings.escapeMarkup(getChoiceRenderer().getIdValue(choice, index)));
                buffer.append("\">");

                String display = displayValue;
                if (localizeDisplayValues()) {
                    display = getLocalizer().getString(displayValue, this, displayValue);
                }
                CharSequence escaped = display;
                if (getEscapeModelStrings()) {
                    escaped = escapeOptionHtml(display);
                }
                buffer.append(escaped);
                buffer.append("</option>");
            }
        };
    }

    protected Map<String, String> enhanceOptionWithAttributes(DomainObject choice, int index, String selected) {
        if (choice.getId().equals(VISIBLE_BY_ALL.getId())) {
            return of("data-all", "data-all");
        }
        return null;
    }

    protected void enhanceOptionHtml(AppendingStringBuffer optionHtmlBuffer, DomainObject choice, int index, String selected) {
        Map<String, String> attributesMap = enhanceOptionWithAttributes(choice, index, selected);
        if (attributesMap != null && !attributesMap.isEmpty()) {
            for (Map.Entry<String, String> entry : attributesMap.entrySet()) {
                String attribute = entry.getKey();
                String value = "\"" + entry.getValue() + "\"";
                optionHtmlBuffer.append(attribute + " = " + value + " ");
            }
        }
    }

    protected void normalizeSubjectIds(Set<Long> subjectIds) {
        if (subjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID) && subjectIds.size() > 1) {
            subjectIds.clear();
            subjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(DomainObjectPermissionsPanel.class,
                DomainObjectPermissionsPanel.class.getSimpleName() + ".js")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(DomainObjectPermissionsPanel.class,
                DomainObjectPermissionsPanel.class.getSimpleName() + ".css")));

        response.render(OnDomReadyHeaderItem.forScript(new JsQuery(this).$().chain("permission_select").render()));
    }
}
