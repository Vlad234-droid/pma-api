package com.tesco.pma.colleague.api.workrelationships;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkLevelHandler implements TypeHandler<WorkLevel> {

    @Override
    public void setParameter(PreparedStatement ps, int index, WorkLevel parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(index, parameter.name());
    }

    @Override
    public WorkLevel getResult(ResultSet rs, String columnName) throws SQLException {
        return WorkLevel.getByCode(rs.getString(columnName));
    }

    @Override
    public WorkLevel getResult(ResultSet rs, int columnIndex) throws SQLException {
        return WorkLevel.getByCode(rs.getString(columnIndex));
    }

    @Override
    public WorkLevel getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return WorkLevel.getByCode(cs.getString(columnIndex));
    }
}