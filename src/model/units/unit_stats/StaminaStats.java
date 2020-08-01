package model.units.unit_stats;

import model.enums.UnitState;

public class StaminaStats {

    public double maxStamina = 1;

    public double staminaStandingChangeRate = 0.0003;
    public double staminaDeceleratingChangeRate = -0.0001;
    public double staminaRoutingChangeRate = -0.0002;
    public double staminaMovingChangeRate = -0.0003;
    public double staminaFightingChangeRate = -0.0005;

    public double getStaminaChangeRate(UnitState state) {
        switch (state) {
            case STANDING:
                return staminaStandingChangeRate;
            case DECELERATING:
                return staminaDeceleratingChangeRate;
            case ROUTING:
                return staminaRoutingChangeRate;
            case FIGHTING:
                return staminaFightingChangeRate;
            case MOVING:
                return staminaMovingChangeRate;
            default:
                throw new IllegalArgumentException();
        }
    }
}
