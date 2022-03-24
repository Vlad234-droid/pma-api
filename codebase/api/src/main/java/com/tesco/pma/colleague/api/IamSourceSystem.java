package com.tesco.pma.colleague.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class IamSourceSystem implements Serializable {

    private static final long serialVersionUID = -5962142297883091083L;

    private String id;
    private String name;
    private String source;

    @Override
    public String toString() {
        return "IamSourceSystem{"
                + "id='*****'"
                + ", name='" + name + '\''
                + ", source='" + source + '\''
                + '}';
    }
}
