package model.surface;

import model.algorithms.TreeHasher;
import model.enums.SurfaceType;
import model.singles.BaseSingle;
import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;

public class ForestSurface extends BaseSurface {

    ArrayList<Tree> trees;
    TreeHasher treeHasher;
    double averageTreeRadius;
    double sizeWiggling;

    public ForestSurface(SurfaceType type,
                         ArrayList<double[]> points,
                         double averageTreeRadius,
                         double sizeWiggling,
                         double averageDistance,
                         double distanceWiggling,
                         double averageTreeHeight,
                         double heightWiggling) {
        super(type, points);

        this.averageTreeRadius = averageTreeRadius;
        this.sizeWiggling = sizeWiggling;

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
                        averageTreeRadius + MathUtils.randDouble(-sizeWiggling, sizeWiggling),
                        averageTreeHeight + MathUtils.randDouble(-heightWiggling, heightWiggling));
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
            }
        }

        treeHasher.addObjectArray(trees);
        treeHasher.hashObjects();
    }

    @Override
    public void impactSingle(BaseSingle single) {

    }

    public ArrayList<Tree> getTrees() {
        return trees;
    }

    public TreeHasher getTreeHasher() {
        return treeHasher;
    }

    public double getTreeMaxRadius() {
        double maxRadius = averageTreeRadius + sizeWiggling;
        return  maxRadius;
    }
}
