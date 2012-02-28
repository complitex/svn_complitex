/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.building.web.edit;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.util.Numbers;
import org.complitex.address.strategy.building.BuildingStrategy;
import org.complitex.address.strategy.building.entity.Building;
import org.complitex.address.strategy.building_address.BuildingAddressStrategy;
import org.complitex.dictionary.strategy.IStrategy;

/**
 *
 * @author Artem
 */
public class BuildingValidator implements IValidator {

    private final Locale systemLocale;

    public BuildingValidator(Locale systemLocale) {
        this.systemLocale = systemLocale;
    }

    @Override
    public boolean validate(DomainObject object, DomainObjectEditPanel editPanel) {
        Building building = (Building) object;
        return validateParents(building, editPanel) && validateCity(building, editPanel) && validateStreets(building, editPanel)
                && validateAdresses(building, editPanel);
    }

    private boolean validateCity(Building building, DomainObjectEditPanel editPanel) {
        boolean valid = true;

        //город для района(если район задан) должен совпадать с городом каждого адреса.
        DomainObject district = building.getDistrict();
        if (district != null && district.getId() != null && district.getId() > 0) {
            Long cityFromDistrict = district.getParentId();

            for (DomainObject address : building.getAllAddresses()) {
                Long cityFromAddress = getCityId(address);
                if (!Numbers.isEqual(cityFromDistrict, cityFromAddress)) {
                    error("city_mismatch_to_district", editPanel);
                    valid = false;
                    break;
                }
            }
        }

        //город для главного адреса должен совпадать с городом каждого альтернативного адреса.
        long primaryCity = getCityId(building.getPrimaryAddress());
        for (DomainObject alternativeAddress : building.getAlternativeAddresses()) {
            Long alternativeCity = getCityId(alternativeAddress);
            if (!Numbers.isEqual(primaryCity, alternativeCity)) {
                error("city_mismatch_to_city", editPanel);
                valid = false;
                break;
            }
        }

        //дом который привязан напрямую к городу может иметь только один адрес
        if (building.getAllAddresses().size() > 1) {
            for (DomainObject address : building.getAllAddresses()) {
                Long addressParentEntityId = address.getParentEntityId();
                if (addressParentEntityId != null && addressParentEntityId.equals(400L)) {
                    error("more_one_city_address", editPanel);
                    valid = false;
                    break;
                }
            }
        }

        return valid;
    }

    private boolean validateStreets(Building building, DomainObjectEditPanel editPanel) {
        //все адреса дома должны иметь разные улицы
        //кол-во адресов:
        final int addressCount = building.getAllAddresses().size();

        //кол-во улиц:
        Set<Long> streetIds = Sets.newHashSet();
        for (DomainObject address : building.getAllAddresses()) {
            final Long streetId = getStreetId(address);
            if (streetId != null) {
                streetIds.add(streetId);
            }
        }
        final int streetCount = streetIds.size();

        if (addressCount == 1 && streetCount == 0) {
            //дом привязан напрямую к городу.
            return true;
        }

        if (addressCount != streetCount) {
            error("repeating_street", editPanel);
            return false;
        }

        return true;
    }

    private Long getCityId(DomainObject address) {
        if (address.getParentEntityId().equals(400L)) {
            return address.getParentId();
        } else if (address.getParentEntityId().equals(300L)) {
            Long streetId = address.getParentId();
            if (streetId != null && streetId > 0) {
                IStrategy streetStrategy = getStrategyFactory().getStrategy("street");
                DomainObject streetObject = streetStrategy.findById(streetId, true);
                return streetObject.getParentId();
            }
        }
        return null;
    }

    private Long getStreetId(DomainObject address) {
        if (address.getParentEntityId().equals(300L)) {
            return address.getParentId();
        }
        return null;
    }

    private boolean validateParents(Building building, DomainObjectEditPanel editPanel) {
        for (DomainObject address : building.getAllAddresses()) {
            if (address.getParentId() == null || address.getParentEntityId() == null) {
                error("parent_not_specified", editPanel);
                return false;
            }
        }
        return true;
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
    private BuildingEditComponent editComponent;

    private BuildingEditComponent findEditComponent(Component component) {
        if (editComponent == null) {
            component.getPage().visitChildren(BuildingEditComponent.class, new Component.IVisitor<BuildingEditComponent>() {

                @Override
                public Object component(BuildingEditComponent comp) {
                    editComponent = comp;
                    return STOP_TRAVERSAL;
                }
            });
        }
        return editComponent;
    }

    private boolean validateAdresses(Building building, DomainObjectEditPanel editPanel) {
        List<DomainObject> addresses = building.getAllAddresses();

        BuildingStrategy buildingStrategy = EjbBeanLocator.getBean("BuildingStrategy");
        StringCultureBean stringBean = EjbBeanLocator.getBean(StringCultureBean.class);

        boolean valid = true;

        for (DomainObject address : addresses) {
            String number = stringBean.displayValue(address.getAttribute(BuildingAddressStrategy.NUMBER).getLocalizedValues(), systemLocale);
            String corp = stringBean.displayValue(address.getAttribute(BuildingAddressStrategy.CORP).getLocalizedValues(), systemLocale);
            String structure = stringBean.displayValue(address.getAttribute(BuildingAddressStrategy.STRUCTURE).getLocalizedValues(), systemLocale);

            Long existingBuildingId = buildingStrategy.checkForExistingAddress(building.getId(), number, Strings.isEmpty(corp) ? null : corp,
                    Strings.isEmpty(structure) ? null : structure, address.getParentEntityId(), address.getParentId(), systemLocale);
            if (existingBuildingId != null) {
                valid = false;
                printExistingAddressErrorMessage(existingBuildingId, number, corp, structure, address.getParentId(), address.getParentEntityId(),
                        systemLocale, editPanel);
            }
        }
        return valid;
    }

    private StrategyFactory getStrategyFactory() {
        return EjbBeanLocator.getBean(StrategyFactory.class);
    }

    private void printExistingAddressErrorMessage(long id, String number, String corp, String structure, Long parentId, Long parentEntityId,
            Locale locale, DomainObjectEditPanel editPanel) {
        String parentEntity = parentEntityId == null ? null : (parentEntityId == 300 ? "street" : (parentEntityId == 400 ? "city" : null));
        IStrategy strategy = getStrategyFactory().getStrategy(parentEntity);
        DomainObject parentObject = strategy.findById(parentId, true);
        String parentTitle = strategy.displayDomainObject(parentObject, editPanel.getLocale());

        IModel<?> model = Model.ofMap(ImmutableMap.builder().
                put("id", id).
                put("number", number).
                put("corp", corp).
                put("structure", structure).
                put("parent", parentTitle).
                put("locale", locale).
                build());

        String errorMessageKey = null;
        if (Strings.isEmpty(corp)) {
            if (Strings.isEmpty(structure)) {
                errorMessageKey = "address_exists_already_number";
            } else {
                errorMessageKey = "address_exists_already_number_structure";
            }
        } else {
            if (Strings.isEmpty(structure)) {
                errorMessageKey = "address_exists_already_number_corp";
            } else {
                errorMessageKey = "address_exists_already_number_corp_structure";
            }
        }
        error(errorMessageKey, editPanel, model);
    }
}
