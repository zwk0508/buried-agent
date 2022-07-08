package com.zwk.enums;

import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * 方法的修饰符
 */
public enum MethodModifier {
    PUBLIC(Modifier.PUBLIC, "public"),
    PRIVATE(Modifier.PRIVATE, "private"),
    PROTECTED(Modifier.PROTECTED, "protected"),
    STATIC(Modifier.STATIC, "static"),
    FINAL(Modifier.FINAL, "final");

    MethodModifier(int access, String name) {
        this.access = access;
        this.name = name;
    }

    int access;
    String name;

    public int getAccess() {
        return access;
    }

    public static MethodModifier getModifier(String modifier) {
        for (MethodModifier value : values()) {
            if (Objects.equals(value.name, modifier)) {
                return value;
            }
        }
        return null;
    }
}
