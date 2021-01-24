package fr.shyrogan.post;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.receiver.Receiver;

import java.util.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;

/**
 * Post's {@link EventBus} and "entry-point" to the library, provides a powerful yet as configurable
 * as possible place to dispatch message.
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
    private final Map<Class<?>, ArrayList<Receiver>> receiversMap;

    /**
     * A cache used to accelerate subscription/unsubscription.
     */
    private final Map<Object, List<Receiver>> factoryCache = new WeakHashMap<>();

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
        this.receiversMap = new HashMap<>(configuration.initialReceiverMapCapacity());
    }

    /**
     * Registers specified receivers to the event bus, allowing them to receive published messages.
     *
     * @param receiverList The receivers.
     * @return The event bus.
     */
    public EventBus subscribe(List<Receiver> receiverList) {
        receiverList.forEach(this::subscribe);
        return this;
    }

    /**
     * Registers specified receiver to the event bus, allowing it to receive published messages.
     *
     * @param receiver The receiver.
     * @return The event bus.
     */
    public EventBus subscribe(Receiver receiver) {
        ArrayList<Receiver> registeredReceivers = receiversMap
                .getOrDefault(receiver.getTopic(), new ArrayList<>(configuration.initialReceiverListCapacity()));
        registeredReceivers.add(receiver);
        registeredReceivers.sort(comparingInt(r -> -r.getPriority()));
        receiversMap.put(receiver.getTopic(), registeredReceivers);

        return this;
    }

    /**
     * Inspects the object in quest of {@link Receiver} using the {@link ReceiverFactory}
     * and then registers them.
     *
     * @param object The object.
     * @return The event bus.
     */
    public EventBus subscribe(Object object) {
        List<Receiver> receivers = factoryCache.get(object);
        if(receivers == null) {
            factoryCache.put(object, receivers = configuration.receiverFactory().lookInto(object, configuration));
        }
        return subscribe(receivers);
    }

    /**
     * Unregisters specified receivers to the event bus, allowing these receivers to be "ignored". They can still
     * be registered again.
     *
     * @param receiverList The receivers.
     * @return The event bus.
     */
    public EventBus unsubscribe(List<Receiver> receiverList) {
        receiverList.forEach(this::unsubscribe);
        return this;
    }

    /**
     * Unregisters specified receiver to the event bus, allowing that receiver to be "ignored". It can still
     * be registered again.
     *
     * @param receiver The receiver.
     * @return The event bus.
     */
    public EventBus unsubscribe(Receiver receiver) {
        ArrayList<Receiver> registeredReceivers = receiversMap.get(receiver.getTopic());
        if(registeredReceivers != null) {
            registeredReceivers.remove(receiver);
            receiversMap.put(receiver.getTopic(), registeredReceivers);
        }
        return this;
    }

    /**
     * Inspects the object in quest of {@link Receiver} using the {@link ReceiverFactory}
     * and then unregisters them.
     *
     * @param object The object.
     * @return The event bus.
     */
    public EventBus unsubscribe(Object object) {
        List<Receiver> receivers = factoryCache.get(object);
        if(receivers == null) return this;
        return unsubscribe(receivers);
    }

    /**
     * Dispatches specified message to each of its receivers, if they exist.
     *
     * @param message Message.
     */
    public void dispatch(Object message) {
        configuration
                .dispatcherFor(receiversMap.get(message.getClass()))
                .dispatch(message);
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
        final String values = receiversMap.entrySet().stream()
                .map(e ->
                        e.getKey().getSimpleName() + "=" + e.getValue().stream()
                                .map(Receiver::toString)
                                .collect(joining(","))
                )
                .collect(joining(", ", "{", "}"));

        return "EventBus{" + "receivers=" + values + '}';
    }

}
