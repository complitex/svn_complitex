package org.complitex.template.web.pages;

import com.google.common.collect.Ordering;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.IConfig;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 13:21:37
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ConfigEdit extends FormTemplatePage {

    @EJB(name = "ConfigBean")
    private ConfigBean configBean;

    public ConfigEdit() {
        super();

        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        Form form = new Form<>("form");
        add(form);

        final List<IConfig> configs = new ArrayList<IConfig>(configBean.getConfigs());

        final Map<IConfig, IModel<String>> model = new HashMap<IConfig, IModel<String>>();

        for (IConfig config : configs) {
            model.put(config, new Model<>(configBean.getString(config, true)));
        }

        //add localization bundles
        addAllResourceBundle(configBean.getResourceBundles());

        final Map<String, List<IConfig>> configGroupMap = configBean.getConfigGroups();

        ListView<String> groupNames = new ListView<String>("groupNames",
                Ordering.natural().immutableSortedCopy(configGroupMap.keySet())) {

            @Override
            protected void populateItem(ListItem<String> item) {
                String groupKey = item.getModelObject();

                item.add(new Label("groupName", getStringOrKey(groupKey)));

                item.add(new ListView<IConfig>("listView", configGroupMap.get(groupKey)) {

                    {
                        setReuseItems(true);
                    }

                    @Override
                    protected void populateItem(ListItem<IConfig> item) {
                        IConfig config = item.getModelObject();

                        item.add(new Label("label", getStringOrKey(config.name())));
                        item.add(new TextField<>("config", model.get(config)));
                    }
                });
            }
        };
        groupNames.setReuseItems(true);
        form.add(groupNames);

        Button save = new Button("save") {

            @Override
            public void onSubmit() {
                for (IConfig configName : configs) {
                    String value = model.get(configName).getObject();

                    if (!configBean.getString(configName, true).equals(value) && value != null) {
                        configBean.update(configName, value);
                    }
                }
                info(getString("saved"));
            }
        };
        form.add(save);
    }
}
