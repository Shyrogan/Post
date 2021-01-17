package fr.shyrogan.post.receiver;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A pretty simple but useful builder to create receivers on the fly!
 * Careful! Incomplete receivers will throw errors!
 *
 * @param <T> The message type.
 */
public class ReceiverBuilder<T> {

    private final Class<T> topic;
    private int priority;
    private Consumer<T> consumer;
    private Function<T, Boolean> filter;

    /**
     * Creates a new builder for specified topic.
     *
     * @param topic The topic.
     */
    public ReceiverBuilder(Class<T> topic) {
        this.topic = topic;
    }

    /**
     * Modifies the priority which is by default set to 0.
     * @see Receiver#getPriority()
     *
     * @param priority The priority.
     * @return The builder.
     */
    public ReceiverBuilder<T> withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Specifies a filter that will block the call if it returns false.
     *
     * @param filter The filter.
     * @return The builder.
     */
    public ReceiverBuilder<T> withFilter(Function<T, Boolean> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Specifies the action performed when a message is received.
     *
     * @param consumer The consumer invoked when the message is received.
     * @return The builder.
     */
    public ReceiverBuilder<T> perform(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    public Receiver<T> build() {
        if(consumer == null) throw new IllegalStateException("A receiver is being built yet no actions performed.");
        if(filter == null) return new NonFilteredReceiver<>(topic, priority, consumer);
        return new FilteredReceiver<>(topic, priority, filter, consumer);
    }

    /**
     * A simple {@link Receiver} implementation that does not contain any filter!
     *
     * @param <T> Message type.
     */
    public static class NonFilteredReceiver<T> implements Receiver<T> {
        private final Class<T> topic;
        private final int priority;
        private final Consumer<T> consumer;

        NonFilteredReceiver(Class<T> topic, int priority, Consumer<T> consumer) {
            this.topic = topic;
            this.priority = priority;
            this.consumer = consumer;
        }

        @Override
        public Class<T> getTopic() {
            return topic;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void call(T message) {
            consumer.accept(message);
        }

        @Override
        public String toString() {
            return "NonFilteredReceiver{" +
                    "topic=" + topic +
                    ", priority=" + priority + '}';
        }
    }

    /**
     * A simple {@link Receiver} implementation that does not contain any filter!
     *
     * @param <T> Message type.
     */
    public static class FilteredReceiver<T> implements Receiver<T> {
        private final Class<T> topic;
        private final int priority;
        private final Function<T, Boolean> filter;
        private final Consumer<T> consumer;

        FilteredReceiver(Class<T> topic, int priority, Function<T, Boolean> filter, Consumer<T> consumer) {
            this.topic = topic;
            this.priority = priority;
            this.filter = filter;
            this.consumer = consumer;
        }

        @Override
        public Class<T> getTopic() {
            return topic;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void call(T message) {
            if(filter.apply(message)) consumer.accept(message);
        }

        @Override
        public String toString() {
            return "FilteredReceiver{" +
                    "topic=" + topic +
                    ", priority=" + priority + '}';
        }
    }

}
