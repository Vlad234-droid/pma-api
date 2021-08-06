package com.tesco.pma.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Parent general class for different Errors. And also general structure for errors details.
 * Can be extended for exceptions specific details.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail implements Serializable {
    private static final long serialVersionUID = 6291929783083881754L;

    private String code;
    private String message;
}
