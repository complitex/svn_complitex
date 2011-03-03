package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.IConfig;
import org.complitex.dictionary.mybatis.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 10:54:14
 */
@Singleton(name = "ConfigBean")
public class ConfigBean extends AbstractBean{
    private static final Logger log = LoggerFactory.getLogger(ConfigBean.class);

    private static final String MAPPING_NAMESPACE = ConfigBean.class.getName();

    private Map<IConfig, String> configMap = new LinkedHashMap<IConfig, String>();

    private Set<String> resourceBundle = new HashSet<String>();

    public void init(String bundle, IConfig... configs){
        resourceBundle.add(bundle);

        for (IConfig config : configs){
            if (!isExist(config.name())){
                insert(config.name(), config.getDefaultValue());
            }

            configMap.put(config, getValue(config.name()));
        }
    }

    public void addResourceBundle(String bundle){
        resourceBundle.add(bundle);
    }

    public Set<String> getResourceBundles() {
        return resourceBundle;
    }

    public Set<IConfig> getConfigs(){
        return configMap.keySet();
    }

    /**
     * Возвращает строковое значение параметра
     * @param config имя
     * @param flush отчистить кэш, обновить значение из базы данных
     * @return числовое строковое параметра
     */
    public String getString(IConfig config, boolean flush){
        if (flush){
            String value = getValue(config.name());

            if (value == null){
                value = config.getDefaultValue();

                log.warn("Нет значений в таблице настроек, а по-хорошему должны быть...");
            }

            configMap.put(config, value);
        }

        return configMap.get(config);
    }

    /**
     * Возвращает числовое значение параметра
     * @param config имя
     * @param flush отчистить кэш, обновить значение из базы данных
     * @return числовое значение параметра
     */
    public Integer getInteger(IConfig config, boolean flush){
        try {
            return Integer.valueOf(getString(config, flush));
        } catch (NumberFormatException e) {
            log.error("Config type error", e);

            return null;
        }
    }

    @Transactional
    public void update(final IConfig config, final String value){
        sqlSession().update(MAPPING_NAMESPACE + ".updateConfig", new HashMap<String, String>() {{
            put("name", config.name());
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
