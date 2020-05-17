package model.events;

import java.util.ArrayList;

public class EventBroadcaster {

    // The list of listener tuning in for the events.
    ArrayList<EventListener> listeners;

    public EventBroadcaster() {
        listeners = new ArrayList<>();
    }

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void broadcastEvent(Event e) {
        for (EventListener listener : listeners) {
            listener.listenEvent(e);
        }
    }
}
