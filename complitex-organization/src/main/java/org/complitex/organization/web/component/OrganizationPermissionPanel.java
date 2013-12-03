package org.complitex.organization.web.component;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.permission.organization.OrganizationPermissionParameters;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class OrganizationPermissionPanel extends DomainObjectPermissionPanel {
    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private IOrganizationStrategy organizationStrategy;

    private final Long organizationId;

    public OrganizationPermissionPanel(String id,
                                       OrganizationPermissionParameters organizationPermissionParameters) {
        super(id, organizationPermissionParameters.getParameters());
        this.organizationId = organizationPermissionParameters.getOrganizationId();
    }

    @Override
    protected boolean isAllowModifyOrganization(int index, Long subjectId) {
        return !(organizationId != null && organizationId > 0) || index != 0;
    }

    @Override
    protected List<DomainObject> initializeSelectedSubjects(Set<Long> selectedSubjectIds) {
        final List<DomainObject> superSelectedSubjects = super.initializeSelectedSubjects(selectedSubjectIds);

        if (organizationId != null) {
            DomainObject itself = organizationStrategy.findById(organizationId, true);

            if (organizationStrategy.isUserOrganization(itself)) {
                if (organizationId > 0) {
                    List<DomainObject> selectedSubjects = new ArrayList<>();

                    for (DomainObject o : superSelectedSubjects) {
                        if (!o.getId().equals(organizationId)) {
                            selectedSubjects.add(o);
                        }
                    }
                    selectedSubjects.add(0, itself);
                    return selectedSubjects;
                }
            }
        }

        return superSelectedSubjects;
    }
}
