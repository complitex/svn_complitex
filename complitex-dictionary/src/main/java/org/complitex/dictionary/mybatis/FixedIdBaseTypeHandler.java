package org.complitex.dictionary.mybatis;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.09.12 18:21
 */
public class FixedIdBaseTypeHandler<T extends Enum & IFixedIdType> implements TypeHandler<T> {
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
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            ps.setLong(i, parameter.getId());
        }else {
            ps.setNull(i, jdbcType.TYPE_CODE);
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        Long l = rs.getLong(columnName);

        return !rs.wasNull() ? getType(l) : null;
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        Long l = rs.getLong(columnIndex);

        return !rs.wasNull() ? getType(l) : null;
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        Long l = cs.getLong(columnIndex);

        return !cs.wasNull() ? getType(l) : null;
    }
}
