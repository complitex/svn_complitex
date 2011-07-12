package org.complitex.dictionary.web.component.name;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.service.NameBean;
import org.odlabs.wiquery.ui.autocomplete.AutocompleteAjaxComponent;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.02.11 15:41
 */
public class FullNamePanel extends Panel {
    private final static int AUTOCOMPLETE_SIZE = 10;

    @EJB(name = "NameBean")
    private NameBean nameBean;

    public FullNamePanel(String id, final IModel<Long> firstNameId, final IModel<Long> middleNameId,
                         final IModel<Long> lastNameId) {
        super(id);

        //First Name
        add(new AutocompleteAjaxComponent<String>("first_name",
                new Model<String>(nameBean.getFirstName(firstNameId.getObject()))) {
            {
                setRequired(true);
            }

            @Override
            protected void onValid() {
                firstNameId.setObject(nameBean.getFirstNameId(getConvertedInput(), true));
            }

            @Override
            public List<String> getValues(String term) {
                return nameBean.getFirstNames(term, AUTOCOMPLETE_SIZE);
            }

            @Override
            public String getValueOnSearchFail(String input) {
                return null;
            }
        });

        //Middle Name
        add(new AutocompleteAjaxComponent<String>("middle_name",
                new Model<String>(nameBean.getMiddleName(middleNameId.getObject()))) {
            {
                setRequired(true);
            }

            @Override
            protected void onValid() {
                middleNameId.setObject(nameBean.getMiddleNameId(getConvertedInput(), true));
            }

            @Override
            public List<String> getValues(String term) {
                return nameBean.getMiddleNames(term, AUTOCOMPLETE_SIZE);
            }

            @Override
            public String getValueOnSearchFail(String input) {
                return null;
            }
        });

        //Last Name
        add(new AutocompleteAjaxComponent<String>("last_name",
                new Model<String>(nameBean.getLastName(lastNameId.getObject()))) {
            {
                setRequired(true);
            }

            @Override
            protected void onValid() {
                lastNameId.setObject(nameBean.getLastNameId(getConvertedInput(), true));
            }

            @Override
            public List<String> getValues(String term) {
                return nameBean.getLastNames(term, AUTOCOMPLETE_SIZE);
            }

            @Override
            public String getValueOnSearchFail(String input) {
                return null;
            }
        });
    }
}
