import model.utils.PhysicUtils;
import processing.core.PApplet;

public class ProjectionSimulation extends PApplet {

    double[][] trapezoidPts;

    public void settings() {
        size(1500, 900);
    }

    public void setup() {
        trapezoidPts = new double[][] {
                {200, 100},
                {700, 100},
                {700, 700},
                {200, 700},
        };
    }

    public void draw() {
        background(255);
        // Check if mouse is inside the polygon.
        boolean hit = PhysicUtils.checkPolygonPointCollision(trapezoidPts, mouseX, mouseY);
        if (hit) fill(250, 150, 0);
        else fill(0, 150, 255);
        // Draw the shape, with color changing whether the mouse is inside the point or not.
        noStroke();
        beginShape();
        for (int i = 0; i < trapezoidPts.length; i++) {
            vertex((float) trapezoidPts[i][0], (float) trapezoidPts[i][1]);
        }
        endShape();
        // Draw the projection to the closest edge.
        double[] projection = PhysicUtils.getClosestPointToEdge(mouseX, mouseY, trapezoidPts);
        fill(0, 150);
        if (hit) {
            stroke(200, 0, 0);
            strokeWeight(3);
            line(mouseX, mouseY, (float) projection[0], (float) projection[1]);
            ellipse((float) projection[0], (float) projection[1], 30,30);
        } else {
            ellipse((float) projection[0], (float) projection[1], 30,30);
            ellipse((float) mouseX, (float) mouseY, 30,30);
        }
    }

    public static void main(String... args){
        PApplet.main("ProjectionSimulation");
    }
}
