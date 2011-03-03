package org.complitex.admin.service;

import org.complitex.dictionary.entity.UserGroup;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.service.AbstractFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.08.2010 17:55:35
 */
public class UserFilter extends AbstractFilter{
    private String login;
    private List<AttributeExample> attributeExamples = new ArrayList<AttributeExample>();
    private Long sortAttributeTypeId;
    private UserGroup.GROUP_NAME groupName;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<AttributeExample> getAttributeExamples() {
        return attributeExamples;
    }

    public void setAttributeExamples(List<AttributeExample> attributeExamples) {
        this.attributeExamples = attributeExamples;
    }

    public boolean isFilterAttributes(){
        for(AttributeExample attributeExample : attributeExamples){
            if (attributeExample.getValue() != null){
                return true;
            }
        }

        return false;
    }

    public Long getSortAttributeTypeId() {
        return sortAttributeTypeId;
    }

    public void setSortAttributeTypeId(Long sortAttributeTypeId) {
        this.sortAttributeTypeId = sortAttributeTypeId;
    }

    public UserGroup.GROUP_NAME getGroupName() {
        return groupName;
    }

    public void setGroupName(UserGroup.GROUP_NAME groupName) {
        this.groupName = groupName;
    }
}
