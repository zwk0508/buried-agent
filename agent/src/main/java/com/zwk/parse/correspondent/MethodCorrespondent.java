package com.zwk.parse.correspondent;

import com.zwk.enums.MethodModifier;
import com.zwk.parse.ArgParser;
import com.zwk.parse.HandlerInfo;
import com.zwk.parse.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodCorrespondent implements Correspondent {

    private int access = -1;
    private ClassInfo ret = new ClassInfo();
    private MethodInfo methodInfo = new MethodInfo();
    private ClassInfo declare = new ClassInfo();

    private List<ClassInfo> params;
    private List<ClassInfo> exceptions;
    private boolean anyParams;
    private boolean anyException;

    private HandlerInfo handlerInfo;

    private Correspondent correspondent;

    public MethodCorrespondent(Tokenizer tokenizer) {
        String token = null;
        if (!tokenizer.hasNext()) {
            correspondent = NotSupport.NOT_SUPPORT;
            return;
        }
        token = tokenizer.nextToken();
        MethodModifier modifier = MethodModifier.getModifier(token);
        if (modifier != null) {
            access = 0;
            access |= modifier.getAccess();
            if (!tokenizer.hasNext()) {
                correspondent = NotSupport.NOT_SUPPORT;
                return;
            }
            token = tokenizer.nextToken();
            MethodModifier modifier1 = MethodModifier.getModifier(token);
            if (modifier1 != null) {
                if (modifier == MethodModifier.STATIC
                        || modifier == MethodModifier.FINAL) {
                    correspondent = NotSupport.NOT_SUPPORT;
                    return;
                }
                if (modifier1 == MethodModifier.PUBLIC
                        || modifier1 == MethodModifier.PRIVATE
                        || modifier1 == MethodModifier.PROTECTED) {
                    correspondent = NotSupport.NOT_SUPPORT;
                    return;
                }
                access |= modifier1.getAccess();
            }
            modifier = modifier1;
        }
        if (modifier != null) {
            if (!tokenizer.hasNext()) {
                correspondent = NotSupport.NOT_SUPPORT;
                return;
            }
            token = tokenizer.nextToken();
        }
        populateInfo(ret, token);

        if (!tokenizer.hasNext()) {
            correspondent = NotSupport.NOT_SUPPORT;
            return;
        }
        token = tokenizer.nextToken();

        populateInfo(declare, token);
        if (!tokenizer.hasNext()) {
            correspondent = NotSupport.NOT_SUPPORT;
            return;
        }

        token = tokenizer.nextToken();
        if (Objects.equals(token, "*")) {
            methodInfo.star = true;
        } else if (token.startsWith("*")) {
            methodInfo.endWith = true;
            methodInfo.methodName = token.substring(1);
        } else if (token.endsWith("*")) {
            methodInfo.startWith = true;
            methodInfo.methodName = token.substring(0, token.length() - 1);
        } else {
            methodInfo.methodName = token;
        }


        if (!tokenizer.hasNext()) {
            correspondent = NotSupport.NOT_SUPPORT;
            return;
        }
        token = tokenizer.nextToken();
        if (Objects.equals(token, "**")) {
            anyParams = true;
        } else {
            String[] params = token.split(",");
            for (String param : params) {
                ClassInfo classInfo = new ClassInfo();
                populateInfo(classInfo, param);
                if (this.params == null) {
                    this.params = new ArrayList<>();
                }
                this.params.add(classInfo);
                if (Objects.equals(param, "**")) {
                    break;
                }
            }
        }

        if (tokenizer.hasNext()) {
            token = tokenizer.nextToken();
            if (Objects.equals("handler", token)) {
                boolean hasNext = tokenizer.hasNext();
                if (hasNext) {
                    handlerInfo = ArgParser.getHandler(tokenizer.nextToken());
                }
                if (!hasNext || handlerInfo == null) {
                    correspondent = NotSupport.NOT_SUPPORT;
                    return;
                }
            } else {
                String[] exceptions = token.split(",");
                for (String exception : exceptions) {
                    ClassInfo classInfo = new ClassInfo();
                    populateInfo(classInfo, exception);
                    if (this.exceptions == null) {
                        this.exceptions = new ArrayList<>();
                    }
                    this.exceptions.add(classInfo);
                    if (Objects.equals(exception, "**")) {
                        break;
                    }
                }
                if (tokenizer.hasNext()) {
                    token = tokenizer.nextToken();
                    if (Objects.equals("handler", token)) {
                        boolean hasNext = tokenizer.hasNext();
                        if (hasNext) {
                            token = tokenizer.nextToken();
                            handlerInfo = ArgParser.getHandler(token);
                        }
                        if (!hasNext || handlerInfo == null) {
                            correspondent = NotSupport.NOT_SUPPORT;
                            return;
                        }
                    } else {
                        correspondent = NotSupport.NOT_SUPPORT;
                        return;
                    }
                }
                if (tokenizer.hasNext()) {
                    correspondent = NotSupport.NOT_SUPPORT;
                    return;
                }
            }
        } else {
            anyException = true;
        }

        correspondent = this;
    }


    @Override
    public Correspondent getInstance() {
        return correspondent;
    }


    public boolean methodModifier(int modifier) {
        if (access == -1) {
            return true;
        }
        return modifier == access;
    }

    public boolean methodReturnType(String retType) {
        return validate(ret, retType);
    }


    public boolean className(String className) {
        className = className.replace('/', '.');
        return validate(declare, className);
    }

    public boolean methodName(String methodName) {
        MethodInfo methodInfo = this.methodInfo;
        if (methodInfo.star) {
            return true;
        }
        if (methodInfo.startWith) {
            return methodName.startsWith(methodInfo.methodName);
        }
        if (methodInfo.endWith) {
            return methodName.endsWith(methodInfo.methodName);
        }
        return Objects.equals(methodName, methodInfo.methodName);
    }

    public boolean methodArgs(List<String> args) {
        if (anyParams) {
            return true;
        }
        return validate(params, args);
    }

    public boolean methodThrows(List<String> exceptions) {
        if (anyException) {
            return true;
        }
        return validate(this.exceptions, exceptions);
    }

    @Override
    public HandlerInfo handlerInfo() {
        return handlerInfo;
    }
}
