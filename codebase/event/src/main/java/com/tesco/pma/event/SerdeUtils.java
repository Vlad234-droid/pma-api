package com.tesco.pma.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.UtilityClass;

@Slf4j
@UtilityClass
public class SerdeUtils {

    public static final String OBJECT_CLASS_FIELD = "class";
    public static final String OBJECT_VALUE_FIELD = "value";

    // Note: to parse localized zone offset (like 'GMT+6') - space should be added to the end of the datetime string.
    // All other formats works fine without any changes.
    // See https://bugs.openjdk.java.net/browse/JDK-8154050
    static final DateTimeFormatter LOOSE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd[ ]['T']HH:mm:ss[.SSS][.SS][XXX][XX][X][O][OOOO][ ]");

    public enum EventProperties {
        EVENT_NAME, EVENT_ID, EVENT_PRIORITY, CALLBACK_EVENT, CALLBACK_SERVICE_URL, PROPERTIES, CREATION_DATE;

        public static EventProperties find(String name) {
            return Arrays.stream(EventProperties.values()).filter(candidate -> candidate.name().equals(name)).findAny().orElse(null);
        }
    }

    public static SimpleDateFormat getDateFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //NOPMD
    }

    public enum SupportedTypes {
        LONG(Long.class), INT(Integer.class), FLOAT(Float.class), DOUBLE(Double.class), STRING(String.class), BIGINT(BigInteger.class),
        BIGDECIMAL(BigDecimal.class), EVENT(EventSupport.class), DATE(java.util.Date.class),
        SERIALIZABLE(Serializable.class);

        private final Class<? extends Serializable> clazz;

        SupportedTypes(Class<? extends Serializable> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends Serializable> getClazz() {
            return clazz;
        }

        public static Optional<SupportedTypes> getSupportedType(String className) {
            Optional<SupportedTypes> optionalType =
                    Arrays.stream(SupportedTypes.values()).filter(type -> type.clazz.getName().equals(className)).findAny();
            if (optionalType.isPresent()) {
                return optionalType;
            } else {
                try {
                    if (SERIALIZABLE.clazz.isAssignableFrom(Class.forName(className))) {
                        return Optional.of(SERIALIZABLE);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Fail to find class - " + className, e);
                }
                return Optional.empty();
            }
        }

        public static Optional<SupportedTypes> getSupportedType(Object object) {
            return getSupportedType(object.getClass().getName());
        }
    }
}
