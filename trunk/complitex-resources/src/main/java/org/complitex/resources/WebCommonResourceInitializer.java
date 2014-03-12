package org.complitex.resources;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.SharedResources;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;

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

    private static final String SCROLL_RELATIVE_PATH = "js/jquery.scrollTo-1.4.2-min.js";
    public static final String SCROLL_RESOURCE_NAME = "js/jquery.scrollTo-1.4.2-min.js";
    public static final ResourceReference SCROLL_JS = newResourceReference(SCROLL_RESOURCE_NAME);

    private static final String PLACEHOLDER_RELATIVE_PATH = "js/jquery.placeholder.js";
    public static final String PLACEHOLDER_RESOURCE_NAME = "js/jquery.placeholder.js";
    public static final ResourceReference PLACEHOLDER_JS = newResourceReference(PLACEHOLDER_RESOURCE_NAME);

    @Override
    public void init(Application application) {
        SharedResources sharedResources = application.getSharedResources();
        sharedResources.add(STYLE_RESOURCE_NAME, new PackageResourceReference(getClass(), STYLE_RELATIVE_PATH).getResource());
        sharedResources.add(COMMON_RESOURCE_NAME, new PackageResourceReference(getClass(), COMMON_RELATIVE_PATH).getResource());
        sharedResources.add(IE_SELECT_FIX_RESOURCE_NAME, new PackageResourceReference(getClass(), IE_SELECT_FIX_RELATIVE_PATH).getResource());
        sharedResources.add(SCROLL_RESOURCE_NAME, new PackageResourceReference(getClass(), SCROLL_RELATIVE_PATH).getResource());
        sharedResources.add(PLACEHOLDER_RESOURCE_NAME, new PackageResourceReference(getClass(), PLACEHOLDER_RELATIVE_PATH).getResource());
    }

    private static ResourceReference newResourceReference(String resourceName) {
        return new SharedResourceReference(resourceName);
    }

    @Override
    public void destroy(Application application) {
    }
}
