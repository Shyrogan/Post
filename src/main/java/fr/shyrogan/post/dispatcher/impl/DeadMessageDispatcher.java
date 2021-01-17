package fr.shyrogan.post.dispatcher.impl;

import fr.shyrogan.post.dispatcher.MessageDispatcher;

/**
 * A dispatcher which dispatches.. nothing.
 */
public final class DeadMessageDispatcher implements MessageDispatcher {

    @Override
    public void dispatch(Object message) { }

}
