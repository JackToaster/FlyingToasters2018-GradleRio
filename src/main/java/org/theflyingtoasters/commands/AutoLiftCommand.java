package org.theflyingtoasters.commands;

import org.theflyingtoasters.commands.interfaces.CommandCallback;
import org.theflyingtoasters.hardware.Lift;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.UDP;

public class AutoLiftCommand extends LiftCommand {
    static final Lift.Positions defaultPos = Lift.Positions.H_SCALE;
    static final int leadTime = 1;

    private boolean left;
    public AutoLiftCommand(CommandCallback opMode, Robot robot, boolean isLeft) {
        super(opMode, robot, defaultPos);
        left = isLeft;
    }

    public void init() {
        double angle = UDP.getScaleAngle(leadTime);
        if(left) angle = -angle;

        if(angle < -3)
            pos = Lift.Positions.H_SCALE;
        else if(angle < 6)
            pos = Lift.Positions.L_SCALE;
        else
            pos = Lift.Positions.LL_SCALE;
        super.init();
    }
}
