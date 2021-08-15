package fr.shyrogan.post;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.dispatcher.MessageDispatcher;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.listener.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;

/**
 * Post's {@link EventBus} and "entry-point" to the library, provides a powerful yet as configurable as possible place
 * to dispatch message.
 */
@SuppressWarnings("ALL")
public class EventBus {

    /**
     * The configuration used by the Event Bus.
     */
    private final EventBusConfiguration configuration;

    /**
     * The map used to associate each receivers to their topic (message's class).
     */
    private final Map<Class<?>, ArrayList<Listener>> receiversMap;
    private final Map<Class<?>, MessageDispatcher>   dispatcherMap;

    /**
     * A cache used to accelerate subscription/unsubscription.
     */
    private final Map<Object, List<Listener>> factoryCache = new WeakHashMap<>();

    /**
     * Creates a new event bus with the default configuration.
     */
    public EventBus() {
        this(EventBusConfiguration.DEFAULT);
    }

    /**
     * Creates a new event bus with specified configuration.
     *
     * @param configuration The configuration.
     */
    public EventBus(EventBusConfiguration configuration) {
        this.configuration = configuration;
        this.receiversMap  = new ConcurrentHashMap<>(configuration.initialReceiverMapCapacity());
        this.dispatcherMap = new ConcurrentHashMap<>();
    }

    /**
     * Registers specified receivers to the event bus, allowing them to receive published messages.
     *
     * @param listenerList The receivers.
     *
     * @return The event bus.
     */
    public EventBus subscribe(List<Listener> listenerList) {
        listenerList.forEach(this::subscribe);
        return this;
    }

    /**
     * Registers specified receiver to the event bus, allowing it to receive published messages.
     *
     * @param listener The receiver.
     *
     * @return The event bus.
     */
    public EventBus subscribe(Listener listener) {
        configuration.executorService().submit(() -> {
            ArrayList<Listener> registeredListeners = receiversMap.getOrDefault(
                    listener.topic(), new ArrayList<>(configuration.initialReceiverListCapacity()));
            registeredListeners.add(listener);
            registeredListeners.sort(comparingInt(r -> -r.priority()));
            receiversMap.put(listener.topic(), registeredListeners);
            dispatcherMap.put(listener.topic(), configuration.dispatcherFor(receiversMap.get(listener.topic())));
        });

        return this;
    }

    /**
     * Inspects the object in quest of {@link Listener} using the {@link ReceiverFactory} and then registers them.
     *
     * @param object The object.
     *
     * @return The event bus.
     */
    public EventBus subscribe(Object object) {
        List<Listener> listeners = factoryCache.get(object);
        if (listeners == null) {
            factoryCache.put(object, listeners = configuration.receiverFactory().lookInto(object, configuration));
        }
        return subscribe(listeners);
    }

    /**
     * Unregisters specified receivers to the event bus, allowing these receivers to be "ignored". They can still be
     * registered again.
     *
     * @param listenerList The receivers.
     *
     * @return The event bus.
     */
    public EventBus unsubscribe(List<Listener> listenerList) {
        listenerList.forEach(this::unsubscribe);
        return this;
    }

    /**
     * Unregisters specified receiver to the event bus, allowing that receiver to be "ignored". It can still be
     * registered again.
     *
     * @param listener The receiver.
     *
     * @return The event bus.
     */
    public EventBus unsubscribe(Listener listener) {
        configuration.executorService().submit(() -> {
            ArrayList<Listener> registeredListeners = receiversMap.get(listener.topic());
            if (registeredListeners != null) {
                registeredListeners.remove(listener);

                if (registeredListeners.isEmpty()) {
                    receiversMap.remove(listener.topic());
                    dispatcherMap.remove(listener.topic());
                } else {
                    dispatcherMap.put(
                            listener.topic(), configuration.dispatcherFor(receiversMap.get(listener.topic())));
                }
            }
        });

        return this;
    }

    /**
     * Inspects the object in quest of {@link Listener} using the {@link ReceiverFactory} and then unregisters them.
     *
     * @param object The object.
     *
     * @return The event bus.
     */
    public EventBus unsubscribe(Object object) {
        List<Listener> listeners = factoryCache.get(object);
        if (listeners == null) return this;
        return unsubscribe(listeners);
    }

    /**
     * Dispatches specified message to each of its receivers, if they exist.
     *
     * @param message Message.
     */
    public void dispatch(Object message) {
        dispatcherMap.get(message.getClass()).dispatch(message);
    }

    /**
     * Clears the event bus (basically clears the cache and subscriptions).
     *
     * @return The event bus drained.
     */
    public EventBus clear() {
        receiversMap.clear();
        factoryCache.clear();
        return this;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        final String values = receiversMap.entrySet().stream().map(e -> e.getKey().getSimpleName() + "=" +
                                                                        e.getValue().stream().map(Listener::toString)
                                                                         .collect(joining(",")))
                                          .collect(joining(", ", "{", "}"));

        return "EventBus{" + "receivers=" + values + '}';
    }

}
