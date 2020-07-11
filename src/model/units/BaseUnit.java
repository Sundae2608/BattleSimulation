package model.units;

import model.constants.GameplayConstants;
import model.constants.UniversalConstants;
import javafx.util.Pair;
import model.enums.PoliticalFaction;
import model.enums.UnitState;
import model.events.EventBroadcaster;
import model.singles.BaseSingle;
import model.enums.SingleState;
import model.terrain.Terrain;
import model.units.unit_stats.UnitStats;
import model.utils.GameplayUtils;
import model.utils.MathUtils;
import model.utils.MovementUtils;

import java.util.*;

public class BaseUnit {

    // Troops and width
    ArrayList<BaseSingle> troops;
    HashMap<BaseSingle, Integer> aliveTroopsMap;
    BaseSingle[][] aliveTroopsFormation;
    int width;
    int depth;

    // Political attribute
    PoliticalFaction politicalFaction;
    UnitStats unitStats;

    // Unit state
    UnitState state = UnitState.STANDING;
    double morale;
    ArrayList<BaseUnit> visibleUnits;  // List of units visible to the troops
    int timeInFightingState;
    int[] flankersCount;
    int[] frontlinePatientCounters;

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
    double averageZ;

    // Collision attributes
    protected double[][] boundingBox;
    protected boolean[][] inDanger;

    // The terrain in which the unit operates on
    Terrain terrain;

    // Patience until fighting
    // When two model.units contact each other, there will be a delay of certain time steps until the unit lose "patience" and
    // is forced to switch to fighting mode. This will be a clock that counts down until it happens.
    int currUnitPatience;
    BaseUnit unitFoughtAgainst;
    boolean inContactWithEnemy;
    boolean isTurning;

    // the "strength" of the unit, indicating how far the unit can go, or if a single in the unit can do an "action"
    // such as moving or firing
    double stamina;

    // Event broadcaster
    EventBroadcaster broadcaster;

    /**
     * Initialize BaseUnit
     */
    public BaseUnit(UnitStats inputUnitStats, Terrain inputTerrain, EventBroadcaster inputBroadcaster) {
        boundingBox = new double[6][2];
        inContactWithEnemy = false;
        unitStats = inputUnitStats;
        terrain = inputTerrain;
        broadcaster = inputBroadcaster;
        morale = GameplayConstants.BASE_MORALE;
        timeInFightingState = 0;
        stamina = inputUnitStats.maxStamina;
    }

    /**
     * Bring flankers back to their supposed position. This happens when the unit needs to re-position
     */
    private void resetFlanker() {
        for (int i = 0; i < width; i++) {
            frontlinePatientCounters[i] = 0;
            flankersCount[i] = 0;
        }
    }

    /**
     * Swap the troop between two positions (row1, col1) and (row2, col2)
     */
    private void swapTwoTroopPositions(int row1, int col1, int row2, int col2) {
        BaseSingle temp = aliveTroopsFormation[row1][col1];
        aliveTroopsFormation[row1][col1] = aliveTroopsFormation[row2][col2];
        aliveTroopsFormation[row2][col2] = temp;
        if (aliveTroopsFormation[row1][col1] != null) {
            aliveTroopsMap.put(aliveTroopsFormation[row1][col1], row1 * width + col1);
        }
        if (aliveTroopsFormation[row2][col2] != null) {
            aliveTroopsMap.put(aliveTroopsFormation[row2][col2], row2 * width + col2);
        }
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

        // Reset anchor angles
        anchorAngle = MathUtils.atan2(yGoal - averageY, xGoal - averageX);
        anchorX = averageX + MathUtils.quickCos((float) anchorAngle) * depth * unitStats.spacing / 2;
        anchorY = averageY + MathUtils.quickSin((float) anchorAngle) * depth * unitStats.spacing / 2;
        reorderTroops(anchorX, anchorY, anchorAngle);

        // Decision has a random delay until it reaches the soldiers ear.
        for (int i = 0; i < troops.size(); i++) {
            // Set the goal and change the state
            BaseSingle troop = troops.get(i);
            troop.setDecisionDelay(MathUtils.randint(2, 20));
        }
    }

    public void changeFrontlineWidth(int newWidth) {
        // TODO: Change this to minimum width of the frontline, probably 4. Put it in GameplayConstants
        if (newWidth <= 0 || newWidth > getNumAlives()) {
            // Frontline can't be changed if the width or is too wide.
            return;
        }

        // Reset the width and depth
        width = newWidth;
        depth = (int) Math.ceil(1.0 * troops.size() / width);

        // Change the troops formation
        BaseSingle[][] newFormation = new BaseSingle[depth][width];
        ArrayList<BaseSingle> aliveArray = new ArrayList<>(aliveTroopsMap.keySet());
        MathUtils.sortSinglesByAngle(aliveArray, anchorAngle + Math.PI);

        // Take each row, sort each row from furthest leftward to furthest rightward, and then put them into the correct
        // position in the array
        int numRows = (int) Math.ceil(1.0 * aliveArray.size() / width);
        for (int row = 0; row < numRows; row++) {
            // Sort the row from leftward to right ward
            int beginIndex = row * width;
            int endIndex = Math.min(aliveArray.size(), (row + 1) * width);
            ArrayList<BaseSingle> rowTroops = new ArrayList<>();
            for (int col = beginIndex; col < endIndex; col++) {
                rowTroops.add(aliveArray.get(col));
            }
            MathUtils.sortSinglesByAngle(rowTroops, anchorAngle - MathUtils.PIO2);

            // Assign the troops to each row and reset their index
            int offsetFromLeft;
            if (row < numRows - 1) {
                offsetFromLeft = 0;
            } else {
                int numLastRow = getNumAlives() - (width * (numRows - 1));
                offsetFromLeft = (width - numLastRow) / 2;
            }
            for (int j = 0; j < rowTroops.size(); j++) {
                newFormation[row][offsetFromLeft + j] = rowTroops.get(j);
                aliveTroopsMap.put(rowTroops.get(j), row * width + offsetFromLeft + j);
            }
        }
        aliveTroopsFormation = newFormation;

        // Reset the flankers
        frontlinePatientCounters = new int[width];
        flankersCount = new int[width];
    }

    /**
     * Reorder a unit to a specific position. Unit might reshuffle the order so that their new formation conveniently
     * heads toward angleGoal.
     * @param xGoal x-coordinate of the position to go to
     * @param yGoal y-coordinate of the position to go to
     * @param angleGoal angle of the unit at the final position
     */
    private void reorderTroops(double xGoal, double yGoal, double angleGoal) {

        // Set the goals
        anchorX = xGoal;
        anchorY = yGoal;
        anchorAngle = angleGoal;

        // Change unit state to moving, reset patience and ignore unit fought against
        state = UnitState.MOVING;
        currUnitPatience = unitStats.patience; // Reset patience.
        unitFoughtAgainst = null;

        // Re-order troops position.
        ArrayList<BaseSingle> aliveArray = new ArrayList<>(aliveTroopsMap.keySet());

        // First, sort position from furthest forward to furthest backward.
        MathUtils.sortSinglesByAngle(aliveArray, -angleGoal + Math.PI);

        // Take each row, sort each row from furthest leftward to furthest rightward, and then put them into the correct
        // position in the array
        int numRows = (int) Math.ceil(1.0 * aliveArray.size() / width);
        for (int row = 0; row < numRows; row++) {
            // Sort the row from leftward to right ward
            int beginIndex = row * width;
            int endIndex = Math.min(aliveArray.size(), (row + 1) * width);
            ArrayList<BaseSingle> rowTroops = new ArrayList<>();
            for (int col = beginIndex; col < endIndex; col++) {
                rowTroops.add(aliveArray.get(col));
            }
            MathUtils.sortSinglesByAngle(rowTroops, -angleGoal - MathUtils.PIO2);

            // Assign the troops to each row and reset their index
            int offsetFromLeft;
            if (row < numRows - 1) {
                offsetFromLeft = 0;
            } else {
                int numLastRow = getNumAlives() - (width * (numRows - 1));
                offsetFromLeft = (width - numLastRow) / 2;
            }
            for (int j = 0; j < rowTroops.size(); j++) {
                aliveTroopsFormation[row][offsetFromLeft + j] = rowTroops.get(j);
                aliveTroopsMap.put(rowTroops.get(j), row * width + offsetFromLeft + j);
            }
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

        // Reset the flankers
        resetFlanker();
    }

    /**
     * Regroup a unit to a specific position. Unit might reshuffle the order so that their new formation conveniently
     * heads toward angleGoal.
     * @param xGoal x-coordinate of the position to go to
     * @param yGoal y-coordinate of the position to go to
     * @param angleGoal angle of the unit at the final position
     */
    private void repositionTo(double xGoal, double yGoal, double angleGoal) {

        // Set the goals
        goalX = xGoal;
        goalY = yGoal;
        goalAngle = angleGoal;
        anchorX = xGoal;
        anchorY = yGoal;
        anchorAngle = angleGoal;

        // Change unit state to moving, reset patience and ignore unit fought against
        state = UnitState.MOVING;
        currUnitPatience = unitStats.patience; // Reset patience.
        unitFoughtAgainst = null;

        // Re-order troops position.
        ArrayList<BaseSingle> aliveArray = new ArrayList<>(aliveTroopsMap.keySet());

        // First, sort position from furthest forward to furthest backward.
        MathUtils.sortSinglesByAngle(aliveArray, -angleGoal + Math.PI);

        // Take each row, sort each row from furthest leftward to furthest rightward, and then put them into the correct
        // position in the array
        int numRows = (int) Math.ceil(1.0 * aliveArray.size() / width);
        for (int row = 0; row < numRows; row++) {
            // Sort the row from leftward to right ward
            int beginIndex = row * width;
            int endIndex = Math.min(aliveArray.size(), (row + 1) * width);
            ArrayList<BaseSingle> rowTroops = new ArrayList<>();
            for (int col = beginIndex; col < endIndex; col++) {
                rowTroops.add(aliveArray.get(col));
            }
            MathUtils.sortSinglesByAngle(rowTroops, -angleGoal - MathUtils.PIO2);

            // Assign the troops to each row and reset their index
            int offsetFromLeft;
            if (row < numRows - 1) {
                offsetFromLeft = 0;
            } else {
                int numLastRow = getNumAlives() - (width * (numRows - 1));
                offsetFromLeft = (width - numLastRow) / 2;
            }
            for (int j = 0; j < rowTroops.size(); j++) {
                aliveTroopsFormation[row][offsetFromLeft + j] = rowTroops.get(j);
                aliveTroopsMap.put(rowTroops.get(j), row * width + offsetFromLeft + j);
            }
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

        // Reset the flankers
        resetFlanker();
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

        // Reset the flankers
        resetFlanker();
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
        for (int j = 0; j < width; j ++) {
            for (int i = 0; i < depth; i++) {
                if (aliveTroopsFormation[i][j] == null) {
                    int numSteps = depth - i;
                    for (int rev = depth - 1; rev >= numSteps; rev--) {
                        swapTwoTroopPositions( rev - numSteps, j, rev, j);
                    }
                    break;
                }
            }
        }

        // Then reverse the order
        for (int index = 0; index < width * depth / 2; index++) {
            int row = index / width;
            int col = index % width;
            swapTwoTroopPositions(row, col, depth - row - 1, width - col - 1);
        }

        // Reset the flankers
        resetFlanker();
    }

    /**
     * Process dead single will process the change in formation and morale when one soldier is found dead in the unit.
     * When a troop died in battlefield, the entire morale of the unit will suffer slightly, troops from behind has to
     * move forward in certain manner in order to maintain the front line.
     * @param deadSingle
     */
    public void processDeadSingle(BaseSingle deadSingle) {
        // Introduce a morale loss.
        morale -= GameplayUtils.moraleLossDueToDeadSoldier(aliveTroopsMap.size());

        // Change the formation of the unit to maintain the frontline.
        int index = aliveTroopsMap.get(deadSingle);
        aliveTroopsMap.remove(deadSingle);
        int row = index / width;
        int col = index % width;
        aliveTroopsFormation[row][col] = null;

        while (true) {
            if (row < aliveTroopsFormation.length - 1 && aliveTroopsFormation[row + 1][col] != null) {
                // If there is someone alive behind, move that person up
                swapTwoTroopPositions(row, col, row + 1, col);
                row += 1;
            } else if ((col > 0) && (col < width / 2) && aliveTroopsFormation[row][col - 1] != null) {
                swapTwoTroopPositions(row, col, row, col - 1);
                col -= 1;
            } else if ((col < width - 1) && (col >= width / 2) && aliveTroopsFormation[row][col + 1] != null) {
                swapTwoTroopPositions(row, col, row, col + 1);
                col += 1;
            } else if (aliveTroopsMap.size() >= width * (row + 1) && col == 0) {
                // If there are still more people in the last row, we shall pick the next left-most person to fill in
                // the spot
                int farLeftCol = 0;
                for (int i = 0; i < width; i++) {
                    if (aliveTroopsFormation[row + 1][i] != null) {
                        farLeftCol = i;
                        break;
                    }
                }
                swapTwoTroopPositions(row, col, row + 1, farLeftCol);
                break;
            } else if (aliveTroopsMap.size() >= width * (row + 1) && col == width - 1) {
                // If there are still more people in the last row, we shall pick the next right-most person to fill in
                // the spot
                int farRightCol = 0;
                for (int i = width - 1; i >= 0; i--) {
                    if (aliveTroopsFormation[row + 1][i] != null) {
                        farRightCol = i;
                        break;
                    }
                }
                swapTwoTroopPositions(row, col, row + 1, farRightCol);
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
            troop = aliveTroopsFormation[i][j];
            if (troop != null) {
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
        BaseSingle troopLeft;
        BaseSingle troopRight;
        for (i = 1; i < depth; i++) {
            troopLeft = aliveTroopsFormation[i][0];
            troopRight = aliveTroopsFormation[i][width - 1];
            if (troopLeft != null) {
                borderTroops.add(troopLeft);
                heightLeft += 1;
                inDanger[i][0] = true;
            }
            if (troopRight != null) {
                borderTroops.add(troopRight);
                heightRight += 1;
                inDanger[i][width - 1] = true;
            }
            if (troopLeft == null || troopRight == null) break;
        }

        // Last line
        int lastLine = Math.min(heightLeft, heightRight);
        if (lastLine == depth - 1) {
            for (j = 1; j < width - 1; j++) {
                inDanger[lastLine][j] = true;
                borderTroops.add(aliveTroopsFormation[lastLine][j]);
            }
        } else {
            for (j = 1; j < width - 1; j++) {
                if (aliveTroopsFormation[lastLine + 1][j] == null) {
                    inDanger[lastLine][j] = true;
                    borderTroops.add(aliveTroopsFormation[lastLine][j]);
                } else {
                    inDanger[lastLine + 1][j] = true;
                    borderTroops.add(aliveTroopsFormation[lastLine + 1][j]);
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
                if (aliveTroopsFormation[i][j] == null) continue;
                if (aliveTroopsFormation[i][j].isInPosition() && !inDanger[i][j]) {
                    inPositionMap[i][j] = true;
                }
            }
        }
        return inPositionMap;
    }

    /**
     * Update the intention of the unit. Intention represents how the unit desired to move forward.
     */
    public void updateIntention() {

        // First, rotate the front line
        double distanceToGoal = MathUtils.quickRoot1((float)((anchorX - goalX) * (anchorX - goalX) + (anchorY - goalY) * (anchorY - goalY)));
        double moveAngle, moveSpeed;
        double moveSpeedX, moveSpeedY;
        double speedModifier;
        double[] deltaVel;
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

                // Apply speed modifier by terrain
                moveSpeedX = Math.cos(moveAngle) * moveSpeed;
                moveSpeedY = Math.sin(moveAngle) * moveSpeed;
                deltaVel = terrain.getDeltaVelFromPos(anchorX, anchorY);
                speedModifier = MathUtils.ratioProjection(deltaVel[0], deltaVel[1], moveSpeedX, moveSpeedY);
                speedModifier = MathUtils.capMinMax(speedModifier,
                        UniversalConstants.MINIMUM_TERRAIN_EFFECT,
                        UniversalConstants.MAXIMUM_TERRAIN_EFFECT);
                moveSpeed *= (1 + speedModifier);
                if (distanceToGoal > moveSpeed) {
                    double moveUnitX = Math.cos(moveAngle);
                    double moveUnitY = Math.sin(moveAngle);
                    anchorX += moveUnitX * moveSpeed;
                    anchorY += moveUnitY * moveSpeed;
                } else {
                    anchorX = goalX;
                    anchorY = goalY;
                }

                // Update flanker status. If the flank has not engaged with the enemy for a long time, they will join
                // the flanker, which will have a different goal position.
                for (int i = 0; i < width; i++) {
                    if (flankersCount[i] < troops.size() / width && aliveTroopsFormation[flankersCount[i]][i] != null) {
                        if (aliveTroopsFormation[0][i].getCombatDelay() < 0) {
                            frontlinePatientCounters[i] += 1;
                        } else {
                            frontlinePatientCounters[i] = 0;
                        }
                    }
                    if (frontlinePatientCounters[i] == GameplayConstants.FLANKER_PATIENT) {
                        // If the front-liner has waited for too long, they will join the flanker.
                        flankersCount[i] += 1;
                        frontlinePatientCounters[i] = 0;
                    }
                }

                for (int i = 0; i < width; i++) {
                    if (flankersCount[i] < troops.size() / width && troops.get(i + flankersCount[i] * width).getState() != SingleState.DEAD) {
                        if (troops.get(i).getCombatDelay() < 0) {
                            frontlinePatientCounters[i] += 1;
                        } else {
                            frontlinePatientCounters[i] = 0;
                        }
                    }
                    if (frontlinePatientCounters[i] == GameplayConstants.FLANKER_PATIENT) {
                        // If the front-liner has waited for too long, they will join the flanker.
                        flankersCount[i] += 1;
                        frontlinePatientCounters[i] = 0;
                    }
                }
                break;
            case ROUTING:
                // Update the direction that the unit ought to run away from, it should be the opposite vector of
                // the sum of difference in unit location difference.
                double dx = 0;
                double dy = 0;
                for (BaseUnit unit : visibleUnits) {
                    if (unit.getPoliticalFaction() != politicalFaction) {
                        dx += unit.getAliveTroopsSet().size() * (averageX - unit.averageX);
                        dy += unit.getAliveTroopsSet().size() * (averageY - unit.averageY);
                    }
                }
                // Invert dx and dy. We need to run in the opposite direction.
                goalAngle = MathUtils.atan2(dy, dx);
                break;
            case MOVING:
                // If army still rotating, half the speed
                moveAngle = MathUtils.atan2(goalY - anchorY, goalX - anchorX);  // TODO: This is currently repeated too much
                moveSpeed = speed;

                // Apply speed modifier by terrain
                moveSpeedX = Math.cos(moveAngle) * moveSpeed;
                moveSpeedY = Math.sin(moveAngle) * moveSpeed;
                deltaVel = terrain.getDeltaVelFromPos(anchorX, anchorY);
                speedModifier = MathUtils.ratioProjection(deltaVel[0], deltaVel[1], moveSpeedX, moveSpeedY);
                speedModifier = MathUtils.capMinMax(speedModifier,
                        UniversalConstants.MINIMUM_TERRAIN_EFFECT,
                        UniversalConstants.MAXIMUM_TERRAIN_EFFECT);
                moveSpeed *= (1 + speedModifier);

                if (MathUtils.doubleEqual(moveAngle, anchorAngle)) {
                    isTurning = false;
                } else {
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
                break;
        }

        // Update goal positions
        updateGoalPositions();

        // Update troop intentions
        for (BaseSingle single : aliveTroopsMap.keySet()) {
            single.updateIntention(terrain);
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
        for (int row = 0; row < aliveTroopsFormation.length; row++) {
            for (int col = 0; col < aliveTroopsFormation[0].length; col++) {
                BaseSingle troop = aliveTroopsFormation[row][col];
                if (troop == null) continue;
                double xGoalSingle;
                double yGoalSingle;
                // If the person is the flanker, go straight to the enemy position.
                if (state == UnitState.FIGHTING) {
                    if (row < flankersCount[col]) {
                        xGoalSingle = this.unitFoughtAgainst.getAverageX();
                        yGoalSingle = this.unitFoughtAgainst.getAverageY();
                    } else {
                        xGoalSingle = topX + col * unitStats.spacing * sideUnitX
                                + (row - flankersCount[col]) * unitStats.spacing * downUnitX;
                        yGoalSingle = topY + col * unitStats.spacing * sideUnitY
                                + (row - flankersCount[col]) * unitStats.spacing * downUnitY;
                    }
                } else {
                    xGoalSingle = topX + col * unitStats.spacing * sideUnitX
                            + row * unitStats.spacing * downUnitX;
                    yGoalSingle = topY + col * unitStats.spacing * sideUnitY
                            + row * unitStats.spacing * downUnitY;
                }
                // Set the goal and change the state
                troop.setxGoal(xGoalSingle);
                troop.setyGoal(yGoalSingle);
                troop.setAngleGoal(anchorAngle);
            }
        }
    }

    /**
     * Update the state of the unit. This include updating the state of all individual troops as well as recalculate
     * the average position and the bounding box.
     */
    public void updateState() {

        // After each frame, the unit has a slight morale recovery, but only up to the full base morale.
        morale = Math.min(morale + GameplayConstants.MORALE_RECOVERY, GameplayConstants.BASE_MORALE);

        if (morale < GameplayConstants.PANIC_MORALE) {
            state = UnitState.ROUTING;
            for (BaseSingle single : aliveTroopsMap.keySet()) {
                single.switchState(SingleState.ROUTING);
            }
        } else if (state == UnitState.ROUTING && morale > GameplayConstants.RECOVER_MORALE) {
            this.repositionTo(averageX, averageY, goalAngle);
            for (BaseSingle single : aliveTroopsMap.keySet()) {
                single.switchState(SingleState.MOVING);
            }
        }


        // Update the state of each single and the average position
        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;
        int count = 0;
        for (BaseSingle single : aliveTroopsMap.keySet()) {
            single.updateState();
            sumX += single.getX();
            sumY += single.getY();
            sumZ += terrain.getHeightFromPos(single.getX(), single.getY());
            count += 1;
        }
        averageX = sumX / count;
        averageY = sumY / count;
        averageZ = sumZ / count;

        // Update the bounding box.
        updateBoundingBox();

        // Update stamina
        updateStamina();
    }

    private void updateStamina() {
        // Stamina is depleted when the unit is in combat and is recovered if doing nothing
        // Todo: Make it work for different actions and/or surfaces
        if (state == UnitState.STANDING) {
            stamina = Math.min(stamina + stamina * unitStats.staminaRecoveryRate, unitStats.maxStamina);
        } else {
            stamina = Math.max(stamina - stamina * unitStats.staminaDepletionRate, 0);
        }
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

        double aliveHeight = aliveTroopsMap.size() * 1.0 / width * unitStats.spacing;
        double fullToAliveRatio = 1.0 * troops.size() / aliveTroopsMap.size() ;

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

        double aliveHeight = aliveTroopsMap.size() * 1.0 / width * unitStats.spacing;

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
        return aliveTroopsFormation[i][j];
    }

    /**
     * Get the index of troops
     */
    public int getTroopIndex(BaseSingle single) {
        return aliveTroopsMap.get(single);
    }

    /**
     * Get number of troops alive
     */
    public int getNumAlives() {
        return aliveTroopsMap.size();
    }

    public double getStamina() { return stamina; }

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

    public double getMorale() {
        return morale;
    }

    public ArrayList<BaseUnit> getVisibleUnits() {
        return visibleUnits;
    }

    public void setVisibleUnits(ArrayList<BaseUnit> visibleUnits) {
        this.visibleUnits = visibleUnits;
    }

    public double getAverageX() {
        return averageX;
    }
    public double getAverageY() {
        return averageY;
    }
    public double getAverageZ() { return averageZ; }

    public PoliticalFaction getPoliticalFaction() {
        return politicalFaction;
    }

    public ArrayList<BaseSingle> getTroops() {
        return troops;
    }

    public BaseSingle[][] getAliveTroopsFormation() {
        return aliveTroopsFormation;
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

    public Set<BaseSingle> getAliveTroopsSet() {
        return aliveTroopsMap.keySet();
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

    public UnitStats getUnitStats() {
        return unitStats;
    }
}
