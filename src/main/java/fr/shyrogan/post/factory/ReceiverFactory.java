package fr.shyrogan.post.factory;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.listener.Listener;

import java.util.List;

/**
 * In most project, we might want to create only a few classes that holds each of our receivers.
 * To achieve this, Post provides the {@link ReceiverFactory}, a class that looks each receivers contained inside of
 * an object.
 */
@SuppressWarnings("ALL")
public interface ReceiverFactory {

    /**
     * Returns a list of each receiver contained inside of specified object.
     *
     * @param object The object.
     * @param configuration The event bus configuration.
     * @return A list of each receiver.
     */
    List<Listener> lookInto(Object object, EventBusConfiguration configuration);

}
