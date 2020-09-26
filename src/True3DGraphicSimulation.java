import processing.core.PApplet;

public class True3DGraphicSimulation extends PApplet {

    public void settings() {
        size(640,360, P3D);
    }
    public void setup() {
        background(0);
        lights();
    }

    public void draw() {
        background(0);
        pushMatrix();
        translate(130, height / 2, 0);
        rotateY((float) 1.25);
        rotateX((float) -0.4);
        noStroke();
        box(100);
        popMatrix();

        pushMatrix();
        translate(500, (float) (height * 0.35), -200);
        noFill();
        stroke(255);
        sphere(280);
        popMatrix();
    }

    public static void main(String[] args){
        PApplet.main("True3DGraphicSimulation");
    }
}