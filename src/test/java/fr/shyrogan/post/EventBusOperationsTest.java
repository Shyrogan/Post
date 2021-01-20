package fr.shyrogan.post;

import fr.shyrogan.post.receiver.annotation.Subscribe;
import fr.shyrogan.post.receiver.Receiver;
import fr.shyrogan.post.receiver.ReceiverBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

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
        assertTrue(receiverContainer.hasReceivedMethod);
    }

    @Test
    void priority() {
        bus.dispatch("a");

        assertTrue(receiverContainer.last.equalsIgnoreCase("Consumer"));
    }

    public static class DummyReceiverContainer {
        public boolean hasReceivedMessageOnBuilder = false;
        public boolean hasReceivedMessageOnConsumer = false;
        public boolean hasReceivedMethod = false;
        public String last = "";

        @Subscribe(priority = -10)
        public final Consumer<String> mySecondReceiver = s -> {
            hasReceivedMessageOnConsumer = true;
            last = "Consumer";
        };

        @Subscribe
        public final Receiver<String> myReceiver = new ReceiverBuilder<>(String.class)
                .withPriority(10)
                .perform(s -> {
                    hasReceivedMessageOnBuilder = true;
                    last = "Builder";
                })
                .build();

        @Subscribe
        public void onReceive(String message) {
            hasReceivedMethod = true;
        }
    }

}
