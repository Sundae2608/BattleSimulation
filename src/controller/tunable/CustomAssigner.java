package controller.tunable;

/**
 * This is the interface that contains a method to assign some variables with the given value.
 *
 * We use an interface os to give the users the ability to tune whatever variables they want through a custom update
 * function.
 */
public interface CustomAssigner {
    /** Update any variable from retrieved value from the scroll bar. */
    void updateValue(double value);
}
