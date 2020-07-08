package model.units.unit_stats;

import model.enums.PoliticalFaction;
import model.enums.UnitType;

public class UnitStats {
    public PoliticalFaction faction;
    public UnitType unitType;
    public double spacing;
    public double speed;
    public double rotationSpeed;
    public int patience;
    public double stamina; // presumably, a unit decreases stamina when it moves, increases when it stops; this parameter
                        // can be used in other scenarios.
    public double maxStamina;
    public double minStamina;
    public double staminaDepletionRate;
    public double staminaRecoveryRate;

    // Archer & skirmisher stats
    public double widthVariation;
    public double depthVariation;

    // Phalanx stats
    public int numFirstRows;
    public double offAngleFirstRow;
}