package fr.shyrogan.post.listener;

import fr.shyrogan.post.EventBus;

/**
 * Represents to the {@link EventBus} an object able of receiving and treating messages.
 *
 * @param <T> The message type.
 */
public interface Listener<T> {

    /**
     * Returns the topic of specified message.
     *
     * @return The topic of this receiver.
     */
    Class<T> topic();

    /**
     * Returns a number used to prioritize some receivers. Higher priority means it will run first.
     *
     * @return Number
     */
    int priority();

    /**
     * Method invoked to call this receiver.
     *
     * @param message The message's instance
     */
    void receive(T message);

}
