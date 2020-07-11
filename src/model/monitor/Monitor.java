package model.monitor;

import java.util.ArrayList;
import java.util.HashMap;

public class Monitor {

    private int index;

    HashMap<MonitorEnum, Integer>[] dataStorage;
    HashMap<MonitorEnum, Integer> counter;

    public Monitor(int storedFrames) {
        counter = new HashMap<>();
        dataStorage = new HashMap[storedFrames];

        index = 0;
    }

    /**
     * Increment counter by 1.
     */
    public void count(MonitorEnum key) {
        if (!counter.containsKey(key)) {
            counter.put(key, 0);
        }
        counter.put(key, counter.get(key) + 1);
    }

    /**
     * Increment counter by however many.
     */
    public void count(MonitorEnum key, int num) {
        if (!counter.containsKey(key)) {
            counter.put(key, 0);
        }
        counter.put(key, counter.get(key) + num);
    }

    /**
     * Clock the counter and store all the data of the frame to the storage.
     */
    public void clockTheData() {
        index = (index + 1) % dataStorage.length;
        dataStorage[index] = counter;
        counter = new HashMap<>();
    }

    /**
     * Get a string represents enums and many
     */
    public String getCounterString(MonitorEnum[] enums) {
        StringBuilder s = new StringBuilder();
        for (MonitorEnum e : enums) {
            s.append(String.format("%-32s: ", e.toString()));
            s.append(counter.get(e));
            s.append("\n");
        }
        return s.toString();
    }
}
