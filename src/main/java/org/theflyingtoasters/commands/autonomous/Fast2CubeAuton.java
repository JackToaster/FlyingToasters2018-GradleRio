package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.DelayedCommand;
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

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Drives to the scale plate, drops in a cube, and repeats.
 * 
 * @author jack
 *
 */
public class Fast2CubeAuton extends OpMode {
	private Waypoint[] leftPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(5.5, 0), 0),
			new Waypoint(new Point(7.2, -0.6), -Math.PI / 4.0) };
	private Waypoint[] leftGetCube = { new Waypoint(new Point(7.2, -0.5), 3 * Math.PI / 4.0),
			new Waypoint(new Point(5.4, -0.82), -3 * Math.PI / 4.0) };
	private Waypoint[] left2ndCube = { new Waypoint(new Point(5.2, -0.35), Math.PI / 4.0),
			new Waypoint(new Point(7.2, -0.4), -Math.PI / 4.0) };

	private Waypoint[] rightPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(4, 0.2), 0),
			new Waypoint(new Point(5.65, -1.5), -Math.PI / 2.0), new Waypoint(new Point(5.65, -2.9), -Math.PI / 2.0),
			new Waypoint(new Point(5.65, -4.75), -Math.PI / 2.0),  new Waypoint(new Point(6.3, -5.8), 0),
			new Waypoint(new Point(6.9, -5.5), Math.PI / 4) };
	
	private Waypoint[] shortRightPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(4, 0.2), 0),
			new Waypoint(new Point(5.65, -1.5), -Math.PI / 2.0), new Waypoint(new Point(5.65, -2.9), -Math.PI / 2.0)};
	// private Waypoint[] rightPath2 = { new Waypoint(new Point(5.65, -2.9)
	// -Math.PI / 2.0),
	// new Waypoint(new Point(5.65, -4.75), -Math.PI / 2.0), new Waypoint(new
	// Point(6.9, -5.5), Math.PI / 4) };
	private Waypoint[] rightGetCube = { new Waypoint(new Point(6.9, -5.5), -3 * Math.PI / 4),
			new Waypoint(new Point(5.5, -5.35), 3 * Math.PI / 4) };
	private Waypoint[] right2ndCube = { new Waypoint(new Point(5.35, -6), -Math.PI / 4),
			new Waypoint(new Point(7.4, -5.9), Math.PI / 4) };

	// First profile run
	private MotionProfileCommand mpCommand;
	// second one to cross the bump when going right
	// private MotionProfileCommand crossMpCommand;
	// drives to get cube
	private MotionProfileCommand getCubeCommand;
	// drives to drop the second command
	private MotionProfileCommand driveToDump2ndCube;
	private IntakeCommand intakeCommand;

	// Amount of time before the end of the motion profile to lift
	final static double liftEndTime = 1;
	final static double longLiftEndTime = 2.9;

	private Command raise1;
	private LiftCommand lower1;

	private DelayedCommand raise2;
	private LiftCommand lower2;

	private IntakeCommand output1;
	private IntakeCommand output2;

	// Whether the robot will cross the center
	private boolean cross;
	// whether the robot starts on the left.
	private boolean startLeft;

	/**
	 * constructor for the left scale plate auton.
	 * 
	 * @param bot
	 */
	public Fast2CubeAuton(Robot bot, boolean startOnLeft, String gameData) {
		super(bot, "Left Scale auton");
		startLeft = startOnLeft;
		Logging.h(gameData);

		// Indicates whether the paths should be mirrored
		final boolean mirrored = !startOnLeft;

		if (gameData.charAt(1) == 'L' && startOnLeft || gameData.charAt(1) == 'R' && !startOnLeft) {
			cross = false;
			mpCommand = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
					MotionProfileCommand.Speed.FAST_LOW_ACCEL, leftPath);
			getCubeCommand = new MotionProfileCommand(this, robot, "Get left cube", false, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, leftGetCube);
			driveToDump2ndCube = new MotionProfileCommand(this, robot, "go to dump cube 2", true, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, left2ndCube);
		} else {
			cross = true;
			if(SmartDashboard.getBoolean("Allow Auton Opposite Side", true)) {
				mpCommand = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, rightPath);
			}
			else {
				mpCommand = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, shortRightPath);;;
			}
			// crossMpCommand = new MotionProfileCommand(this, robot, "Mp
			// command 2", true, mirrored,
			// MotionProfileCommand.Speed.MED_LOW_ACCEL, rightPath2);
			getCubeCommand = new MotionProfileCommand(this, robot, "Get right cube", false, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, rightGetCube);
			driveToDump2ndCube = new MotionProfileCommand(this, robot, "go to dump cube 2", true, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, right2ndCube);
		}

		intakeCommand = new IntakeCommand(this, robot, Intake.State.INTAKING);
		/*
		 * if (cross) { raise1 = new LiftCommand(this, bot,
		 * Positions.H_SCALE).delay(crossMpCommand.getDuration() - liftEndTime);
		 * } else {
		 */
		if(!cross)
			raise1 = new LiftCommand(this, bot, Positions.H_SCALE).delay(mpCommand.getDuration() - liftEndTime);
		else raise1 = new LiftCommand(this, bot, Positions.H_SCALE);
		// }
		lower1 = new LiftCommand(this, bot, Positions.GROUND);
		output1 = new IntakeCommand(this, bot, State.OUTPUTTING);
		raise2 = new LiftCommand(this, bot, Positions.L_SCALE)
				.delay(driveToDump2ndCube.getDuration() - longLiftEndTime);
		lower2 = new LiftCommand(this, bot, Positions.SWITCH);
		output2 = new IntakeCommand(this, bot, State.OUTPUTTING);
	}

	/**
	 * called once when initialized.
	 */
	public void init() {
		super.init();
		addCommand(mpCommand);
		if (!cross) {
			addCommand(raise1);
		}
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
		super.stop();
	}

	public void commandFinished(Command cmd) {
		Logging.h("Command ended: " + cmd.getName());
		Logging.h("Running commands: " + commands.toString());
		// Add the intake command to output the cube.
		if (cmd == mpCommand) {
			// Check if the robot can cross over. If not, auton stops here.
			/*
			 * if (cross) { if
			 * (SmartDashboard.getBoolean("Allow Auton Opposite Side", true)) {
			 * // Cross over addCommand(crossMpCommand); addCommand(raise1); } }
			 * else {
			 */
			if (!cross) {
				// Dump the cube and keep going to the next one
				addCommand(output1);
				addCommand(lower1);
				addCommand(getCubeCommand);
			} else {
				if(SmartDashboard.getBoolean("Allow Auton Opposite Side", true))
					addCommand(raise1);
			}
			// }
			/*
			 * } else if (cmd == crossMpCommand) { // Dump the cube and keep
			 * going to the next one addCommand(output1); addCommand(lower1);
			 * addCommand(getCubeCommand);
			 */
		} else if(cross && cmd == raise1) {
			addCommand(output1);
			addCommand(lower1);
			addCommand(getCubeCommand);
		} else if (cmd == getCubeCommand) {
			// Pick up the next cube
			addCommand(intakeCommand);
		} else if (cmd == intakeCommand) {
			// Go to dump the next cube
			addCommand(driveToDump2ndCube);
			addCommand(raise2);
		} else if (cmd == driveToDump2ndCube) {
			// dump the second cube. Don't keep moving quickly since we're done
			// now!
			addCommand(output2);
		} else if (cmd == output2) {
			// Lower to switch position.
			addCommand(lower2);
		}
		// Clean up after the command
		super.commandFinished(cmd);
	}
}
