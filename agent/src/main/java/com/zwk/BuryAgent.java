package com.zwk;

import com.zwk.transformer.BuryTransformer;

import java.lang.instrument.Instrumentation;

public class BuryAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new BuryTransformer(agentArgs));
    }
}
