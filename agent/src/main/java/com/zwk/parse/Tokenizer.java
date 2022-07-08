package com.zwk.parse;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private int index;
    private int len;
    private String[] tokens;

    public Tokenizer(char delimiter, String origin) {
        origin = origin.trim();
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < origin.length(); i++) {
            char c = origin.charAt(i);
            if (c == delimiter) {
                if (sb.length() > 0) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }
        int size = tokens.size();
        this.tokens = tokens.toArray(new String[size]);
        this.len = size;
    }

    public boolean hasNext() {
        return index < len;
    }

    public String nextToken() {
        return tokens[index++];
    }
}
