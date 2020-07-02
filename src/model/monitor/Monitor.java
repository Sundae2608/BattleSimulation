package model.monitor;

import processing.core.PApplet;

import java.util.HashMap;

public class Monitor {

    HashMap<MonitorEnum, Integer> counter;
    public Monitor() {
        counter = new HashMap<>();
    }

    public void count(MonitorEnum key) {
        if (!counter.containsKey(key)) {
            counter.put(key, 0);
        }
        counter.put(key, counter.get(key) + 1);
    }

    public void resetCounter() {
        for (MonitorEnum key : counter.keySet()) {
            counter.put(key, 0);
        }
    }
}
