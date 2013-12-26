package org.complitex.correction.web.component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.dictionary.web.model.OrganizationModel;
import org.complitex.template.web.template.TemplateSession;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.List;
import java.util.Locale;

/**
 * Абстрактная панель для редактирования коррекций.
 * @author Artem
 */
public abstract class AbstractCorrectionEditPanel<T extends Correction> extends Panel {

    @EJB
    private SessionBean sessionBean;

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    private Long correctionId;

    private T correction;

    private WebMarkupContainer form;
    private Panel correctionInputPanel;
    private IModel<List<DomainObject>> allOuterOrganizationsModel;

    public AbstractCorrectionEditPanel(String id, Long correctionId) {
        super(id);

        this.correctionId = correctionId;

        correction = isNew() ? newCorrection() : getCorrection(correctionId);

        init();
    }

    public boolean isNew() {
        return correctionId == null;
    }

    protected abstract T getCorrection(Long correctionId);

    protected abstract T newCorrection();

    @Override
    public TemplateSession getSession() {
        return (TemplateSession) super.getSession();
    }

    protected T getCorrection() {
        return correction;
    }

    protected String displayCorrection() {
        return correction.getCorrection();
    }

    protected abstract IModel<String> internalObjectLabel(Locale locale);

    protected abstract WebMarkupContainer internalObjectPanel(String id);

    protected abstract String getNullObjectErrorMessage();

    protected String getNullCorrectionErrorMessage() {
        return new StringResourceModel("Required", Model.ofMap(ImmutableMap.of("label", getString("correction")))).getObject();
    }

    protected boolean freezeOrganization() {
        return false;
    }

    protected boolean checkCorrectionEmptiness() {
        return true;
    }

    protected final boolean validate() {
        boolean valid = preValidate();
        if (checkCorrectionEmptiness() && Strings.isEmpty(getCorrection().getCorrection())) {
            error(getNullCorrectionErrorMessage());
            valid = false;
        }

        if (getCorrection().getObjectId() == null) {
            error(getNullObjectErrorMessage());
            valid = false;
        }

        //calculation center must have null user organization but oszn must have non null user organization.
        if (isNew()) {
            boolean isOszn = false;  //todo fix validate
            //find outer organization object and determine whether it is oszn or calculation center.
//            for (DomainObject outerOrganization : allOuterOrganizationsModel.getObject()) {
//                if (outerOrganization.getId().equals(correction.getOrganizationId())) {
//                    //choosen outer organization found.
//                    isOszn = outerOrganization.getAttribute(IOsznOrganizationStrategy.ORGANIZATION_TYPE).getValueId().
//                            equals(OsznOrganizationTypeStrategy.OSZN);
//                    break;
//                }
//            }
//
//            if (isOszn && correction.getUserOrganizationId() == null) {
//                error(getString("oszn_must_have_user_organization"));
//                valid = false;
//            }
//
//            if (!isOszn && correction.getUserOrganizationId() != null) {
//                error(getString("calculation_center_must_not_have_user_organization"));
//                valid = false;
//            }
        }


        if (valid && validateExistence()) {
            error(getString("exist"));
            valid = false;
        }
        return valid;
    }

    protected boolean preValidate() {
        return true;
    }

    protected abstract boolean validateExistence();

    protected void back(boolean useScrolling) {
        PageParameters backPageParameters = getBackPageParameters();
        if (backPageParameters == null && useScrolling) {
            backPageParameters = new PageParameters();
        }
//        if (useScrolling) {
//            backPageParameters.set(AbstractCorrectionList.SCROLL_PARAMETER, getModel().getId());
//        }
        if (backPageParameters != null) {
            setResponsePage(getBackPageClass(), backPageParameters);
        } else {
            setResponsePage(getBackPageClass());
        }
    }

    protected abstract Class<? extends Page> getBackPageClass();

    protected abstract PageParameters getBackPageParameters();

    protected abstract void save();

    protected abstract void delete();

    public void executeDeletion() {
        try {
            delete();
            back(false);
        } catch (Exception e) {
            error(getString("db_error"));
            LoggerFactory.getLogger(getClass()).error("", e);
        }
    }

    protected WebMarkupContainer getFormContainer() {
        return form;
    }

    protected boolean isOrganizationCodeRequired() {
        return false;
    }

    protected Panel getCorrectionInputPanel(String id) {
        return new DefaultCorrectionInputPanel(id, new PropertyModel<String>(getCorrection(), "correction"));
    }

    protected abstract IModel<String> getTitleModel();

    protected void init() {
        IModel<String> titleModel = getTitleModel();
        add(new Label("title", titleModel));
        add(new Label("label", titleModel));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        form = new Form<Void>("form");
        add(form);

        WebMarkupContainer codeRequiredContainer = new WebMarkupContainer("codeRequiredContainer");
        form.add(codeRequiredContainer);

        boolean isOrganizationCodeRequired = isOrganizationCodeRequired();

        codeRequiredContainer.setVisible(isOrganizationCodeRequired);

        TextField<String> code = new TextField<>("code", new PropertyModel<String>(correction, "externalId"));
        code.setRequired(isOrganizationCodeRequired);

        form.add(code);

        allOuterOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return (List<DomainObject>) organizationStrategy.getAllOuterOrganizations(getLocale());
            }
        };

        final IModel<DomainObject> outerOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return correction.getOrganizationId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                correction.setOrganizationId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allOuterOrganizationsModel.getObject();
            }
        };
        final DomainObjectDisableAwareRenderer organizationRenderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        final DisableAwareDropDownChoice<DomainObject> organization = new DisableAwareDropDownChoice<>("organization",
                outerOrganizationModel, allOuterOrganizationsModel, organizationRenderer);
        if (freezeOrganization()) {
            organization.add(new AjaxFormComponentUpdatingBehavior("onchange") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    organization.setEnabled(false);
                    target.add(organization);
                    if (correctionInputPanel.isVisible() && freezeOrganization()) {
                        target.add(correctionInputPanel);
                    }
                }
            });
        }
        organization.setRequired(true);
        organization.setEnabled(isNew());
        form.add(organization);

        //user organization
        final IModel<List<DomainObject>> allUserOrganizationsModel = new LoadableDetachableModel<List<DomainObject>>() {

            @Override
            protected List<DomainObject> load() {
                return (List<DomainObject>) organizationStrategy.getUserOrganizations(getLocale());
            }
        };

        final IModel<DomainObject> userOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return correction.getUserOrganizationId();
            }

            @Override
            public void setOrganizationId(Long userOrganizationId) {
                correction.setUserOrganizationId(userOrganizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allUserOrganizationsModel.getObject();
            }
        };
        //todo change select to organization picker
        final DisableAwareDropDownChoice<DomainObject> userOrganization = new DisableAwareDropDownChoice<>("userOrganization",
                userOrganizationModel, allUserOrganizationsModel, organizationRenderer);
        userOrganization.setNullValid(true);
        userOrganization.setEnabled(isNew() && sessionBean.isAdmin());
        form.add(userOrganization);

        if (isNew()) {
            correction.setModuleId(organizationStrategy.getModuleId());
        }

        final List<DomainObject> internalOrganizations = Lists.newArrayList(organizationStrategy.getModule());
        IModel<DomainObject> internalOrganizationModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId() {
                return correction.getModuleId();
            }

            @Override
            public void setOrganizationId(Long organizationId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return internalOrganizations;
            }
        };
        DisableAwareDropDownChoice<DomainObject> internalOrganization = new DisableAwareDropDownChoice<>("internalOrganization",
                internalOrganizationModel, internalOrganizations, organizationRenderer);
        internalOrganization.setEnabled(false);
        form.add(internalOrganization);

        form.add(new Label("internalObjectLabel", internalObjectLabel(getLocale())));
        form.add(internalObjectPanel("internalObject"));

        //correction input panel
        correctionInputPanel = getCorrectionInputPanel("correctionInput");
        correctionInputPanel.setVisible(isNew());
        if (correctionInputPanel.isVisible() && freezeOrganization()) {
            correctionInputPanel.setOutputMarkupId(true);
        }
        form.add(correctionInputPanel);
        //correction label
        form.add(new Label("correctionLabel", !isNew() ? displayCorrection() : "").setVisible(!isNew()));

        //save-cancel functional
        AjaxSubmitLink submit = new AjaxSubmitLink("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (AbstractCorrectionEditPanel.this.validate()) {
                    save();
                    back(true);
                }else {
                    target.add(messages);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        };
        form.add(submit);

        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                back(true);
            }
        };
        form.add(cancel);
    }
}
