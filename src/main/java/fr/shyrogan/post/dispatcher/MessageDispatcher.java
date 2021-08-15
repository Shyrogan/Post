package fr.shyrogan.post.dispatcher;

/**
 * A message dispatcher is used to dispatch a message to each of its receivers. This allows people to implement specific
 * behaviour if they need it.
 */
public interface MessageDispatcher {

    /**
     * Dispatches the message to each of the receivers.
     *
     * @param message The message.
     */
    void dispatch(Object message);

}
