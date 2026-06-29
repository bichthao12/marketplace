package com.marketplace.common.util;

public final class EnumFormat {

    private EnumFormat() {
    }

    public static String toApi(Enum<?> value) {
        if (value == null) {
            return null;
        }
        return value.name().toLowerCase();
    }
}
