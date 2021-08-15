package fr.shyrogan.post.factory.impl;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.listener.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@link ReceiverFactory} combines both {@link AnnotatedFieldFactory} and {@link AnnotatedMethodFactory}.
 * @see AnnotatedFieldFactory
 * @see AnnotatedMethodFactory
 */
@SuppressWarnings("ALL")
public enum AnnotatedFieldAndMethodFactory implements ReceiverFactory {
    /** Singleton **/
    INSTANCE;

    /**
     * Looks for the receiver fields.
     *
     * @param object The object.
     * @return The receivers found.
     */
    @Override
    public List<Listener> lookInto(Object object, EventBusConfiguration configuration) {
        // Combines both of the method and field factory.
        final List<Listener> listeners = new ArrayList<>();
        listeners.addAll(AnnotatedMethodFactory.INSTANCE.lookInto(object, configuration));
        listeners.addAll(AnnotatedFieldFactory.INSTANCE.lookInto(object, configuration));
        return listeners;
    }

}
