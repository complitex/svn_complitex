package org.complitex.correction.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.complitex.correction.entity.OrganizationCorrection;
import org.complitex.correction.service.OrganizationCorrectionBean;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.ModuleBean;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.organization.OrganizationPicker;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.01.14 18:11
 */
public class OrganizationCorrectionDialog extends Panel {
    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    @EJB
    private OrganizationCorrectionBean organizationCorrectionBean;

    @EJB
    private ModuleBean moduleBean;

    private Dialog dialog;
    private Form<OrganizationCorrection> form;

    public OrganizationCorrectionDialog(String id, final List<WebMarkupContainer> toUpdate) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setTitle(new ResourceModel("title"));
        dialog.setWidth(500);
        dialog.setModal(true);
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

        form.add(new OrganizationPicker("objectId", form.getModelObject()));

        form.add(new Label("correction"));

        form.add(new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                OrganizationCorrection correction = OrganizationCorrectionDialog.this.form.getModelObject();

                if (organizationCorrectionBean.getOrganizationCorrectionsCount(FilterWrapper.of(correction)) == 0){
                    organizationCorrectionBean.save(correction);

                    getSession().info(getString("info_correction_added"));
                }else {
                    getSession().error(getString("error_correction_exist"));
                }

                dialog.close(target);

                for (Component component : toUpdate) {
                    target.add(component);
                }
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    public void open(AjaxRequestTarget target, String correction, Long organizationId, Long userOrganizationId){
        form.setModelObject(new OrganizationCorrection(null, null, correction, organizationId, userOrganizationId,
                moduleBean.getModuleId()));

        target.add(form);

        dialog.open(target);
    }
}
