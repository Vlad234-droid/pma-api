package com.tesco.pma.dao.utils;

import com.tesco.pma.api.Identified;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * TypeHandler for mybatis for identifier usage
 */
public abstract class AbstractIdentifiedEnumTypeHandler<T extends Enum<T> & Identified<Integer>> implements TypeHandler<T> {

    protected abstract Class<T> getEnumClass();

    @Override
    public void setParameter(PreparedStatement ps, int parameterIndex, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            ps.setInt(parameterIndex, parameter.getId());
        } else {
            ps.setNull(parameterIndex, Types.INTEGER);
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        var id = rs.getInt(columnName);
        return getEnumValue(id);
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        var id = rs.getInt(columnIndex);
        return getEnumValue(id);
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        var id = cs.getInt(columnIndex);
        return getEnumValue(id);
    }

    private T getEnumValue(int id) {
        for (T cnst : getEnumClass().getEnumConstants()) {
            if (cnst.getId() == id) {
                return cnst;
            }
        }
        return null;
    }

}