package fr.shyrogan.post;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.receiver.Receiver;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;
import java.util.WeakHashMap;

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
    private final MutableMap<Class<?>, MutableList<Receiver>> receiversMap;

    /**
     * A cache used to accelerate subscription/unsubscription.
     */
    private final Map<Object, ImmutableList<Receiver>> factoryCache = new WeakHashMap<>();

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
        this.receiversMap = new UnifiedMap<>(configuration.initialReceiverMapCapacity());
    }

    /**
     * Registers specified receivers to the event bus, allowing them to receive published messages.
     *
     * @param receiverList The receivers.
     * @return The event bus.
     */
    public EventBus with(ListIterable<Receiver> receiverList) {
        if(receiverList.isEmpty()) return this;

        receiverList.groupBy(Receiver::getTopic)
                .forEachKeyMultiValues((topic, receivers) -> receiversMap.put(topic,
                        receiversMap
                                .getOrDefault(topic, new FastList<>(configuration.initialReceiverListCapacity()))
                                .withAll(receivers)
                                .sortThisByInt(Receiver::getPriority)
                                .reverseThis()
                ));

        return this;
    }

    /**
     * Registers specified receiver to the event bus, allowing it to receive published messages.
     *
     * @param receiver The receiver.
     * @return The event bus.
     */
    public EventBus with(Receiver receiver) {
        receiversMap
                .getOrDefault(receiver.getTopic(), new FastList<>(configuration.initialReceiverListCapacity()))
                .with(receiver)
                .sortThisByInt(Receiver::getPriority)
                .reverseThis();

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
        ImmutableList<Receiver> receivers = factoryCache.get(object);
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
    public EventBus without(ListIterable<Receiver> receiverList) {
        if(receiverList.isEmpty()) return this;

        receiverList.groupBy(Receiver::getTopic)
                .forEachKeyMultiValues((topic, receiver) -> receiversMap.ifPresentApply(topic, (receivers) ->
                        receivers
                                .withoutAll(receiver)
                                .sortThisByInt(Receiver::getPriority)
                                .reverseThis()
                ));

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
        receiversMap.ifPresentApply(receiver.getTopic(), (receivers) ->
                receivers
                        .without(receiver)
                        .sortThisByInt(Receiver::getPriority)
                        .reverseThis()
        );
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

        ImmutableList<Receiver> receivers = factoryCache.get(object);
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
