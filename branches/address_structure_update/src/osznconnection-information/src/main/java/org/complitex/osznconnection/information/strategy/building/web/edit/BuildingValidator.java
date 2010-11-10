///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.complitex.osznconnection.information.strategy.building.web.edit;
//
//import com.google.common.base.Predicate;
//import com.google.common.collect.Iterables;
//import com.google.common.collect.Lists;
//import org.apache.wicket.Component;
//import org.complitex.dictionaryfw.entity.Attribute;
//import org.complitex.dictionaryfw.entity.DomainObject;
//import org.complitex.dictionaryfw.strategy.web.IValidator;
//import org.complitex.dictionaryfw.util.ResourceUtil;
//import org.complitex.osznconnection.information.strategy.building.BuildingStrategy;
//
//import java.util.List;
//
///**
// *
// * @author Artem
// */
//public class BuildingValidator implements IValidator {
//
//    @Override
//    public boolean validate(DomainObject object, Component component) {
//        boolean valid = checkNumber(object, component);
//        valid &= checkStreets(object, component);
//        return valid;
//    }
//
//    private boolean checkStreets(DomainObject object, Component component) {
//        List<Attribute> streets = Lists.newArrayList(Iterables.filter(object.getAttributes(), new Predicate<Attribute>() {
//
//            @Override
//            public boolean apply(Attribute attr) {
//                return attr.getAttributeTypeId().equals(BuildingStrategy.STREET);
//            }
//        }));
//        for (Attribute street1 : streets) {
//            for (Attribute street2 : streets) {
//                if (!street1.getAttributeId().equals(street2.getAttributeId()) && street1.getValueId() != null
//                        && street1.getValueId().equals(street2.getValueId())) {
//                    component.getPage().error(ResourceUtil.getString(BuildingStrategy.RESOURCE_BUNDLE, "multiple_numbers_on_one_street",
//                            component.getLocale()));
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    private boolean checkNumber(DomainObject object, Component component) {
//        List<Attribute> numbers = Lists.newArrayList(Iterables.filter(object.getAttributes(), new Predicate<Attribute>() {
//
//            @Override
//            public boolean apply(Attribute attr) {
//                return attr.getAttributeTypeId().equals(BuildingStrategy.NUMBER);
//            }
//        }));
//        if (numbers.isEmpty()) {
//            component.getPage().error(ResourceUtil.getString(BuildingStrategy.RESOURCE_BUNDLE, "no_numbers", component.getLocale()));
//            return false;
//        }
//        return true;
//    }
//}
