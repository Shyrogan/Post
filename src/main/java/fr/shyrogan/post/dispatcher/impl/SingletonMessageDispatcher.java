package fr.shyrogan.post.dispatcher.impl;

import fr.shyrogan.post.dispatcher.MessageDispatcher;
import fr.shyrogan.post.listener.Listener;

/**
 * A dispatcher which dispatches.. nothing.
 */
@SuppressWarnings("ALL")
public final class SingletonMessageDispatcher implements MessageDispatcher {

    private final Listener listener;

    public SingletonMessageDispatcher(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void dispatch(Object message) {
        listener.receive(message);
    }

}
