package org.complitex.dictionaryfw.web;

import org.complitex.dictionaryfw.entity.Preference;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.11.10 17:08
 */
public interface ISessionStorage {
    public List<Preference> load();

    public void save(Preference preference);

    public Long getUserId();
}
