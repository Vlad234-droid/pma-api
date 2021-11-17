package com.tesco.pma.colleague.profile.parser;

import com.github.pjfanning.xlsx.StreamingReader;
import com.tesco.pma.colleague.profile.parser.model.FieldDescriptor;
import com.tesco.pma.colleague.profile.parser.model.FieldSet;
import com.tesco.pma.colleague.profile.parser.model.Metadata;
import com.tesco.pma.colleague.profile.parser.model.ParsingResult;
import com.tesco.pma.colleague.profile.parser.model.ParsingError;
import com.tesco.pma.colleague.profile.parser.model.Value;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.LocaleUtil;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import static com.tesco.pma.colleague.profile.parser.ParsingErrorCode.PARSE_IO;
import static com.tesco.pma.colleague.profile.parser.ParsingErrorCode.PARSE_NOT_OOXML;
import static com.tesco.pma.colleague.profile.parser.ParsingErrorCode.PARSE_PASSWORD_PROTECTED;
import static com.tesco.pma.colleague.profile.parser.ParsingErrorCode.PARSE_UNHANDLED_ERROR;
import static com.tesco.pma.colleague.profile.parser.model.ValueType.BOOLEAN;
import static com.tesco.pma.colleague.profile.parser.model.ValueType.DATE;
import static com.tesco.pma.colleague.profile.parser.model.ValueType.NUMBER;
import static com.tesco.pma.colleague.profile.parser.model.ValueType.STRING;

@Slf4j
@Data
public class XlsxParser {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int DEFAULT_ROW_CACHE_SIZE = 20;
    private static final boolean DEFAULT_READ_COMMENTS = false;
    private static final boolean DEFAULT_AVOID_TEMP_FILES = true;

    /**
     * DecimalFormat pattern.
     * Ex: 0.00###
     */
    private String outNumberFormat = "0.###";

    /**
     * boolean values in form <pre>[true value]|[false value](,[true value]|[false value])</pre>.
     * ex. 'Y|N,yes|no,T|F'
     */
    private String booleanConversion = "1|0";

    public ParsingResult parse(@NonNull InputStream is) {
        final var res = ParsingResult.builder();

        try (var wb = createWorkBook(is)) {

            var iterator = wb.iterator();
            if (iterator.hasNext()) {
                var sheet = iterator.next();
                final var sheetParser = new SheetParser(res, sheet);
                return sheetParser.parse();
            }

        } catch (OLE2NotOfficeXmlFileException e) {
            //TODO:: implement different algorithm of password detection.
            processError(res, e, PARSE_PASSWORD_PROTECTED);
        } catch (NotOfficeXmlFileException e) {
            processError(res, e, PARSE_NOT_OOXML);
        } catch (IOException e) {
            processError(res, e, PARSE_IO);
        } catch (Exception e) {
            processError(res, e, PARSE_UNHANDLED_ERROR);
        }

        return res.build();
    }


    private void processError(ParsingResult.ParsingResultBuilder res, Exception ex, ParsingErrorCode parsingErrorCode) {
        final var error = ParsingError.builder().code(parsingErrorCode.getCode()).build();
        res.success(false).error(error);
        log.error(parsingErrorCode.getCode(), ex);
    }


    private Workbook createWorkBook(InputStream is) {
        return StreamingReader.builder()
                .bufferSize(DEFAULT_BUFFER_SIZE)
                .rowCacheSize(DEFAULT_ROW_CACHE_SIZE)
                .setReadComments(DEFAULT_READ_COMMENTS)
                .setAvoidTempFiles(DEFAULT_AVOID_TEMP_FILES)
                .open(is);
    }

    private class SheetParser {

        private final Sheet sheet;
        private final ParsingResult.ParsingResultBuilder resultBuilder;
        private final DataFormatter df;
        private final ValueCreatorHolder valueCreatorHolder;
        private final ZoneId timezone = ZoneOffset.UTC;
        private final Locale locale = Locale.ENGLISH;

        private DecimalFormat numberFormat;
        private Metadata metadata;

        SheetParser(ParsingResult.ParsingResultBuilder builder, Sheet sheet) {
            this.sheet = sheet;
            this.resultBuilder = builder;
            valueCreatorHolder = new ValueCreatorHolder();
            configure();
            df = new DataFormatter(locale);
        }

        public ParsingResult parse() {
            final var userLocale = LocaleUtil.getUserLocale();
            final var userTimeZone = LocaleUtil.getUserTimeZone();
            LocaleUtil.setUserLocale(locale);
            LocaleUtil.setUserTimeZone(TimeZone.getTimeZone(timezone));
            try {
                final var rowIterator = sheet.rowIterator();

                processToData(rowIterator);

                processData(rowIterator);

                return resultBuilder.success(true).build();
            } finally {
                LocaleUtil.setUserLocale(userLocale);
                LocaleUtil.setUserTimeZone(userTimeZone);
            }
        }

        private void configure() {

            if (StringUtils.isNotBlank(XlsxParser.this.getOutNumberFormat())) {
                numberFormat = new DecimalFormat(XlsxParser.this.getOutNumberFormat(), new DecimalFormatSymbols(Locale.ROOT));
            } else {
                numberFormat = new DecimalFormat();
            }

            if (StringUtils.isNotBlank(XlsxParser.this.getBooleanConversion())) {
                var booleanValueCreator = new BooleanValueCreator();
                for (String booleanPair : XlsxParser.this.getBooleanConversion().split(",")) {
                    final var booleanValues = booleanPair.split("\\|");
                    booleanValueCreator.addTrueValue(booleanValues[0]);
                    booleanValueCreator.addFalseValue(booleanValues[1]);
                }
                valueCreatorHolder.addValueCreator(booleanValueCreator);
            }
        }

        private void processToData(Iterator<Row> rowIterator) {
            Map<Integer, FieldDescriptor.FieldDescriptorBuilder> descriptorsByColumnIndex = new TreeMap<>();

            if (rowIterator.hasNext()) {
                final var row = rowIterator.next();
                Map<String, List<Integer>> cellIndexesByValue = prepareCellIndexesByValue(descriptorsByColumnIndex, row);

                cellIndexesByValue.entrySet().stream()
                        .filter(e -> e.getValue().size() == 1)
                        .forEach(k -> descriptorsByColumnIndex.get(k.getValue().iterator().next()).name(k.getKey()));

                cellIndexesByValue.entrySet().stream()
                        .filter(e -> e.getValue().size() > 1)
                        .forEach(e -> resultBuilder.error(ParsingErrorUtils.multiProperties(e.getKey(), e.getValue())));
            }

            var metadataBuilder = Metadata.builder();
            // build, filter, sort descriptors and add to meta
            descriptorsByColumnIndex.values().stream()
                    .map(FieldDescriptor.FieldDescriptorBuilder::build)
                    .sorted(Comparator.comparingInt(d -> CellReference.convertColStringToIndex(d.getId())))
                    .forEach(metadataBuilder::descriptor);
            metadata = metadataBuilder.build();
            resultBuilder.metadata(metadata);
        }

        private Map<String, List<Integer>> prepareCellIndexesByValue(Map<Integer,
                FieldDescriptor.FieldDescriptorBuilder> descriptorsByColumnIndex, Row row) {
            Map<String, List<Integer>> cellIndexesByValue = new HashMap<>();
            for (Cell cell : row) {
                descriptorsByColumnIndex.computeIfAbsent(
                        cell.getColumnIndex(), columnIndex ->
                                FieldDescriptor.builder().id(CellReference.convertNumToColString(columnIndex)));
                final var value = df.formatCellValue(cell);
                cellIndexesByValue.computeIfAbsent(value, s -> new ArrayList<>()).add(cell.getColumnIndex());
            }
            return cellIndexesByValue;
        }

        private void processData(Iterator<Row> rowIterator) {
            if (metadata.getDescriptors().isEmpty()) {
                //no descriptors - skip data
                return;
            }

            while (rowIterator.hasNext()) {
                final var row = rowIterator.next();
                var fieldSetBuilder = FieldSet.builderForId(row.getRowNum() + 1);
                for (FieldDescriptor descriptor : metadata.getDescriptors()) {
                    final var columnIndex = CellReference.convertColStringToIndex(descriptor.getId());
                    final var cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    fieldSetBuilder.value(descriptor.getName(), createValue(cell, cell.getCellType()));
                }
                resultBuilder.datum(fieldSetBuilder.build());
            }
        }

        private Value createValue(Cell cell, CellType cellType) { //NOPMD
            final Value res;
            final var cellReference = new CellReference(cell).formatAsString(false);

            switch (cellType) {
                case _NONE:
                case BLANK:
                    res = Value.blank();
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        final var original = Long.toString(cell.getDateCellValue().getTime());
                        final var formatted = cell.getDateCellValue().toInstant().toString();
                        res = Value.of(DATE, original, formatted);
                    } else {
                        final var original = cell.getNumericCellValue();
                        final var formatted = getNumberFormat().format(cell.getNumericCellValue());
                        res = Value.of(NUMBER, Double.toString(original), formatted);
                    }
                    break;
                case STRING:
                    res = valueCreatorHolder.resolveValueCreator(cell.getStringCellValue())
                            .create(cell.getStringCellValue());
                    break;
                case BOOLEAN:
                    final var original = Boolean.toString(cell.getBooleanCellValue());
                    res = Value.of(BOOLEAN, original, original);
                    break;
                case FORMULA:
                    res = createValue(cell, cell.getCachedFormulaResultType());
                    break;
                case ERROR:
                    resultBuilder.error(ParsingErrorUtils.cellError(cell.getStringCellValue(),
                            df.formatCellValue(cell), cellReference));
                    res = Value.error(df.formatCellValue(cell));
                    break;
                default:
                    //should never happens but
                    resultBuilder.error(ParsingErrorUtils.cellUnknownType(cellType, cellReference));
                    res = Value.error(null);
            }

            return res;
        }

        private NumberFormat getNumberFormat() {
            return numberFormat;
        }


    }

    interface ValueCreator {
        boolean canCreate(String value);

        Value create(String value);
    }

    static class ValueCreatorHolder {
        ValueCreator defaultValueCreator = new ValueCreator() {
            @Override
            public boolean canCreate(String value) {
                return true;
            }

            @Override
            public Value create(String value) {
                final var formatted = value.strip();
                if (formatted.isEmpty()) {
                    return Value.blank(value);
                }
                return Value.of(STRING, value, formatted);
            }
        };

        List<ValueCreator> valueCreators = new ArrayList<>();

        void addValueCreator(ValueCreator creator) {
            valueCreators.add(creator);
        }

        ValueCreator resolveValueCreator(String value) {
            for (ValueCreator valueCreator : valueCreators) {
                if (valueCreator.canCreate(value)) {
                    return valueCreator;
                }
            }
            return defaultValueCreator;
        }

    }

    static class BooleanValueCreator implements ValueCreator {
        private final Set<String> trueValuesUppercase = new HashSet<>();
        private final Set<String> falseValuesUppercase = new HashSet<>();
        private final Map<String, Boolean> cache = new HashMap<>();

        @Override
        public boolean canCreate(String value) {
            return cache.containsKey(value)
                    || trueValuesUppercase.contains(value)
                    || falseValuesUppercase.contains(value);
        }

        public void addTrueValue(String trueValue) {
            trueValuesUppercase.add(trueValue.trim().toUpperCase());
        }

        public void addFalseValue(String falseValue) {
            falseValuesUppercase.add(falseValue.trim().toUpperCase());
        }

        @Override
        public Value create(String value) {
            if (!cache.containsKey(value)) {
                if (trueValuesUppercase.contains(value.toUpperCase())) {
                    cache.put(value, Boolean.TRUE);
                } else if (falseValuesUppercase.contains(value.toUpperCase())) {
                    cache.put(value, Boolean.FALSE);
                }
            }
            return Value.of(BOOLEAN, value, cache.get(value).toString());
        }
    }
}
