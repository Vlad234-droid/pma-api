package com.tesco.pma.dao.utils.jsonb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.api.Jsonb;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class JsonbTypeHandler<T extends Jsonb> implements TypeHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonbTypeHandler.class);

    private final Class<T> jsonbClass;
    private final ObjectMapper objectMapper;

    public JsonbTypeHandler(Class<T> jsonbClass, ObjectMapper objectMapper) {
        this.jsonbClass = jsonbClass;
        this.objectMapper = objectMapper;
    }

    @Override
    public void setParameter(PreparedStatement ps, int parameterIndex, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setObject(parameterIndex, null, Types.OTHER);
        } else {
            try {
                ps.setObject(parameterIndex, objectMapper.writeValueAsString(parameter), Types.OTHER);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Cannot serialize json", e);
                ps.setObject(parameterIndex, null, Types.OTHER);
            }
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        return toJsonbObject(rs.getString(columnName));
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        return toJsonbObject(rs.getString(columnIndex));
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toJsonbObject(cs.getString(columnIndex));
    }

    private T toJsonbObject(String val) {
        if (val == null) {
            return null;
        }
        try {
            return objectMapper.readValue(val, jsonbClass);
        } catch (Exception e) {
            LOGGER.warn("Cannot deserialize json", e);
            return null;
        }
    }
}
