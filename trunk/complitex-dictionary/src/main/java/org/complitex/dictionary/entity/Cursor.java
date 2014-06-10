package org.complitex.dictionary.entity;

import java.util.List;

/**
 * @author Anatoly Ivanov
 *         Date: 10.06.14 18:41
 */
public class Cursor<T> {
    private Integer resultCode;
    private List<T> list;

    public Cursor(Integer resultCode, List<T> list) {
        this.resultCode = resultCode;
        this.list = list;
    }

    public boolean isEmpty(){
        return list == null || list.isEmpty();
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public List<T> getList() {
        return list;
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "resultCode=" + resultCode +
                ", list=" + list +
                '}';
    }
}
