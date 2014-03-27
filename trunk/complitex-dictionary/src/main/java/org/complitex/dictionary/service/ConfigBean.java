package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.IConfig;
import org.complitex.dictionary.mybatis.Transactional;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 10:54:14
 */
@Startup
@Singleton(name = "ConfigBean")
public class ConfigBean extends AbstractBean{
    private final Logger log = LoggerFactory.getLogger(ConfigBean.class);

    private static final String MAPPING_NAMESPACE = ConfigBean.class.getName();

    private Map<IConfig, String> configMap = new LinkedHashMap<>();

    private Set<String> resourceBundle = new HashSet<String>();

    @PostConstruct
    public void init(){

        Set<Class<? extends IConfig>> configs = getIConfigClasses();

        for (Class<? extends IConfig> c : configs){
            try {
                if (c.getEnumConstants() != null) {
                    init2(c.getCanonicalName(),c.getEnumConstants());
                }
            } catch (Exception e) {
                log.error("Ошибка создания конфигурации", e);
            }
        }
    }

    //OpenJDK bug: deployment exception on method overriding
    private void init2(String bundle, IConfig... configs){
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

    public Map<String, List<IConfig>> getConfigGroups(){
        Map<String, List<IConfig>> map = new LinkedHashMap<>();

        for (IConfig c : getConfigs()){
            if (!map.containsKey(c.getGroupKey())){
                map.put(c.getGroupKey(), new ArrayList<IConfig>());
            }

            map.get(c.getGroupKey()).add(c);
        }

        return map;
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

                log.warn("хм.. нет записи для конфигурации {} в базе данных, используем значение по умолчанию {}", config.name(), value);
            }

            configMap.put(config, value);
        }

        return configMap.get(config);
    }

    public String getString(IConfig config){
        return getString(config, false);
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

    public Long getLong(IConfig config, boolean flush){
        try {
            return Long.valueOf(getString(config, flush));
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

    protected Set<Class<? extends IConfig>> getIConfigClasses() {
        Reflections reflections = new Reflections("org.complitex", "ru.flexpay");

        return reflections.getSubTypesOf(IConfig.class);
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
