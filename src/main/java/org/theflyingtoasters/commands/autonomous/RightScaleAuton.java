package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.IntakeCommand;
import org.theflyingtoasters.commands.LiftCommand;
import org.theflyingtoasters.commands.MotionProfileCommand;
import org.theflyingtoasters.commands.interfaces.Command;
import org.theflyingtoasters.commands.interfaces.OpMode;
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
public class RightScaleAuton extends OpMode {
	private Waypoint[] rightPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(5.5, 0), 0),
			new Waypoint(new Point(7.0, 0.7), Math.PI / 4.0) };
	private Waypoint[] leftPath = { new Waypoint(new Point(0, 0), 0), new Waypoint(new Point(4, -0.2), 0),
			new Waypoint(new Point(5.65, 1.5), Math.PI / 2.0), new Waypoint(new Point(5.65, 2.9), Math.PI / 2.0) };
	private Waypoint[] leftPath2 = { new Waypoint(new Point(5.65, 2.9), Math.PI / 2.0),
			new Waypoint(new Point(5.65, 4.75), Math.PI / 2.0), new Waypoint(new Point(6.9, 5.5), -Math.PI / 4) };
	
	private MotionProfileCommand mpCommand;
	private MotionProfileCommand leftMpCommand2;
	private boolean left;
	
	private LiftCommand raise;
	private LiftCommand lower;
	private IntakeCommand output;
	/**
	 * constructor for the left scale plate auton.
	 * 
	 * @param bot
	 */
	public RightScaleAuton(Robot bot) {
		super(bot, "Left Scale auton");
		String gameData = DriverStation.getInstance().getGameSpecificMessage();
		Logging.h(gameData);

		raise = new LiftCommand(this, bot, Positions.H_SCALE);
		lower = new LiftCommand(this, bot, Positions.SWITCH);
		output = new IntakeCommand(this, bot, State.OUTPUTTING);
		
		if (gameData.charAt(1) == 'L') {
			left = true;
			mpCommand = new MotionProfileCommand(this, robot, "mp command", true, MotionProfileCommand.Speed.MED, leftPath);
			leftMpCommand2 = new MotionProfileCommand(this, robot, "Mp command 2", true, MotionProfileCommand.Speed.SLOW_LOW_ACCEL, leftPath2);
		} else {
			left = false;
			mpCommand = new MotionProfileCommand(this, robot, "mp command", true, MotionProfileCommand.Speed.MED_LOW_ACCEL, rightPath);
		}
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
				addCommand(raise);
			}
		} else if (cmd == leftMpCommand2) {
			addCommand(raise);
		} else if(cmd == raise) {
			addCommand(output);
		} else if(cmd == output) {
			addCommand(lower);
		}
		super.commandFinished(cmd);
	}
}
