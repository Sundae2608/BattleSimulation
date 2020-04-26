package model.units;

import model.constants.UniversalConstants;
import javafx.util.Pair;
import model.enums.PoliticalFaction;
import model.enums.UnitState;
import model.singles.BaseSingle;
import model.enums.SingleState;
import model.units.unit_stats.UnitStats;
import model.universal_interface.Updatable;
import model.utils.MathUtils;
import model.utils.MovementUtils;

import java.util.*;

public class BaseUnit implements Updatable {

    // Troops and width
    ArrayList<BaseSingle> troops;
    HashSet<BaseSingle> aliveTroopsSet;
    int width;
    int depth;

    // Political attribute
    PoliticalFaction politicalFaction;
    UnitStats unitStats;

    // Unit state
    UnitState state = UnitState.STANDING;

    // Variables to indicate anchored position (unused)
    double speed;
    double goalX;
    double goalY;
    double goalAngle;

    // The anchor position of each unit.
    double anchorX;
    double anchorY;
    double anchorAngle;

    // The average position of each unit
    double averageX;
    double averageY;

    // Collision attributes
    protected double[][] boundingBox;
    protected boolean[][] inDanger;

    // Patience until fighting
    // When two model.units contact each other, there will be a delay of certain time steps until the unit lose "patience" and
    // is forced to switch to fighting mode. This will be a clock that counts down until it happens.
    protected int currUnitPatience;
    protected BaseUnit unitFoughtAgainst;
    protected boolean inContactWithEnemy;
    protected boolean isTurning;

    /**
     * Initialize BaseUnit
     */
    public BaseUnit(UnitStats inputUnitStats) {
        boundingBox = new double[6][2];
        inContactWithEnemy = false;
        unitStats = inputUnitStats;
    }

    /**
     * Move the unit to a particular location, but with their formation kept
     * @param xGoal x-coordinate of the position to go to
     * @param yGoal y-coordinate of the position to go to
     * @param angleGoal angle of the unit at the final position
     */
    public void moveFormationKeptTo(double xGoal, double yGoal, double angleGoal) {

        // Set the goals
        goalX = xGoal;
        goalY = yGoal;
        goalAngle = angleGoal;

        // Change unit state to moving, reset patience and ignore unit fought against
        state = UnitState.MOVING;
        currUnitPatience = unitStats.patience; // Reset patience.
        unitFoughtAgainst = null;

        // Reorder the troops if the turn is bigger than 90 degree
        // Essentially the last row of the block will become the new front row.
        double moveAngle = Math.atan2(goalY - anchorY, goalX - anchorX);
        if (Math.abs(MathUtils.signedAngleDifference(anchorAngle, moveAngle)) > UniversalConstants.NON_UTURN_ANGLE) {
            uTurnFormation();
            // Shift the anchorX,Y to the last row
            double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
            double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
            anchorX += depth * unitStats.spacing * downUnitX;
            anchorY += depth * unitStats.spacing * downUnitY;
            // Flip the angle
            anchorAngle += Math.PI;
        }

        if (this instanceof ArcherUnit) {
            ((ArcherUnit) this).generatePositionalVariation(troops.size());
        }

        // Decision has a random delay until it reaches the soldiers ear.
        for (int i = 0; i < troops.size(); i++) {
            // Set the goal and change the state
            BaseSingle troop = troops.get(i);
            troop.setDecisionDelay(MathUtils.randint(2, 20));
        }
    }

    /**
     * Attack another unit.
     * @param unit the unit to be attacked
     */
    public void attackUnit(BaseUnit unit) {

        // Unit fought against = unit.
        unitFoughtAgainst = unit;
        state = UnitState.FIGHTING;
    }
    /**
     * Move the unit to a particular location at an angle
     * @param xGoal x-coordinate of the position to go to
     * @param yGoal y-coordinate of the position to go to
     * @param angleGoal angle of the unit at the final position
     */
    public void moveNoFormationTo(double xGoal, double yGoal, double angleGoal) {

        // Set the goals
        goalX = xGoal;
        goalY = yGoal;
        goalAngle = angleGoal;

        // Convert angle to unit vector
        double downUnitX = MathUtils.quickCos((float) (angleGoal + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (angleGoal + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (angleGoal + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (angleGoal + Math.PI / 2));

        // Sort distances of each soldier to the final point
        for (BaseSingle single : troops) {
            single.setTempSquaredDistanceFromGoal((single.getX() - xGoal) * (single.getX() - xGoal) +
                    (single.getY() - yGoal) * (single.getY() - yGoal));
        }

        // Sort the troops by distance to the goal
        Collections.sort(troops, new Comparator<BaseSingle>() {
            @Override
            public int compare(BaseSingle o1, BaseSingle o2) {
                return Double.compare(o1.getTempSquaredDistanceFromGoal(), o2.getTempSquaredDistanceFromGoal());
            }
        });

        // Calculate final positions
        for (int i = 0; i < troops.size(); i++) {
            double xGoalSingle = goalX + (i % width) * unitStats.spacing * sideUnitX + (i / width) * unitStats.spacing * downUnitX;
            double yGoalSingle = goalY + (i % width) * unitStats.spacing * sideUnitY + (i / width) * unitStats.spacing * downUnitY;

            // Set the goal and change the state
            BaseSingle troop = troops.get(i);
            troop.setxGoal(xGoalSingle);
            troop.setyGoal(yGoalSingle);
            troop.switchState(SingleState.MOVING);
            troop.setSpeedGoal(troops.get(i).getSpeedStat());
            troop.setAngleGoal(angleGoal);
            troop.setDecisionDelay(MathUtils.randint(2, 20));
        }
    }

    /**
     * Perform formation uturn. Formation uturn ensures that even when the unit runs backward, it still keeps the
     * formation that satisfies formation check in the unit test. An example of the uturn is as below.
     * Before:                    | After:
     * o o o o o o o o o o o o o  | - - - - - - - - - - - - -
     * o o o o o o o o o o o o o  | - - - - - - - - - - - - -
     * - - - o o o o o - - - - -  | - - - o o o o o - - - - -
     * - - - - - - - - - - - - -  | o o o o o o o o o o o o o
     * - - - - - - - - - - - - -  | o o o o o o o o o o o o o
     */
    public void uTurnFormation() {
        // First, move all troops down column wise
        int index;
        int newIndex;
        int oldIndex;
        for (int j = 0; j < width; j ++) {
            for (int i = 0; i < depth; i++) {
                index = i * width + j;
                if (troops.get(index).getState() == SingleState.DEAD) {
                    int num_steps = depth - i;
                    for (int rev = depth - 1; rev >= num_steps; rev--) {
                        newIndex = rev * width + j;
                        oldIndex = (rev - num_steps) * width + j;
                        BaseSingle alive = troops.get(oldIndex);
                        BaseSingle dead = troops.get(newIndex);
                        troops.set(newIndex, alive);
                        troops.set(oldIndex, dead);
                        dead.setSingleIndex(oldIndex);
                        alive.setSingleIndex(newIndex);
                    }
                    break;
                }
            }
        }

        // Then reverse all the troops and there number
        Collections.reverse(troops);
        for (int i = 0; i < troops.size(); i++) {
            troops.get(i).setSingleIndex(i);
        }
    }

    /**
     * Deadmorph is the rearrangement of the unit based on the dead soldier. When a troop died in battlefield, troops
     * from behind has to move forward in certain manner to maintain the "ideal" battle composition, which maintains
     * maximum width at front rows whenever available and keep the remaining troops in the last line in the enter.
     */
    public void deadMorph(BaseSingle deadSingle) {
        // The deadSingle is supposed to be already dead. Somehow, this line still needs to be there
        // for the algorithm to work.
        // deadSingle.setState(SingleState.DEAD);
        aliveTroopsSet.remove(deadSingle);
        int index = deadSingle.getSingleIndex();
        int row;
        int col;
        while (true) {
            row = index / width;
            col = index % width;
            int behindIndex = (row + 1) * width + col;
            int besideLeft = index - 1;
            int besideRight = index + 1;
            if (behindIndex < troops.size() && troops.get(behindIndex).getState() != SingleState.DEAD) {
                // If there is someone alive behind, move that person up
                BaseSingle alive = troops.get(behindIndex);
                troops.set(index, alive);
                troops.set(behindIndex, deadSingle);
                deadSingle.setSingleIndex(alive.getSingleIndex());
                alive.setSingleIndex(index);
                index = behindIndex;
            } else if ((col > 0) && (col < width / 2) && troops.get(besideLeft).getState() != SingleState.DEAD) {
                // If there is someone alive on the left while the dead person is on the left side,
                // move the dead person out.
                BaseSingle alive = troops.get(besideLeft);
                troops.set(index, alive);
                troops.set(besideLeft, deadSingle);
                deadSingle.setSingleIndex(alive.getSingleIndex());
                alive.setSingleIndex(index);
                index = besideLeft;
            } else if ((col < width - 1) && (col >= width / 2) && troops.get(besideRight).getState() != SingleState.DEAD) {
                // If there is someone alive on the right while the dead person is on the right side,
                // move the dead person out.
                BaseSingle alive = troops.get(besideRight);
                troops.set(index, alive);
                troops.set(besideRight, deadSingle);
                deadSingle.setSingleIndex(alive.getSingleIndex());
                alive.setSingleIndex(index);
                index = besideRight;
            } else if (aliveTroopsSet.size() >= width * (row + 1) && col == 0) {
                // If there are still more people in the last row, we shall pick the next left-most person to fill in
                // the spot
                int farLeft = 0;
                for (int i = 0; i < width; i++) {
                    if (troops.get((row + 1) * width + i).getState() != SingleState.DEAD) {
                        farLeft = (row + 1) * width + i;
                        break;
                    }
                }
                BaseSingle alive = troops.get(farLeft);
                troops.set(index, alive);
                troops.set(farLeft, deadSingle);
                deadSingle.setSingleIndex(alive.getSingleIndex());
                alive.setSingleIndex(index);
                break;
            } else if (aliveTroopsSet.size() >= width * (row + 1) && col == width - 1) {
                // If there are still more people in the last row, we shall pick the next right-most person to fill
                // the spot
                int farRight = 0;
                for (int i = width - 1; i >= 0; i--) {
                    if (troops.get((row + 1) * width + i).getState() != SingleState.DEAD) {
                        farRight = (row + 1) * width + i;
                        break;
                    }
                }
                BaseSingle alive = troops.get(farRight);
                troops.set(index, alive);
                troops.set(farRight, deadSingle);
                deadSingle.setSingleIndex(alive.getSingleIndex());
                alive.setSingleIndex(index);
                break;
            } else {
                break;
            }
        }
    }

    /**
     * Get the troops from the outside of the the army. It is very likely that these troops will be attacked first.
     * Checking them first and slowly going into the inside is better for the collision system.
     */
    public Pair<Queue<BaseSingle>, boolean[][]> getBorderTroops() {
        Queue<BaseSingle> borderTroops = new LinkedList<>();
        boolean[][] inDanger = new boolean[depth][width];
        int i = 0;
        int j = 0;

        // First, scan the first line.
        BaseSingle troop;
        boolean oneLineLeft = false;
        for (j = 0; j < width; j++) {
            troop = getTroopsAtRowCol(i, j);
            if (troop.getState() != SingleState.DEAD) {
                borderTroops.add(troop);
                inDanger[i][j] = true;
            } else {
                oneLineLeft = true;
            }
        }
        if (oneLineLeft) return new Pair<>(borderTroops, inDanger);

        // Then scan downward from both sides
        int heightLeft = 0;
        int heightRight = 0;
        int rightMost = width - 1;
        BaseSingle troopLeft;
        BaseSingle troopRight;
        for (i = 1; i < depth; i++) {
            troopLeft = getTroopsAtRowCol(i, 0);
            troopRight = getTroopsAtRowCol(i, rightMost);
            if (troopLeft.getState() != SingleState.DEAD) {
                borderTroops.add(troopLeft);
                heightLeft += 1;
                inDanger[i][0] = true;
            }
            if (troopRight.getState() != SingleState.DEAD) {
                borderTroops.add(troopRight);
                heightRight += 1;
                inDanger[i][rightMost] = true;
            }
            if (troopLeft.getState() == SingleState.DEAD || troopRight.getState() == SingleState.DEAD) break;
        }

        // Last line
        int lastLine = Math.min(heightLeft, heightRight);
        if (lastLine == depth - 1) {
            for (j = 1; j < width - 1; j++) {
                inDanger[lastLine][j] = true;
                borderTroops.add(getTroopsAtRowCol(lastLine, j));
            }
        } else {
            for (j = 1; j < width - 1; j++) {
                if (getTroopsAtRowCol(lastLine + 1, j).getState() == SingleState.DEAD) {
                    inDanger[lastLine][j] = true;
                    borderTroops.add(getTroopsAtRowCol(lastLine, j));
                } else {
                    inDanger[lastLine + 1][j] = true;
                    borderTroops.add(getTroopsAtRowCol(lastLine + 1, j));
                }
            }
        }
        return new Pair<>(borderTroops, inDanger);
    }

    /**
     * Get all the troops that are roughly in the position they are supposed to be.
     * This is very helpful for troops drawer optimization.
     */
    public boolean[][] getTroopsInPosition() {
        boolean[][] inPositionMap = new boolean[depth][width];

        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                BaseSingle single = getTroopsAtRowCol(i, j);
                if (single.getState() == SingleState.DEAD) continue;
                if (getTroopsAtRowCol(i, j).isInPosition() && !inDanger[i][j]) {
                    inPositionMap[i][j] = true;
                }
            }
        }
        return inPositionMap;
    }

    /**
     * Update the intention of the unit. Intention represents how the unit desired to move forward.
     */
    @Override
    public void updateIntention() {

        // First, rotate the front line
        double distanceToGoal = MathUtils.quickRoot1((float)((anchorX - goalX) * (anchorX - goalX) + (anchorY - goalY) * (anchorY - goalY)));
        double moveAngle, moveSpeed;
        switch (state) {
            case FIGHTING:
                // TODO(sonpham): Come up with a way to change FIGHTING to IN_POSITION when comebat with a unit is over.
                if (unitFoughtAgainst.getNumAlives() == 0) {
                    state = UnitState.STANDING;
                    unitFoughtAgainst = null;
                    break;
                }
                goalAngle = MathUtils.atan2(unitFoughtAgainst.getAverageY() - averageY,
                        unitFoughtAgainst.getAverageX() - averageX);
                goalX = unitFoughtAgainst.getAverageX();
                goalY = unitFoughtAgainst.getAverageY();

                // If army still rotating, half the speed
                moveAngle = MathUtils.atan2(goalY - anchorY, goalX - anchorX);  // TODO: This is currently repeated too much
                moveSpeed = speed / 2;

                if (distanceToGoal > moveSpeed) {
                    double moveUnitX = Math.cos(moveAngle);
                    double moveUnitY = Math.sin(moveAngle);
                    anchorX += moveUnitX * moveSpeed;
                    anchorY += moveUnitY * moveSpeed;
                } else {
                    anchorX = goalX;
                    anchorY = goalY;
                }
                break;
            case MOVING:
                // If army still rotating, half the speed
                moveAngle = MathUtils.atan2(goalY - anchorY, goalX - anchorX);  // TODO: This is currently repeated too much
                if (MathUtils.equal(moveAngle, anchorAngle)) {
                    moveSpeed = speed;
                    isTurning = false;
                } else {
                    moveSpeed = speed;
                    isTurning = true;
                }

                // Rotate towards the goal
                anchorAngle = MovementUtils.rotate(anchorAngle, moveAngle, unitStats.rotationSpeed);

                if (distanceToGoal > moveSpeed) {
                    double moveUnitX = Math.cos(anchorAngle);
                    double moveUnitY = Math.sin(anchorAngle);
                    anchorX += moveUnitX * moveSpeed;
                    anchorY += moveUnitY * moveSpeed;
                } else {
                    anchorX = goalX;
                    anchorY = goalY;
                    state = UnitState.STANDING;
                }
                break;
            case STANDING:
                anchorAngle = MovementUtils.rotate(anchorAngle, goalAngle, unitStats.rotationSpeed);
                isTurning = false;
        }

        // Update goal positions
        updateGoalPositions();

        // Update troop intentions
        for (BaseSingle single : troops) {
            single.updateIntention();
        }
    }

    /**
     * Update goal positions for base unit.
     * This can be overriden if the unit has a different formation than usual
     */
    public void updateGoalPositions() {
        // Convert angle to unit vector
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        // Create troops and set starting positions for each troop
        double topX = anchorX - (width - 1) * unitStats.spacing * sideUnitX / 2;
        double topY = anchorY - (width - 1) * unitStats.spacing * sideUnitY / 2;

        // Update troops goal positions
        for (int i = 0; i < troops.size(); i++) {
            double xGoalSingle = topX + (i % width) * unitStats.spacing * sideUnitX
                    + (i / width) * unitStats.spacing * downUnitX;
            double yGoalSingle = topY + (i % width) * unitStats.spacing * sideUnitY
                    + (i / width) * unitStats.spacing * downUnitY;

            // Set the goal and change the state
            BaseSingle troop = troops.get(i);
            troop.setxGoal(xGoalSingle);
            troop.setyGoal(yGoalSingle);
            troop.setAngleGoal(anchorAngle);
        }
    }

    /**
     * Update the state of the unit. This include updating the state of all individual troops as well as recalculate
     * the average position and the bounding box.
     */
    @Override
    public void updateState() {

        // Update the state of each single and the average position
        double sumX = 0;
        double sumY = 0;
        int count = 0;
        for (BaseSingle single : aliveTroopsSet) {
            single.updateState();
            sumX += single.getX();
            sumY += single.getY();
            count += 1;
        }
        averageX = sumX / count;
        averageY = sumY / count;

        // Update the bounding box.
        // Convert angle to unit vector
        updateBoundingBox();
    }

    /**
     * Update the bounding box of the unit.
     */
    private void updateBoundingBox() {
        // TODO(sonpham): Optimize this. There is an amazing amount of repetition in this code.
        double downUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (anchorAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (anchorAngle + Math.PI / 2));

        double aliveHeight = aliveTroopsSet.size() * 1.0 / width * unitStats.spacing;
        double fullToAliveRatio = 1.0 * troops.size() / aliveTroopsSet.size() ;

        double topLeftX = averageX - downUnitX * aliveHeight / 2 - sideUnitX * unitStats.spacing * width / 2;
        double topLeftY = averageY - downUnitY * aliveHeight / 2 - sideUnitY * unitStats.spacing * width / 2;

        double topRightX = averageX - downUnitX * aliveHeight / 2 + sideUnitX * unitStats.spacing * width / 2;
        double topRightY = averageY - downUnitY * aliveHeight / 2 + sideUnitY * unitStats.spacing * width / 2;

        double healthBotLeftX = averageX + downUnitX * aliveHeight / 2 - sideUnitX * unitStats.spacing * width / 2;
        double healthBotLeftY = averageY + downUnitY * aliveHeight / 2 - sideUnitY * unitStats.spacing * width / 2;

        double healthBotRightX = averageX + downUnitX * aliveHeight / 2 + sideUnitX * unitStats.spacing * width / 2;
        double healthBotRightY = averageY + downUnitY * aliveHeight / 2 + sideUnitY * unitStats.spacing *width / 2;

        double botLeftX = (healthBotLeftX - topLeftX) * fullToAliveRatio + topLeftX;
        double botLeftY = (healthBotLeftY - topLeftY) * fullToAliveRatio + topLeftY;

        double botRightX = (healthBotRightX - topRightX) * fullToAliveRatio + topRightX;
        double botRightY = (healthBotRightY - topRightY) * fullToAliveRatio + topRightY;

        boundingBox[0][0] = topLeftX;        boundingBox[0][1] = topLeftY;
        boundingBox[1][0] = topRightX;       boundingBox[1][1] = topRightY;
        boundingBox[2][0] = healthBotRightX; boundingBox[2][1] = healthBotRightY;
        boundingBox[3][0] = healthBotLeftX;  boundingBox[3][1] = healthBotLeftY;
        boundingBox[4][0] = botRightX;       boundingBox[4][1] = botRightY;
        boundingBox[5][0] = botLeftX;        boundingBox[5][1] = botLeftY;
    }

    /**
     * Update the bounding box of the unit.
     */
    public double[][] getGoalBoundingBox() {
        // TODO(sonpham): Combine with the bounding box code to make it more modular. This is repetitive.
        double[][] boundingBox = new double[4][2];
        double downUnitX = MathUtils.quickCos((float) (goalAngle + Math.PI));
        double downUnitY = MathUtils.quickSin((float) (goalAngle + Math.PI));
        double sideUnitX = MathUtils.quickCos((float) (goalAngle + Math.PI / 2));
        double sideUnitY = MathUtils.quickSin((float) (goalAngle + Math.PI / 2));

        double aliveHeight = aliveTroopsSet.size() * 1.0 / width * unitStats.spacing;

        double topLeftX = goalX - sideUnitX * unitStats.spacing * width / 2;
        double topLeftY = goalY - sideUnitY * unitStats.spacing * width / 2;

        double topRightX = goalX + sideUnitX * unitStats.spacing * width / 2;
        double topRightY = goalY + sideUnitY * unitStats.spacing * width / 2;

        double botLeftX = goalX + downUnitX * aliveHeight - sideUnitX * unitStats.spacing * width / 2;
        double botLeftY = goalY + downUnitY * aliveHeight - sideUnitY * unitStats.spacing * width / 2;

        double botRightX = goalX + downUnitX * aliveHeight + sideUnitX * unitStats.spacing * width / 2;
        double botRightY = goalY + downUnitY * aliveHeight + sideUnitY * unitStats.spacing * width / 2;

        boundingBox[0][0] = topLeftX;  boundingBox[0][1] = topLeftY;
        boundingBox[1][0] = topRightX; boundingBox[1][1] = topRightY;
        boundingBox[2][0] = botRightX; boundingBox[2][1] = botRightY;
        boundingBox[3][0] = botLeftX;  boundingBox[3][1] = botLeftY;

        return boundingBox;
    }

    /***
     * Set inDanger view.map for the unit and all the troops inside the unit.
     * @param inDanger
     */
    public void setInDanger(boolean[][] inDanger) {
        this.inDanger = inDanger;
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                getTroopsAtRowCol(i, j).setInDanger(this.inDanger[i][j]);
            }
        }
    }


    /**
     * A convenient way to get troop at specific position.
     */
    public BaseSingle getTroopsAtRowCol(int i, int j) {
        return troops.get(i * width + j);
    }

    /**
     * Get number of troops alive
     */
    public int getNumAlives() {
        return aliveTroopsSet.size();
    }

    public double getAnchorX() {
        return anchorX;
    }
    public void setAnchorX(double anchorX) {
        this.anchorX = anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }
    public void setAnchorY(double anchorY) {
        this.anchorY = anchorY;
    }

    public double getGoalX() {
        return goalX;
    }
    public double getGoalY() {
        return goalY;
    }
    public double getGoalAngle() {
        return goalAngle;
    }

    public double getAnchorAngle() {
        return anchorAngle;
    }
    public void setAnchorAngle(double anchorAngle) {
        this.anchorAngle = anchorAngle;
    }

    public int getCurrUnitPatience() {
        return currUnitPatience;
    }
    public void setCurrUnitPatience(int currUnitPatience) {
        this.currUnitPatience = currUnitPatience;
    }

    public UnitState getState() {
        return state;
    }
    public void setState(UnitState state) {
        this.state = state;
    }

    public double getAverageX() {
        return averageX;
    }
    public double getAverageY() {
        return averageY;
    }

    public PoliticalFaction getPoliticalFaction() {
        return politicalFaction;
    }

    public ArrayList<BaseSingle> getTroops() {
        return troops;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public double getSpacing() {
        return unitStats.spacing;
    }

    public BaseUnit getUnitFoughtAgainst() {
        return unitFoughtAgainst;
    }
    public void setUnitFoughtAgainst(BaseUnit unitFoughtAgainst) {
        this.unitFoughtAgainst = unitFoughtAgainst;
    }

    public HashSet<BaseSingle> getAliveTroopsSet() {
        return aliveTroopsSet;
    }

    public boolean isInContactWithEnemy() {
        return inContactWithEnemy;
    }
    public void setInContactWithEnemy(boolean inContactWithEnemy) {
        this.inContactWithEnemy = inContactWithEnemy;
    }

    public double[][] getBoundingBox() {
        return boundingBox;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isTurning() {
        return isTurning;
    }

    public boolean[][] getInDanger() {
        return inDanger;
    }
}
