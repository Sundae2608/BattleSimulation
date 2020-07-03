import processing.core.PApplet;
import processing.core.PImage;

public class TextureSimulation extends PApplet {

    PImage texture;

    public void settings() {
        size(600, 600, P2D);
    }

    public void setup() {
        texture = loadImage("imgs/MapTiles/pharsalus/IMG-0-10.png");
    }

    public void draw() {
        background(51);
        stroke(255);
        strokeWeight(0);
        noFill();
        textureMode(NORMAL);
        beginShape(TRIANGLE_STRIP);
        texture(texture);
        for (float x = 100; x < 500; x += 50) {
            float u = map(x, 100, 500, 0, 1);
            vertex(x - random(10), 200, u, 0);
            vertex(x, 250 + random(10), u, 0.33f);
            vertex(x, 300 + random(10), u, 0.66f);
            vertex(x, 350 + random(10), u, 1);
        }
        endShape();
    }

    public static void main(String... args) {
        PApplet.main("TextureSimulation");
    }
}
