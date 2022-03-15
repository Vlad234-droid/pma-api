package com.tesco.pma.pdp.api;

import com.tesco.pma.cycle.api.model.PMFormElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class PDPResponse implements Serializable {

    private static final long serialVersionUID = 5457741975642793776L;

    private List<PDPGoal> goals;

    private PMFormElement form;
}