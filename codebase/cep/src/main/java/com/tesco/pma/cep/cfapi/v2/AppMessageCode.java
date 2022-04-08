package com.tesco.pma.cep.cfapi.v2;

import com.tesco.pma.api.CodeAware;

public enum AppMessageCode implements CodeAware {

    /**
     * CEP event occurred
     */
    CEP_EVENT_OCCURRED;

    @Override
    public String getCode() {
        return name();
    }

}
