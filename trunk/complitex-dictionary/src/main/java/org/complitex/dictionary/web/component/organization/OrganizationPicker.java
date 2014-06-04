package org.complitex.dictionary.web.component.organization;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.util.AttributeUtil;
import org.complitex.dictionary.web.component.datatable.DataProvider;
import org.complitex.dictionary.web.component.paging.PagingNavigator;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.complitex.dictionary.strategy.organization.IOrganizationStrategy.*;

/**
 *
 * @author Artem
 */
public class OrganizationPicker extends FormComponentPanel<DomainObject> {

    private static final TextTemplate CENTER_DIALOG_JS =
            new PackageTextTemplate(OrganizationPicker.class, "CenterDialog.js");

    @EJB
    private LocaleBean localeBean;
    private boolean showData = false; //todo RadioGroup bug on showData = true
    private DomainObjectExample example;

    private IModel<Long> outerModel;

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    protected IOrganizationStrategy organizationStrategy;

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(new PackageResourceReference(
                OrganizationPicker.class, OrganizationPicker.class.getSimpleName() + ".css")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(
                OrganizationPicker.class, OrganizationPicker.class.getSimpleName() + ".js")));
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        if (outerModel != null && outerModel.getObject() == null && getModelObject() != null) {
            setModelObject(null);
        }
    }

    public OrganizationPicker(String id, IModel<Long> modelObject, final Long organizationTypeId) {
        this(id, modelObject, false, null, true, organizationTypeId);
    }

    public OrganizationPicker(String id, IModel<Long> modelObject, boolean required,
                              IModel<String> labelModel, boolean enabled, final Long organizationTypeId) {
        super(id);
        outerModel = modelObject;
        setModel(new Model<>(outerModel.getObject() == null? null : organizationStrategy.findById(outerModel.getObject(), true)));

        init(required, labelModel, enabled, organizationTypeId != null ? Arrays.asList(organizationTypeId) : null);
    }

    public OrganizationPicker(String id, Object modelObject, final Long... organizationTypeId) {
        this(id, modelObject, false, null, true, organizationTypeId);
    }

    public OrganizationPicker(String id, Object modelObject, List<Long> organizationTypeId) {
        this(id, modelObject, false, null, true, organizationTypeId);
    }

    public OrganizationPicker(String id, Object modelObject, boolean required,
                              IModel<String> labelModel, boolean enabled, final Long... organizationTypeId) {
        this(id, modelObject, required, labelModel, enabled, organizationTypeId != null ? Arrays.asList(organizationTypeId) : Collections.<Long>emptyList());
    }

    public OrganizationPicker(String id, Object modelObject, boolean required,
                              IModel<String> labelModel, boolean enabled, final List<Long> organizationTypeId) {
        super(id);
        outerModel = new PropertyModel<>(modelObject, id);
        setModel(new Model<>(outerModel.getObject() == null? null : organizationStrategy.findById(outerModel.getObject(), true)));
        init(required, labelModel, enabled, organizationTypeId);
    }

    public OrganizationPicker(String id, Long... organizationTypeIds) {
        this(id, null, false, null, true, Arrays.asList(organizationTypeIds));
    }

    public OrganizationPicker(String id, IModel<DomainObject> model, Long... organizationTypeIds) {
        this(id, model, false, null, true, Arrays.asList(organizationTypeIds));
    }

    public OrganizationPicker(String id, IModel<DomainObject> model, List<Long> organizationTypeIds) {
        this(id, model, false, null, true, organizationTypeIds);
    }

    public OrganizationPicker(String id, IModel<DomainObject> model, boolean required,
                              IModel<String> labelModel, boolean enabled, Long... organizationTypeIds) {
        this(id, model, required, labelModel, enabled, Arrays.asList(organizationTypeIds));
    }

    public OrganizationPicker(String id, IModel<DomainObject> model, boolean required,
            IModel<String> labelModel, boolean enabled, List<Long> organizationTypeIds) {
        super(id, model);
        init(required, labelModel, enabled, organizationTypeIds);
    }

    private void init(boolean required,
                      IModel<String> labelModel, boolean enabled, List<Long> organizationTypeIds) {

        setRequired(required);
        setLabel(labelModel);

        final Label organizationLabel = new Label("organizationLabel",
                new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        DomainObject organization = getModelObject();
                        if (organization != null) {
                            return organizationStrategy.displayShortNameAndCode(organization, getLocale());
                        } else {
                            return getString("organization_not_selected");
                        }
                    }
                });
        organizationLabel.setOutputMarkupId(true);
        add(organizationLabel);

        final Dialog lookupDialog = new Dialog("lookupDialog") {

            {
                getOptions().putLiteral("width", "auto");
            }
        };

        lookupDialog.setModal(true);
        lookupDialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        lookupDialog.setCloseOnEscape(false);
        add(lookupDialog);
        lookupDialog.setVisibilityAllowed(enabled);
        add(lookupDialog);

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupPlaceholderTag(true);
        lookupDialog.add(content);

        final Form<Void> filterForm = new Form<Void>("filterForm");
        content.add(filterForm);

        example = newExample(organizationTypeIds);

        final DataProvider<DomainObject> dataProvider = new DataProvider<DomainObject>() {

            @Override
            protected Iterable<? extends DomainObject> getData(long first, long count) {
                if (!showData) {
                    return Collections.emptyList();
                }
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                example.setStart(first);
                example.setSize(count);
                return organizationStrategy.find(example);
            }

            @Override
            protected int getSize() {
                if (!showData) {
                    return 0;
                }
                example.setLocaleId(localeBean.convert(getLocale()).getId());
                return organizationStrategy.count(example);
            }
        };

        filterForm.add(new TextField<>("nameFilter", new Model<String>() {

            @Override
            public String getObject() {
                return example.getAttributeExample(NAME).getValue();
            }

            @Override
            public void setObject(String name) {
                example.getAttributeExample(NAME).setValue(name);
            }
        }));
        filterForm.add(new TextField<>("codeFilter", new Model<String>() {

            @Override
            public String getObject() {
                return example.getAttributeExample(CODE).getValue();
            }

            @Override
            public void setObject(String code) {
                example.getAttributeExample(CODE).setValue(code);
            }
        }));

        final IModel<DomainObject> organizationModel = new Model<>();

        final AjaxLink<Void> select = new AjaxLink<Void>("select") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (organizationModel.getObject() == null) {
                    throw new IllegalStateException("Unexpected behaviour.");
                } else {
                    if (OrganizationPicker.this.getModelObject() != null &&
                            !organizationModel.getObject().getId().equals(OrganizationPicker.this.getModelObject().getId())) {
                        onUpdate(target);
                    }
                    if (outerModel != null) {
                        outerModel.setObject(organizationModel.getObject().getId());
                    }
                    OrganizationPicker.this.getModel().setObject(organizationModel.getObject());
                    clearAndCloseLookupDialog(organizationModel, target, lookupDialog, content, this);
                    target.add(organizationLabel);
                }
            }
        };
        select.setOutputMarkupPlaceholderTag(true);
        select.setVisible(false);
        content.add(select);

        final RadioGroup<DomainObject> radioGroup = new RadioGroup<DomainObject>("radioGroup", organizationModel);
        radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                toggleSelectButton(select, target, organizationModel);
            }
        });
        filterForm.add(radioGroup);

        DataView<DomainObject> data = new DataView<DomainObject>("data", dataProvider) {

            @Override
            protected void populateItem(Item<DomainObject> item) {
                final DomainObject organization = item.getModelObject();

                item.add(new Radio<>("radio", item.getModel(), radioGroup));
                item.add(new Label("name", AttributeUtil.getStringCultureValue(organization, NAME, getLocale())));
                item.add(new Label("code", organizationStrategy.getCode(organization)));
            }
        };
        radioGroup.add(data);

        PagingNavigator pagingNavigator = new PagingNavigator("navigator", data, content) {

            @Override
            public boolean isVisible() {
                return showData;
            }
        };
        pagingNavigator.setOutputMarkupPlaceholderTag(true);
        content.add(pagingNavigator);

        IndicatingAjaxButton find = new IndicatingAjaxButton("find", filterForm) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                showData = true;
                target.add(content);
                target.appendJavaScript(CENTER_DIALOG_JS.asString(
                        ImmutableMap.of("dialogId", lookupDialog.getMarkupId())));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        };
        filterForm.add(find);

        AjaxLink<Void> cancel = new AjaxLink<Void>("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                clearAndCloseLookupDialog(organizationModel, target, lookupDialog, content, select);
            }
        };
        content.add(cancel);

        AjaxLink<Void> choose = new AjaxLink<Void>("choose") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                lookupDialog.open(target);
            }
        };
        choose.setVisibilityAllowed(enabled);
        add(choose);
    }

    private void toggleSelectButton(Component select, AjaxRequestTarget target, IModel<DomainObject> organizationModel) {
        boolean wasVisible = select.isVisible();
        select.setVisible(organizationModel.getObject() != null);
        if (select.isVisible() ^ wasVisible) {
            target.add(select);
        }
    }

    private void clearAndCloseLookupDialog(IModel<DomainObject> organizationModel,
            AjaxRequestTarget target, Dialog lookupDialog, WebMarkupContainer content, Component select) {
        organizationModel.setObject(null);
        select.setVisible(false);
        this.showData = false;
        clearExample();
        target.add(content);
        lookupDialog.close(target);
    }

    private DomainObjectExample newExample(List<Long> organizationTypeIds) {
        DomainObjectExample e = new DomainObjectExample();
        e.addAttributeExample(new AttributeExample(NAME));
        e.addAttributeExample(new AttributeExample(CODE));
        if (organizationTypeIds != null && !organizationTypeIds.isEmpty()) {
            e.addAdditionalParam(ORGANIZATION_TYPE_PARAMETER, organizationTypeIds);
        }
        return e;
    }

    private void clearExample() {
        example.getAttributeExample(NAME).setValue(null);
        example.getAttributeExample(CODE).setValue(null);
    }

    @Override
    protected void convertInput() {
        setConvertedInput(getModelObject());
    }

    protected void onUpdate(AjaxRequestTarget target) {

    }
}
