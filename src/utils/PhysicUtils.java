package utils;

public final class PhysicUtils {

    /**
     * Check if two axis-aligned bounding boxes collide
     * @param box1 four corners of box 1
     * @param box2 four corners of box 2
     * @return true if they are collided
     */
    public static boolean axisAlignedBoundingBoxCollide(double[][] box1, double[][] box2) {
        double left1 = Math.min(Math.min(Math.min(box1[0][0], box1[1][0]), box1[2][0]), box1[3][0]);
        double left2 = Math.min(Math.min(Math.min(box2[0][0], box2[1][0]), box2[2][0]), box2[3][0]);

        double top1 = Math.min(Math.min(Math.min(box1[0][1], box1[1][1]), box1[2][1]), box1[3][1]);
        double top2 = Math.min(Math.min(Math.min(box2[0][1], box2[1][1]), box2[2][1]), box2[3][1]);

        double right1 = Math.max(Math.max(Math.max(box1[0][0], box1[1][0]), box1[2][0]), box1[3][0]);
        double right2 = Math.max(Math.max(Math.max(box2[0][0], box2[1][0]), box2[2][0]), box2[3][0]);

        double bot1 = Math.max(Math.max(Math.max(box1[0][1], box1[1][1]), box1[2][1]), box1[3][1]);
        double bot2 = Math.max(Math.max(Math.max(box2[0][1], box2[1][1]), box2[2][1]), box2[3][1]);

        return !(left2 > right1
                || right2 < left1
                || top2 > bot1
                || bot2 < top1);
    }
    /**
     * Check if two rotated bounding boxes collide
     * @param box1 four corners of box 1
     * @param box2 four corners of box 2
     * @return true if they are collided
     */
    public static boolean rotatedBoundingBoxCollide(double[][] box1, double[][] box2) {
        double[][][] boxes = {box1, box2};

        for (int box_i = 0; box_i < 2; box_i++) {
            double[][] box = boxes[box_i];
            for (int i1 = 0; i1 < 4; i1++) {
                int i2 = (i1 + 1) % 4;

                // Grab 2 vertices to create an edge.
                double[] p1 = box[i1];
                double[] p2 = box[i2];

                // Perpendicular line
                double[] normal = {p1[1] - p2[1], p1[0] - p2[0]};

                // for each vertex in the first shape, project it onto the line perpendicular to the edge
                // and keep track of the min and max of these values
                double minA = Double.MAX_VALUE;
                double maxA = Double.MIN_VALUE;
                for (int j = 0; j < 4; j++) {
                    double projected = normal[0] * box1[j][0] + normal[1] * box1[j][1];
                    if (projected < minA) {
                        minA = projected;
                    }
                    if (projected > maxA) {
                        maxA = projected;
                    }
                }

                // for each vertex in the second shape, project it onto the line perpendicular to the edge
                // and keep track of the min and max of these values
                double minB = Double.MAX_VALUE;
                double maxB = Double.MIN_VALUE;
                for (int j = 0; j < 4; j++) {
                    double projected = normal[0] * box2[j][0] + normal[1] * box2[j][1];
                    if (projected < minB) {
                        minB = projected;
                    }
                    if (projected > maxB) {
                        maxB = projected;
                    }
                }

                // if there is no overlap between the projects, the edge we are looking at separates the two
                // polygons, and we know there is no overlap
                if (maxA < minB || maxB < minA) {
                    return false;
                }
            }
        }
        return true;
    }
}
