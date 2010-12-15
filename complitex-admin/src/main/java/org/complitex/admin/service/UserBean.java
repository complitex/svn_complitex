package org.complitex.admin.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.complitex.dictionary.entity.User;
import org.complitex.dictionary.entity.UserGroup;
import org.complitex.dictionary.entity.description.EntityAttributeType;
import org.complitex.dictionary.entity.example.AttributeExample;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.strategy.Strategy;
import org.complitex.dictionary.strategy.StrategyFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.util.DateUtil;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.07.2010 15:05:09
 */
@Stateless(name = "UserBean")
public class UserBean extends AbstractBean {
    public static final String STATEMENT_PREFIX = UserBean.class.getCanonicalName();
    public static final String USER_INFO_ENTITY_TABLE = "user_info";

    @EJB(beanName = "StrategyFactory")
    private StrategyFactory strategyFactory;

    public Strategy getUserInfoStrategy(){
        return strategyFactory.getStrategy(USER_INFO_ENTITY_TABLE);
    }

    public User newUser(){
        User user = new User();

        user.setUserGroups(new ArrayList<UserGroup>());
        user.setUserInfo(getUserInfoStrategy().newInstance());

        return user;
    }

    @Transactional
    public boolean isUniqueLogin(String login){
        return (Boolean) sqlSession().selectOne(STATEMENT_PREFIX + ".isUniqueLogin", login);
    }

    @Transactional
    public User getUser(Long id){
        User user = (User) sqlSession().selectOne(STATEMENT_PREFIX + ".selectUser", id);

        if (user.getUserInfoObjectId() != null){
            user.setUserInfo(getUserInfoStrategy().findById(user.getUserInfoObjectId()));
        }else{
            user.setUserInfo(getUserInfoStrategy().newInstance());
        }

        return user;
    }

    @Transactional
    public void save(User user){
        if (user.getId() == null){ //Сохранение нового пользователя
            //сохранение информации о пользователе
            getUserInfoStrategy().insert(user.getUserInfo());
            user.setUserInfoObjectId(user.getUserInfo().getId());

            user.setPassword(DigestUtils.md5Hex(user.getLogin())); //md5 password

            sqlSession().insert(STATEMENT_PREFIX + ".insertUser", user);

            //сохранение групп привилегий
            for(UserGroup userGroup : user.getUserGroups()){
                userGroup.setLogin(user.getLogin());

                //сохранение информации о пользователе
                sqlSession().insert(STATEMENT_PREFIX + ".insertUserGroup", userGroup);
            }
        }else{ //Редактирование пользователя
            User dbUser = (User) sqlSession().selectOne(STATEMENT_PREFIX + ".selectUser", user.getId());

            //удаление групп привилегий
            for (UserGroup dbUserGroup : dbUser.getUserGroups()){
                boolean contain = false;

                for (UserGroup userGroup : user.getUserGroups()){
                    if (userGroup.getGroupName().equals(dbUserGroup.getGroupName())){
                        contain = true;
                        break;
                    }
                }

                if (!contain){
                    sqlSession().delete(STATEMENT_PREFIX + ".deleteUserGroup", dbUserGroup.getId());
                }
            }

            //добавление групп привилегий
            for (UserGroup userGroup : user.getUserGroups()){
                boolean contain = false;

                for (UserGroup dbUserGroup : dbUser.getUserGroups()){
                    if (userGroup.getGroupName().equals(dbUserGroup.getGroupName())){
                        contain = true;
                        break;
                    }
                }

                if (!contain){
                    userGroup.setLogin(user.getLogin());
                    sqlSession().insert(STATEMENT_PREFIX + ".insertUserGroup", userGroup);
                }
            }

            //изменение пароля
            if(user.getNewPassword() != null){
                user.setPassword(DigestUtils.md5Hex(user.getNewPassword())); //md5 password
                sqlSession().update(STATEMENT_PREFIX + ".updateUser", user);
            }else{
                user.setPassword(null); //не обновлять пароль
            }

            //сохранение информации о пользователе
            if (user.getUserInfoObjectId() != null){
                DomainObject userInfo = user.getUserInfo();
                getUserInfoStrategy().update(getUserInfoStrategy().findById(userInfo.getId()), userInfo, DateUtil.getCurrentDate());
            }else{
                getUserInfoStrategy().insert(user.getUserInfo());
                user.setUserInfoObjectId(user.getUserInfo().getId());
                sqlSession().update(STATEMENT_PREFIX + ".updateUser", user);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<User> getUsers(UserFilter filter){
        List<User> users = sqlSession().selectList(STATEMENT_PREFIX + ".selectUsers", filter);
        //todo change to db load
        for (User user : users){
            if (user.getUserInfoObjectId() != null){
                user.setUserInfo(getUserInfoStrategy().findById(user.getUserInfoObjectId()));
            }
        }

        return users;
    }

    @Transactional
    public int getUsersCount(UserFilter filter){
        return (Integer) sqlSession().selectOne(STATEMENT_PREFIX + ".selectUsersCount", filter);
    }

    public UserFilter newUserFilter(){
        UserFilter userFilter = new UserFilter();

        for (EntityAttributeType entityAttributeType : getUserInfoStrategy().getListColumns()){
            userFilter.getAttributeExamples().add(new AttributeExample(entityAttributeType.getId()));
        }

        return userFilter;
    }

    public List<Attribute> getAttributeColumns(DomainObject object) {
        if (object == null) {
            return getUserInfoStrategy().newInstance().getAttributes();
        }

        List<EntityAttributeType> entityAttributeTypes = getUserInfoStrategy().getListColumns();
        List<Attribute> attributeColumns = new ArrayList<Attribute>(entityAttributeTypes.size());

        for (EntityAttributeType entityAttributeType : entityAttributeTypes) {
            for (Attribute attribute : object.getAttributes()) {
                if (attribute.getAttributeTypeId().equals(entityAttributeType.getId())) {
                    attributeColumns.add(attribute);
                }
            }
        }

        return attributeColumns;
    }
}

