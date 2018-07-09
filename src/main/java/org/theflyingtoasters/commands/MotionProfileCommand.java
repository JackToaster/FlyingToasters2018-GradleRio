package org.theflyingtoasters.commands;

import org.theflyingtoasters.commands.interfaces.Command;
import org.theflyingtoasters.commands.interfaces.CommandCallback;
import org.theflyingtoasters.path_generation.Path;
import org.theflyingtoasters.path_generation.Waypoint;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

/**
 * Command to follow a motion profile. Generates the path from the waypoints
 * when the object is created, so the object should be created before the robot
 * starts moving because it will freeze up the code a bit!
 * 
 * @author jack
 *
 */
public class MotionProfileCommand extends Command {
	/**
	 * If set to true, all commands will use the SAFE speed. BE SURE TO SET TO FALSE
	 * BEFORE COMPETITION!
	 */
	final static boolean SAFE_SPEED_OVERRIDE = false;

	/**
	 * Speed settings for motion profiles. Safe is very slow and should be used for
	 * testing. Slow, med, and fast use a relatively high acceleration, while the
	 * settings with _low_accel have a low acceleration. Lightspeed is the fastest
	 * the robot can travel in a straight line. Do not attempt to turn when using
	 * lighspeed. Do not attempt ludicrous speed.
	 * 
	 * @author jack
	 *
	 */
	public enum Speed {
		SAFE(0.5, 0.5), SLOW(1, 3), MED(2, 3), FAST(3, 4), LIGHTSPEED(3.75, 4), LUDICROUS_SPEED(10,
				10), SLOW_LOW_ACCEL(1, 1), MED_LOW_ACCEL(2, 1.5), FAST_LOW_ACCEL(3, 1.59);

		double vel, accel;

		Speed(double v, double a) {
			vel = v;
			accel = a;
		}
	}

	/**
	 * The amount of extra time to add to the end of motion profiles to ensure that
	 * they are really done.
	 */
	public static double END_TIME_EXTRA = 0.1;
	
	private double endExtraTime = END_TIME_EXTRA;
	/**
	 * The waypoints the path will be generated from
	 */
	private Waypoint[] wp;
	/**
	 * Whether the path will be followed backwards or forwards.
	 */
	private boolean backwards;
	/**
	 * Whether the path will be mirrored left to right.
	 */
	private boolean mirrored;
	/**
	 * The robot object that will be driven
	 */
	private Robot bot;
	/**
	 * The generated path from the waypoints
	 */
	private Path path;
	/**
	 * Time used to determine when the command is done
	 */
	private double time = 0;
	/**
	 * End time of the command determined from the lenght of the motion profile and
	 * the extra time
	 */
	private double endTime;

	/**
	 * Create a motion profile command with custom settings for speed/accel
	 * 
	 * @param opMode
	 *            the opmode calling the command. used for callback.
	 * @param robot
	 *            the robot being driven
	 * @param name
	 *            the name of the command
	 * @param isBackwards
	 *            whether the robot should drive backwards
	 * @param waypoints
	 *            the waypoints to generate the path from
	 */
	public MotionProfileCommand(CommandCallback opMode, Robot robot, String name, boolean isBackwards, Speed speed,
			Waypoint... waypoints) {
		super(opMode, name);
		wp = waypoints;
		backwards = isBackwards;
		mirrored = false;
		bot = robot;
		// generate path
		if (!SAFE_SPEED_OVERRIDE) {
			path = new Path(speed.vel, speed.accel, wp);
		} else {
			path = new Path(Speed.SAFE.vel, Speed.SAFE.accel, wp);
		}
		Logging.l(path);
		endTime = path.endTime + endExtraTime;
	}

	public MotionProfileCommand(CommandCallback opMode, Robot robot, String name, boolean isBackwards,
			boolean isMirrored, Speed speed, Waypoint... waypoints) {
		super(opMode, name);
		backwards = isBackwards;
		mirrored = isMirrored;
		wp = waypoints;
		// Mirror waypoints if necessary
		if (mirrored) {
			for(Waypoint waypoint : wp) {
				waypoint.position.y = -waypoint.position.y;
				waypoint.rotation = -waypoint.rotation;
			}
		}
		bot = robot;
		// generate path
		if (!SAFE_SPEED_OVERRIDE) {
			path = new Path(speed.vel, speed.accel, wp);
		} else {
			path = new Path(Speed.SAFE.vel, Speed.SAFE.accel, wp);
		}
		Logging.l(path);
		endTime = path.endTime + endExtraTime;
	}
	
	public void removeExtraEndTime() {
		endExtraTime = 0;
	}
	/**
	 * Returns the duration of the command
	 * @return duration of the command in seconds
	 */
	public double getDuration() {
		return endTime;
	}

	/**
	 * starts following the motion profile
	 */
	public void init() {
		time = 0;
		bot.driveBase.drivePath(path, backwards);
	}

	/**
	 * called continuously when the command is running. Checks if the command should
	 * end.
	 */
	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
		time += deltaTime;
		if (time >= endTime) {
			endCommand();
		}
	}

	/**
	 * called to stop the command early. Stops feedback control on the drivebase.
	 */
	public void stop() {
		super.stop();
		bot.driveBase.setFeedbackActive(false);
	}
}
