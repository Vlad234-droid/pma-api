package com.tesco.pma.dao.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

class JsonbDataType extends AbstractDataType {

    public JsonbDataType() {
        super("jsonb", Types.OTHER, String.class, false);
    }

    @Override
    public Object typeCast(Object obj) {
        if (obj == null || obj == ITable.NO_VALUE) {
            return null;
        }
        try {
            return new ObjectMapper().readTree(obj.toString()).asText();
        } catch (JsonProcessingException e) {
            return new TypeCastException(e);
        }
    }

    @Override
    public Object getSqlValue(int column, ResultSet resultSet) throws SQLException {
        return resultSet.getString(column);
    }

    @Override
    public void setSqlValue(Object value,
                            int column,
                            PreparedStatement statement) throws SQLException {
        final PGobject jsonObj = new PGobject();
        jsonObj.setType("json");
        jsonObj.setValue(value == null ? null : value.toString());

        statement.setObject(column, jsonObj);
    }
}