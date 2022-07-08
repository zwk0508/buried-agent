package com.zwk.handler;

public class ParameterHandler {
    public static void handle(Object t, Object r, Object e, Object... p) {
        if (t != null) {
            System.out.println("target class: " + t.getClass());
        } else {
            System.out.println("static method");
        }
        if (r != null) {
            System.out.println("return: " + r);
        } else {
            System.out.println("ret was null or void method");
        }
        if (e != null) {
            System.out.println("method exception: " + e);
        }
        if (p != null && p.length > 0) {
            for (Object param : p) {
                System.out.println("parameter: " + param);
            }
        } else {
            System.out.println("non parameters");
        }
    }
}
