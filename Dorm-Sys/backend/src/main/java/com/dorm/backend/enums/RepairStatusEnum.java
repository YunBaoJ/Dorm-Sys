package com.dorm.backend.enums;

/**
 * 报修状态枚举
 */
public enum RepairStatusEnum {
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    COMPLETED("COMPLETED", "已完成");

    private final String value;
    private final String label;

    RepairStatusEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    public static RepairStatusEnum fromValue(String value) {
        for (RepairStatusEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }
}
