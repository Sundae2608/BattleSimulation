package units;

import constants.*;

public enum UnitEnum {
    ARCHER, CAVALRY, HORSE_ARCHER, PHALANX, SKIRMISHER, SLINGER, SWORDMAN;

    public double getSpacing() {
        switch (this) {
            case HORSE_ARCHER:
                return HorseArcherConstants.SPACING;
            case SKIRMISHER:
                return SkirmisherConstants.SPACING;
            case SWORDMAN:
                return SwordmanConstants.SPACING;
            case SLINGER:
                return SlingerConstants.SPACING;
            case PHALANX:
                return PhalanxConstants.SPACING;
            case CAVALRY:
                return CavalryConstants.SPACING;
            case ARCHER:
                return ArcherConstants.SPACING;
            default:
                return SwordmanConstants.SPACING;
        }
    }
}
