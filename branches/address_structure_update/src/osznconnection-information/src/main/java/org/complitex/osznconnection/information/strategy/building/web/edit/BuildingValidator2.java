/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.information.strategy.building.web.edit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.service.StringCultureBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactoryStatic;
import org.complitex.dictionaryfw.strategy.web.IValidator;
import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;
import org.complitex.osznconnection.information.strategy.building.entity.Building;
import org.complitex.osznconnection.information.strategy.building_address.BuildingAddressStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public class BuildingValidator2 implements IValidator {

    private static final Logger log = LoggerFactory.getLogger(BuildingValidator2.class);

    private BuildingStrategy buildingStrategy;

    private Locale systemLocale;

    private StringCultureBean stringBean;

    public BuildingValidator2(BuildingStrategy buildingStrategy, Locale systemLocale, StringCultureBean stringBean) {
        this.buildingStrategy = buildingStrategy;
        this.systemLocale = systemLocale;
        this.stringBean = stringBean;
    }

    @Override
    public boolean validate(DomainObject object, Component component) {
        boolean valid = validateParents((Building) object, component);
        if (valid) {
            valid &= validateCity((Building) object, component);
            if (valid) {
                valid &= validateAdresses((Building) object, component);
            }
        }
        return valid;
    }

    private boolean validateCity(Building building, Component component) {
        DomainObject district = building.getDistrict();
        if (district != null) {
            long cityFromDistrict = district.getParentId();
            long cityFromPrimaryAddress = -1;

            DomainObject primaryAddress = building.getPrimaryAddress();
            if (primaryAddress.getParentEntityId().equals(400L)) {
                cityFromPrimaryAddress = primaryAddress.getParentId();
            } else if (primaryAddress.getParentEntityId().equals(300L)) {
                cityFromPrimaryAddress = building.getPrimaryStreet().getParentId();
            }

            if (cityFromDistrict != cityFromPrimaryAddress) {
                error("city_mismatch", component);
                return false;
            }
        }
        return true;
    }

//    private boolean validateStreets(Building building, Component component) {
//        boolean streetsValid = true;
//        Long primaryStreetId = building.getPrimaryStreetId();
//        if(primaryStreetId == null){
//            component.error(ResourceUtil.getString(BuildingStrategy.RESOURCE_BUNDLE, "primary_street_required", component.getLocale()));
//            streetsValid = false;
//        }
//        for(DomainObject alternativeAddress : building.getAlternativeAddresses()){
//            long parentEntityId =
//            Long alternativeStreetId = alternativeAddress.getParentId();
//        }
//    }
    private boolean validateParents(Building building, Component component) {
        boolean valid = true;
        if (building.getPrimaryAddress().getParentId() == null || building.getPrimaryAddress().getParentEntityId() == null) {
            valid = false;
        }
        for (DomainObject alternativeAddress : building.getAlternativeAddresses()) {
            if (alternativeAddress.getParentId() == null || alternativeAddress.getParentEntityId() == null) {
                valid = false;
            }
        }

        if (!valid) {
            error("parent_not_specified", component);
        }
        return valid;
    }

    private void error(String key, Component component, Object... formatArguments) {
        if (formatArguments == null) {
            component.error(findEditComponent(component).getString(key));
        } else {
            component.error(MessageFormat.format(findEditComponent(component).getString(key), formatArguments));
        }

    }

    private void error(String key, Component component, IModel<?> model) {
        component.error(findEditComponent(component).getString(key, model));
    }

    private BuildingEditComponent2 editComponent;

    private BuildingEditComponent2 findEditComponent(Component component) {
        if (editComponent == null) {
            component.getPage().visitChildren(BuildingEditComponent2.class, new Component.IVisitor<BuildingEditComponent2>() {

                @Override
                public Object component(BuildingEditComponent2 comp) {
                    editComponent = comp;
                    return STOP_TRAVERSAL;
                }
            });
        }
        return editComponent;
    }

    private boolean validateAdresses(Building building, Component component) {
        List<DomainObject> addresses = building.getAllAddresses();

        boolean valid = true;

        for (DomainObject address : addresses) {
            String number = stringBean.displayValue(address.getAttribute(BuildingAddressStrategy.NUMBER).getLocalizedValues(), systemLocale);
            String corp = stringBean.displayValue(address.getAttribute(BuildingAddressStrategy.CORP).getLocalizedValues(), systemLocale);
            String structure = stringBean.displayValue(address.getAttribute(BuildingAddressStrategy.STRUCTURE).getLocalizedValues(), systemLocale);

            Long existingBuildingId = buildingStrategy.checkForExistingAddress(number, Strings.isEmpty(corp) ? null : corp,
                    Strings.isEmpty(structure) ? null : structure, address.getParentEntityId(), address.getParentId(), systemLocale);
            if (existingBuildingId != null && !existingBuildingId.equals(building.getId())) {
                valid = false;

                Long parentEntityId = address.getParentEntityId();
                String parentEntity = parentEntityId == null ? null : (parentEntityId == 300 ? "street" : (parentEntityId == 400 ? "city" : null));
                Strategy strategy = StrategyFactoryStatic.getStrategy(parentEntity);
                DomainObject parentObject = strategy.findById(address.getParentId());
                String parentTitle = strategy.displayDomainObject(parentObject, component.getLocale());

                IModel<?> model = Model.ofMap(ImmutableMap.builder().
                        put("id", existingBuildingId).
                        put("number", number).
                        put("corp", corp != null ? corp : "").
                        put("structure", structure != null ? structure : "").
                        put("parent", parentTitle).
                        put("locale", systemLocale).
                        build());
                error("address_exists_already", component, model);
            }
        }
        return valid;
    }
}
