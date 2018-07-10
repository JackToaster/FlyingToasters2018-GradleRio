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
public class OPScaleAuton extends OpMode {

	private Waypoint[] scalePath1 = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(7.73, 0), 0)};
	private Waypoint[] scalePath2 = { new Waypoint(new Point(7.73, 0.5), 0), new Waypoint(new Point(8.13, 0), -Math.PI / 2.0) };
	private Waypoint[] switchPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(3.8, -0.8), -Math.PI / 2.0)};
	private Waypoint[] crossPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(3, 0), 0)};

	private enum Mode {
		SCALE, SWITCH, LINE;
	}
	
	private Mode mode;
	
	MotionProfileCommand driveToScale;
	MotionProfileCommand scaleTurn;
	MotionProfileCommand driveToSwitch;
	MotionProfileCommand line;
	
	
	
	LiftCommand high;
	public OPScaleAuton(Robot bot, boolean startOnLeft, String gameData) {
		super(bot, "Motion Profile Auton");
		Logging.h(gameData);
		
		// Indicates whether the paths should be mirrored
		final boolean mirrored = !startOnLeft;
		
		//Have scale
		if (gameData.charAt(1) == 'L' && startOnLeft || gameData.charAt(1) == 'R' && !startOnLeft) {
			mode = Mode.SCALE;
			driveToScale = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
					MotionProfileCommand.Speed.FAST_LOW_ACCEL, scalePath1);
			scaleTurn = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
					MotionProfileCommand.Speed.SLOW_LOW_ACCEL, scalePath2);

			high = new LiftCommand(this, bot, Lift.Positions.H_SCALE);
		} else if (gameData.charAt(0) == 'L' && startOnLeft || gameData.charAt(0) == 'R' && !startOnLeft) {
			//have switch
			mode = Mode.SWITCH;
			driveToSwitch = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, switchPath);
		} else {
			//Line
			mode = Mode.LINE;
			line = new MotionProfileCommand(this, robot, "mp cmd", true, mirrored,
					MotionProfileCommand.Speed.SLOW_LOW_ACCEL, crossPath);
		}
	}

	public void init() {
		//Logging.h("Init run!");
		
		super.init();
		switch(mode) {
		case LINE:
			addCommand(line);
			break;
		case SWITCH:
			addCommand(driveToSwitch);
			break;
		case SCALE:
			addCommand(driveToScale);
			break;
		}
		//addCommand(motionProfileCmd);
	}

	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
//		Logging.l("Left pos: " + robot.driveBase.left.getPosition() + ", right pos: "
//				+ robot.driveBase.right.getPosition());
	}

	public void stop() {
		robot.driveBase.setFeedbackActive(false);
	}

	public void commandFinished(Command cmd) {
		if(cmd == driveToSwitch) {
			robot.intake.setOutputPower(0.8);
			addCommand(new IntakeCommand(this, robot, State.OUTPUTTING));
		}else if(cmd == driveToScale) {
			addCommand(high);
			addCommand(scaleTurn);
		} else if(cmd == scaleTurn) {
			addCommand(new IntakeCommand(this, robot, State.OUTPUTTING));
		}
		super.commandFinished(cmd);
	}
}
