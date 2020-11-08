package view.video;

import processing.core.PImage;

public class StaticTemplate {

    StaticElementType type;
    PImage image;

    // Min and maximum size of the static element
    double minSize;
    double maxSize;

    // The frame number in which the fade starts and ends.
    int fadeStart;
    int fadeEnd;

    // Angle fluctuation
    double angleFluctuation;

    public StaticTemplate(StaticElementType type, PImage image,
                          int fadeStart, int fadeEnd, double minSize, double maxSize, double angleFluctuation) {
        this.type = type;
        this.image = image;
        this.fadeStart = fadeStart;
        this.fadeEnd = fadeEnd;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.angleFluctuation = angleFluctuation;
    }
}
