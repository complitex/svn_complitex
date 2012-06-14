/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.template;

import java.util.Arrays;
import java.util.List;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 *
 * @author Artem
 */
class TemplateApplicationCommonResourceGroup extends ResourceReference {

    private final List<ResourceReference> resourceReferences;

    TemplateApplicationCommonResourceGroup(ResourceReference... resourceReferences) {
        super(TemplateApplicationDecoratingHeaderResponse.TEMPLATE_APPLICATION_COMMON_RESOURCE_GROUP);
        this.resourceReferences = Arrays.asList(resourceReferences);
    }

    public List<ResourceReference> getResourceReferences() {
        return resourceReferences;
    }

    @Override
    public IResource getResource() {
        return null;
    }
}
