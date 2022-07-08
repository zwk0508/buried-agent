package com.zwk.visitor;


import com.zwk.enums.BoxType;
import com.zwk.parse.HandlerInfo;
import com.zwk.parse.correspondent.Correspondent;
import com.zwk.parse.correspondent.TargetAnnotationCorrespondent;
import jdk.internal.org.objectweb.asm.*;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BuryClassVisitor extends ClassVisitor implements Opcodes {

    private final AtomicInteger index = new AtomicInteger(1);
    private final List<MethodInfo> methodInfos = new ArrayList<>();
    private final HandlerInfo defaultHandlerInfo;
    private HandlerInfo currentHandlerInfo;
    private final List<Correspondent> correspondents;
    private final boolean targetAnnotationExists;
    private final boolean targetPass;
    private List<String> classAnnotations;
    private String className;

    private static final Map<String, Integer> primitiveLoadIns = new HashMap<>();
    private static final Map<String, Integer> primitiveStoreIns = new HashMap<>();
    private static final Map<String, Integer> primitiveReturnIns = new HashMap<>();
    private static final Map<String, BoxType> primitiveBoxType = new HashMap<>();
    private static final String METHOD_PREFIX = "___auto_generate_method_";

    public BuryClassVisitor(int api,
                            ClassVisitor classVisitor,
                            HandlerInfo defaultHandlerInfo,
                            HandlerInfo currentHandlerInfo,
                            List<Correspondent> correspondents,
                            boolean targetPass,
                            boolean targetAnnotationExists) {
        super(api, classVisitor);
        this.defaultHandlerInfo = defaultHandlerInfo;
        this.currentHandlerInfo = currentHandlerInfo;
        this.correspondents = correspondents;
        this.targetPass = targetPass;
        this.targetAnnotationExists = targetAnnotationExists;
        if (targetAnnotationExists) {
            classAnnotations = new ArrayList<>();
        }
    }


    static {
        primitiveLoadIns.put("B", ILOAD);
        primitiveLoadIns.put("S", ILOAD);
        primitiveLoadIns.put("I", ILOAD);
        primitiveLoadIns.put("J", LLOAD);
        primitiveLoadIns.put("F", FLOAD);
        primitiveLoadIns.put("D", DLOAD);
        primitiveLoadIns.put("C", ILOAD);
        primitiveLoadIns.put("Z", ILOAD);

        primitiveStoreIns.put("B", ISTORE);
        primitiveStoreIns.put("S", ISTORE);
        primitiveStoreIns.put("I", ISTORE);
        primitiveStoreIns.put("J", LSTORE);
        primitiveStoreIns.put("F", FSTORE);
        primitiveStoreIns.put("D", DSTORE);
        primitiveStoreIns.put("C", ISTORE);
        primitiveStoreIns.put("Z", ISTORE);

        primitiveReturnIns.put("B", IRETURN);
        primitiveReturnIns.put("S", IRETURN);
        primitiveReturnIns.put("I", IRETURN);
        primitiveReturnIns.put("J", LRETURN);
        primitiveReturnIns.put("F", FRETURN);
        primitiveReturnIns.put("D", DRETURN);
        primitiveReturnIns.put("C", IRETURN);
        primitiveReturnIns.put("Z", IRETURN);

        primitiveBoxType.put("B", BoxType.BYTE);
        primitiveBoxType.put("S", BoxType.SHORT);
        primitiveBoxType.put("I", BoxType.INT);
        primitiveBoxType.put("J", BoxType.LONG);
        primitiveBoxType.put("F", BoxType.FLOAT);
        primitiveBoxType.put("D", BoxType.DOUBLE);
        primitiveBoxType.put("C", BoxType.CHAR);
        primitiveBoxType.put("Z", BoxType.BOOLEAN);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        if (b && !targetPass && classAnnotations != null) {
            String className = Type.getType(s).getClassName();
            classAnnotations.add(className);
        }
        return cv.visitAnnotation(s, b);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String methodName = name;
        if (!skip(name) &&
                (targetPass
                        || (targetAnnotationExists && annotationCorrespond())
                        || (correspondents.size() > 0 && methodCorrespond(access, name, descriptor, exceptions))
                )
        ) {
            methodName = METHOD_PREFIX + name + "_" + index.getAndIncrement();
            methodInfos.add(new MethodInfo(access, name, descriptor, signature, exceptions, methodName));
            if ((access & Modifier.PUBLIC) != 0) {
                access = (~Modifier.PUBLIC & access) | Modifier.PRIVATE;
            }
        }
        return cv.visitMethod(access, methodName, descriptor, signature, exceptions);
    }

    private boolean skip(String name) {
        return "<init>".equals(name) || "<clinit>".equals(name);
    }

    private boolean annotationCorrespond() {
        Iterator<Correspondent> iterator = correspondents.iterator();
        while (iterator.hasNext()) {
            Correspondent correspondent = iterator.next();
            if (correspondent instanceof TargetAnnotationCorrespondent) {
                iterator.remove();
                if (correspondent.classAnnotation(classAnnotations)) {
                    currentHandlerInfo = correspondent.handlerInfo();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean methodCorrespond(int access, String name, String descriptor, String[] exceptions) {
        List<Correspondent> correspondents = this.correspondents;
        for (Correspondent correspondent : correspondents) {
            boolean methodModifier = correspondent.methodModifier(access);
            Type type = Type.getType(descriptor);
            Type returnType = type.getReturnType();
            String className = returnType.getClassName();
            boolean methodReturnType = correspondent.methodReturnType(className);
            boolean methodName = correspondent.methodName(name);
            Type[] argumentTypes = type.getArgumentTypes();
            List<String> argClassNames = Stream.of(argumentTypes).map(Type::getClassName).collect(Collectors.toList());
            boolean methodArgs = correspondent.methodArgs(argClassNames);
            List<String> exs = Collections.emptyList();
            if (exceptions != null && exceptions.length > 0) {
                exs = Stream.of(exceptions).map(e -> Type.getObjectType(e).getClassName()).collect(Collectors.toList());
            }
            boolean methodThrows = correspondent.methodThrows(exs);
            if (methodModifier && methodReturnType && methodName && methodArgs && methodThrows) {
                currentHandlerInfo = correspondent.handlerInfo();
                return true;
            }
        }
        return false;
    }

    @Override
    public void visitEnd() {
        for (MethodInfo methodInfo : methodInfos) {
            MethodVisitor mv = cv.visitMethod(methodInfo.access, methodInfo.name, methodInfo.descriptor, methodInfo.signature, methodInfo.exceptions);
            AnnotationVisitor annotationVisitor = mv.visitAnnotation("Lcom/zwk/annotation/GeneratedMethod;", true);
            annotationVisitor.visit("value", methodInfo.methodName);
            annotationVisitor.visit("descriptor", methodInfo.descriptor);
            annotationVisitor.visitEnd();
            Type type = Type.getType(methodInfo.descriptor);
            Type[] argumentTypes = type.getArgumentTypes();
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, null);
            Label l3 = new Label();
            mv.visitTryCatchBlock(l2, l3, l2, null);
            mv.visitLabel(l0);
            boolean isStatic = Modifier.isStatic(methodInfo.access);
            int step = 0;
            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                step = 1;
            }
            int len = argumentTypes.length;
            for (Type argumentType : argumentTypes) {
                String descriptor = argumentType.getDescriptor();
                Integer integer = primitiveLoadIns.get(descriptor);
                if (integer == null) {
                    integer = ALOAD;
                }
                mv.visitVarInsn(integer, step);
                if (integer == DLOAD || integer == LLOAD) {
                    step += 2;
                } else {
                    step++;
                }
            }

            mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKESPECIAL, className, methodInfo.methodName, methodInfo.descriptor, false);
            Type returnType = type.getReturnType();
            String returnTypeDescriptor = returnType.getDescriptor();
            int retIndex = -1;
            boolean voidReturnType = Objects.equals("V", returnTypeDescriptor);
            if (!voidReturnType) {
                retIndex = step;
                Integer integer = primitiveStoreIns.get(returnTypeDescriptor);
                if (integer == null) {
                    integer = ASTORE;
                }
                mv.visitVarInsn(integer, retIndex);
            }
            int throwIndex;
            int paramsIndex;
            if (Objects.equals(returnTypeDescriptor, "D") || Objects.equals(returnTypeDescriptor, "J")) {
                paramsIndex = step + 2;
                throwIndex = step + 3;
            } else if (Objects.equals(returnTypeDescriptor, "V")) {
                paramsIndex = step;
                throwIndex = step + 1;
            } else {
                paramsIndex = step + 1;
                throwIndex = step + 2;
            }
            mv.visitLabel(l1);
            mv.visitIntInsn(BIPUSH, len);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            step = isStatic ? 0 : 1;
            for (int i = 0; i < len; i++) {
                mv.visitIntInsn(BIPUSH, i);
                Type argumentType = argumentTypes[i];
                String descriptor = argumentType.getDescriptor();
                Integer integer = primitiveLoadIns.get(descriptor);
                if (integer == null) {
                    integer = ALOAD;
                    mv.visitVarInsn(integer, step);
                } else {
                    mv.visitVarInsn(integer, step);
                    BoxType boxType = primitiveBoxType.get(descriptor);
                    String owner = boxType.getOwner();
                    String name = boxType.getName();
                    String desc = boxType.getDescriptor();
                    mv.visitMethodInsn(INVOKESTATIC, owner, name, desc, false);
                }
                mv.visitInsn(AASTORE);
                if (i < len - 1) {
                    mv.visitInsn(DUP);
                }
                if (integer == DLOAD || integer == LLOAD) {
                    step += 2;
                } else {
                    step++;
                }
            }
            HandlerInfo handlerInfo = this.currentHandlerInfo == null
                    ? this.defaultHandlerInfo
                    : this.currentHandlerInfo;
            mv.visitVarInsn(ASTORE, paramsIndex);
            if (isStatic) {
                mv.visitInsn(ACONST_NULL);
            } else {
                mv.visitVarInsn(ALOAD, 0);
            }
            if (voidReturnType) {
                mv.visitInsn(ACONST_NULL);
            } else {
                Integer integer = primitiveLoadIns.get(returnTypeDescriptor);
                if (integer == null) {
                    integer = ALOAD;
                }
                mv.visitVarInsn(integer, retIndex);
                if (integer != ALOAD) {
                    BoxType boxType = primitiveBoxType.get(returnTypeDescriptor);
                    String owner = boxType.getOwner();
                    String name = boxType.getName();
                    String desc = boxType.getDescriptor();
                    mv.visitMethodInsn(INVOKESTATIC, owner, name, desc, false);
                }
            }
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, paramsIndex);
            String handlerDescriptor = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)V";
            mv.visitMethodInsn(INVOKESTATIC, handlerInfo.getClassName(), handlerInfo.getMethod(), handlerDescriptor, false);
            if (voidReturnType) {
                mv.visitInsn(RETURN);
            } else {
                Integer integer = primitiveLoadIns.get(returnTypeDescriptor);
                if (integer == null) {
                    integer = ALOAD;
                }
                mv.visitVarInsn(integer, retIndex);
                integer = primitiveReturnIns.get(returnTypeDescriptor);
                if (integer == null) {
                    integer = ARETURN;
                }
                mv.visitInsn(integer);
            }
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
            mv.visitVarInsn(ASTORE, throwIndex);
            mv.visitLabel(l3);
            mv.visitIntInsn(BIPUSH, len);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            step = isStatic ? 0 : 1;
            for (int i = 0; i < len; i++) {
                mv.visitIntInsn(BIPUSH, i);
                Type argumentType = argumentTypes[i];
                String descriptor = argumentType.getDescriptor();
                Integer integer = primitiveLoadIns.get(descriptor);
                if (integer == null) {
                    integer = ALOAD;
                    mv.visitVarInsn(integer, step);
                } else {
                    mv.visitVarInsn(integer, step);
                    BoxType boxType = primitiveBoxType.get(descriptor);
                    String owner = boxType.getOwner();
                    String name = boxType.getName();
                    String desc = boxType.getDescriptor();
                    mv.visitMethodInsn(INVOKESTATIC, owner, name, desc, false);
                }
                mv.visitInsn(AASTORE);
                if (i < len - 1) {
                    mv.visitInsn(DUP);
                }
                if (integer == DLOAD || integer == LLOAD) {
                    step += 2;
                } else {
                    step++;
                }
            }

            mv.visitVarInsn(ASTORE, throwIndex + 1);
            if (isStatic) {
                mv.visitInsn(ACONST_NULL);
            } else {
                mv.visitVarInsn(ALOAD, 0);
            }
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ALOAD, throwIndex);
            mv.visitVarInsn(ALOAD, throwIndex + 1);
            mv.visitMethodInsn(INVOKESTATIC, handlerInfo.getClassName(), handlerInfo.getMethod(), handlerDescriptor, false);
            mv.visitVarInsn(ALOAD, throwIndex);
            mv.visitInsn(ATHROW);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cv.visitEnd();
    }

    private static class MethodInfo {
        int access;
        String name;
        String descriptor;
        String signature;
        String[] exceptions;
        String methodName;

        public MethodInfo(int access, String name, String descriptor, String signature, String[] exceptions, String methodName) {
            this.access = access;
            this.name = name;
            this.descriptor = descriptor;
            this.signature = signature;
            this.exceptions = exceptions;
            this.methodName = methodName;
        }
    }
}