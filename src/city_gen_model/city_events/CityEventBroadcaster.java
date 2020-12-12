package city_gen_model.city_events;

import java.util.ArrayList;
import java.util.List;

public class CityEventBroadcaster {
    List<CityEventListener> cityEventListeners;

    public CityEventBroadcaster() {
        cityEventListeners = new ArrayList<>();
    }

    public void addListener(CityEventListener listener) {
        cityEventListeners.add(listener);
    }

    public void broadcastEvent(CityEvent event) {
        for (CityEventListener listener : cityEventListeners) {
            listener.listenEvent(event);
        }
    }
}
