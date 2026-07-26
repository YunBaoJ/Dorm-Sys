package com.dorm.backend.enums;

/**
 * 房间状态枚举
 */
public enum RoomStatusEnum {
    NORMAL("NORMAL", "正常"),
    FULL("FULL", "已满"),
    MAINTENANCE("MAINTENANCE", "维护中");

    private final String value;
    private final String label;

    RoomStatusEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() { return value; }
    public String getLabel() { return label; }

    public static RoomStatusEnum fromValue(String value) {
        for (RoomStatusEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }
}
