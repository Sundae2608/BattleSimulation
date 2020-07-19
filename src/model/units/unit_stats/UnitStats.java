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

    public StaminaStats staminaStats = new StaminaStats();

    // Archer & skirmisher stats
    public double widthVariation;
    public double depthVariation;

    // Phalanx stats
    public int numFirstRows;
    public double offAngleFirstRow;
}