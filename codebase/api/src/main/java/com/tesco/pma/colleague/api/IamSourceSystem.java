package com.tesco.pma.colleague.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class IamSourceSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String source;
}
