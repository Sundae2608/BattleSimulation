package model.monitor;

import java.util.HashMap;

public class Monitor {

    private int index;

    HashMap<MonitorEnum, Integer>[] frameLevelDataStorage;
    HashMap<MonitorEnum, Integer> counter;
    HashMap<MonitorEnum, Integer> totalCounter;

    public Monitor(int storedFrames) {
        counter = new HashMap<>();
        totalCounter = new HashMap<>();
        frameLevelDataStorage = new HashMap[storedFrames];

        index = 0;
    }

    /**
     * Increment counter by 1.
     */
    public void count(MonitorEnum key) {
        this.count(key, 1);
    }

    /**
     * Increment counter by however many.
     */
    public void count(MonitorEnum key, int num) {
        if (!counter.containsKey(key)) {
            counter.put(key, 0);
        }
        if (!totalCounter.containsKey(key)) {
            totalCounter.put(key, 0);
        }
        counter.put(key, counter.get(key) + num);
        totalCounter.put(key, totalCounter.get(key) + num);
    }

    /**
     * Clock the counter and store all the data of the frame to the storage.
     */
    public void clockTheData() {
        index = (index + 1) % frameLevelDataStorage.length;
        frameLevelDataStorage[index] = counter;
        counter = new HashMap<>();
    }

    /**
     * Get a string represents the count for each input enum recorded during each frame.
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

    /**
     * Get a string represents the count for each input enum recorded through the entire duration of the game.
     */
    public String getTotalCounterString(MonitorEnum[] enums) {
        StringBuilder s = new StringBuilder();
        for (MonitorEnum e : enums) {
            s.append(String.format("%-32s: ", e.toString()));
            s.append(totalCounter.get(e));
            s.append("\n");
        }
        return s.toString();
    }
}
