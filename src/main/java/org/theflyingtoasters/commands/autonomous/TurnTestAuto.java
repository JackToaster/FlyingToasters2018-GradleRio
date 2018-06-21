package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.DelayedCommand;
import org.theflyingtoasters.commands.LiftCommand;
import org.theflyingtoasters.commands.MotionProfileCommand;
import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.hardware.Lift;
import org.theflyingtoasters.path_generation.Point;
import org.theflyingtoasters.path_generation.Waypoint;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

/**
 * Auto line autonomous mode. Drives forward 2.5 meters.
 * 
 * @author jack
 *
 */
public class TurnTestAuto extends OpMode {
	private final static double dist_m = 2.5;

	private Waypoint start = new Waypoint(new Point(0, 0), 0);
	private Waypoint end = new Waypoint(new Point(dist_m, 0), Math.PI / 2.0);
	MotionProfileCommand motionProfile;
	DelayedCommand delay;
	LiftCommand flip;
	/**
	 * constructor for auto line auton. Takes the robot object as a parameter.
	 *
	 * @param bot
	 */
	public TurnTestAuto(Robot bot) {
		super(bot, "Motion Profile Auton");
		motionProfile = new MotionProfileCommand(this, bot, "cross line", false, MotionProfileCommand.Speed.SLOW_LOW_ACCEL, start, end);
		delay = new DelayedCommand(this, 5);
		flip = new LiftCommand(delay, bot, Lift.Positions.STARTING_FLIP);
		delay.setCommand(flip);
	}

	/**
	 * called when the opmode is initialized.
	 */
	public void init() {
		Logging.h("Starting baseline auton");
		super.init();
		motionProfile.init();
		delay.init();
	}

	/**
	 * called periodically when the opmode runs.
	 */
	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
		motionProfile.periodic(deltaTime);
		Logging.l("Left pos: " + robot.driveBase.left.getPosition() + ", right pos: "
				+ robot.driveBase.right.getPosition());
		delay.periodic(deltaTime);
	}

	/**
	 * called once when the opmode is stopped.
	 */
	public void stop() {
		robot.driveBase.setFeedbackActive(false);
		motionProfile.stop();
	}
}
