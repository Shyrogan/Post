package fr.shyrogan.post.factory.impl;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.receiver.annotation.Subscribe;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.receiver.Receiver;
import fr.shyrogan.post.receiver.ReceiverBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 * This {@link ReceiverFactory} looks for each field type {@link Receiver} or
 * annotated {@link Consumer}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public enum AnnotatedFieldFactory implements ReceiverFactory {
    /** Singleton **/
    INSTANCE;

    /**
     * Looks for the receiver fields.
     *
     * @param object The object.
     * @return The receivers found.
     */
    @Override
    public List<Receiver> lookInto(Object object, EventBusConfiguration configuration) {
        return Arrays.stream(object.getClass().getDeclaredFields())
                .map(f -> toReceiver(f, object))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    /**
     * Returns the field mapped to a receiver (if it was possible) or null.
     *
     * @param field The field.
     * @return The field mapped to a receiver (if it was possible) or null.
     */
    private static Receiver toReceiver(Field field, Object instance) {
        Subscribe annotation = field.getAnnotation(Subscribe.class);
        if(annotation == null) return null;
        if(Receiver.class.isAssignableFrom(field.getType())) {
            if(!field.isAccessible()) field.setAccessible(true);
            try {
                return (Receiver)field.get(instance);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else if(Consumer.class.isAssignableFrom(field.getType())) {
            if(!field.isAccessible()) field.setAccessible(true);
            try {
                // Savage solution but, would work, I guess.
                return new ReceiverBuilder((Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0])
                        .withPriority(annotation.priority())
                        .perform((Consumer)field.get(instance))
                        .build();
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
