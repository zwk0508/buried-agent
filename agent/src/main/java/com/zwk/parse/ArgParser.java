package com.zwk.parse;

import com.zwk.enums.Instruction;
import com.zwk.parse.correspondent.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArgParser {
    private final String args;

    public ArgParser(String args) {
        this.args = args;
    }

    public Correspondent parse() {
        File file = new File(args);
        if (!file.exists()) {
            System.err.println("config file not exists : [ " + args + " ]");
            return NotSupport.NOT_SUPPORT;
        } else {
            List<ArgInfo> args = getArgs(file);
            if (args.size() == 0) {
                System.err.println("config file content empty!");
                return NotSupport.NOT_SUPPORT;
            }
            HandlerInfo handlerInfo = null;
            List<ErrorInfo> errorInfos = new ArrayList<>();
            List<Correspondent> correspondents = new ArrayList<>();
            for (ArgInfo argInfo : args) {
                int line = argInfo.line;
                String arg = argInfo.arg;
                Tokenizer tokenizer = new Tokenizer(' ', arg);
                Instruction instruction;
                String token = tokenizer.nextToken();
                instruction = Instruction.getInstruction(token);
                if (instruction == null) {
                    errorInfos.add(new ErrorInfo("unknown instruction", line));
                    continue;
                }

                if (!tokenizer.hasNext()) {
                    errorInfos.add(new ErrorInfo("instruction argument not found", line));
                    continue;
                }
                if (instruction == Instruction.HANDLER) {
                    handlerInfo = getHandler(tokenizer.nextToken());
                    if (handlerInfo == null) {
                        errorInfos.add(new ErrorInfo("handler instruction argument error," +
                                "and correct like : fullQualifiedClassName.methodName", line));
                    }
                } else {
                    Correspondent correspondent = null;
                    switch (instruction) {
                        case METHOD:
                            correspondent = new MethodCorrespondent(tokenizer).getInstance();
                            if (correspondent == NotSupport.NOT_SUPPORT) {
                                errorInfos.add(new ErrorInfo("method instruction argument error," +
                                        "and correct like : modifier? ret-type declare-type method-name params throws? " +
                                        "(handler fullQualifiedClassName.methodName)?",
                                        line));
                            }
                            break;
                        case TARGET:
                            correspondent = new TargetCorrespondent(tokenizer).getInstance();
                            if (correspondent == NotSupport.NOT_SUPPORT) {
                                errorInfos.add(new ErrorInfo("target instruction argument error," +
                                        "and correct like : fullQualifiedClassName " +
                                        "(handler fullQualifiedClassName.methodName)?",
                                        line));
                            }
                            break;
                        case MONKEY_AT_TARGET:
                            correspondent = new TargetAnnotationCorrespondent(tokenizer).getInstance();
                            if (correspondent == NotSupport.NOT_SUPPORT) {
                                errorInfos.add(new ErrorInfo("method instruction argument error," +
                                        "and correct like : annotation fullQualifiedClassName " +
                                        "(handler fullQualifiedClassName.methodName)?",
                                        line));
                            }

                            break;
                    }
                    correspondents.add(correspondent);
                }
            }

            if (errorInfos.size() > 0) {
                for (ErrorInfo errorInfo : errorInfos) {
                    System.err.println(errorInfo.format());
                }
                return NotSupport.NOT_SUPPORT;
            }

            CompositeCorrespondent correspondent = new CompositeCorrespondent();
            correspondent.setHandlerInfo(handlerInfo);
            correspondent.setCorrespondents(correspondents);
            return correspondent;
        }
    }


    public static HandlerInfo getHandler(String handler) {
        int len = handler.length();
        String className;
        String method;

        int index = handler.lastIndexOf(".");
        if (index <= 0 || index == len - 1) {
            return null;
        }
        method = handler.substring(index + 1, len);
        className = handler.substring(0, index).replace('.', '/');
        return new HandlerInfo(className, method);
    }

    private List<ArgInfo> getArgs(File file) {
        List<ArgInfo> args = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String s;
            int n = 0;
            while ((s = reader.readLine()) != null) {
                n++;
                s = s.trim();
                if (s.length() > 0) {
                    if (s.charAt(0) == '#') {
                        continue;
                    }
                    args.add(new ArgInfo(s, n));
                }
            }
        } catch (IOException e) {
            //do nothing
        }
        return args;
    }

    private static class ArgInfo {
        String arg;
        int line;

        public ArgInfo(String arg, int line) {
            this.arg = arg;
            this.line = line;
        }
    }

}
