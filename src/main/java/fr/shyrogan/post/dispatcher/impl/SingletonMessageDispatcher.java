package fr.shyrogan.post.dispatcher.impl;

import fr.shyrogan.post.dispatcher.MessageDispatcher;
import fr.shyrogan.post.receiver.Receiver;

/**
 * A dispatcher which dispatches.. nothing.
 */
@SuppressWarnings("ALL")
public final class SingletonMessageDispatcher implements MessageDispatcher {

    private final Receiver receiver;

    public SingletonMessageDispatcher(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void dispatch(Object message) {
        receiver.call(message);
    }

}
