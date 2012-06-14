package org.complitex.template.web.template;

import java.util.List;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.07.2010 16:51:09
 */
public class FormTemplatePage extends TemplatePage {

    private static final String UNFOCUSABLE_CSS_CLASS = "form-template-page-unfocusable";

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderJavaScriptReference(new PackageResourceReference(
                FormTemplatePage.class, FormTemplatePage.class.getSimpleName() + ".js"));
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        //All Autocomplete components that are children of WiQuerySearchComponent to be unfocusable
        visitChildren(WiQuerySearchComponent.class, new IVisitor<WiQuerySearchComponent, Void>() {

            @Override
            public void component(WiQuerySearchComponent searchComponent, IVisit<Void> visit) {
                searchComponent.visitChildren(TextField.class, new IVisitor<TextField, Void>() {

                    @Override
                    public void component(TextField autocomplete, IVisit<Void> visit) {
                        List<? extends Behavior> behaviors = autocomplete.getBehaviors();
                        boolean containsCssBehaviour = false;
                        if (behaviors != null && !behaviors.isEmpty()) {
                            for (Behavior behavior : behaviors) {
                                if (behavior instanceof CssAttributeBehavior) {
                                    containsCssBehaviour = true;
                                    break;
                                }
                            }
                        }
                        if (!containsCssBehaviour) {
                            autocomplete.add(new CssAttributeBehavior(UNFOCUSABLE_CSS_CLASS));
                        }
                        visit.dontGoDeeper();
                    }
                });
                visit.dontGoDeeper();
            }
        });
    }
}
