package org.theflyingtoasters.hardware;

import org.theflyingtoasters.controllers.PIDcontroller;
import org.theflyingtoasters.controllers.motion_profiles.MotionProfile;
import org.theflyingtoasters.controllers.motion_profiles.SkidsteerProfileGenerator;
import org.theflyingtoasters.controllers.motion_profiles.WheelProfileGenerator;
import org.theflyingtoasters.hardware.motors.LinkedTalons;
import org.theflyingtoasters.hardware.motors.Talon;
import org.theflyingtoasters.hardware.motors.Victor;
import org.theflyingtoasters.path_generation.Path;
import org.theflyingtoasters.path_generation.Waypoint;
import org.theflyingtoasters.utilities.Logging;
import org.theflyingtoasters.utilities.Utilities;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The 2018 robot drivertrain.
 * 
 * @author jack
 *
 */
public class DriveBase2018 {
	public LinkedTalons left;
	public LinkedTalons right;
	
	private final static double MAX_VELOCITY = 3400;
	
	public final static double wheelDistance = 0.665;
	//public final static double wheelDistance = 0.735;
	
	final static double velGain = 0.255;
	final static double accelGain = 0.003;

	private PIDcontroller leftMotionProfilePID = new PIDcontroller(6.5, 13, 0.275);
	private PIDcontroller rightMotionProfilePID = new PIDcontroller(6.5, 13, 0.275);
	private WheelProfileGenerator leftProfileGen;
	private WheelProfileGenerator rightProfileGen;
	
	public MotionProfile leftMotionProfile;
	public MotionProfile rightMotionProfile;

	private boolean runningMotionProfile = false;
	
	public double leftPower = 0;
	public double rightPower = 0;

	public enum Motors {
		LEFT0(3), LEFT1(4), LEFT2(13), RIGHT0(1), RIGHT1(2), RIGHT2(12);

		public int id;

		Motors(int talonID) {
			id = talonID;
		}

		public Talon getTalon() {
			return new Talon(id);
		}

		public Victor getVictor() {
			return new Victor(id);
		}
	}

	public DriveBase2018() {
		super();
		left = new LinkedTalons(FeedbackDevice.CTRE_MagEncoder_Absolute, Motors.LEFT1.id,
				Motors.LEFT0.getVictor(), Motors.LEFT2.getVictor());
		left.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_20Ms, 0);
		right = new LinkedTalons(FeedbackDevice.CTRE_MagEncoder_Absolute, Motors.RIGHT1.id,
				Motors.RIGHT0.getVictor(), Motors.RIGHT2.getVictor());
		right.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_20Ms, 0);
		right.setInverted(true);
		
		lowCurrentLimiting();

		leftProfileGen = new SkidsteerProfileGenerator(-wheelDistance / 2);
		rightProfileGen = new SkidsteerProfileGenerator(wheelDistance / 2);

		leftMotionProfilePID.setDOnMeasurement(false);
		rightMotionProfilePID.setDOnMeasurement(false);

		leftMotionProfile = new MotionProfile(leftMotionProfilePID, velGain, accelGain, leftProfileGen);
		rightMotionProfile = new MotionProfile(rightMotionProfilePID, velGain, accelGain, rightProfileGen);
	}

	public double getNormalizedRobotVelocity() {
		double vl = Math.abs(left.getRawSensorVelocity());
		double vr = Math.abs(right.getRawSensorVelocity());
		double v = Math.min(Math.max(vl,vr),MAX_VELOCITY)/MAX_VELOCITY;
		v = Utilities.expInput(v,1.4);
		return v;
	}
	
	public void update(double dT) {
		if(runningMotionProfile) {
			left.runFeedback(0, dT);
			right.runFeedback(0, dT);
		}
		
		SmartDashboard.putNumber("left current", left.getOutputCurrent());
		SmartDashboard.putNumber("right current", right.getOutputCurrent());
		SmartDashboard.putNumber("left position", left.getPosition());
		SmartDashboard.putNumber("right position", right.getPosition());
		leftMotionProfile.writeErrorToDashboard("left MP error");
		rightMotionProfile.writeErrorToDashboard("right MP error");
	}
	
	public void enableCurrentLimiting() {
		SmartDashboard.putBoolean("CurrentLimiting", true);
		Logging.h("Enabling Current Limiting");
		left.setCurrentLimit(40);
		right.setCurrentLimit(40);
		left.enableCurrentLimit(true);
		right.enableCurrentLimit(true);
	}
	
	public void lowCurrentLimiting() {
		SmartDashboard.putBoolean("CurrentLimiting", false);
		Logging.h("Disabling Current Limiting");
		left.setCurrentLimit(70);
		right.setCurrentLimit(70);
		left.enableCurrentLimit(true);
		right.enableCurrentLimit(true);
	}
	
	
	public void disableCurrentLimiting() {
		SmartDashboard.putBoolean("CurrentLimiting", false);
		Logging.h("Disabling Current Limiting");
		left.enableCurrentLimit(false);
		right.enableCurrentLimit(false);
	}
	
	public void drive(double... inputs) {
		if (inputs.length == 2) {
			driveArcade(inputs[0], inputs[1]);
		} else {
			Logging.e("Invalid number of inputs to drive");
		}
	}

	public double driveArcade(double power, double turn) {
		double leftPower = power + turn;
		double rightPower = power - turn;
		return driveTank(leftPower, rightPower);
	}
	
	public double driveTank(double leftPower, double rightPower) {
		leftPower = Math.max(Math.min(leftPower, 1),-1);
		rightPower = Math.max(Math.min(rightPower, 1),-1);
		left.setPower(leftPower);
		right.setPower(rightPower);
		return Math.sqrt(leftPower*leftPower + rightPower*rightPower)/2.0;
	}

	public double drivePureCheese(double power, double rotation) {
		double gain = 1.1;
		double exp = 1.375;

		rotation = Utilities.expInput(rotation, exp);
		double outputPower = Utilities.expInput(power, exp);

		double cheesyRotation = rotation * gain * Math.abs(outputPower);
		
		return driveArcade(outputPower, cheesyRotation);
	}
	
	public double driveGrilledCheese(double power, double rotation) {
		double gain = 1;
		double limit = 0.25;
		double subLimitWeight = .8;
		double exp = 1.5;

		rotation = Utilities.expInput(rotation, exp);
		double outputPower = Utilities.expInput(power, exp);

		double arcadeRotation = rotation * 0.8;
		double cheesyRotation = rotation * gain * Math.abs(outputPower);
		double arcadeWeight = (1 - Math.abs(power) / limit / subLimitWeight);
		double cheesyWeight = (Math.abs(power) / limit * subLimitWeight);

		double outputRotation = cheesyRotation;
		if (Math.abs(power) <= limit)
			outputRotation = cheesyWeight * cheesyRotation + arcadeWeight * arcadeRotation;

		return driveArcade(outputPower, outputRotation);
	}

	public void drivePath(Path p, boolean isBackwards) {
		// reset motion profiles
		leftMotionProfile.reset();
		rightMotionProfile.reset();

		// generate profiles
		leftMotionProfile.generateProfileFromPath(p, isBackwards);
		rightMotionProfile.generateProfileFromPath(p, isBackwards);

		// set offsets
		leftMotionProfile.setOffset(left.getPosition());
		rightMotionProfile.setOffset(right.getPosition());

		// enable them
		left.setFeedbackController(leftMotionProfile);
		right.setFeedbackController(rightMotionProfile);
		setMotionProfileActive(true);
	}

	/**
	 * drive from one waypoint to another
	 * 
	 * @param from
	 *            starting waypoint
	 * @param to
	 *            ending waypoint
	 */
	public void driveFromTo(Waypoint from, Waypoint to, boolean isBackwards) {
		// generate path then drive it
		Path path = new Path(from, to);
		Logging.l(path);
		drivePath(path, isBackwards);
	}

	public void driveFromTo(Waypoint from, Waypoint to, boolean isBackwards, double vel, double accel) {
		// generate path then drive it
		Path path = new Path(from, to);
		Logging.l(path);
		drivePath(path, isBackwards);
	}

	/**
	 * Drives a series of waypoints, similar to driveFromTo
	 * 
	 * @param waypoints
	 *            the series of waypoints to drive, at least 2
	 */
	public void driveWaypoints(boolean isBackwards, Waypoint... waypoints) {
		// generate path then drive it
		Path path = new Path(waypoints);
		Logging.l(path);
		drivePath(path, isBackwards);
	}

	public void setMotionProfileActive(boolean active) {
		runningMotionProfile = active;
	}
}
