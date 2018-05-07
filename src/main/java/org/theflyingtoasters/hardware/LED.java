package org.theflyingtoasters.hardware;

import org.theflyingtoasters.hardware.Intake.State;
import org.theflyingtoasters.hardware.Lift.Positions;
import org.theflyingtoasters.robot.Robot;

import edu.wpi.first.wpilibj.Spark;

public class LED {
	Spark revBlinkin;

	public enum LightCode {
		DEFALT(0.55), TALL_PATTERN(0.41), RAMPS_PATTERN(-0.79), CUBE_PATTERN(0.39);

		double outVal;

		LightCode(double val) {
			outVal = val;
		}
	}

	public LED(int pwmPort) {
		revBlinkin = new Spark(pwmPort);
	}

	public void setPattern(LightCode code) {
		revBlinkin.set(code.outVal);
	}

	public void updateLightsToRobotState(Robot bot) {
		if (bot.lift.currentPos == Positions.L_SCALE || bot.lift.currentPos == Positions.H_SCALE) {
			setPattern(LightCode.TALL_PATTERN);
		} else if (bot.intake.getState() == State.HAS_CUBE || bot.intake.getState() == State.RESTING_WITH_CUBE) {
			setPattern(LightCode.CUBE_PATTERN);
		} else {
			setPattern(LightCode.DEFALT);

		}
	}
}
