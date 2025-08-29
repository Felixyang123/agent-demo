package org.example;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

public class AttachTool {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar attach-tool.jar <agent-jar-path> <target-process-id|main-class-name>");
            System.out.println("Available JVM processes:");
            listRunningJVMs();
            return;
        }

        String agentJarPath = args[0];
        String target = args[1];

        try {
            VirtualMachine vm = null;
            
            // 尝试通过PID连接
            try {
                int pid = Integer.parseInt(target);
                System.out.println("Trying to attach to PID: " + pid);
                vm = VirtualMachine.attach(String.valueOf(pid));
            } catch (NumberFormatException e) {
                // 如果不是数字，尝试通过主类名查找
                System.out.println("Searching for process with main class: " + target);
                vm = attachByMainClass(target);
            }
            
            if (vm == null) {
                System.err.println("Target JVM not found: " + target);
                listRunningJVMs();
                return;
            }
            
            System.out.println("Successfully attached to JVM: " + vm.id());
            
            // 加载agent
            vm.loadAgent(agentJarPath, "optional-agent-args");
            System.out.println("Agent loaded successfully!");
            
            // 断开连接
            vm.detach();
            System.out.println("Detached from JVM.");
            
        } catch (Exception e) {
            System.err.println("Error during attach process: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static VirtualMachine attachByMainClass(String mainClassName) throws Exception {
        List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vmds) {
            // 显示名称通常包含主类名
            if (vmd.displayName().contains(mainClassName)) {
                System.out.println("Found matching JVM: " + vmd.id() + " - " + vmd.displayName());
                return VirtualMachine.attach(vmd);
            }
        }
        return null;
    }
    
    private static void listRunningJVMs() {
        System.out.println("\nList of running JVM processes:");
        List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vmds) {
            System.out.println("PID: " + vmd.id() + " | Main-Class: " + vmd.displayName());
        }
    }
}