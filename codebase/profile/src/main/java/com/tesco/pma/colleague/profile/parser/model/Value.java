package com.tesco.pma.colleague.profile.parser.model;

import com.tesco.pma.api.Identified;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.NonFinal;

import static com.tesco.pma.colleague.profile.parser.model.ValueType.BLANK;
import static com.tesco.pma.colleague.profile.parser.model.ValueType.ERROR;


@lombok.Value(staticConstructor = "of")
public class Value implements Identified<String> {
    @NonNull
    ValueType type;

    String original;

    String formatted;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @NonFinal
    @Setter(AccessLevel.PACKAGE)
    FieldSet fieldSet;

    @EqualsAndHashCode.Exclude
    @NonFinal
    @Setter(AccessLevel.PACKAGE)
    String id;

    public static Value of(ValueType type, String original, String formatted) { //NOPMD
        return of(type, original, formatted, null, null);
    }

    public static Value blank() {
        return blank(null);
    }

    public static Value blank(String original) {
        return Value.of(BLANK, original, null);
    }

    public static Value error(String originalValue) {
        return Value.of(ERROR, originalValue, null);
    }

    @Override
    public String getId() {
        return id;
    }


}
