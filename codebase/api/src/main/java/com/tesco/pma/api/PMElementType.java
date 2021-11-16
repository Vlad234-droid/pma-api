package com.tesco.pma.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PMElementType {

    ELEMENT,
    TIMELINE_POINT,
    REVIEW,
    CYCLE;
}
