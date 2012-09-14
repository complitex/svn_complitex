package org.complitex.dictionary.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.09.12 18:21
 */
public class FixedIdBaseTypeHandler<T extends Enum & IFixedIdType> extends BaseTypeHandler<T>{
    private Class<T> _class;
    private final T[] enums;

    public FixedIdBaseTypeHandler(Class<T> _class) {
        this._class = _class;
        this.enums = _class.getEnumConstants();

        if (this.enums == null) {
            throw new IllegalArgumentException(_class.getSimpleName() + " does not represent an enum type.");
        }
    }

    private T getType(Long id){
        for (T type : enums){
            if (id.equals(type.getId())){
                return type;
            }
        }

        throw new IllegalArgumentException("Cannot convert " + id + " to " + _class.getSimpleName());
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter.getId());
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getType(rs.getLong(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getType(rs.getLong(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getType(cs.getLong(columnIndex));
    }
}
