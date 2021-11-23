package com.tesco.pma.colleague.api.workrelationships;

public enum WorkLevel {
    WL1, WL2, WL3, WL4, WL5;

    public static WorkLevel getByCode(String code) {
        for (WorkLevel wl : values()) {
            if (wl.name().equalsIgnoreCase(code)) {
                return wl;
            }
        }
        return null;
    }
}
