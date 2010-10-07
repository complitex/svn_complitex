package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.ConfigName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 10:54:14
 */
@Singleton(name = "ConfigBean")
public class ConfigBean extends AbstractBean{
    private static final Logger log = LoggerFactory.getLogger(ConfigBean.class);

    private static final String MAPPING_NAMESPACE = ConfigBean.class.getName();

    public Map<ConfigName, String> configs = new EnumMap<ConfigName, String>(ConfigName.class);

    @PostConstruct
    public void init(){
        for (ConfigName configName : ConfigName.values()){
            if (!isExist(configName.name())){
                insert(configName.name(), configName.getDefaultValue());
            }

            configs.put(configName, getValue(configName.name()));
        }
    }

    /**
     * Возвращает строковое значение параметра
     * @param configName имя 
     * @param flush отчистить кэш, обновить значение из базы данных
     * @return числовое строковое параметра
     */
    public String getString(ConfigName configName, boolean flush){
        if (flush){
            configs.put(configName, getValue(configName.name()));
        }

        return configs.get(configName);
    }

    /**
     * Возвращает числовое значение параметра
     * @param configName имя
     * @param flush отчистить кэш, обновить значение из базы данных
     * @return числовое значение параметра
     */
    public Integer getInteger(ConfigName configName, boolean flush){
        try {
            return Integer.valueOf(getString(configName, flush));
        } catch (NumberFormatException e) {
            log.error("Config type error", e);

            return null;
        }
    }

    @Transactional
    public void update(final ConfigName configName, final String value){
        sqlSession().insert(MAPPING_NAMESPACE + ".updateConfig", new HashMap<String, String>(){{
            put("name", configName.name());
            put("value", value);
        }});
    }

    @Transactional
    private void insert(final String name, final String value){
        sqlSession().insert(MAPPING_NAMESPACE + ".insertConfig", new HashMap<String, String>(){{
            put("name", name);
            put("value", value);
        }});
    }

    @Transactional
    private boolean isExist(String name){
        return (Boolean) sqlSession().selectOne(MAPPING_NAMESPACE + ".isExistConfig", name);
    }

    private String getValue(String name){
        return (String) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectConfigValue", name);
    }

}
