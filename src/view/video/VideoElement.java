package view.video;

public class VideoElement {
    double x;
    double y;
    double z;
    double angle;
    VideoTemplate template;
    int frame;

    public VideoElement(double inputX, double inputY, double inputZ, double inputAngle, VideoTemplate inputTemplate) {
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
    public boolean videoHasEnded() {
        return frame >= template.getSequence().size();
    }
}
