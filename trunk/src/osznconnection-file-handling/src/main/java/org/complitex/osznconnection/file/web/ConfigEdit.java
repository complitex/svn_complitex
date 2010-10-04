package org.complitex.osznconnection.file.web;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.osznconnection.commons.web.security.SecurityRole;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.ConfigName;
import org.complitex.osznconnection.file.service.ConfigBean;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 13:21:37
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ConfigEdit extends FormTemplatePage{
    @EJB(name = "ConfigEdit")
    private ConfigBean configBean;

    public ConfigEdit() {
        super();

        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        Form form = new Form("form");
        add(form);

        final Map<ConfigName, IModel<String>> model = new HashMap<ConfigName, IModel<String>>();
        for (ConfigName configName : ConfigName.values()){
            model.put(configName, new Model<String>(configBean.getString(configName, true)));
        }

        ListView<ConfigName> listView = new ListView<ConfigName>("listView", Arrays.asList(ConfigName.values())){

            @Override
            protected void populateItem(ListItem<ConfigName> item) {
                ConfigName configName = item.getModelObject();

                item.add(new Label("label", getStringOrKey(configName)));
                item.add(new TextField<String>("config", model.get(configName)));
            }
        };
        listView.setReuseItems(true);
        form.add(listView);

        Button save = new Button("save"){
            @Override
            public void onSubmit() {
                for (ConfigName  configName : ConfigName.values()){
                    String value = model.get(configName).getObject();

                    if (!configBean.getString(configName, true).equals(value)){
                        configBean.update(configName, value);
                    }
                }
                info(getString("saved"));
            }
        };
        form.add(save);
    }
}
