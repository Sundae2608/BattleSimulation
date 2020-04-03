import singles.BaseSingle;
import singles.SingleState;
import units.BaseUnit;
import units.CavalryUnit;
import units.PoliticalFaction;
import units.SwordmenUnit;
import utils.MathUtils;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Test for dead morph of each unit.
 * Dead morph refers to rearranging action a unit does to rearrange its formation when one person dies.
 * The rearrangement has to be rearrange in such a way that top rows will always try to max out the width, while the
 * remaining troops that don't make up a row will clump up around the center.
 * Example:
 * o o o o o o o o o o o o o o o o o o |
 * o o o o o o o o o o o o o o o o o o | First 3 rows max out
 * o o o o o o o o o o o o o o o o o o |
 *           o o o o o o o o o         | Last row clump up in the center.
 */
public class DeadMorphTest {

    private final static int UNIT_SIZE = 80;
    private final static int UNIT_WIDTH = 20;
    public static void main(String[] args) {
        BaseUnit unit = new CavalryUnit(0, 0, 0, UNIT_SIZE, PoliticalFaction.ROME, UNIT_WIDTH);
        ArrayList<BaseSingle> troops = unit.getTroops();
        ArrayList<BaseSingle> randomList = new ArrayList<>();
        for (int i = 0; i < UNIT_SIZE; i += 1) {
            randomList.add(troops.get(i));
        }
        Collections.shuffle(randomList);
        for (int i = 0; i < UNIT_SIZE; i += 1) {
            // Randomly kill 1 troop and perform dead morph
            BaseSingle random = randomList.get(i);
            int deadPosition = random.getSingleIndex();
            random.receiveDamage(Integer.MAX_VALUE);
            unit.deadMorph(random);
            printUnit(unit, deadPosition);
            System.out.println(TestUtils.checkFormation(unit));
            unit.uTurnFormation();
            System.out.println(TestUtils.checkFormation(unit));
        }
    }

    private static void printUnit(BaseUnit unit, int deadIndex) {
        ArrayList<BaseSingle> troops = unit.getTroops();
        HashSet<BaseSingle> troopSet = unit.getAliveTroopsSet();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < troops.size(); i++) {
            if (i == deadIndex) {
                s.append("X ");
            } else if (!troopSet.contains(troops.get(i))) {
                s.append("  ");
            } else {
                s.append("o ");
            }
            // Print at the end of row
            if (i % unit.getWidth() == unit.getWidth() - 1) {
                System.out.println(s);
                s.setLength(0);
            }
        }
    }


}
