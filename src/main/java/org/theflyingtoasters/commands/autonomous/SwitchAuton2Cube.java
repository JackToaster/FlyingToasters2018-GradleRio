package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.IntakeCommand;
import org.theflyingtoasters.commands.LiftCommand;
import org.theflyingtoasters.commands.MotionProfileCommand;
import org.theflyingtoasters.commands.interfaces.Command;
import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.hardware.Intake;
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
public class SwitchAuton2Cube extends OpMode {
	// meters from wall to switch
	final static double switch_dist = 2.4;

	final static double switch_left = 1.6;
	final static double switch_right = -1.0;

	final Waypoint start = new Waypoint(new Point(0, 0), 0);
	final Waypoint centered = new Waypoint(new Point(0.5, 0.432), 0);
	final Waypoint upToCube = new Waypoint(new Point(1.3, 0.432), 0);
	final Waypoint afterPickup = new Waypoint(new Point(1.7, 0.432), 0);
	final Waypoint end;

	MotionProfileCommand firstCube;
	MotionProfileCommand returnFromSwitch;
	MotionProfileCommand getCube;
	MotionProfileCommand returnFromCube;
	MotionProfileCommand secondCube;
	
	LiftCommand flip;
	LiftCommand toGround;
	LiftCommand secondLift;
	
	IntakeCommand pickupCube;
	IntakeCommand output1;
	
	public SwitchAuton2Cube(Robot bot, String gameData) {
		super(bot, "Motion Profile Auton");
		Logging.h(gameData);
		if (gameData.charAt(0) == 'L') {
			end = new Waypoint(new Point(switch_dist, switch_left), 0.0);
		} else {
			end = new Waypoint(new Point(switch_dist, switch_right), 0.0);
		}
		firstCube = new MotionProfileCommand(this, robot, "drive to switch", false,
				MotionProfileCommand.Speed.SLOW_LOW_ACCEL, start, end);
		returnFromSwitch = new MotionProfileCommand(this, robot, "Drive from switch", true, MotionProfileCommand.Speed.MED,
				end.backwards(), centered.backwards());
		getCube = new MotionProfileCommand(this, robot, "Drive to cube", false, MotionProfileCommand.Speed.MED,
				centered, upToCube);
		returnFromCube = new MotionProfileCommand(this, robot, "Drive from cube", true, MotionProfileCommand.Speed.MED,
				afterPickup.backwards(), centered.backwards());
		secondCube = new MotionProfileCommand(this, robot, "drive to switch 2", false, MotionProfileCommand.Speed.MED,
				centered, end);
		
		flip = new LiftCommand(this, bot, Lift.Positions.STARTING_FLIP);
		toGround = new LiftCommand(this, bot, Lift.Positions.GROUND);
		secondLift = new LiftCommand(this, bot, Lift.Positions.SWITCH);
		
		output1 = new IntakeCommand(this, robot, Intake.State.OUTPUTTING);
		pickupCube = new IntakeCommand(this, robot, Intake.State.INTAKING);
	}

	public void init() {
		//Logging.h("Init run!");

		super.init();
		addCommand(firstCube);
		addCommand(flip);
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
		if (cmd == firstCube) {
			addCommand(output1);
		}else if(cmd == output1) {
			addCommand(returnFromSwitch);
		}else if(cmd == returnFromSwitch){
			addCommand(toGround);
			addCommand(getCube);
		}else if(cmd == getCube) {
			addCommand(pickupCube);
		}else if(cmd == pickupCube) {
			addCommand(returnFromCube);
		}else if(cmd == returnFromCube) {
			addCommand(secondLift);
			addCommand(secondCube);
		}else if(cmd == secondCube) {
			addCommand(new IntakeCommand(this, robot, State.OUTPUTTING));
		}
		super.commandFinished(cmd);
	}
}
