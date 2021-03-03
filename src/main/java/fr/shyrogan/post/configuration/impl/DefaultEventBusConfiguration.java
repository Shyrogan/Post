package fr.shyrogan.post.configuration.impl;

import fr.shyrogan.post.configuration.EventBusConfiguration;
import fr.shyrogan.post.dispatcher.MessageDispatcher;
import fr.shyrogan.post.dispatcher.impl.DeadMessageDispatcher;
import fr.shyrogan.post.dispatcher.impl.IterativeMessageDispatcher;
import fr.shyrogan.post.dispatcher.impl.SingletonMessageDispatcher;
import fr.shyrogan.post.factory.ReceiverFactory;
import fr.shyrogan.post.factory.impl.AnnotatedFieldAndMethodFactory;
import fr.shyrogan.post.receiver.Receiver;
import fr.shyrogan.post.utils.DynamicClassLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * The default configuration (and greatest and a lot of cases!).
 */
@SuppressWarnings("ALL")
public class DefaultEventBusConfiguration implements EventBusConfiguration {

    private final static DynamicClassLoader CLASS_LOADER = new DynamicClassLoader();

    @Override
    public int initialReceiverMapCapacity() {
        return 20;
    }

    @Override
    public int initialReceiverListCapacity() {
        return 8;
    }

    @Override
    public ReceiverFactory receiverFactory() {
        return AnnotatedFieldAndMethodFactory.INSTANCE;
    }

    @Override
    public MessageDispatcher dispatcherFor(ArrayList<Receiver> receivers) {
        final int size = receivers == null ? 0 : receivers.size();
        // If it's an empty/null list, does nothing
        if(size == 0) return new DeadMessageDispatcher();
        // If it's a singleton then we don't need an iteration
        else if(size == 1) return new SingletonMessageDispatcher(receivers.get(0));
        // Otherwise just iterates through the list, simple as that.
        else return new IterativeMessageDispatcher(size, receivers);
    }

    @Override
    public DynamicClassLoader classLoader() {
        return CLASS_LOADER;
    }
}
