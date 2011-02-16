package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.Name;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.02.11 14:53
 */
@Stateless
public class NameBean extends AbstractBean{
    public final static String NS = NameBean.class.getName();

    /*select names by filter*/

    @SuppressWarnings({"unchecked"})
    public List<String> getFirstNames(String filter){
        return sqlSession().selectList(NS + ".selectFirstNames", filter);
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getMiddleNames(String filter){
        return sqlSession().selectList(NS + ".selectMiddleNames", filter);
    }

    @SuppressWarnings({"unchecked"})
    public List<String> getLastNames(String filter){
        return sqlSession().selectList(NS + ".selectLastNames", filter);
    }

    /*select name by id*/

    public String getFirstName(Long id){
        return (String) sqlSession().selectOne(NS + ".selectFirstName", id);
    }

    public String getMiddleName(Long id){
        return (String) sqlSession().selectOne(NS + ".selectMiddleName", id);
    }

    public String getLastName(Long id){
        return (String) sqlSession().selectOne(NS + ".selectLastName", id);
    }

    /*select id by name*/

    @Transactional
    public Long getFirstNameId(String firstName, boolean createIfNotExist){
        Long id = (Long) sqlSession().selectOne(NS + ".selectFirstNameId", firstName);

        if (id == null && createIfNotExist){
            id = saveFirstName(firstName);
        }

        return id;
    }

    @Transactional
    public Long getMiddleNameId(String middleName, boolean createIfNotExist){
        Long id = (Long) sqlSession().selectOne(NS + ".selectMiddleNameId", middleName);

        if (id == null && createIfNotExist){
            id = saveMiddleName(middleName);
        }

        return id;
    }

    @Transactional
    public Long getLastNameId(String lastName, boolean createIfNotExist){
        Long id = (Long) sqlSession().selectOne(NS + ".selectLastNameId", lastName);

        if (id == null && createIfNotExist){
            id = saveLastName(lastName);
        }

        return id;
    }

    /*save name*/

    @Transactional
    public Long saveFirstName(String firstName){
        Name name = new Name(formatCase(firstName));

        sqlSession().insert(NS + ".insertFirstName", name);

        return name.getId();
    }

    @Transactional
    public Long saveMiddleName(String middleName){
        Name name = new Name(formatCase(middleName));

        sqlSession().insert(NS + ".insertMiddleName", name);

        return name.getId();
    }

    @Transactional
    public Long  saveLastName(String lastName){
        Name name = new Name(formatCase(lastName));

        sqlSession().insert(NS + ".insertLastName", name);

        return name.getId();
    }

    private String formatCase(String s){
        if (s == null || s.length() < 1 || s.indexOf('-') > -1){
            return s;
        }

        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
