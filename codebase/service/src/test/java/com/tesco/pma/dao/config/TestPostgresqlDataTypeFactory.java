package com.tesco.pma.dao.config;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

public class TestPostgresqlDataTypeFactory extends PostgresqlDataTypeFactory {

    private static final String JSONB = "jsonb";

    @Override
    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
        if (JSONB.equalsIgnoreCase(sqlTypeName)) {
            return new JsonbDataType();
        }
        return super.createDataType(sqlType, sqlTypeName);
    }

}