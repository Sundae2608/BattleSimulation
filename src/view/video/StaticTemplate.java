package view.video;

import processing.core.PImage;

public class StaticTemplate {

    StaticElementType type;
    PImage image;

    // The frame number in which the fade starts and ends.
    int fadeStart;
    int fadeEnd;

    public StaticTemplate(StaticElementType type, PImage image, int fadeStart, int fadeEnd) {
        this.type = type;
        this.image = image;
        this.fadeStart = fadeStart;
        this.fadeEnd = fadeEnd;
    }

    public StaticElementType getType() {
        return type;
    }

    public PImage getImage() {
        return image;
    }

    public int getFadeStart() {
        return fadeStart;
    }

    public int getFadeEnd() {
        return fadeEnd;
    }
}
