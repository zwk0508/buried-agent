package com.zwk.parse.correspondent;

import com.zwk.parse.HandlerInfo;

import java.util.List;
import java.util.Objects;

public interface Correspondent {

    default boolean methodModifier(int modifier) {
        return true;
    }

    default boolean methodReturnType(String retType) {
        return true;
    }

    default boolean className(String className) {
        return true;
    }

    default boolean methodName(String methodName) {
        return true;
    }

    default boolean methodArgs(List<String> args) {
        return true;
    }

    default boolean methodThrows(List<String> exceptions) {
        return true;
    }

    default boolean classAnnotation(List<String> annotations) {
        return true;
    }

    default Correspondent getInstance(){
        return this;
    }

    default HandlerInfo handlerInfo() {
        return HandlerInfo.DEFAULT_HANDLER;
    }




    default void populateInfo(ClassInfo classInfo, String token) {
        if (Objects.equals(token, "*")) {
            classInfo.star = true;
            return;
        }
        int end = token.length();
        if (token.endsWith("..*")) {
            classInfo.startWith = true;
            end = end - 3;
        } else if (token.endsWith(".*")) {
            classInfo.startWithPackage = true;
            end = end - 2;
        }
        classInfo.declare = token.substring(0, end);
    }

    default boolean validate(ClassInfo classInfo, String type) {
        if (classInfo.star) {
            return true;
        }
        if (classInfo.startWith) {
            return type.startsWith(classInfo.declare);
        }
        if (classInfo.startWithPackage) {
            int index = type.lastIndexOf(".");
            return Objects.equals(classInfo.declare, type.substring(0, index));
        }
        return Objects.equals(type, classInfo.declare);
    }

    default boolean validate(List<ClassInfo> classInfos, List<String> args) {
        if (classInfos == null || classInfos.size() == 0) {
            return true;
        }
        int size = classInfos.size();
        if (args == null || args.size() == 0) {
            if (size == 1) {
                ClassInfo classInfo = classInfos.get(0);
                return Objects.equals("**", classInfo.declare);
            }
            return false;
        }
        int argSize = args.size();
        if (size > argSize) {
            if (argSize != size - 1) {
                return false;
            }
        }
        for (int i = 0; i < size; i++) {
            ClassInfo classInfo = classInfos.get(i);
            if (Objects.equals("**", classInfo.declare)) {
                return true;
            }
            String s = args.get(i);
            if (!validate(classInfo, s)) {
                return false;
            }
        }
        return argSize <= size;
    }

    class ClassInfo {
        public String declare;
        public boolean star;
        public boolean startWith;
        public boolean startWithPackage;
    }

    class MethodInfo {
        public String methodName;
        public boolean star;
        public boolean startWith;
        public boolean endWith;
    }
}
