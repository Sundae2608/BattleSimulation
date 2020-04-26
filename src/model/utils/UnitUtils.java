package model.utils;

import model.enums.PoliticalFaction;
import model.units.*;

public final class UnitUtils {

    /**
     * Get unit type
     */
    public static String getUnitName(BaseUnit unit) {
        return getFactionParticular(unit.getPoliticalFaction()) + " " + getUnitType(unit);

    }

    /**
     * Generic name of the unit type
     */
    private static String getUnitType(BaseUnit unit) {
        PoliticalFaction faction = unit.getPoliticalFaction();
        if (unit instanceof SwordmenUnit) {
            switch (faction) {
                case ROME: return "Legionary Cohort";
                case GAUL: return "Oathsworn";
                default: return "Swordmen";
            }
        } else if (unit instanceof ArcherUnit) {
            switch (faction) {
                default: return "Archers";
            }
        } else if (unit instanceof CavalryUnit) {
            switch (faction) {
                case ROME: return "Equite";
                default: return "Cavalry";
            }
        } else if (unit instanceof PhalanxUnit) {
            switch (faction) {
                case THEBES: return "Sacred Band";
                case ATHENS:
                case SPARTA: return "Hoplites";
                default: return "Cavalry";
            }
        } else if (unit instanceof SkirmisherUnit) {
            switch (faction) {
                case ROME: return "Velites";
                default: return "Skirmishers";
            }
        } else if (unit instanceof SlingerUnit) {
            switch (faction) {
                default: return "Slingers";
            }
        } else {
            return "soldiers";
        }
    }

    /**
     * Get faction particulars
     */
    private static String getFactionParticular(PoliticalFaction faction) {
        switch (faction) {
            case ROME:
                return "Roman";
            case GAUL:
                return "Gallic";
            case ATHENS:
                return "Athenian";
            case SPARTA:
                return "Spartan";
            case THEBES:
                return "Theban";
            default:
                return "Mercenary";
        }
    }
}
