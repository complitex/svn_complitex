package org.complitex.dictionary.service;

import org.complitex.dictionary.entity.FilterWrapper;

import java.io.Serializable;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.10.12 16:31
 */
public interface IListBean<T extends Serializable> {
    List<T> getList(FilterWrapper<T> filterWrapper);

    Integer getCount(FilterWrapper<T> filterWrapper);
}
