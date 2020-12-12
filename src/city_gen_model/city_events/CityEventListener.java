package city_gen_model.city_events;

public class CityEventListener {

    CityEventBroadcaster cityEventBroadcaster;

    public CityEventListener(CityEventBroadcaster broadcaster) {
        this.cityEventBroadcaster = broadcaster;
        this.cityEventBroadcaster.addListener(this);
    }

    protected void listenEvent(CityEvent e) { }
}
