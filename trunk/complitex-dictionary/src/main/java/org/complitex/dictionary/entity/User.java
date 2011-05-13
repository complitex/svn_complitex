package org.complitex.dictionary.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.07.2010 15:26:12
 */
public class User implements Serializable{
    private Long id;
    private String login;
    private String password;
    private String newPassword;
    private Long userInfoObjectId;
    private List<UserOrganization> userOrganizations = new ArrayList<UserOrganization>();
    private DomainObject userInfo;    

    private List<UserGroup> userGroups;

    public void setMainUserOrganization(Long userOrganizationId){
        for (UserOrganization uo : userOrganizations){
            uo.setMain(uo.getOrganizationObjectId().equals(userOrganizationId));
        }
    }

    public Long getMainUserOrganization(){
        for (UserOrganization userOrganization : userOrganizations){
            if (userOrganization.isMain()){
                return userOrganization.getOrganizationObjectId();
            }
        }

        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public Long getUserInfoObjectId() {
        return userInfoObjectId;
    }

    public void setUserInfoObjectId(Long userInfoObjectId) {
        this.userInfoObjectId = userInfoObjectId;
    }

    public List<UserOrganization> getUserOrganizations() {
        return userOrganizations;
    }

    public void setUserOrganizations(List<UserOrganization> userOrganizations) {
        this.userOrganizations = userOrganizations;
    }

    public DomainObject getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(DomainObject userInfo) {
        this.userInfo = userInfo;
    }

    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", newPassword='" + newPassword + '\'' +
                ", userInfo=" + userInfo +
                ", userGroups=" + userGroups +
                '}';
    }
}
