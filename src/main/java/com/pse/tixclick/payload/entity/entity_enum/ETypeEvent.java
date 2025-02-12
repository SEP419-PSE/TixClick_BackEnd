package com.pse.tixclick.payload.entity.entity_enum;

public enum ETypeEvent {
    ONLINE(0),
    OFFLINE(1);

    private final int value;

    ETypeEvent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ETypeEvent fromValue(int value) {
        for (ETypeEvent type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ETypeEvent value: " + value);
    }
}
