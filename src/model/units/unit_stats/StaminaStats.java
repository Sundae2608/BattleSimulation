package model.units.unit_stats;

import model.enums.UnitState;

public class StaminaStats {

    public double maxStamina = 100;

    public double staminaStandingChangeRate = 0.03;
    public double staminaDeceleratingChangeRate = -0.01;
    public double staminaRoutingChangeRate = -0.02;
    public double staminaMovingChangeRate = -0.03;
    public double staminaFightingChangeRate = -0.05;

    public double getStaminaChangeRate(UnitState state) {
        return switch (state) {
            case STANDING -> staminaStandingChangeRate;
            case DECELERATING -> staminaDeceleratingChangeRate;
            case ROUTING -> staminaRoutingChangeRate;
            case FIGHTING -> staminaFightingChangeRate;
            case MOVING -> staminaMovingChangeRate;
        };
    }
}
