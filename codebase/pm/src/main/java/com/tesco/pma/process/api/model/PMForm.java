package com.tesco.pma.process.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 19.10.2021 Time: 11:07
 */
@Data
@AllArgsConstructor
public class PMForm {
    private String key;
    private String name;
    private String json;
}