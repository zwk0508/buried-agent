package com.zwk.helper;

import com.zwk.annotation.GeneratedMethod;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodHelper {
    private static final Map<String, Class<?>> primitiveType = new HashMap<>();

    static {
        primitiveType.put("B", byte.class);
        primitiveType.put("S", short.class);
        primitiveType.put("I", int.class);
        primitiveType.put("J", long.class);
        primitiveType.put("F", float.class);
        primitiveType.put("D", double.class);
        primitiveType.put("C", char.class);
        primitiveType.put("Z", boolean.class);
    }

    public static Method getMethod(GeneratedMethod generateMethod, Class<?> clazz, ClassLoader classLoader, Class<?>[] args) throws NoSuchMethodException, ClassNotFoundException {
        if (classLoader == null) {
            classLoader = MethodHelper.class.getClassLoader();
        }
        String methodName = generateMethod.value();
        if (args == null) {
            String descriptor = generateMethod.descriptor();
            Type methodType = Type.getMethodType(descriptor);
            Type[] argumentTypes = methodType.getArgumentTypes();
            int len = argumentTypes != null ? argumentTypes.length : 0;
            args = new Class[len];
            for (int i = 0; i < len; i++) {
                Type argumentType = argumentTypes[i];
                String argumentTypeDescriptor = argumentType.getDescriptor();
                Class<?> arg = primitiveType.get(argumentTypeDescriptor);
                if (arg == null) {
                    arg = Class.forName(argumentType.getClassName(), false, classLoader);
                }
                args[i] = arg;
            }
        }
        return clazz.getDeclaredMethod(methodName, args);
    }

    public static Method getMethod(GeneratedMethod generateMethod, Class<?> clazz) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(generateMethod, clazz, null, null);
    }

    public static Method getMethod(GeneratedMethod generateMethod, Class<?> clazz, ClassLoader loader) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(generateMethod, clazz, loader, null);
    }

    public static Method getMethod(GeneratedMethod generateMethod, Class<?> clazz, Class<?>[] args) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(generateMethod, clazz, null, args);
    }
}
