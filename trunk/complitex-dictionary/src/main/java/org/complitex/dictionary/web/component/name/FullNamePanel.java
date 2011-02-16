package org.complitex.dictionary.web.component.name;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.service.NameBean;

import javax.ejb.EJB;
import java.util.Iterator;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.02.11 15:41
 */
public class FullNamePanel extends Panel {
    @EJB(name = "NameBean")
    private NameBean nameBean;

    public FullNamePanel(String id, final IModel<Long> firstNameId, final IModel<Long> middleNameId,
                         final IModel<Long> lastNameId) {
        super(id);

        //First Name
        add(new AutoCompleteTextField<String>("first_name",
                new Model<String>(nameBean.getFirstName(firstNameId.getObject()))) {
            {
                setRequired(true);
            }

            @Override
            protected Iterator<String> getChoices(String input) {
                return nameBean.getFirstNames(input).iterator();
            }

            @Override
            protected void onValid() {
                firstNameId.setObject(nameBean.getFirstNameId(getConvertedInput(), true));
            }
        });

        //Middle Name
        add(new AutoCompleteTextField<String>("middle_name",
                new Model<String>(nameBean.getMiddleName(middleNameId.getObject()))) {
            {
                setRequired(true);
            }

            @Override
            protected Iterator<String> getChoices(String input) {
                return nameBean.getMiddleNames(input).iterator();
            }

            @Override
            protected void onValid() {
                middleNameId.setObject(nameBean.getMiddleNameId(getConvertedInput(), true));
            }
        });

        //Last Name
        add(new AutoCompleteTextField<String>("last_name",
                new Model<String>(nameBean.getLastName(lastNameId.getObject()))) {
            {
                setRequired(true);
            }

            @Override
            protected Iterator<String> getChoices(String input) {
                return nameBean.getLastNames(input).iterator();
            }

            @Override
            protected void onValid() {
                lastNameId.setObject(nameBean.getLastNameId(getConvertedInput(), true));
            }
        });
    }
}
