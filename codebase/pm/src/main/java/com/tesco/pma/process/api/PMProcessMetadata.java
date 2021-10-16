package com.tesco.pma.process.api;

import java.util.ArrayList;
import java.util.List;

import com.tesco.pma.process.api.model.PMElement;

import lombok.Getter;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 22:35
 */
@Getter
public class PMProcessMetadata {
    private List<PMElement> elements = new ArrayList<>();
}
