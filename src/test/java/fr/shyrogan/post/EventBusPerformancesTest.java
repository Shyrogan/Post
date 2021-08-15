package fr.shyrogan.post;

import fr.shyrogan.post.listener.annotation.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

@DisplayName("Basic performances test")
public class EventBusPerformancesTest {

    private final EventBus bus = new EventBus();

    @BeforeEach
    void registerReceiver() {
        for(int i = 0; i < 1e1; i++) {
            bus.subscribe(new DummyReceiverContainer());
        }
    }

    @RepeatedTest(200)
    void dispatch() {
        long begin = System.nanoTime();
        String message = "Hey!";
        for(int i = 0; i < 1e2; i++) {
            bus.dispatch(message);
        }
        long end   = System.nanoTime();
        System.out.println("Post: " + (end - begin) * 1.0e-6 + "ms for 1e6 dispatch to 10 receivers.");
    }

    public static class DummyReceiverContainer {
        @Subscribe
        public void onMessage(String message) {
        }
    }

}
