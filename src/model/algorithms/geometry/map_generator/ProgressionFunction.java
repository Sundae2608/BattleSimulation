package model.algorithms.geometry.map_generator;

import java.util.List;

/***
 * Used to produce the next time step of the map
 * Example: Input - list of current houses
 * Output - list of houses in next step
*/
public interface ProgressionFunction<T> {
    List<T> progress(List<T> objects);
}
