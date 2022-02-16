package com.tesco.pma.config.parser;

import com.tesco.pma.config.parser.model.ParsingError;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.CellType;

import java.util.List;

import static com.tesco.pma.config.parser.ParsingErrorCode.PARSE_CELL_UNKNOWN_TYPE;
import static com.tesco.pma.config.parser.ParsingErrorCode.PARSE_ERROR_CELL;
import static com.tesco.pma.config.parser.ParsingErrorCode.PARSE_MULTI_PROPERTIES;

@UtilityClass
public class ParsingErrorUtils {

    public static ParsingError cellError(String stringCellValue, String formatCellValue, String cellReference) {
        return ParsingError.builder()
                .code(PARSE_ERROR_CELL.getCode())
                .property("stringCellValue", stringCellValue)
                .property("formatCellValue", formatCellValue)
                .property("cellReference", cellReference)
                .build();
    }

    public static ParsingError cellUnknownType(CellType cellType, String cellReference) {
        return ParsingError.builder()
                .code(PARSE_CELL_UNKNOWN_TYPE.getCode())
                .property("cellType", cellType.name())
                .property("cellReference", cellReference)
                .build();
    }

    public static ParsingError multiProperties(String property, List<Integer> sources) {
        return ParsingError.builder()
                .code(PARSE_MULTI_PROPERTIES.getCode())
                .property("property", property)
                .property("sources", sources.toString())
                .build();
    }
}
