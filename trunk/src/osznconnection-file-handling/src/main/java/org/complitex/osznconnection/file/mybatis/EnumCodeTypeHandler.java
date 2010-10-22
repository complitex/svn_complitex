/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.complitex.osznconnection.file.entity.EnumCodeManager;
import org.complitex.osznconnection.file.entity.IEnumCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Artem
 */
public class EnumCodeTypeHandler extends BaseTypeHandler {

    private static final Logger log = LoggerFactory.getLogger(EnumCodeTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        IEnumCode enumCode = (IEnumCode) parameter;
//        if (log.isDebugEnabled()) {
//            log.debug("saving enum: {}, code: {}", enumCode.getClass(), enumCode.getCode());
//        }
        ps.setInt(i, enumCode.getCode());
    }

    @Override
    public IEnumCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
//        if (log.isDebugEnabled()) {
//            log.debug("getting code, column: {}, code: {}, was null: {}", new Object[]{columnName, rs.getInt(columnName), rs.wasNull()});
//        }
        int code = rs.getInt(columnName);
        if (!rs.wasNull()) {
            return EnumCodeManager.valueOf(code);
        }
        return null;
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
