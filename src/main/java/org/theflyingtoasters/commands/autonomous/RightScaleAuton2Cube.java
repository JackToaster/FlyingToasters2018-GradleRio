package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.IntakeCommand;
import org.theflyingtoasters.commands.LiftCommand;
import org.theflyingtoasters.commands.MotionProfileCommand;
import org.theflyingtoasters.commands.interfaces.Command;
import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.hardware.Intake;
import org.theflyingtoasters.hardware.Intake.State;
import org.theflyingtoasters.hardware.Lift.Positions;
import org.theflyingtoasters.path_generation.Point;
import org.theflyingtoasters.path_generation.Waypoint;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Drives to the left scale plate.
 * 
 * @author jack
 *
 */
public class RightScaleAuton2Cube extends OpMode {
	private Waypoint[] rightPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(5.5, 0), 0),
			new Waypoint(new Point(7.0, 0.7), Math.PI / 4.0) };
	private Waypoint[] rightGetCube = { new Waypoint(new Point(7.0, 0.7), -3 * Math.PI / 4),
			new Waypoint(new Point(5.3, .7), 3 * Math.PI / 4) };
	private Waypoint[] right2ndCube = { new Waypoint(new Point(5.3, .7), -Math.PI / 4),
			new Waypoint(new Point(6.9, 0.9), Math.PI / 4) };
	
	private Waypoint[] leftPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(4, -0.2), 0),
			new Waypoint(new Point(5.65, 1.5), Math.PI / 2.0), new Waypoint(new Point(5.65, 2.9), Math.PI / 2.0) };
	private Waypoint[] leftPath2 = { new Waypoint(new Point(5.65, 2.9), Math.PI / 2.0),
			new Waypoint(new Point(5.65, 4.75), Math.PI / 2.0), new Waypoint(new Point(6.9, 5.5), -Math.PI / 4) };
	private Waypoint[] leftGetCube = { new Waypoint(new Point(6.9, -0.7), 3 * Math.PI / 4.0),
			new Waypoint(new Point(5.5, -0.7), -3 * Math.PI / 4.0) };
	private Waypoint[] left2ndCube = { new Waypoint(new Point(5.5, -1.2), Math.PI / 4.0),
			new Waypoint(new Point(7.4, -0.2), -Math.PI / 4.0) };

	private MotionProfileCommand mpCommand;
	private MotionProfileCommand leftMpCommand2;
	// drives to get cube
	private MotionProfileCommand getCubeCommand;
	// drives to drop the second command
	private MotionProfileCommand driveToDump2ndCube;
	private IntakeCommand intakeCommand;
	private boolean left;
	
	private LiftCommand raise1;
	private LiftCommand lower1;
	
	private LiftCommand raise2;
	private LiftCommand lower2;
	
	private IntakeCommand output1;
	private IntakeCommand output2;
	
	/**
	 * constructor for the left scale plate auton.
	 * 
	 * @param bot
	 */
	public RightScaleAuton2Cube(Robot bot) {
		super(bot, "Left Scale auton");
		String gameData = DriverStation.getInstance().getGameSpecificMessage();
		Logging.h(gameData);
		
		raise1 = new LiftCommand(this, bot, Positions.H_SCALE);
		lower1 = new LiftCommand(this, bot, Positions.SWITCH);
		output1 = new IntakeCommand(this, bot, State.OUTPUTTING);
		raise2 = new LiftCommand(this, bot, Positions.L_SCALE);
		lower2 = new LiftCommand(this, bot, Positions.SWITCH);
		output2 = new IntakeCommand(this, bot, State.OUTPUTTING);
		
		if (gameData.charAt(1) == 'L') {
			left = true;
			mpCommand = new MotionProfileCommand(this, robot, "mp command", true, MotionProfileCommand.Speed.MED, leftPath);
			leftMpCommand2 = new MotionProfileCommand(this, robot, "Mp command 2", true, MotionProfileCommand.Speed.SLOW_LOW_ACCEL, leftPath2);
			getCubeCommand = new MotionProfileCommand(this, robot, "Get left cube", false, MotionProfileCommand.Speed.MED_LOW_ACCEL, leftGetCube);
			driveToDump2ndCube = new MotionProfileCommand(this, robot, "go to dump cube 2", true, MotionProfileCommand.Speed.MED_LOW_ACCEL, left2ndCube);

		} else {
			left = false;
			mpCommand = new MotionProfileCommand(this, robot, "mp command", true, MotionProfileCommand.Speed.FAST_LOW_ACCEL, rightPath);
			getCubeCommand = new MotionProfileCommand(this, robot, "Get right cube", false, MotionProfileCommand.Speed.MED_LOW_ACCEL, rightGetCube);
			driveToDump2ndCube = new MotionProfileCommand(this, robot, "go to dump cube 2", true, MotionProfileCommand.Speed.MED_LOW_ACCEL, right2ndCube);
		}
		
		intakeCommand = new IntakeCommand(this, robot, Intake.State.INTAKING);
	}

	/**
	 * called once when initialized.
	 */
	public void init() {
		super.init();
		addCommand(mpCommand);
	}

	/**
	 * called periodically when opmode is running.
	 */
	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
		Logging.l("Left pos: " + robot.driveBase.left.getPosition() + ", right pos: "
				+ robot.driveBase.right.getPosition());
	}

	/**
	 * called once when the opmode is stopped.
	 */
	public void stop() {
		robot.driveBase.setFeedbackActive(false);
	}

	public void commandFinished(Command cmd) {
		// Add the intake command to output the cube.
		if (cmd == mpCommand) {
			if (left) {
				if(SmartDashboard.getBoolean("Allow Auton Opposite Side", true))
					addCommand(leftMpCommand2);
			} else {
				addCommand(raise1);
			}
		} else if (cmd == leftMpCommand2) {
			addCommand(raise1);
		} else if(cmd == raise1) {
			addCommand(output1);
		} else if(cmd == output1) {
			addCommand(lower1);
		} else if(cmd == lower1) {
			robot.lift.trackToPos(Positions.GROUND);
			addCommand(getCubeCommand);
		} else if (cmd == getCubeCommand) {
			addCommand(intakeCommand);
		} else if (cmd == intakeCommand) {
			addCommand(driveToDump2ndCube);
		} else if (cmd == driveToDump2ndCube) {
			addCommand(raise2);
		} else if (cmd == raise2) {
			addCommand(output2);
		} else if (cmd == output2) {
			addCommand(lower2);
		}
		super.commandFinished(cmd);
	}
}
