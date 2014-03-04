package org.complitex.organization.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.PermissionBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.fieldset.CollapsibleFieldset;
import org.complitex.dictionary.web.component.list.AjaxRemovableListView;
import org.complitex.dictionary.web.component.permission.AbstractDomainObjectPermissionPanel;
import org.complitex.dictionary.web.component.permission.DomainObjectPermissionParameters;
import org.complitex.organization_type.strategy.OrganizationTypeStrategy;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class DomainObjectPermissionPanel extends AbstractDomainObjectPermissionPanel {
    
    private enum PermissionMode {
        
        ALL, SELECT
    }
    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;
    
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderCSSReference(new PackageResourceReference(DomainObjectPermissionPanel.class,
                DomainObjectPermissionPanel.class.getSimpleName() + ".css"));
    }
    
    public DomainObjectPermissionPanel(String id, final DomainObjectPermissionParameters parameters) {
        super(id, parameters);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        final DomainObjectPermissionParameters parameters = getParameters();
        
        final Set<Long> organizationSubjectIds = parameters.getSubjectIds();
        
        Set<Long> selectedSubjectIds =
                parameters.getParentSubjectIds() != null && !parameters.getParentSubjectIds().isEmpty()
                ? parameters.getParentSubjectIds() : organizationSubjectIds;
        
        CollapsibleFieldset permissionsFieldset = new CollapsibleFieldset("permissionsFieldset",
                new ResourceModel("permissions"), false);
        add(permissionsFieldset);
        
        boolean visibleByAll = selectedSubjectIds.size() == 1
                && selectedSubjectIds.contains(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
        final IModel<PermissionMode> permissionModeModel =
                new Model<>(visibleByAll ? PermissionMode.ALL : PermissionMode.SELECT);
        
        final WebMarkupContainer organizationsContainer = new WebMarkupContainer("organizationsContainer") {
            
            @Override
            public boolean isVisible() {
                return permissionModeModel.getObject() == PermissionMode.SELECT;
            }
        };
        organizationsContainer.setOutputMarkupPlaceholderTag(true);
        permissionsFieldset.add(organizationsContainer);
        
        RadioChoice<PermissionMode> permissionMode = new RadioChoice<PermissionMode>("permissionMode",
                permissionModeModel, Arrays.asList(PermissionMode.values()),
                new EnumChoiceRenderer<PermissionMode>(this));
        
        final List<DomainObject> selectedSubjects = initializeSelectedSubjects(selectedSubjectIds);
        
        permissionMode.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                PermissionMode mode = permissionModeModel.getObject();
                if (mode == PermissionMode.ALL) {
                    organizationSubjectIds.clear();
                    organizationSubjectIds.add(PermissionBean.VISIBLE_BY_ALL_PERMISSION_ID);
                } else {
                    setupSubjectIds(organizationSubjectIds, selectedSubjects);
                }
                target.add(organizationsContainer);
            }
        });
        permissionMode.setSuffix("");
        permissionsFieldset.add(permissionMode);
        
        ListView<DomainObject> organizations = new AjaxRemovableListView<DomainObject>("organizations", selectedSubjects) {
            
            @Override
            protected void populateItem(ListItem<DomainObject> item) {
                final WebMarkupContainer fakeContainer = new WebMarkupContainer("fakeContainer");
                item.add(fakeContainer);
                
                item.add(new Label("number", new AbstractReadOnlyModel<String>() {
                    
                    @Override
                    public String getObject() {
                        return String.valueOf(getCurrentIndex(fakeContainer) + 1);
                    }
                }));
                
                IModel<DomainObject> organizationModel = new Model<DomainObject>() {
                    
                    @Override
                    public DomainObject getObject() {
                        int index = getCurrentIndex(fakeContainer);
                        return selectedSubjects.get(index);
                    }
                    
                    @Override
                    public void setObject(DomainObject organization) {
                        int index = getCurrentIndex(fakeContainer);
                        selectedSubjects.set(index, organization);
                        
                        setupSubjectIds(organizationSubjectIds, selectedSubjects);
                    }
                };
                DomainObject organization = item.getModelObject();
                organizationModel.setObject(organization);
                
                boolean allowModifyOrganization = isAllowModifyOrganization(getCurrentIndex(fakeContainer),
                        organization != null ? organization.getId() : null);
                OrganizationPicker organizationPicker = new OrganizationPicker("organizationPicker",
                        organizationModel, false, null, parameters.isEnabled() && allowModifyOrganization,
                        Arrays.asList(OrganizationTypeStrategy.USER_ORGANIZATION_TYPE));
                item.add(organizationPicker);
                
                addRemoveLink("removeOrganization", item, null, organizationsContainer).
                        setVisible(parameters.isEnabled() && allowModifyOrganization);
            }
            
            @Override
            protected AjaxLink<Void> getRemoveLink(String linkId, final Component toFocus, final Component... toUpdate) {
                return new AjaxLink<Void>(linkId) {
                    
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        updateListViewOnRemoval(this, target, toFocus, toUpdate);
                        
                        setupSubjectIds(organizationSubjectIds, selectedSubjects);
                    }
                };
            }
        };
        organizationsContainer.add(organizations);
        
        AjaxLink<Void> addOrganization = new AjaxLink<Void>("addOrganization") {
            
            @Override
            public void onClick(AjaxRequestTarget target) {
                DomainObject newSubject = null;
                selectedSubjects.add(newSubject);
                target.add(organizationsContainer);
            }
        };
        organizationsContainer.add(addOrganization);
    }
    
    protected boolean isAllowModifyOrganization(int index, Long organizationId) {
        return true;
    }
    
    protected List<DomainObject> initializeSelectedSubjects(Set<Long> selectedSubjectIds) {
        List<DomainObject> selectedSubjects = new ArrayList<>();
        for (long selecetSubjectId : selectedSubjectIds) {
            if (selecetSubjectId > 0) {
                DomainObject o = organizationStrategy.findById(selecetSubjectId, false);
                if (o != null) {
                    selectedSubjects.add(o);
                }
            }
        }
        return selectedSubjects;
    }

    private void setupSubjectIds(Set<Long> subjectIds, List<DomainObject> selectedSubjects) {
        subjectIds.clear();
        for (DomainObject selectedSubject : selectedSubjects) {
            if (selectedSubject != null && selectedSubject.getId() != null && selectedSubject.getId() > 0) {
                subjectIds.add(selectedSubject.getId());
            }
        }
    }
}
