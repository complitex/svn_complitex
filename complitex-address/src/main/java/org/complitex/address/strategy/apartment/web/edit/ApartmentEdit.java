/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.address.strategy.apartment.web.edit;

import java.util.Locale;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.util.string.Strings;
import org.complitex.address.strategy.apartment.ApartmentStrategy;
import org.complitex.dictionary.Module;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.util.AddressNumberParser;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.template.web.pages.DomainObjectEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
public final class ApartmentEdit extends DomainObjectEdit {

    private static final Logger log = LoggerFactory.getLogger(ApartmentEdit.class);
    @EJB
    private StringCultureBean stringBean;

    public ApartmentEdit(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected DomainObjectEditPanel newEditPanel(String id, String entity, String strategy, Long objectId,
            Long parentId, String parentEntity, String scrollListPageParameterName) {
        return new DomainObjectEditPanel(id, entity, strategy, objectId, parentId, parentEntity, scrollListPageParameterName) {

            private boolean bulkSaveFlag;

            @Override
            protected boolean validate() {
                boolean superValid = super.validate();
                if (isNew()) {
                    String value = getValue();
                    if (!Strings.isEmpty(value)) {
                        if (!AddressNumberParser.matches(value)) {
                            error(getString("invalid_number_format"));
                            return false;
                        }
                    }
                }
                return superValid;
            }

            private String getValue() {
                return stringBean.getSystemStringCulture(
                        getNewObject().getAttribute(ApartmentStrategy.NAME).getLocalizedValues()).getValue();
            }

            private boolean performDefaultValidation(DomainObject object) {
                return getStrategy().getValidator().validate(object, this);
            }

            @Override
            protected void save(boolean propagate) {
                if (isNew()) {
                    String value = getValue();
                    String[] numbers = AddressNumberParser.parse(value);
                    if (numbers.length > 1) {
                        bulkSaveFlag = true;
                        onInsert();
                        beforeBulkSave(Module.NAME, DomainObjectEditPanel.class, value, getLocale());
                        boolean bulkOperationSuccess = true;
                        for (String number : numbers) {
                            DomainObject currentObject = CloneUtil.cloneObject(getNewObject());
                            stringBean.getSystemStringCulture(currentObject.getAttribute(ApartmentStrategy.NAME).
                                    getLocalizedValues()).setValue(number);
                            if (performDefaultValidation(currentObject)) {
                                try {
                                    getStrategy().insert(currentObject, DateUtil.getCurrentDate());
                                } catch (Exception e) {
                                    bulkOperationSuccess = false;
                                    log.error("", e);
                                    onFailBulkSave(Module.NAME, DomainObjectEditPanel.class, currentObject, value,
                                            number, getLocale());
                                }
                            } else {
                                onInvalidateBulkSave(Module.NAME, DomainObjectEditPanel.class, currentObject, value,
                                        number, getLocale());
                            }
                        }
                        afterBulkSave(Module.NAME, DomainObjectEditPanel.class, value, bulkOperationSuccess, getLocale());
                        getSession().getFeedbackMessages().clear();
                    } else {
                        onInsert();
                        getStrategy().insert(getNewObject(), DateUtil.getCurrentDate());
                    }
                } else {
                    onUpdate();
                    if (!propagate) {
                        getStrategy().update(getOldObject(), getNewObject(), DateUtil.getCurrentDate());
                    } else {
                        getStrategy().updateAndPropagate(getOldObject(), getNewObject(), DateUtil.getCurrentDate());
                    }
                }

                if (!bulkSaveFlag) {
                    getLogBean().log(Log.STATUS.OK, Module.NAME, DomainObjectEditPanel.class,
                            isNew() ? Log.EVENT.CREATE : Log.EVENT.EDIT, getStrategy(),
                            getOldObject(), getNewObject(), null);
                }
                back();
            }

            @Override
            protected void back() {
                if (bulkSaveFlag) {
                    //return to list page for current entity.
                    PageParameters listPageParams = getStrategy().getListPageParams();
                    setResponsePage(getStrategy().getListPage(), listPageParams);
                } else {
                    super.back();
                }
            }
        };
    }
    private static final String RESOURCE_BUNDLE = ApartmentEdit.class.getName();

    private static LogBean getLogBean() {
        return EjbBeanLocator.getBean(LogBean.class);
    }

    public static void beforeBulkSave(String moduleName, Class controllerClass, String numbers,
            Locale locale) {
        getLogBean().info(moduleName, controllerClass, DomainObject.class, "apartment", null, Log.EVENT.BULK_SAVE,
                null, ResourceUtil.getString(RESOURCE_BUNDLE, "apartment_bulk_save_start", locale), numbers);
    }

    public static void afterBulkSave(String moduleName, Class controllerClass, String numbers,
            boolean operationSuccessed, Locale locale) {
        LogBean logBean = getLogBean();
        if (operationSuccessed) {
            logBean.info(moduleName, controllerClass, DomainObject.class, "apartment", null, Log.EVENT.BULK_SAVE,
                    null, ResourceUtil.getString(RESOURCE_BUNDLE, "apartment_bulk_save_success_finish", locale), numbers);
        } else {
            logBean.error(moduleName, controllerClass, DomainObject.class, "apartment", null, Log.EVENT.BULK_SAVE,
                    null, ResourceUtil.getString(RESOURCE_BUNDLE, "apartment_bulk_save_fail_finish", locale), numbers);
        }
    }

    public static void onFailBulkSave(String moduleName, Class controllerClass, DomainObject failObject,
            String numbers, String failNumber, Locale locale) {
        getLogBean().log(Log.STATUS.ERROR, moduleName, controllerClass, Log.EVENT.CREATE,
                EjbBeanLocator.getBean(ApartmentStrategy.class), null, failObject,
                ResourceUtil.getString(RESOURCE_BUNDLE, "apartment_bulk_save_fail", locale), numbers, failNumber);
    }

    public static void onInvalidateBulkSave(String moduleName, Class controllerClass,
            DomainObject invalidObject, String numbers, String invalidNumber, Locale locale) {
        getLogBean().log(Log.STATUS.WARN, moduleName, controllerClass, Log.EVENT.CREATE,
                EjbBeanLocator.getBean(ApartmentStrategy.class), null, invalidObject,
                ResourceUtil.getString(RESOURCE_BUNDLE, "apartment_bulk_save_invalid", locale), numbers, invalidNumber);
    }
}
