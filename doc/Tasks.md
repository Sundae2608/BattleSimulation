# Tasks

These are the tasks that have to be done too improve the game

## High priorities
* Determine a good system of constants. Eventually, most constants should be local to the unit.
* Create the fighting stance for the unit
    * This goes with the fighting system.
* Camera system. This is critical if any documentary is to be successful. A freewheeling camera allows watching the
battle from different angle and also a lighter rendering of the army.
* Mouse control of all units in the army.
* Dead soldier should stack on top of each other instead of 

## Medium priorities
* Add weights into the system.
    * Heavy infantry should push light infantry signifantly
    * Cavalry should push infantry significantly

## Experimental
* Somehow makes ally push affect less than enemy push. There is still no good way around this.
* Handle the acceleration on a unit-based manner. We currently have a universal constant for acceleration, which will
not work for cavalry unit since it is mostly tested on infantry.

## Issues
* The drawer draw all dead body before drawer the live body.
* The soldier dies immediately. This is not real. We should probably have some death animation.
* The game should be faster as time moves on.