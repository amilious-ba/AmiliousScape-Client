package rt4.amilious.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Event<T> {
    // Stores a list of functional consumers (the callbacks)
    private final List<Consumer<T>> subscribers = new ArrayList<>();

    // Replaces the += operator
    public void addListener(Consumer<T> listener) {
        subscribers.add(listener);
    }

    // Replaces the -= operator
    public void removeListener(Consumer<T> listener) {
        subscribers.remove(listener);
    }

    // Replaces the .Invoke() method
    public void invoke(T eventArgs) {
        for (Consumer<T> subscriber : subscribers) {
            subscriber.accept(eventArgs);
        }
    }
}
