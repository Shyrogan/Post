package fr.shyrogan.post.dispatcher.impl;

import fr.shyrogan.post.dispatcher.MessageDispatcher;
import fr.shyrogan.post.listener.Listener;

import java.util.ArrayList;

/**
 * A dispatcher which dispatches.. nothing.
 */
@SuppressWarnings("ALL")
public final class IterativeMessageDispatcher implements MessageDispatcher {

    private final int                 size;
    private final ArrayList<Listener> listeners;

    public IterativeMessageDispatcher(int size, ArrayList<Listener> listeners) {
        this.size      = size;
        this.listeners = listeners;
    }

    @Override
    public void dispatch(Object message) {
        for(int i = 0; i < size; i++) {
            listeners.get(i).receive(message);
        }
    }

}
