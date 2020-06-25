package model.surface;

import model.enums.SurfaceType;
import model.singles.BaseSingle;
import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;

public class ForestSurface extends BaseSurface {

    ArrayList<Tree> trees;
    public ForestSurface(SurfaceType type,
                         ArrayList<double[]> points,
                         double averageTreeRadius,
                         double sizeWiggling,
                         double averageDistance,
                         double distanceWiggling) {
        super(type, points);

        // Generate trees for the area.
        double minX = Double.MAX_VALUE; double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE; double maxY = Double.MIN_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double[] pt = points.get(i);
            if (pt[0] < minX) {
                minX = pt[0];
            }
            if (pt[0] > maxX) {
                maxX = pt[0];
            }
            if (pt[1] < minY) {
                minY = pt[1];
            }
            if (pt[1] > maxY) {
                maxY = pt[1];
            }
        }
        double xJump = averageDistance * Math.sqrt(3) / 2;
        double yJump = averageDistance;
        int row = 0;
        double startX = minX;
        double startY = minY;
        double currX = startX;
        double currY = startY;
        trees = new ArrayList<>();
        while (true) {
            if (PhysicUtils.checkPolygonPointCollision(surfaceBoundary, currX, currY)) {
                Tree tree = new Tree(
                        currX + MathUtils.randDouble(-distanceWiggling, distanceWiggling),
                        currY + MathUtils.randDouble(-distanceWiggling, distanceWiggling),
                        averageTreeRadius + MathUtils.randDouble(-sizeWiggling, sizeWiggling));
                trees.add(tree);
            }
            currY += yJump;
            if (currY > maxY) {
                row += 1;
                currX += xJump;
                if (currX > maxX) {
                    break;
                }
                currY = minY + (row % 2) * averageDistance / 2;
                System.out.println(String.valueOf(currX) + " " + String.valueOf(currY));
            }
        }
    }

    @Override
    public void impactSingle(BaseSingle single) {

    }

    public ArrayList<Tree> getTrees() {
        return trees;
    }
}
