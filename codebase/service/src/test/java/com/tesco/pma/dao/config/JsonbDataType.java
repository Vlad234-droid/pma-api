package com.tesco.pma.dao.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

class JsonbDataType extends AbstractDataType {

    private static final ObjectMapper SORTED_MAPPER = new ObjectMapper();

    static {
        SORTED_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public JsonbDataType() {
        super("jsonb", Types.OTHER, String.class, false);
    }

    @Override
    public Object typeCast(Object obj) throws TypeCastException {
        if (obj == null || obj == ITable.NO_VALUE) {
            return null;
        }
        try {
            return convertNode(SORTED_MAPPER.readTree(obj.toString()));
        } catch (JsonProcessingException e) {
            throw new TypeCastException(e);
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

    private String convertNode(final JsonNode node) throws JsonProcessingException {
        final Object obj = SORTED_MAPPER.treeToValue(node, Object.class);
        return SORTED_MAPPER.writeValueAsString(obj);
    }
}