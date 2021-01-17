package fr.shyrogan.post;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.receiver.Receiver;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

/**
 * Post's {@link EventBus} and "entry-point" to the library, this event bus is based on eclipse collections
 * and provides a powerful yet as configurable as possible place to dispatch message.
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
    private final Map<Class<?>, List<Receiver>> receiversMap;

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
    public EventBus with(List<Receiver> receiverList) {
        if(receiverList.isEmpty()) return this;

        receiverList
                .stream()
                .collect(Collectors.groupingBy(Receiver::getTopic))
                .forEach((topic, receivers) -> {
                    List<Receiver> registeredReceivers =  receiversMap
                            .getOrDefault(topic, new ArrayList<>(configuration.initialReceiverListCapacity()));
                    registeredReceivers.addAll(receivers);
                    registeredReceivers.sort(comparingInt(r -> -r.getPriority()));
                    receiversMap.put(topic, registeredReceivers);
                });

        return this;
    }

    /**
     * Registers specified receiver to the event bus, allowing it to receive published messages.
     *
     * @param receiver The receiver.
     * @return The event bus.
     */
    public EventBus with(Receiver receiver) {
        List<Receiver> registeredReceivers = receiversMap
                .getOrDefault(receiver.getTopic(), new ArrayList<>(configuration.initialReceiverListCapacity()));
        registeredReceivers.add(receiver);
        registeredReceivers.sort(comparingInt(r -> -r.getPriority()));
        receiversMap.put(receiver.getTopic(), registeredReceivers);

        return this;
    }

    /**
     * Looks for each {@link Receiver} inside of specified object using the {@link ReceiverFactory}
     * and then registers them.
     *
     * @param object The object.
     * @return The event bus.
     */
    public EventBus with(Object object) {
        if(!configuration.allowCachingReceiver())
            return with(configuration.receiverFactory().lookInto(object, configuration));
        List<Receiver> receivers = factoryCache.get(object);
        if(receivers == null) {
            factoryCache.put(object, receivers = configuration.receiverFactory().lookInto(object, configuration));
        }

        return with(receivers);
    }

    /**
     * Unregisters specified receivers to the event bus, allowing these receivers to be "ignored". They can still
     * be registered again.
     * Be careful to either implement the {@link Object#equals(Object)} method or unregister the correct receiver instance,
     * if it is being recreated without the equals method, this will not work!
     *
     * @param receiverList The receivers.
     * @return The event bus.
     */
    public EventBus without(List<Receiver> receiverList) {
        if(receiverList.isEmpty()) return this;

        receiverList
                .stream()
                .collect(Collectors.groupingBy(Receiver::getTopic))
                .forEach((topic, receivers) -> {
                    List<Receiver> registeredReceivers = receiversMap.get(topic);
                    if(registeredReceivers != null) {
                        registeredReceivers.removeAll(receivers);
                        registeredReceivers.sort(comparingInt(r -> -r.getPriority()));
                        receiversMap.put(topic, registeredReceivers);
                    }
                });

        return this;
    }

    /**
     * Unregisters specified receiver to the event bus, allowing that receiver to be "ignored". It can still
     * be registered again.
     * Be careful to either implement the {@link Object#equals(Object)} method or unregister the correct receiver instance,
     * if it is being recreated without the equals method, this will not work!
     *
     * @param receiver The receiver.
     * @return The event bus.
     */
    public EventBus without(Receiver receiver) {
        List<Receiver> registeredReceivers = receiversMap.get(receiver.getTopic());
        registeredReceivers.remove(receiver);
        registeredReceivers.sort(comparingInt(r -> -r.getPriority()));
        receiversMap.put(receiver.getTopic(), registeredReceivers);
        return this;
    }

    /**
     * Looks for each {@link Receiver} inside of specified object using the {@link ReceiverFactory}
     * and then unregisters them.
     *
     * @param object The object.
     * @return The event bus.
     */
    public EventBus without(Object object) {
        if(!configuration.allowCachingReceiver())
            return without(configuration.receiverFactory().lookInto(object, configuration));

        List<Receiver> receivers = factoryCache.get(object);
        if(receivers == null) return this;
        return without(receivers);
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
     * Drains the event bus (basically clears the cache and subscriptions).
     *
     * @return The event bus drained.
     */
    public EventBus drain() {
        receiversMap.clear();
        factoryCache.clear();
        return this;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "EventBus{" + "receiversMap=" + receiversMap + '}';
    }
}
