/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionaryfw.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.PersonAccount;
import org.complitex.osznconnection.file.service.PersonAccountLocalBean;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class PersonAccountEdit extends FormTemplatePage {

    public static final String CORRECTION_ID = "correction_id";

    @EJB(name = "PersonAccountLocalBean")
    private PersonAccountLocalBean personAccountLocalBean;

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    private Long correctionId;

    private PersonAccount personAccount;

    public PersonAccountEdit(PageParameters params) {
        this.correctionId = params.getAsLong(CORRECTION_ID);
//        if (isNew()) {
//            newPersonAccount = new PersonAccount();
//        } else {
        personAccount = personAccountLocalBean.findById(this.correctionId);
//        }
        init();
    }

//    private boolean isNew() {
//        return correctionId == null;
//    }
    private void saveOrUpdate() {
//        if (isNew()) {
//            personAccountLocalBean.insert(personAccount);
//        } else {
        personAccountLocalBean.update(personAccount);
//        }
    }

    private void init() {
        IModel<String> labelModel = new ResourceModel("label");
        add(new Label("title", labelModel));
        add(new Label("label", labelModel));

        FeedbackPanel messages = new FeedbackPanel("messages");
        add(messages);

        final IModel<PersonAccount> model = new CompoundPropertyModel<PersonAccount>(personAccount);
        Form<PersonAccount> form = new Form<PersonAccount>("form", model);
        add(form);

        form.add(new TextField<String>("lastName").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("firstName").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("middleName").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("city").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("street").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("buildingNumber").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("buildingCorp").setEnabled(false));
        form.add(new TextField<String>("apartment").setRequired(true).setEnabled(false));
        form.add(new TextField<String>("accountNumber").setRequired(true));
        form.add(new TextField<String>("ownNumSr").setRequired(true).setEnabled(false));

        final List<DomainObject> allOSZNs = organizationStrategy.getAllOSZNs();

        abstract class OrganizationModel extends Model<DomainObject> {

            @Override
            public DomainObject getObject() {
                final Long organizationId = getOrganizationId(model.getObject());
                if (organizationId != null) {
                    return Iterables.find(getOrganizations(), new Predicate<DomainObject>() {

                        @Override
                        public boolean apply(DomainObject object) {
                            return object.getId().equals(organizationId);
                        }
                    });
                }
                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                setOrganizationId(model.getObject(), object.getId());
            }

            public abstract Long getOrganizationId(PersonAccount personAccount);

            public abstract void setOrganizationId(PersonAccount personAccount, Long organizationId);

            public abstract List<DomainObject> getOrganizations();
        }
        IModel<DomainObject> osznModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId(PersonAccount personAccount) {
                return personAccount.getOsznId();
            }

            @Override
            public void setOrganizationId(PersonAccount personAccount, Long organizationId) {
                personAccount.setOsznId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allOSZNs;
            }
        };
        DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());
            }
        };
        DisableAwareDropDownChoice<DomainObject> oszn = new DisableAwareDropDownChoice<DomainObject>("oszn", osznModel, allOSZNs, renderer);
        oszn.setRequired(true);
        oszn.setEnabled(false);
        form.add(oszn);

        final List<DomainObject> allCalculationCentres = organizationStrategy.getAllCalculationCentres();
        IModel<DomainObject> calculationCenterModel = new OrganizationModel() {

            @Override
            public Long getOrganizationId(PersonAccount personAccount) {
                return personAccount.getCalculationCenterId();
            }

            @Override
            public void setOrganizationId(PersonAccount personAccount, Long organizationId) {
                personAccount.setCalculationCenterId(organizationId);
            }

            @Override
            public List<DomainObject> getOrganizations() {
                return allCalculationCentres;
            }
        };
        DisableAwareDropDownChoice<DomainObject> calculationCenter = new DisableAwareDropDownChoice<DomainObject>("calculationCenter",
                calculationCenterModel, allCalculationCentres, renderer);
        calculationCenter.setRequired(true);
        calculationCenter.setEnabled(false);
        form.add(calculationCenter);

        //save-cancel functional
        Button submit = new Button("submit") {

            @Override
            public void onSubmit() {
                saveOrUpdate();
                setResponsePage(PersonAccountList.class);
            }
        };
        form.add(submit);
        Link cancel = new Link("cancel") {

            @Override
            public void onClick() {
                setResponsePage(PersonAccountList.class);
            }
        };
        form.add(cancel);
    }
}

