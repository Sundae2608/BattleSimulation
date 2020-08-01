package controller.tunable;

/**
 * This is the interface that allows users to be able to tune a specific variable based on a scroll bar.
 *
 * We use an interface os to give the users the ability to tune whatever variables they want through a custom update
 * function.
 */
public interface TunableScrollbar {
    /** Update any variable from retrieved value from the scroll bar. */
    void updateValue(double value);
}
