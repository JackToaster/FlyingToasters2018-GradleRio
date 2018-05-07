package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.IntakeCommand;
import org.theflyingtoasters.commands.LiftCommand;
import org.theflyingtoasters.commands.MotionProfileCommand;
import org.theflyingtoasters.commands.interfaces.Command;
import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.hardware.Lift;
import org.theflyingtoasters.hardware.Intake.State;
import org.theflyingtoasters.path_generation.Point;
import org.theflyingtoasters.path_generation.Waypoint;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

/**
 * An autonomous mode which automatically drives to the correct side of the
 * switch based on the game data.
 * 
 * @author jack
 *
 */
public class SwitchAuton extends OpMode {
	// meters from wall to switch
	final static double switch_dist = 2.4;

	final static double switch_left = 2.0;
	final static double switch_right = -1.0;

	Waypoint start = new Waypoint(new Point(0, 0), 0);
	Waypoint end;

	MotionProfileCommand motionProfileCmd;

	LiftCommand flip;
	
	public SwitchAuton(Robot bot, String gameData) {
		super(bot, "Motion Profile Auton");
		Logging.h(gameData);
		if (gameData.charAt(0) == 'L') {
			end = new Waypoint(new Point(switch_dist, switch_left), 0.0);
		} else {
			end = new Waypoint(new Point(switch_dist, switch_right), 0.0);
		}
		motionProfileCmd = new MotionProfileCommand(this, robot, "drive to switch", false, MotionProfileCommand.Speed.MED_LOW_ACCEL, start, end);
		flip = new LiftCommand(this, bot, Lift.Positions.STARTING_FLIP);
	}

	public void init() {
		Logging.h("Init run!");

		super.init();
		addCommand(motionProfileCmd);
	}

	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
		Logging.l("Left pos: " + robot.driveBase.left.getPosition() + ", right pos: "
				+ robot.driveBase.right.getPosition());
	}

	public void stop() {
		robot.driveBase.setFeedbackActive(false);
	}

	public void commandFinished(Command cmd) {
		//Add the intake command to output the cube.
		if (cmd == motionProfileCmd) {
			addCommand(flip);
		}else if(cmd == flip){
			addCommand(new IntakeCommand(this, robot, State.OUTPUTTING));
		}
		super.commandFinished(cmd);
	}
}
