package com.zwk.parse.correspondent;

import com.zwk.parse.ArgParser;
import com.zwk.parse.HandlerInfo;
import com.zwk.parse.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TargetAnnotationCorrespondent implements Correspondent {
    private List<ClassInfo> annotations;
    private boolean anyAnnotations;
    private HandlerInfo handlerInfo;

    private final Correspondent correspondent;

    public TargetAnnotationCorrespondent(Tokenizer tokenizer) {
        String token = tokenizer.nextToken();
        if (Objects.equals(token, "**")) {
            anyAnnotations = true;
        } else {
            String[] annotations = token.split(",");
            for (String annotation : annotations) {
                ClassInfo classInfo = new ClassInfo();
                populateInfo(classInfo, annotation);
                if (this.annotations == null) {
                    this.annotations = new ArrayList<>();
                }
                this.annotations.add(classInfo);
                if (Objects.equals(annotation, "**")) {
                    break;
                }
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
        correspondent = this;
    }

    @Override
    public boolean classAnnotation(List<String> annotations) {
        if (anyAnnotations) {
            return true;
        }
        return validate(this.annotations, annotations);
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
