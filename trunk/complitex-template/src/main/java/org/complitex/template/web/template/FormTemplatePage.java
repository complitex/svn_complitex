package org.complitex.template.web.template;

import java.util.List;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.odlabs.wiquery.ui.autocomplete.Autocomplete;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.07.2010 16:51:09
 */
public class FormTemplatePage extends TemplatePage {

    private static final String UNFOCUSABLE_CSS_CLASS = "form-template-page-unfocusable";

    public FormTemplatePage() {
        add(JavascriptPackageResource.getHeaderContribution(FormTemplatePage.class, FormTemplatePage.class.getSimpleName() + ".js"));
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        //All Autocomplete component are unfocusable
        visitChildren(WiQuerySearchComponent.class, new IVisitor<WiQuerySearchComponent>() {

            @Override
            public Object component(WiQuerySearchComponent searchComponent) {
                searchComponent.visitChildren(Autocomplete.class, new IVisitor<Autocomplete>() {

                    @Override
                    public Object component(Autocomplete autocomplete) {
                        List<IBehavior> behaviors = autocomplete.getBehaviors();
                        boolean containsCssBehaviour = false;
                        if (behaviors != null && !behaviors.isEmpty()) {
                            for (IBehavior behavior : behaviors) {
                                if (behavior instanceof CssAttributeBehavior) {
                                    containsCssBehaviour = true;
                                    break;
                                }
                            }
                        }
                        if (!containsCssBehaviour) {
                            autocomplete.add(new CssAttributeBehavior(UNFOCUSABLE_CSS_CLASS));
                        }
                        return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
                    }
                });
                return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
            }
        });
    }
}
