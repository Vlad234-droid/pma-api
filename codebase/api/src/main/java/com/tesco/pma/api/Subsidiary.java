package com.tesco.pma.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * Subsidiary class.
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subsidiary {
    /**
     * Identifier.
     */
    UUID uuid;

    /**
     * Name.
     */
    String name;
}
