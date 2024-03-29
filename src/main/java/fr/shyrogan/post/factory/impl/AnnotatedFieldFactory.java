package fr.shyrogan.post.factory.impl;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.listener.Listener;
import fr.shyrogan.post.listener.annotation.Subscribe;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.listener.ListenerBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 * This {@link ReceiverFactory} looks for each field type {@link Listener} or annotated {@link Consumer}.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public enum AnnotatedFieldFactory implements ReceiverFactory {
    /** Singleton **/
    INSTANCE;

    /**
     * Looks for the receiver fields.
     *
     * @param object The object.
     *
     * @return The receivers found.
     */
    @Override
    public List<Listener> lookInto(Object object, EventBusConfiguration configuration) {
        return Arrays.stream(object.getClass().getDeclaredFields()).map(f -> toReceiver(f, object))
                     .filter(Objects::nonNull).collect(toList());
    }

    /**
     * Returns the field mapped to a receiver (if it was possible) or null.
     *
     * @param field The field.
     *
     * @return The field mapped to a receiver (if it was possible) or null.
     */
    private static Listener toReceiver(Field field, Object instance) {
        Subscribe annotation = field.getAnnotation(Subscribe.class);
        if (annotation == null) return null;
        if (Listener.class.isAssignableFrom(field.getType())) {
            if (!field.canAccess(instance)) field.setAccessible(true);
            try {
                return (Listener) field.get(instance);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else if (Consumer.class.isAssignableFrom(field.getType())) {
            if (!field.canAccess(instance)) field.setAccessible(true);
            try {
                // Savage solution but, would work, I guess.
                return new ListenerBuilder(
                        (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]).priority(
                        annotation.priority()).perform((Consumer) field.get(instance)).build();
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
