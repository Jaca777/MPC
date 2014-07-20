package tracer;

import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author JacaDev
 */
public class CallsInjector {

    public static void inject(ClassNode clazz, String path) {
        List<MethodNode> methods = clazz.methods;
        for (MethodNode method : methods) {
            injectCalls(method, path);
        }
    }

    private static void injectCalls(MethodNode method, String classPath) {
        String methodPath = classPath + ":" + method.name + method.desc;
        AbstractInsnNode[] instructions = method.instructions.toArray();
        InsnList newStack = new InsnList();
        newStack.add(method.instructions);
        for (AbstractInsnNode instruction : instructions) {
            if (isCallInsn(instruction.getOpcode())) {
                LocalVariableNode localVariable =
                        getLocalVariableNode((VarInsnNode) instruction, method);
                InsnList call = new InsnList();
                call.add(new InsnNode(DUP));
                if (!localVariable.desc.startsWith("L"))
                    call.add(new MethodInsnNode(INVOKESTATIC, "tracer/Boxer", "box", "(" + localVariable.desc + ")Ljava/lang/Object;", false));
                call.add(new LdcInsnNode(localVariable.desc + " " + localVariable.name));
                call.add(new LdcInsnNode(methodPath));
                call.add(new MethodInsnNode(INVOKESTATIC, "tracer/Tracer", "varChanged", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V", false));
                newStack.insertBefore(instruction, call);
            }
        }
        method.instructions = newStack;
    }
    private static boolean isCallInsn(int storeInsn) {
        return 53 < storeInsn && storeInsn < 59;
    }

    private static LocalVariableNode getLocalVariableNode(VarInsnNode varInsnNode, MethodNode methodNode) {
        int varIdx = varInsnNode.var;
        int insnIdx = getInsnIndex(varInsnNode);
        List<?> localVariables = methodNode.localVariables;
        for (int idx = 0; idx < localVariables.size(); idx++) {
            LocalVariableNode localVariableNode = (LocalVariableNode) localVariables.get(idx);
            if (localVariableNode.index == varIdx) {
                int scopeEndInstrIndex = getInsnIndex(localVariableNode.end);
                if (scopeEndInstrIndex >= insnIdx) {
                    return localVariableNode;
                }
            }
        }
        throw new RuntimeException("Var not found: " + varIdx + " in " + methodNode.name);
    }

    private static int getInsnIndex(AbstractInsnNode insnNode) {
        try {
            Field indexField = AbstractInsnNode.class.getDeclaredField("index");
            indexField.setAccessible(true);
            Object indexValue = indexField.get(insnNode);
            return ((Integer) indexValue);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
}
