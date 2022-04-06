package com.tesco.pma.cep.cfapi.v2.exception;

import com.tesco.pma.api.CodeAware;

public enum DebugCodes implements CodeAware {

    /**
     * CEP event headers
     */
    CEP_EVENT_HEADERS,

    /**
     * CEP event payload
     */
    CEP_EVENT_PAYLOAD;

    @Override
    public String getCode() {
        return name();
    }

}
