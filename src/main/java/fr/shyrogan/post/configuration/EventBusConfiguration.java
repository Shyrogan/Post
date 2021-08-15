package fr.shyrogan.post.configuration;

import fr.shyrogan.post.configuration.impl.DefaultEventBusConfiguration;
import fr.shyrogan.post.dispatcher.MessageDispatcher;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.listener.Listener;
import fr.shyrogan.post.utils.DynamicClassLoader;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Abstract representation of an event bus configuration.
 */
@SuppressWarnings("ALL")
public interface EventBusConfiguration {

    static final EventBusConfiguration DEFAULT = new DefaultEventBusConfiguration();

    /**
     * Returns the initial capacity of the map used to associate each message class to its receivers.
     *
     * @return The initial capacity of the map used to associate each message class to its receivers.
     */
    default int initialReceiverMapCapacity() {
        return DEFAULT.initialReceiverMapCapacity();
    }

    /**
     * Returns the initial capacity of the list that contains all of our receivers for a specific topic.
     *
     * @return The initial capacity of the list that contains all of our receivers for a specific topic.
     */
    default int initialReceiverListCapacity() {
        return DEFAULT.initialReceiverListCapacity();
    }

    /**
     * Returns the receiver factory used to create receiver from an object instance. (From its fields/methods).
     *
     * @return The receiver factory.
     */
    default ReceiverFactory receiverFactory() {
        return DEFAULT.receiverFactory();
    }

    /**
     * Provides a function that returns a dispatcher based on the receiver list.
     *
     * @param listeners The list of receivers (can be null!)
     *
     * @return A dispatcher factory function.
     */
    default MessageDispatcher dispatcherFor(ArrayList<Listener> listeners) {
        return DEFAULT.dispatcherFor(listeners);
    }

    /**
     * Provides a dynamic class loader used to load compiled on the fly receivers.
     *
     * @return A dynamic class loader.
     */
    default DynamicClassLoader classLoader() {
        return DEFAULT.classLoader();
    }

    default ExecutorService executorService() {
        return ForkJoinPool.commonPool();
    }

}
