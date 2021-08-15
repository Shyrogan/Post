package fr.shyrogan.post;

import fr.shyrogan.post.listener.Listener;
import fr.shyrogan.post.listener.ListenerBuilder;
import fr.shyrogan.post.listener.annotation.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static fr.shyrogan.post.EventBusOperationsTest.DummyReceiverContainer.hasReceivedMethod;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Basic operation test")
public class EventBusOperationsTest {

    private final EventBus bus = new EventBus();
    private final DummyReceiverContainer receiverContainer = new DummyReceiverContainer();

    @BeforeEach
    void register() {
        bus.subscribe(receiverContainer);
    }

    @Test
    void dispatch() {
        bus.dispatch("a");
        assertTrue(receiverContainer.hasReceivedMessageOnBuilder);
        assertTrue(receiverContainer.hasReceivedMessageOnConsumer);
        assertTrue(hasReceivedMethod);
    }

    @Test
    void priority() {
        bus.dispatch("a");

        assertTrue(receiverContainer.last.equalsIgnoreCase("Consumer"));
    }

    @Test
    void unsubscribe() {
        bus.unsubscribe(receiverContainer);
        bus.dispatch("test");
        assertFalse(receiverContainer.message.equalsIgnoreCase("test"));
    }

    public static class DummyReceiverContainer {
        public boolean hasReceivedMessageOnBuilder = false;
        public boolean hasReceivedMessageOnConsumer = false;
        public static boolean hasReceivedMethod = false;
        public String last = "";
        public String message = "";

        @Subscribe(priority = -10)
        public final Consumer<String> mySecondReceiver = s -> {
            hasReceivedMessageOnConsumer = true;
            last = "Consumer";
            message = s;
        };

        @Subscribe
        public final Listener<String> myListener = new ListenerBuilder<>(String.class)
                .priority(10)
                .perform(s -> {
                    hasReceivedMessageOnBuilder = true;
                    last = "Builder";
                    message = s;
                })
                .build();

        @Subscribe
        public void onReceive(String message) {
            hasReceivedMethod = true;
            this.message = message;
        }
    }

}
