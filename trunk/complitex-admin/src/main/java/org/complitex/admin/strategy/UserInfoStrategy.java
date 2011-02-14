package org.complitex.admin.strategy;

import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.template.strategy.AbstractStrategy;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.08.2010 14:43:55
 */
@Stateless(name = "User_infoStrategy")
public class UserInfoStrategy extends AbstractStrategy {
    @EJB(beanName = "StringCultureBean")
    private StringCultureBean stringBean;

    public final static Long LAST_NAME = 1000L;
    public final static Long FIRST_NAME = 1001L;
    public final static Long MIDDLE_NAME = 1002L;

    @Override
    public String getEntityTable() {
        return "user_info";
    }

    @Override
    public String displayDomainObject(DomainObject object, Locale locale) {
        Attribute lastName = object.getAttribute(LAST_NAME);
        Attribute firstName = object.getAttribute(FIRST_NAME);
        Attribute middleName = object.getAttribute(MIDDLE_NAME);

        String s = "";

        if (lastName != null){
            s += stringBean.displayValue(lastName.getLocalizedValues(), locale);
        }
        if (firstName != null){
            s += " " + stringBean.displayValue(firstName.getLocalizedValues(), locale);
        }
        if (middleName != null){
            s += " " + stringBean.displayValue(middleName.getLocalizedValues(), locale);
        }

        return s;
    }

    @Override
    public String[] getEditRoles() {
        return new String[]{SecurityRole.ADMIN_MODULE_EDIT};
    }
}
