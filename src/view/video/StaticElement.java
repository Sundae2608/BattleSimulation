package view.video;

public class StaticElement {
    double x;
    double y;
    double z;
    double angle;
    StaticTemplate template;
    int frame;

    public StaticElement(double inputX, double inputY, double inputZ, double inputAngle, StaticTemplate inputTemplate) {
        x = inputX;
        y = inputY;
        z = inputZ;
        angle = inputAngle;
        template = inputTemplate;
        frame = 0;
    }

    /**
     * Go to the next frame in the sequence.
     */
    public void step() {
        frame += 1;
    }

    /**
     * True if the video element sequence has ran its course.
     */
    public boolean hasFaded() {
        return frame >= template.getFadeEnd();
    }
}
