/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.template.web.template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.aggregation.ResourceReferenceAndStringData;
import org.apache.wicket.resource.aggregation.ResourceReferenceCollection;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference.ResourceType;
import org.apache.wicket.util.string.Strings;
import org.odlabs.wiquery.core.WiQueryDecoratingHeaderResponse;

/**
 *
 * @author Artem
 */
class TemplateApplicationDecoratingHeaderResponse extends WiQueryDecoratingHeaderResponse {

    public static final String TEMPLATE_APPLICATION_COMMON_RESOURCE_GROUP = "TemplateAppllicationCommonResourceGroup";

    TemplateApplicationDecoratingHeaderResponse(IHeaderResponse real) {
        super(real);
    }

    @Override
    protected Comparator<String> getGroupingKeyComparator() {
        final Comparator<String> superGroupingKeyComparator = super.getGroupingKeyComparator();
        return new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                if (TEMPLATE_APPLICATION_COMMON_RESOURCE_GROUP.equals(o1)) {
                    return -1;
                } else if (TEMPLATE_APPLICATION_COMMON_RESOURCE_GROUP.equals(o2)) {
                    return 1;
                } else {
                    return superGroupingKeyComparator.compare(o1, o2);
                }
            }
        };
    }

    @Override
    protected void renderCollection(Set<ResourceReferenceAndStringData> alreadyRendered, String key, ResourceReferenceCollection coll) {
        List<ResourceReferenceAndStringData> templateApplicationResourceGroups = new ArrayList<ResourceReferenceAndStringData>();

        for (Iterator<ResourceReferenceAndStringData> it = coll.iterator(); it.hasNext();) {
            ResourceReferenceAndStringData data = it.next();
            ResourceReference ref = data.getReference();
            if (ref instanceof TemplateApplicationCommonResourceGroup) {
                templateApplicationResourceGroups.add(data);
                it.remove();
            }
        }

        for (ResourceReferenceAndStringData data : templateApplicationResourceGroups) {
            ResourceReferenceCollection childColl = newResourceReferenceCollection(key);
            for (ResourceReference child : ((TemplateApplicationCommonResourceGroup) data.getReference()).getResourceReferences()) {
                childColl.add(toData(child));
            }
            // render the group of dependencies before the parent
            super.renderCollection(alreadyRendered, key, childColl);
        }

        super.renderCollection(alreadyRendered, key, coll);
    }

    private static ResourceType getResourceType(ResourceReference reference) {
        String resourceName = reference.getName();

        final ResourceType type;
        if (Strings.isEmpty(resourceName)) {
            type = ResourceType.PLAIN;
        } else if (resourceName.endsWith(".css")) {
            type = ResourceType.CSS;
        } else if (resourceName.endsWith(".js")) {
            type = ResourceType.JS;
        } else {
            throw new IllegalStateException("Cannot determine the resource's type by its name: "
                    + resourceName);
        }
        return type;
    }

    private static ResourceReferenceAndStringData toData(
            ResourceReference reference) {
        return new ResourceReferenceAndStringData(reference, null, null, null,
                getResourceType(reference), false, null, null);
    }

    @Override
    protected String newGroupingKey(ResourceReferenceAndStringData ref) {
        if (ref.getReference() instanceof TemplateApplicationCommonResourceGroup) {
            return TEMPLATE_APPLICATION_COMMON_RESOURCE_GROUP;
        } else {
            return super.newGroupingKey(ref);
        }
    }
}
