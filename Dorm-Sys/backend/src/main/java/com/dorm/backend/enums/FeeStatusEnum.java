package com.dorm.backend.enums;

/**
 * 费用状态枚举
 */
public enum FeeStatusEnum {
    UNPAID("UNPAID", "未缴纳"),
    PAID("PAID", "已缴纳");

    private final String value;
    private final String label;

    FeeStatusEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    public static FeeStatusEnum fromValue(String value) {
        for (FeeStatusEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }
}
