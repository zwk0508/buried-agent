package com.zwk.enums;

/**
 * 基本类型转成包装类型
 */
public enum BoxType {
    BYTE("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;"),
    SHORT("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;"),
    INT("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"),
    LONG("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"),
    FLOAT("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;"),
    DOUBLE("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;"),
    CHAR("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;"),
    BOOLEAN("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");

    BoxType(String owner, String name, String descriptor) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    /**
     * 方法所属的类
     */
    String owner;
    /**
     * 方法名称
     */
    String name;
    /**
     * 方法描述符
     */
    String descriptor;

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
