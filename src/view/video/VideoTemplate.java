package view.video;

import processing.core.PImage;

import java.util.ArrayList;

public class VideoTemplate {
    private VideoElementType type;
    private ArrayList<PImage> sequence;

    public VideoTemplate(VideoElementType inputType, ArrayList<PImage> inputSequence) {
        type = inputType;
        sequence = inputSequence;
    }

    public VideoElementType getType() {
        return type;
    }

    public ArrayList<PImage> getSequence() {
        return sequence;
    }
}
