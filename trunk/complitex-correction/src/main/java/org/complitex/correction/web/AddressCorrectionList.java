package org.complitex.correction.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.Correction;

/**
 * Страница для списка коррекций элементов адреса(город, улица).
 * @author Artem
 */
public abstract class AddressCorrectionList<T extends Correction> extends AbstractCorrectionList<T> {
    public AddressCorrectionList(String entity) {
        super(entity);

        add(new AjaxLink("sync"){
            @Override
            public void onClick(AjaxRequestTarget target) {
                onSync(target);
            }
        }.setVisible(isSyncVisible()));
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return AddressCorrectionEdit.class;
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();
        parameters.set(AddressCorrectionEdit.CORRECTED_ENTITY, getEntity());
        if (objectCorrectionId != null) {
            parameters.set(AddressCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }

    protected boolean isSyncVisible() {
        return true;
    }

    protected void onSync(AjaxRequestTarget target){
    }
}
