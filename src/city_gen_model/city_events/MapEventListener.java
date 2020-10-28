package city_gen_model.city_events;

public class MapEventListener {

    MapEventBroadcaster mapEventBroadcaster;

    public MapEventListener(MapEventBroadcaster broadcaster) {
        this.mapEventBroadcaster = broadcaster;
        this.mapEventBroadcaster.addListener(this);
    }

    protected void listenEvent(MapEvent e) { }
}
