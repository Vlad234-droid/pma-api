package com.tesco.pma.colleague.api;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ColleagueList implements Serializable {

    private static final long serialVersionUID = -5098417934686933901L;

    private List<Colleague> colleagues;
}
