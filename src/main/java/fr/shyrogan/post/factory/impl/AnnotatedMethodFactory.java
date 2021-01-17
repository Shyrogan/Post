package fr.shyrogan.post.factory.impl;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.receiver.Receiver;
import fr.shyrogan.post.receiver.annotation.Subscribe;
import org.eclipse.collections.api.list.ImmutableList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static fr.shyrogan.post.utils.ReceiverCompiler.forMethod;
import static fr.shyrogan.post.utils.ReceiverCompiler.getUniqueMethodName;
import static org.eclipse.collections.impl.collector.Collectors2.toImmutableList;

/**
 * This {@link ReceiverFactory} looks for each method annotated with the {@link Subscribe} annotation
 * and then compiles a receiver on the fly to invoke it.
 */
@SuppressWarnings("ALL")
public enum AnnotatedMethodFactory implements ReceiverFactory {
    /** Singleton **/
    INSTANCE;

    /**
     * Looks for the receiver methods.
     *
     * @param object The object.
     * @param configuration The event bus configuration.
     * @return The receivers found.
     */
    @Override
    public ImmutableList<Receiver> lookInto(Object object, EventBusConfiguration configuration) {
        return Arrays.stream(object.getClass().getDeclaredMethods())
                .map(m -> toReceiver(m, object, configuration))
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }

    /**
     * Returns the method mapped to a receiver (if it was possible) or null.
     *
     * @param method The method.
     * @return The method mapped to a receiver (if it was possible) or null.
     */
    private static final Receiver toReceiver(Method method, Object instance, EventBusConfiguration configuration) {
        Subscribe annotation = method.getAnnotation(Subscribe.class);
        if(annotation == null || method.getParameterTypes().length != 1) return null;
        Class<?> topicType = method.getParameterTypes()[0];
        String targetClass = "post_generated_" + instance.getClass().getName().replace('.', '_') + "_" + getUniqueMethodName(method);

        try {
            return (Receiver) configuration.classLoader()
                    .lookForClass(targetClass)
                    .orElse(configuration.classLoader().createClass(targetClass, forMethod(instance.getClass(), topicType, method)))
                    .getDeclaredConstructor(Object.class, Class.class, int.class)
                    .newInstance(instance, topicType, annotation.priority());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
