package fr.shyrogan.post.dispatcher.impl;

import fr.shyrogan.post.dispatcher.MessageDispatcher;
import fr.shyrogan.post.receiver.Receiver;

import java.util.List;

/**
 * A dispatcher which dispatches.. nothing.
 */
@SuppressWarnings("ALL")
public final class IterativeMessageDispatcher implements MessageDispatcher {

    private final int size;
    private final List<Receiver> receivers;

    public IterativeMessageDispatcher(int size, List<Receiver> receivers) {
        this.size = size;
        this.receivers = receivers;
    }

    @Override
    public void dispatch(Object message) {
        for(int i = 0; i < size; i++) {
            receivers.get(i).call(message);
        }
    }

}
