package org.complitex.address.strategy.room.web.edit;

import com.google.common.collect.Lists;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.complitex.address.Module;
import org.complitex.address.strategy.room.RoomStrategy;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.StringCulture;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.dictionary.web.component.DomainObjectComponentUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.RangeNumbersPanel;
import org.complitex.dictionary.web.component.RangeNumbersPanel.NumbersList;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.component.toolbar.search.CollapsibleInputSearchToolbarButton;
import org.complitex.template.web.pages.DomainObjectEdit;

import java.util.List;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public class RoomEdit extends DomainObjectEdit {

    public RoomEdit(PageParameters parameters) {
        super(parameters);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new PackageResourceReference(RoomEdit.class, RoomEdit.class.getSimpleName() + ".css")));
    }

    @Override
    protected DomainObjectEditPanel newEditPanel(String id, String entity, String strategy, Long objectId,
            Long parentId, String parentEntity, String scrollListPageParameterName, String backInfoSessionKey) {

        if (objectId == null) { // new object
            return new DomainObjectEditPanel(id, entity, strategy, objectId, parentId, parentEntity,
                    scrollListPageParameterName, backInfoSessionKey) {

                NumbersList numbersList = new NumbersList();
                RangeNumbersPanel rangeNumbersPanel;
                boolean bulkSaveFlag;

                @Override
                protected DomainObjectInputPanel newInputPanel(String id, DomainObject newObject, String entity,
                        String strategyName, Long parentId, String parentEntity) {
                    return new DomainObjectInputPanel(id, newObject, entity, strategyName, parentId, parentEntity) {

                        @Override
                        protected List<Attribute> getSimpleAttributes(List<Attribute> allAttributes) {
                            return Lists.newArrayList();
                        }

                        @Override
                        protected void addComplexAttributesPanelBefore(String id) {
                            final IModel<String> labelModel = DomainObjectComponentUtil.labelModel(getStrategy().getEntity().
                                    getAttributeType(RoomStrategy.NAME).getAttributeNames(), getLocale());
                            rangeNumbersPanel = new RangeNumbersPanel(id, labelModel, numbersList);
                            rangeNumbersPanel.add(new CssAttributeBehavior("roomRangeNumberPanel"));
                            add(rangeNumbersPanel);
                        }
                    };
                }

                @Override
                protected boolean validate() {
                    if (isNew() && !rangeNumbersPanel.validate()) {
                        return false;
                    }
                    return super.validate();
                }

                private boolean performDefaultValidation(DomainObject object) {
                    return getStrategy().getValidator().validate(object, this);
                }

                @Override
                protected void save(boolean propagate) {
                    final String numbersAsString = numbersList.asString();
                    final List<List<StringCulture>> numbers = numbersList.getNumbers();
                    if (numbers.size() > 1) {
                        bulkSaveFlag = true;
                        onInsert();
                        beforeBulkSave(Module.NAME, DomainObjectEditPanel.class, numbersAsString, getLocale());
                        boolean bulkOperationSuccess = true;
                        for (List<StringCulture> number : numbers) {
                            final DomainObject currentObject = CloneUtil.cloneObject(getNewObject());
                            currentObject.getAttribute(RoomStrategy.NAME).setLocalizedValues(number);
                            if (performDefaultValidation(currentObject)) {
                                try {
                                    getStrategy().insert(currentObject, DateUtil.getCurrentDate());
                                } catch (Exception e) {
                                    bulkOperationSuccess = false;
                                    log().error("", e);
                                    onFailBulkSave(Module.NAME, DomainObjectEditPanel.class, currentObject,
                                            numbersAsString, numbersList.asString(number), getLocale());
                                }
                            } else {
                                onInvalidateBulkSave(Module.NAME, DomainObjectEditPanel.class, currentObject,
                                        numbersAsString, numbersList.asString(number), getLocale());
                            }
                        }
                        afterBulkSave(Module.NAME, DomainObjectEditPanel.class, numbersAsString, bulkOperationSuccess, getLocale());
                        getSession().getFeedbackMessages().clear();
                    } else {
                        onInsert();
                        final DomainObject object = getNewObject();
                        object.getAttribute(RoomStrategy.NAME).setLocalizedValues(numbers.get(0));
                        getStrategy().insert(getNewObject(), DateUtil.getCurrentDate());
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
        } else {
            return super.newEditPanel(id, entity, strategy, objectId, parentId, parentEntity,
                    scrollListPageParameterName, backInfoSessionKey);
        }
    }
    private static final String RESOURCE_BUNDLE = RoomEdit.class.getName();

    private static LogBean getLogBean() {
        return EjbBeanLocator.getBean(LogBean.class);
    }

    public static void beforeBulkSave(String moduleName, Class<?> controllerClass, String numbers,
            Locale locale) {
        getLogBean().info(moduleName, controllerClass, DomainObject.class, "room", null, Log.EVENT.BULK_SAVE,
                null, ResourceUtil.getString(RESOURCE_BUNDLE, "room_bulk_save_start", locale), numbers);
    }

    public static void afterBulkSave(String moduleName, Class<?> controllerClass, String numbers,
            boolean operationSuccessed, Locale locale) {
        LogBean logBean = getLogBean();
        if (operationSuccessed) {
            logBean.info(moduleName, controllerClass, DomainObject.class, "room", null, Log.EVENT.BULK_SAVE,
                    null, ResourceUtil.getString(RESOURCE_BUNDLE, "room_bulk_save_success_finish", locale), numbers);
        } else {
            logBean.error(moduleName, controllerClass, DomainObject.class, "room", null, Log.EVENT.BULK_SAVE,
                    null, ResourceUtil.getString(RESOURCE_BUNDLE, "room_bulk_save_fail_finish", locale), numbers);
        }
    }

    public static void onFailBulkSave(String moduleName, Class<?> controllerClass, DomainObject failObject,
            String numbers, String failNumber, Locale locale) {
        getLogBean().log(Log.STATUS.ERROR, moduleName, controllerClass, Log.EVENT.CREATE,
                EjbBeanLocator.getBean(RoomStrategy.class), null, failObject,
                ResourceUtil.getString(RESOURCE_BUNDLE, "room_bulk_save_fail", locale), numbers, failNumber);
    }

    public static void onInvalidateBulkSave(String moduleName, Class<?> controllerClass,
            DomainObject invalidObject, String numbers, String invalidNumber, Locale locale) {
        getLogBean().log(Log.STATUS.WARN, moduleName, controllerClass, Log.EVENT.CREATE,
                EjbBeanLocator.getBean(RoomStrategy.class), null, invalidObject,
                ResourceUtil.getString(RESOURCE_BUNDLE, "room_bulk_save_invalid", locale), numbers, invalidNumber);
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbarButtons = Lists.newArrayList();
        toolbarButtons.addAll(super.getToolbarButtons(id));
        toolbarButtons.add(new CollapsibleInputSearchToolbarButton(id));
        return toolbarButtons;

    }
}
