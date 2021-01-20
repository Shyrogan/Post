package fr.shyrogan.post.receiver;

import fr.shyrogan.post.EventBus;

/**
 * Represents to the {@link EventBus} an object able of receiving and treating messages.
 *
 * @param <T> The message type.
 */
public interface Receiver<T> {

    /**
     * Returns the topic of specified message.
     *
     * @return The topic of this receiver.
     */
    Class<T> getTopic();

    /**
     * Returns a number used to prioritize some receivers.
     * Higher priority means it will run first.
     *
     * @return Number
     */
    int getPriority();

    /**
     * Method invoked to call this receiver.
     *
     * @param message The message's instance
     */
    void onReceive(T message);

}
