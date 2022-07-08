package com.zwk.parse;

class ErrorInfo {
    String error;
    int line;

    public ErrorInfo(String error, int line) {
        this.error = error;
        this.line = line;
    }

    public String format() {
        return String.format("line: %d, error: %s", line, error);
    }
}