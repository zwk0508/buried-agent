package com.zwk.parse;

public class HandlerInfo {

    public static final HandlerInfo DEFAULT_HANDLER = new HandlerInfo();

    private String className = "com.zwk.handler.ParameterHandler";
    private String method = "handle";

    private HandlerInfo() {
    }

    public HandlerInfo(String className, String method) {
        this.className = className;
        this.method = method;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }


}
