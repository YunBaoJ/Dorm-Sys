package com.dorm.backend.enums;

/**
 * 床位状态枚举
 */
public enum BedStatusEnum {
    EMPTY("EMPTY", "空闲"),
    OCCUPIED("OCCUPIED", "已入住"),
    BROKEN("BROKEN", "损坏");

    private final String value;
    private final String label;

    BedStatusEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    public static BedStatusEnum fromValue(String value) {
        for (BedStatusEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }
}
