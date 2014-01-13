package org.complitex.correction.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.correction.service.OrganizationCorrectionBean;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.organization.web.component.OrganizationPicker;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.01.14 18:11
 */
public class OrganizationCorrectionDialog extends Panel {
    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    @EJB
    private OrganizationCorrectionBean organizationCorrectionBean;

    private Dialog dialog;
    private Form<OrganizationCorrection> form;

    public OrganizationCorrectionDialog(String id, final Component toUpdate) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setTitle(new ResourceModel("title"));
        dialog.setWidth(500);
        add(dialog);

        form = new Form<>("form", new CompoundPropertyModel<>(Model.of(new OrganizationCorrection())));
        dialog.add(form);

        //Организация
        form.add(new Label("organizationId", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return organizationStrategy.displayShortNameAndCode(form.getModelObject().getOrganizationId(), getLocale());
            }
        }));

        //Пользовательская организация
        form.add(new Label("userOrganizationId", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return organizationStrategy.displayShortNameAndCode(form.getModelObject().getUserOrganizationId(), getLocale());
            }
        }));

        form.add(new OrganizationPicker("objectId", new Model<DomainObject>() {
            @Override
            public DomainObject getObject() {
                if (form.getModelObject().getObjectId() != null) {
                    return organizationStrategy.findById(form.getModelObject().getObjectId(), true);
                }

                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                form.getModelObject().setObjectId(object.getId());
            }
        }));

        form.add(new Label("correction"));

        form.add(new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                organizationCorrectionBean.save(OrganizationCorrectionDialog.this.form.getModelObject());

                dialog.close(target);
                target.add(toUpdate);
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    public void open(AjaxRequestTarget target, String correction, Long organizationId, Long userOrganizationId, Long moduleId){
        form.setModelObject(new OrganizationCorrection(null, null, correction, organizationId, userOrganizationId, moduleId));

        target.add(form);

        dialog.open(target);
    }
}
