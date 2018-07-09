package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.*;
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
public class Scale3CubeAutoFast extends OpMode {
	private Waypoint[] leftPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(5.5, 0), 0),
			new Waypoint(new Point(7.2, -0.6), -Math.PI / 4.0) };
	private Waypoint[] leftGetCube2 = { new Waypoint(new Point(7.2, -0.5), 3 * Math.PI / 4.0),
			new Waypoint(new Point(5.4, -0.82), -3 * Math.PI / 4.0) };
	private Waypoint[] left2ndCube = { new Waypoint(new Point(5.2, -0.35), Math.PI / 4.0),
			new Waypoint(new Point(7.2, -0.4), -Math.PI / 4.0) };
	private Waypoint[] leftGetCube3 = { new Waypoint(new Point(7.2, -0.5), 3 * Math.PI / 4.0),
			new Waypoint(new Point(5.4, -1.5), -3 * Math.PI / 4.0) };
	private Waypoint[] left3rdCube = { new Waypoint(new Point(5.2, -1), Math.PI / 4.0),
			new Waypoint(new Point(7.2, -0.4), -Math.PI / 4.0) };

	private Waypoint[] rightPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(4, 0.2), 0),
			new Waypoint(new Point(5.45, -1.5), -Math.PI / 2.0), new Waypoint(new Point(5.45, -2.9), -Math.PI / 2.0),
			new Waypoint(new Point(5.45, -4.75), -Math.PI / 2.0), new Waypoint(new Point(6.3, -5.8), 0),
			new Waypoint(new Point(6.9, -5.5), Math.PI / 4 + 0.2) };

	private Waypoint[] shortRightPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(4, 0.2), 0),
			new Waypoint(new Point(5.65, -1.5), -Math.PI / 2.0), new Waypoint(new Point(5.65, -2.9), -Math.PI / 2.0) };
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
	private MotionProfileCommand getCube2Command;
	// drives to drop the second command
	private MotionProfileCommand driveToDump2ndCube;

	// drives to get cube
	private MotionProfileCommand getCube3Command;
	// drives to drop the second command
	private MotionProfileCommand driveToDump3rdCube;

	private IntakeCommand intakeCommand;
	private IntakeCommand intakeCommand2;

	// Amount of time before the end of the motion profile to lift
	final static double liftEndTime = 1.3;
	final static double longLiftEndTime = 2.9;

	private Command raise1;
	private LiftCommand lower1;

	private DelayedCommand raise2;
	private LiftCommand lower2;

	private DelayedCommand raise3;
	private LiftCommand lower3;

	private IntakeCommand output1;
	private IntakeCommand output2;
	private IntakeCommand output3;

	// Whether the robot will cross the center
	private boolean cross;
	// whether the robot starts on the left.
	private boolean startLeft;

	/**
	 * constructor for the left scale plate auton.
	 * 
	 * @param bot
	 */
	public Scale3CubeAutoFast(Robot bot, boolean startOnLeft, String gameData) {
		super(bot, "Left Scale auton");
		startLeft = startOnLeft;
		Logging.h(gameData);

		// Indicates whether the paths should be mirrored
		final boolean mirrored = !startOnLeft;
		final boolean scaleLeft = gameData.charAt(1) == 'L';

		if (gameData.charAt(1) == 'L' && startOnLeft || gameData.charAt(1) == 'R' && !startOnLeft) {
			cross = false;
			mpCommand = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
					MotionProfileCommand.Speed.FAST_LOW_ACCEL, leftPath);
			mpCommand.removeExtraEndTime();
			getCube2Command = new MotionProfileCommand(this, robot, "Get left cube", false, mirrored,
					MotionProfileCommand.Speed.SLOW, leftGetCube2);
			getCube2Command.removeExtraEndTime();
			driveToDump2ndCube = new MotionProfileCommand(this, robot, "go to dump cube 2", true, mirrored,
					MotionProfileCommand.Speed.MED, left2ndCube);
			driveToDump2ndCube.removeExtraEndTime();
			getCube3Command = new MotionProfileCommand(this, robot, "Get 3rd cube", false, mirrored,
					MotionProfileCommand.Speed.MED_LOW_ACCEL, leftGetCube3);
			getCube3Command.removeExtraEndTime();
			driveToDump3rdCube = new MotionProfileCommand(this, robot, "go to dump cube 3", true, mirrored,
					MotionProfileCommand.Speed.MED, left3rdCube);
			driveToDump3rdCube.removeExtraEndTime();

			output3 = new IntakeCommand(this, bot, State.OUTPUTTING);
			raise3 = new LiftCommand(this, bot, Positions.L_SCALE)
					.delay(driveToDump3rdCube.getDuration() - longLiftEndTime);

		} else {
			cross = true;
			if (SmartDashboard.getBoolean("Allow Auton Opposite Side", true)) {
				mpCommand = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
						MotionProfileCommand.Speed.MED_LOW_ACCEL, rightPath);
				mpCommand.removeExtraEndTime();
			} else {
				mpCommand = new MotionProfileCommand(this, robot, "mp command", true, mirrored,
						MotionProfileCommand.Speed.MED_LOW_ACCEL, shortRightPath);
				mpCommand.removeExtraEndTime();
				;
			}
			// crossMpCommand = new MotionProfileCommand(this, robot, "Mp
			// command 2", true, mirrored,
			// MotionProfileCommand.Speed.MED_LOW_ACCEL, rightPath2);
			getCube2Command = new MotionProfileCommand(this, robot, "Get right cube", false, mirrored,
					MotionProfileCommand.Speed.MED, rightGetCube);
			getCube2Command.removeExtraEndTime();
			driveToDump2ndCube = new MotionProfileCommand(this, robot, "go to dump cube 2", true, mirrored,
					MotionProfileCommand.Speed.MED, right2ndCube);
			driveToDump2ndCube.removeExtraEndTime();
		}

		intakeCommand = new IntakeCommand(this, robot, Intake.State.INTAKING, 0.2);
		intakeCommand2 = new IntakeCommand(this, robot, Intake.State.INTAKING, 0.2);

		/*
		 * if (cross) { raise1 = new LiftCommand(this, bot,
		 * Positions.H_SCALE).delay(crossMpCommand.getDuration() - liftEndTime);
		 * } else {
		 */
		if (!cross)
			raise1 = new AutoLiftCommand(this, bot, scaleLeft).delay(mpCommand.getDuration() - liftEndTime);
		else
			raise1 = new AutoLiftCommand(this, bot, scaleLeft);
		// }
		lower1 = new LiftCommand(this, bot, Positions.GROUND);
		output1 = new IntakeCommand(this, bot, State.OUTPUTTING);
		raise2 = new AutoLiftCommand(this, bot, scaleLeft)
				.delay(driveToDump2ndCube.getDuration() - longLiftEndTime);

		lower2 = new LiftCommand(this, bot, Positions.GROUND);
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
				addCommand(getCube2Command);
			} else {
				if (SmartDashboard.getBoolean("Allow Auton Opposite Side", true))
					addCommand(raise1);
			}
			// }
			/*
			 * } else if (cmd == crossMpCommand) { // Dump the cube and keep
			 * going to the next one addCommand(output1); addCommand(lower1);
			 * addCommand(getCubeCommand);
			 */
		} else if (cross && cmd == raise1) {

			robot.intake.setOutputPower(0.5);
			addCommand(output1);
			addCommand(lower1);
			addCommand(getCube2Command);
		} else if (cmd == getCube2Command) {
			// Pick up the next cube
			addCommand(intakeCommand);
		} else if (cmd == intakeCommand) {
			// Go to dump the next cube
			addCommand(driveToDump2ndCube);
			addCommand(raise2);
		} else if (cmd == driveToDump2ndCube) {
			if (cross) {
				robot.intake.setOutputPower(0.4);
				addCommand(output2);
			} else {
				// dump the second cube. keep moving quickly since we're not
				// done yet!
				addCommand(output2);
				// Lower to switch position.
				addCommand(lower2);

				addCommand(getCube3Command);
			}
		} else if (cmd == getCube3Command) {
			addCommand(intakeCommand2);
		} else if (cmd == intakeCommand2) {
			addCommand(driveToDump3rdCube);
			addCommand(raise3);
		} else if (cmd == driveToDump3rdCube) {
			addCommand(output3);
		}
		// Clean up after the command
		super.commandFinished(cmd);
	}
}
