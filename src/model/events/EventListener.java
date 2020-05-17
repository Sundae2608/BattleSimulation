package model.events;

public class EventListener {

    EventBroadcaster broadcaster;

    public EventListener(EventBroadcaster inputBroadcaster) {
        broadcaster = inputBroadcaster;
        broadcaster.addListener(this);
    }

    protected void listenEvent(Event e) { };
}
