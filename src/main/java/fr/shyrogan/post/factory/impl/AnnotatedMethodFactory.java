package fr.shyrogan.post.factory.impl;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.listener.Listener;
import fr.shyrogan.post.listener.annotation.Subscribe;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static fr.shyrogan.post.utils.ListenerCompiler.byteCode;
import static fr.shyrogan.post.utils.ListenerCompiler.getUniqueMethodName;
import static java.util.stream.Collectors.toList;

/**
 * This {@link ReceiverFactory} looks for each method annotated with the {@link Subscribe} annotation and then compiles
 * a receiver on the fly to invoke it.
 */
@SuppressWarnings("ALL")
public enum AnnotatedMethodFactory implements ReceiverFactory {
    /**
     * Singleton
     **/
    INSTANCE;

    /**
     * Looks for the receiver methods.
     *
     * @param object        The object.
     * @param configuration The event bus configuration.
     *
     * @return The receivers found.
     */
    @Override
    public List<Listener> lookInto(Object object, EventBusConfiguration configuration) {
        List<Listener> list = new ArrayList<>();

        Class<?> currentClass = object.getClass();
        do {
            list.addAll(
                    Arrays.stream(currentClass.getDeclaredMethods())
                            .map(m -> toReceiver(m, object, configuration))
                            .filter(Objects::nonNull).collect(toList())
            );

            currentClass = currentClass.getSuperclass();
        } while (currentClass != Object.class);

        return list;
    }

    /**
     * Returns the method mapped to a receiver (if it was possible) or null.
     *
     * @param method The method.
     *
     * @return The method mapped to a receiver (if it was possible) or null.
     */
    private static final Listener toReceiver(Method method, Object instance, EventBusConfiguration configuration) {
        Subscribe annotation = method.getAnnotation(Subscribe.class);
        if (annotation == null || method.getParameterTypes().length != 1) return null;
        Class<?> topicType          = method.getParameterTypes()[0];
        String   generatedClassName = getUniqueMethodName(method);

        try {
            return (Listener) configuration.classLoader().lookForClass(generatedClassName)
                                           .orElse(configuration.classLoader().createClass(
                                                   generatedClassName,
                                                   byteCode(generatedClassName, instance.getClass(), topicType, method)
                                           )).getDeclaredConstructor(Object.class, Class.class, int.class)
                                           .newInstance(instance, topicType, annotation.priority());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
