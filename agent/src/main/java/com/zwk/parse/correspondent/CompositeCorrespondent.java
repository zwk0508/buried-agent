package com.zwk.parse.correspondent;

import com.zwk.parse.HandlerInfo;

import java.util.List;

public class CompositeCorrespondent implements Correspondent {
    private HandlerInfo handlerInfo;
    private List<Correspondent> correspondents;


    public void setCorrespondents(List<Correspondent> correspondents) {
        this.correspondents = correspondents;
    }

    public List<Correspondent> getCorrespondents() {
        return correspondents;
    }

    public void setHandlerInfo(HandlerInfo handlerInfo) {
        this.handlerInfo = handlerInfo;
    }

    @Override
    public HandlerInfo handlerInfo() {
        return handlerInfo == null ? Correspondent.super.handlerInfo() : handlerInfo;
    }
}
