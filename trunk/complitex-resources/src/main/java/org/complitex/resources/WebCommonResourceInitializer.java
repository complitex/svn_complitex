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
    private static final String STYLE_RELATIVE_PATH = "css/style.css";
    public static final String STYLE_RESOURCE_NAME = "css/style.css";
    public static final ResourceReference STYLE_CSS = newResourceReference(STYLE_RESOURCE_NAME);

    /* js resources */
    private static final String COMMON_RELATIVE_PATH = "js/common.js";
    public static final String COMMON_RESOURCE_NAME = "js/common.js";
    public static final ResourceReference COMMON_JS = newResourceReference(COMMON_RESOURCE_NAME);

    private static final String IE_SELECT_FIX_RELATIVE_PATH = "js/ie_select_fix.js";
    public static final String IE_SELECT_FIX_RESOURCE_NAME = "js/ie_select_fix.js";
    public static final ResourceReference IE_SELECT_FIX_JS = newResourceReference(IE_SELECT_FIX_RESOURCE_NAME);

    private static final String HIGHLIGHT_RELATIVE_PATH = "js/jquery-ui-1.7.3.highlight.min.js";
    public static final String HIGHLIHT_RESOURCE_NAME = "js/jquery-ui-1.7.3.highlight.min.js";
    public static final ResourceReference HIGHLIGHT_JS = newResourceReference(HIGHLIHT_RESOURCE_NAME);

    private static final String SCROLL_RELATIVE_PATH = "js/jquery.scrollTo-1.4.2-min.js";
    public static final String SCROLL_RESOURCE_NAME = "js/jquery.scrollTo-1.4.2-min.js";
    public static final ResourceReference SCROLL_JS = newResourceReference(SCROLL_RESOURCE_NAME);

    private static final String PLACEHOLDER_RELATIVE_PATH = "js/jquery.placeholder.js";
    public static final String PLACEHOLDER_RESOURCE_NAME = "js/jquery.placeholder.js";
    public static final ResourceReference PLACEHOLDER_JS = newResourceReference(PLACEHOLDER_RESOURCE_NAME);

    private static final String COLLAPSIBLE_FS_RELATIVE_PATH = "js/CollapsibleFieldset.js";
    public static final String COLLAPSIBLE_FS_RESOURCE_NAME = "js/CollapsibleFieldset.js";
    public static final ResourceReference COLLAPSIBLE_FS_JS = newResourceReference(COLLAPSIBLE_FS_RESOURCE_NAME);

    @Override
    public void init(Application application) {
        SharedResources sharedResources = application.getSharedResources();
        sharedResources.add(STYLE_RESOURCE_NAME, CSSPackageResource.get(getClass(), STYLE_RELATIVE_PATH));
        sharedResources.add(COMMON_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), COMMON_RELATIVE_PATH));
        sharedResources.add(IE_SELECT_FIX_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), IE_SELECT_FIX_RELATIVE_PATH));
        sharedResources.add(HIGHLIHT_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), HIGHLIGHT_RELATIVE_PATH));
        sharedResources.add(SCROLL_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), SCROLL_RELATIVE_PATH));
        sharedResources.add(PLACEHOLDER_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), PLACEHOLDER_RELATIVE_PATH));
        sharedResources.add(COLLAPSIBLE_FS_RESOURCE_NAME, JavascriptPackageResource.get(getClass(), COLLAPSIBLE_FS_RELATIVE_PATH));
    }

    private static ResourceReference newResourceReference(String resourceName) {
        return new ResourceReference(resourceName);
    }
}
