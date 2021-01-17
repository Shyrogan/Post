package fr.shyrogan.post;

import fr.shyrogan.post.receiver.annotation.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

@DisplayName("Basic performances test")
public class EventBusPerformancesTest {

    private final EventBus bus = new EventBus();

    @BeforeEach
    void registerReceiver() {
        for(int i = 0; i < 2; i++) {
            bus.with(new DummyReceiverContainer());
        }
    }

    @RepeatedTest(1)
    void dispatch() {
        long begin = System.nanoTime();
        String message = "Hey!";
        for(int i = 0; i < 1_000; i++) {
            bus.dispatch(message);
        }
        long end   = System.nanoTime();
        System.out.println((end - begin) * 1.0e-6 + "ms!");
    }

    public static class DummyReceiverContainer {
        @Subscribe
        public void onMessage(String message) {
        }
    }

}
