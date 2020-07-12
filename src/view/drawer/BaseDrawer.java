package view.drawer;

public abstract class BaseDrawer {

    // Pre-process method will perform some optimization for certain variables and work such as size & offset
    // calculation. This is often done before the drawing of each frame.
    public abstract void preprocess();
}
