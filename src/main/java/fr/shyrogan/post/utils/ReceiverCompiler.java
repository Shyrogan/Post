package fr.shyrogan.post.utils;

import fr.shyrogan.post.receiver.Receiver;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * Returns whether
 */
public class ReceiverCompiler {

    /** The object type name **/
    private final static String OBJECT_TYPE = "java/lang/Object";
    /** The class type name **/
    private final static String CLASS_TYPE = "java/lang/Class";
    /** The receiver type name **/
    private final static String RECEIVER_TYPE = getTypeName(Receiver.class);

    /**
     * Generates a receiver implementation that calls specified method.
     * The class output looks like this (kinda):
     * <p>
     *     fields for parent, topic, priority
     *
     *     constructor(Parent, Topic, Priority): assigns the value to each field
     *
     *     getTopic(): return topic
     *     getPriority(): return priority
     *     call(T): calls the method
     *     call(Object): A simple method that check cast if the object is T and then calls call(T)
     * </p>
     *
     * @param parent The parent type.
     * @param topic The topic type.
     * @param method The method type.
     * @return A receiver implementation compiled on the fly.
     */
    public static byte[] forMethod(Class<?> parent, Class<?> topic, Method method) {
        // A bench of utilities required later
        String parentType = getTypeName(parent);
        String topicType = getTypeName(topic);

        String generatedClassName = "post_generated_" + parent.getName().replace('.', '_') + "_" + getUniqueMethodName(method);

        ClassNode NODE = new ClassNode();
        NODE.visit(V1_8, ACC_PUBLIC + ACC_SUPER,
                generatedClassName,
                'L' + OBJECT_TYPE + ";L" + RECEIVER_TYPE + "<L" + topicType + ";>;",
                OBJECT_TYPE,
                new String[] {RECEIVER_TYPE}
        );
        NODE.fields = new ArrayList<>();
        NODE.methods = new ArrayList<>();

        //<editor-fold desc="Fields">
        // The parent field
        FieldNode PARENT_FIELD = new FieldNode(
                ACC_PRIVATE + ACC_FINAL, "parent", 'L' + parentType + ';',
                null, null
        );

        // The topic field
        FieldNode TOPIC_FIELD = new FieldNode(
                ACC_PRIVATE + ACC_FINAL, "topic", 'L' + CLASS_TYPE + ";",
                'L' + CLASS_TYPE + "<L" + topicType + ";>;",
                null
        );

        // The priority field
        FieldNode PRIORITY_FIELD = new FieldNode(ACC_PRIVATE + ACC_FINAL, "priority", "I", null, null);

        // Put them all together
        NODE.fields.add(PARENT_FIELD);
        NODE.fields.add(TOPIC_FIELD);
        NODE.fields.add(PRIORITY_FIELD);
        //</editor-fold>

        //<editor-fold desc="Methods">
        // Builds a constructor to create our receiver using the (Parent, Topic, Priority) parameters.
        MethodNode INIT_METHOD = new MethodNode(ACC_PUBLIC, "<init>",
                "(L" + OBJECT_TYPE + ";L" + CLASS_TYPE + ";I)V",
                "(L" + OBJECT_TYPE + ";L" + CLASS_TYPE + "<*>;I)V",
                null
        );
        INIT_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        INIT_METHOD.instructions.add(new MethodInsnNode(INVOKESPECIAL, OBJECT_TYPE, "<init>", "()V", false));
        // Puts the parent field value
        INIT_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        INIT_METHOD.instructions.add(new VarInsnNode(ALOAD, 1));
        INIT_METHOD.instructions.add(new TypeInsnNode(CHECKCAST, parentType));
        INIT_METHOD.instructions.add(new FieldInsnNode(PUTFIELD, generatedClassName, "parent", 'L' + parentType + ';'));
        // Puts the topic field value
        INIT_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        INIT_METHOD.instructions.add(new VarInsnNode(ALOAD, 2));
        INIT_METHOD.instructions.add(new FieldInsnNode(PUTFIELD, generatedClassName, "topic", 'L' + CLASS_TYPE + ';'));
        // Puts the priority field value
        INIT_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        INIT_METHOD.instructions.add(new VarInsnNode(ILOAD, 3));
        INIT_METHOD.instructions.add(new FieldInsnNode(PUTFIELD, generatedClassName, "priority", "I"));
        // return
        INIT_METHOD.instructions.add(new InsnNode(RETURN));

        // Implements the getTopic() method using the topic field.
        MethodNode GET_TOPIC_METHOD = new MethodNode(ACC_PUBLIC, "getTopic",
                "()L" + CLASS_TYPE + ';', "()L" + CLASS_TYPE + "<L" + topicType + ";>;",
                null
        );
        GET_TOPIC_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        GET_TOPIC_METHOD.instructions.add(new FieldInsnNode(GETFIELD, generatedClassName, "topic", 'L' + CLASS_TYPE + ';'));
        GET_TOPIC_METHOD.instructions.add(new InsnNode(ARETURN));

        // Implements the getTopic() method using the topic field.
        MethodNode GET_PRIORITY_METHOD = new MethodNode(ACC_PUBLIC, "getPriority", "()I", null, null);
        GET_PRIORITY_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        GET_PRIORITY_METHOD.instructions.add(new FieldInsnNode(GETFIELD, generatedClassName, "priority", "I"));
        GET_PRIORITY_METHOD.instructions.add(new InsnNode(IRETURN));

        // Implements the call(T) method.
        MethodNode CALL_METHOD = new MethodNode(ACC_PUBLIC, "call", "(L" + topicType + ";)V", null, null);
        CALL_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        CALL_METHOD.instructions.add(new FieldInsnNode(GETFIELD, generatedClassName, "parent", "L" + parentType + ";"));
        CALL_METHOD.instructions.add(new VarInsnNode(ALOAD, 1));
        CALL_METHOD.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, parentType, method.getName(), getMethodDescriptor(method)));
        CALL_METHOD.instructions.add(new InsnNode(RETURN));

        // Casts the type and then call the call(T) method.
        MethodNode CASTED_CALL_METHOD = new MethodNode(ACC_PUBLIC, "call", "(L" + OBJECT_TYPE + ";)V", null, null);
        CASTED_CALL_METHOD.instructions.add(new VarInsnNode(ALOAD, 0));
        CASTED_CALL_METHOD.instructions.add(new VarInsnNode(ALOAD, 1));
        CASTED_CALL_METHOD.instructions.add(new TypeInsnNode(CHECKCAST, topicType));
        CASTED_CALL_METHOD.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, generatedClassName, "call", "(L" + topicType + ";)V", false));
        CASTED_CALL_METHOD.instructions.add(new InsnNode(RETURN));

        // Put them all together
        NODE.methods.add(INIT_METHOD);
        NODE.methods.add(GET_TOPIC_METHOD);
        NODE.methods.add(GET_PRIORITY_METHOD);
        NODE.methods.add(CALL_METHOD);
        NODE.methods.add(CASTED_CALL_METHOD);
        //</editor-fold>

        ClassWriter WRITER = new ClassWriter(COMPUTE_FRAMES);
        NODE.accept(WRITER);

        return WRITER.toByteArray();
    }

    /**
     * Returns the type name.
     *
     * @param clazz The class.
     * @return The type name.
     */
    private static String getTypeName(Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Gets a unique method name from a method instance.
     * From Hippo on LWJEB.
     * @see <a href="https://github.com/Hippo/LWJEB">LWJEB</a>
     *
     * @param method  The method.
     * @return  The unique name.
     */
    public static String getUniqueMethodName(Method method) {
        StringBuilder parameters = new StringBuilder();
        for (Parameter parameter : method.getParameters()) {
            parameters.append(parameter.getType().getName().replace('.', '_'));
        }
        return method.getName() + parameters.toString();
    }

}
