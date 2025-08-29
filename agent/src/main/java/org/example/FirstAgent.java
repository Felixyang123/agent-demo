package org.example;

import java.lang.instrument.Instrumentation;

public class FirstAgent {
    // 方式一：简单版本
    public static void premain(String agentArgs, Instrumentation inst) {
        // JVM传入的Instrumentation实例，是操作字节码的核心API
        System.out.println("Agent is loaded! Arguments: " + agentArgs);
        // 在这里注册你的类转换器(ClassFileTransformer)
        inst.addTransformer(new TraceMethodTimeTransformer());
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("[Agent] Dynamically attached to JVM!");
        System.out.println("[Agent] Args: " + agentArgs);

        // 添加转换器
        TraceMethodTimeTransformer transformer = new TraceMethodTimeTransformer();
        inst.addTransformer(transformer, true); // true表示可以retransform

        try {
            // 重新转换所有已加载的类，让转换器生效
            Class<?>[] loadedClasses = inst.getAllLoadedClasses();
            for (Class<?> clazz : loadedClasses) {
                if (clazz.getName().equals("org.example.Test")) {
                    System.out.println("[Agent] Retransforming: " + clazz.getName());
                    inst.retransformClasses(clazz);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Agent] Error during retransform: " + e.getMessage());
            e.printStackTrace();
        }
    }
}