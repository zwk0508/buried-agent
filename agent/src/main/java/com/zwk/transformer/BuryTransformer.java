package com.zwk.transformer;

import com.zwk.parse.ArgParser;
import com.zwk.parse.HandlerInfo;
import com.zwk.parse.correspondent.*;
import com.zwk.visitor.BuryClassVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;

public class BuryTransformer implements ClassFileTransformer {
    private final Correspondent correspondent;

    public BuryTransformer(String args) {
        correspondent = new ArgParser(args).parse();
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className != null && correspondent != NotSupport.NOT_SUPPORT) {
            CompositeCorrespondent correspondent = (CompositeCorrespondent) this.correspondent;
            List<Correspondent> correspondents = correspondent.getCorrespondents();
            correspondents = new ArrayList<>(correspondents);
            Iterator<Correspondent> iterator = correspondents.iterator();
            boolean targetAnnotationExists = false;
            boolean targetPass = false;
            boolean pass = false;
            HandlerInfo handlerInfo = null;
            while (iterator.hasNext()) {
                Correspondent next = iterator.next();
                if (!targetAnnotationExists && (next instanceof TargetAnnotationCorrespondent)) {
                    targetAnnotationExists = true;
                    pass = true;
                }
                if (next instanceof TargetCorrespondent) {
                    if (!targetPass && next.className(className)) {
                        handlerInfo = next.handlerInfo();
                        targetPass = true;
                        pass = true;
                    }
                    iterator.remove();
                } else if (next instanceof MethodCorrespondent) {
                    if (next.className(className)) {
                        pass = true;
                    } else {
                        iterator.remove();
                    }
                }
            }
            if (pass) {
                return handle(classfileBuffer, correspondents, handlerInfo, targetPass, targetAnnotationExists);
            }
        }
        return classfileBuffer;
    }

    private byte[] handle(byte[] classfileBuffer, List<Correspondent> correspondents, HandlerInfo handlerInfo, boolean targetPass, boolean targetAnnotationExists) {
        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            BuryClassVisitor classVisitor = new BuryClassVisitor(ASM5, classWriter,
                    correspondent.handlerInfo(),
                    handlerInfo,
                    correspondents,
                    targetPass,
                    targetAnnotationExists);
            cr.accept(classVisitor,
                    ClassReader.SKIP_DEBUG);
            return classWriter.toByteArray();
        } catch (Exception e) {
            return classfileBuffer;
        }
    }
}
