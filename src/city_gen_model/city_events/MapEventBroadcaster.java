package city_gen_model.city_events;

import model.events.EventListener;

import java.util.ArrayList;
import java.util.List;

public class MapEventBroadcaster {
    List<MapEventListener> mapEventListeners;

    public MapEventBroadcaster() {
        mapEventListeners = new ArrayList<>();
    }

    public void addListener(MapEventListener listener) {
        mapEventListeners.add(listener);
    }

    public void broadcastEvent(MapEvent event) {
        for (MapEventListener listener : mapEventListeners) {
            listener.listenEvent(event);
        }
    }
}
