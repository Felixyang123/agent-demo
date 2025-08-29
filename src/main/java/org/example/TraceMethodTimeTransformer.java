package org.example;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class TraceMethodTimeTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        // 1. 过滤掉不需要处理的类（如JDK自身的类）
        if (className == null || className.startsWith("java/") || className.startsWith("sun/")) {
            return null; // 返回null表示不进行转换
        }

        // 将斜杠分隔的类名转换为点分隔
        className = className.replace('/', '.');

        // 2. 只处理我们关心的类
        if (!className.startsWith("org.example")) {
            return null;
        }

        System.out.println("[Agent] Transforming: " + className);
        try {
            // 3. 使用Javassist解析传入的字节码
            ClassPool cp = ClassPool.getDefault();
            cp.appendClassPath(new LoaderClassPath(loader));
            CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));

            // 4. 获取所有方法，进行增强
            for (CtMethod m : cc.getDeclaredMethods()) {
                // 在方法开始处插入代码
                m.addLocalVariable("start", CtClass.longType);
                m.insertBefore("start =System.currentTimeMillis();");
                // 在方法最后插入代码
                m.insertAfter("System.out.println(\"Method " + m.getName() + " executed in \" + (System.currentTimeMillis() - start) + \" ms\");");
            }

            // 5. 返回修改后的新字节码
            return cc.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 如果转换失败，返回null
    }
}