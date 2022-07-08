package com.zwk.parse.correspondent;

import com.zwk.parse.ArgParser;
import com.zwk.parse.HandlerInfo;
import com.zwk.parse.Tokenizer;

import java.util.Objects;

public class TargetCorrespondent implements Correspondent {
    private final ClassInfo classInfo = new ClassInfo();

    private HandlerInfo handlerInfo;

    private final Correspondent correspondent;

    public TargetCorrespondent(Tokenizer tokenizer) {
        String token = tokenizer.nextToken();
        populateInfo(classInfo, token);
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
        correspondent = this;
    }

    @Override
    public boolean className(String className) {
        className = className.replace('/', '.');
        return validate(classInfo, className);
    }

    @Override
    public HandlerInfo handlerInfo() {
        return handlerInfo;
    }

    @Override
    public Correspondent getInstance() {
        return correspondent;
    }
}
