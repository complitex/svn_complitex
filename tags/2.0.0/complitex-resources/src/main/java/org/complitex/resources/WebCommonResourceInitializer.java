package org.complitex.resources;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.SharedResources;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;

/**
 *
 * @author Artem
 */
public final class WebCommonResourceInitializer implements IInitializer {

    /* css resources */
    private static final String STYLE_RELATED_PATH = "css/style.css";
    public static final String STYLE_RESOURCE_NAME = "css/style.css";
    public static final ResourceReference STYLE_CSS = getResourceReference(STYLE_RESOURCE_NAME);

    /* js resources */
    private static final String COMMON_RELATED_PATH = "js/common.js";
    public static final String COMMON_RESOURCE_NAME = "js/common.js";
    public static final ResourceReference COMMON_JS = getResourceReference(COMMON_RESOURCE_NAME);

    private static final String IE_SELECT_FIX_RELATED_PATH = "js/ie_select_fix.js";
    public static final String IE_SELECT_FIX_RESOURCE_NAME = "js/ie_select_fix.js";
    public static final ResourceReference IE_SELECT_FIX_RESOURCE_NAME_JS = getResourceReference(IE_SELECT_FIX_RESOURCE_NAME);

    private static final String HIGHLIGHT_JS_PATH = "js/jquery-ui-1.7.3.highlight.min.js";
    public static final ResourceReference HIGHLIGHT_JS = getResourceReference(HIGHLIGHT_JS_PATH);

    @Override
    public void init(Application application) {
        SharedResources sharedResources = application.getSharedResources();
        sharedResources.add(STYLE_RESOURCE_NAME, CSSPackageResource.get(getClass(), STYLE_RELATED_PATH));
        sharedResources.add(COMMON_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), COMMON_RELATED_PATH));
        sharedResources.add(IE_SELECT_FIX_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), IE_SELECT_FIX_RELATED_PATH));
        sharedResources.add(HIGHLIGHT_JS_PATH, JavascriptPackageResource.get(getClass(), HIGHLIGHT_JS_PATH));
    }

    private static ResourceReference getResourceReference(String resourceName) {
        return new ResourceReference(resourceName);
    }
}
