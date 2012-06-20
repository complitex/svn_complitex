package org.complitex.template.web.template;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.07.2010 16:51:09
 */
public class FormTemplatePage extends TemplatePage {

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderJavaScriptReference(new PackageResourceReference(
                FormTemplatePage.class, FormTemplatePage.class.getSimpleName() + ".js"));
    }
}
