package city_gen_model.algorithms.geometry.tree_generation;

import city_gen_model.algorithms.geometry.Edge;
import city_gen_model.algorithms.geometry.Polygon;
import city_gen_model.algorithms.geometry.PolygonHasher;
import city_gen_model.algorithms.geometry.Vertex;
import model.map_objects.Tree;
import model.utils.MathUtils;
import model.utils.PhysicUtils;

import java.util.ArrayList;

public class TreeFactory {

    TreeGenerationSettings treeGenerationSettings;

    public TreeFactory(TreeGenerationSettings inputSettings) {
        treeGenerationSettings = inputSettings;
    }

    public ArrayList<Tree> generateTreesFromEdge(Edge e, PolygonHasher hasher) {
        // Pre-calculate  several directional information
        Vertex vertex1 = e.getVertex1();
        Vertex vertex2 = e.getVertex2();
        double roadDistance = MathUtils.quickDistance(vertex1.getX(), vertex1.getY(), vertex2.getX(), vertex2.getY());
        double angle = MathUtils.atan2(vertex2.getY() - vertex1.getY(), vertex2.getX() - vertex1.getX());
        double beginX = vertex1.getX();
        double beginY = vertex1.getY();
        double roadUnitX = MathUtils.quickCos((float) angle);
        double roadUnitY = MathUtils.quickSin((float) angle);
        double rightSideUnitX = MathUtils.quickCos((float) (angle + MathUtils.PIO2));
        double rightSideUnitY = MathUtils.quickSin((float) (angle + MathUtils.PIO2));
        double leftSideUnitX = MathUtils.quickCos((float) (angle - MathUtils.PIO2));
        double leftSideUnitY = MathUtils.quickSin((float) (angle - MathUtils.PIO2));

        // Generate the left-side trees
        ArrayList<Tree> trees = new ArrayList<>();
        double currentDist = treeGenerationSettings.getDistanceFromCrossRoad();
        Tree lastTree = null;
        while (true) {
            // Generate a house if possible
            double distFromEdge = treeGenerationSettings.getDistanceFromEdge() +
                    MathUtils.randDouble(-treeGenerationSettings.getDistanceFromEdgeWiggle(), treeGenerationSettings.getDistanceFromEdgeWiggle());
            double treeX = beginX + roadUnitX * currentDist + distFromEdge * rightSideUnitX;
            double treeY = beginY + roadUnitY * currentDist + distFromEdge * rightSideUnitY;
            if (lastTree != null && PhysicUtils.checkPointCircleCollision(treeX, treeY,
                    lastTree.getX(), lastTree.getY(), lastTree.getRadius())) {
                double distFromHouse = treeGenerationSettings.getDistanceFromOther() +
                        MathUtils.randDouble(-treeGenerationSettings.getDistanceFromOtherWiggle(), treeGenerationSettings.getDistanceFromOtherWiggle());
                currentDist = currentDist + distFromHouse;
                continue;
            }
            Tree newTree = new Tree(treeX, treeY,
                    treeGenerationSettings.getSize() + MathUtils.randDouble(
                            -treeGenerationSettings.getSizeWiggle(),
                            treeGenerationSettings.getSizeWiggle()),
                    treeGenerationSettings.getSize() + MathUtils.randDouble(
                            -treeGenerationSettings.getSizeWiggle(),
                            treeGenerationSettings.getSizeWiggle()
                    ));

            // Check the newly generated house with the hasher and make sure that it does not collide with existing
            // house
            // TODO: Right now we are only checking the tree center, but include Polygon / Circle check as well.
            //  Potential source: http://www.jeffreythompson.org/collision-detection/poly-circle.php
            boolean collide = false;
            for (Polygon p : hasher.getCollisionObjects(newTree.getX(), newTree.getY(), newTree.getRadius())) {
                if (PhysicUtils.checkPolygonPointCollision(p.getBoundaryPoints(), newTree.getX(), newTree.getY())) {
                    collide = true;
                    break;
                };
            }
            if (!collide) {
                trees.add(newTree);
                lastTree = newTree;
            }
            double distFromTree = treeGenerationSettings.getDistanceFromOther() +
                    MathUtils.randDouble(
                            -treeGenerationSettings.getDistanceFromOtherWiggle(), treeGenerationSettings.getDistanceFromEdgeWiggle());
            currentDist = currentDist + distFromTree;
            if (currentDist > roadDistance -  treeGenerationSettings.getDistanceFromCrossRoad()) break;
        }

        // Generate the right-side houses
        currentDist = treeGenerationSettings.getDistanceFromCrossRoad();
        beginX = vertex2.getX();
        beginY = vertex2.getY();
        angle = angle + Math.PI;
        lastTree = null;
        while (true) {
            // Generate a house if possible
            double distFromEdge = treeGenerationSettings.getDistanceFromEdge() +
                    MathUtils.randDouble(
                            -treeGenerationSettings.getDistanceFromEdgeWiggle(),
                            treeGenerationSettings.getDistanceFromEdgeWiggle());
            double treeX = beginX - roadUnitX * currentDist + distFromEdge * leftSideUnitX;
            double treeY = beginY - roadUnitY * currentDist + distFromEdge * leftSideUnitY;
            if (lastTree != null && PhysicUtils.checkPointCircleCollision(treeX, treeY,
                    lastTree.getX(), lastTree.getY(), lastTree.getRadius())) {
                double distFromHouse = treeGenerationSettings.getDistanceFromOther() +
                        MathUtils.randDouble(
                                -treeGenerationSettings.getDistanceFromOtherWiggle(),
                                treeGenerationSettings.getDistanceFromOtherWiggle());
                currentDist = currentDist + distFromHouse;
                continue;
            }
            Tree newTree = new Tree(treeX, treeY,
                    treeGenerationSettings.getSize() + MathUtils.randDouble(
                            -treeGenerationSettings.getSizeWiggle(),
                            treeGenerationSettings.getSizeWiggle()),
                    treeGenerationSettings.getHeight() + MathUtils.randDouble(
                            -treeGenerationSettings.getHeightWiggle(),
                            treeGenerationSettings.getHeightWiggle()
                    ));

            // Check the newly generated house with the hasher and make sure that it does not collide with existing
            // house
            boolean collide = false;
            for (Polygon p : hasher.getCollisionObjects(newTree.getX(), newTree.getY(), newTree.getRadius())) {
                if (PhysicUtils.checkPolygonPointCollision(p.getBoundaryPoints(), newTree.getX(), newTree.getY())) {
                    collide = true;
                    break;
                };
            }

            // Add the polygon to the system, and reconfigure the settings to generate the next house.
            if (!collide) {
                trees.add(newTree);
                lastTree = newTree;
            }
            double distFromTree = treeGenerationSettings.getDistanceFromOther() +
                    MathUtils.randDouble(
                            -treeGenerationSettings.getDistanceFromOtherWiggle(),
                            treeGenerationSettings.getDistanceFromEdgeWiggle());
            currentDist = currentDist + distFromTree;
            if (currentDist > roadDistance -  treeGenerationSettings.getDistanceFromCrossRoad()) break;
        }
        return trees;
    }
}
