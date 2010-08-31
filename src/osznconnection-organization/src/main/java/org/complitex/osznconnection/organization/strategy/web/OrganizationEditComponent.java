/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web;

import com.google.common.collect.ImmutableMap;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.strategy.web.AbstractComplexAttributesPanel;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

import javax.ejb.EJB;
import java.util.List;

/**
 *
 * @author Artem
 */
public class OrganizationEditComponent extends AbstractComplexAttributesPanel {

    @EJB(name = "OrganizationStrategy")
    private OrganizationStrategy organizationStrategy;

    private DomainObject parentObject;

    public OrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected void init() {
        final DomainObject currentOrganization = getInputPanel().getObject();
        List<DomainObject> oszns = organizationStrategy.getAllOSZNs();
        IModel<DomainObject> parentModel = new Model<DomainObject>() {

            @Override
            public DomainObject getObject() {
                if (currentOrganization.getParentId() != null) {
                    DomainObjectExample example = new DomainObjectExample();
                    example.setId(currentOrganization.getParentId());
                    organizationStrategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
                    List<DomainObject> objects = organizationStrategy.find(example);
                    if (!objects.isEmpty()) {
                        return objects.get(0);
                    }
                }
                return null;
            }

            @Override
            public void setObject(DomainObject object) {
                if (object != null) {
                    currentOrganization.setParentId(object.getId());
                    currentOrganization.setParentEntityId(900L);
                    parentObject = object;
                }
            }
        };
        IChoiceRenderer<DomainObject> renderer = new IChoiceRenderer<DomainObject>() {

            @Override
            public Object getDisplayValue(DomainObject object) {
                return organizationStrategy.displayDomainObject(object, getLocale());

            }

            @Override
            public String getIdValue(DomainObject object, int index) {
                return String.valueOf(object.getId());
            }
        };
        DropDownChoice<DomainObject> parent = new DropDownChoice<DomainObject>("parent", parentModel, oszns, renderer);
        add(parent);
    }

    public DomainObject getParentObject() {
        return parentObject;
    }
}
