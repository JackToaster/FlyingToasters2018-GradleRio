package org.theflyingtoasters.hardware;

import org.theflyingtoasters.controllers.AngularPID;
import org.theflyingtoasters.controllers.PIDcontroller;
import org.theflyingtoasters.controllers.motion_profiles.*;
import org.theflyingtoasters.hardware.interfaces.DriveBase;
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
public class DriveBase2018 extends DriveBase {

	public FeedbackLinkedCAN left;
	public FeedbackLinkedCAN right;


	private final static double MAX_VELOCITY = 3400;
	
	public final static double wheelDistance = 0.665;
	//public final static double wheelDistance = 0.735;
	
	final static double velGain = 0.255;
	final static double accelGain = 0.003;




	//TODO TUNE PIDS FOR LINEAR/ANGULAR MOTION
	//linMotionProfilePID should be close, but angMotionProfilePID (for turning)
	//will not be.
	//Tune driving straight first, then tune in turning.
	private PIDcontroller linMotionProfilePID = new PIDcontroller(3.25, 5.5, 0.11);
	private AngularPID angMotionProfilePID = new AngularPID(1,0,0);
	private CenterProfileGenerator profileGen;

	public DualPIDMotionProfile motionProfile;

	public double leftPower = 0;
	public double rightPower = 0;


	private double angleOffset = 0;

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

		left = new FeedbackLinkedCAN(FeedbackDevice.CTRE_MagEncoder_Absolute, Motors.LEFT1.id,
				Motors.LEFT0.getVictor(), Motors.LEFT2.getVictor());
		left.feedbackTalon.talon.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_20Ms, 0);
		right = new FeedbackLinkedCAN(FeedbackDevice.CTRE_MagEncoder_Absolute, Motors.RIGHT1.id,
				Motors.RIGHT0.getVictor(), Motors.RIGHT2.getVictor());
		right.feedbackTalon.talon.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_20Ms, 0);
		right.setInverted(true);
		left.setEncoderReversed(true);
		
		left.setCurrentLimit(50);
		right.setCurrentLimit(50);
		left.enableCurrentLimit(false);
		right.enableCurrentLimit(false);
		
		// add the motor controllers to the list to be updated
		registerMotorController(left);
		registerMotorController(right);


		profileGen = new CenterProfileGenerator();

		linMotionProfilePID.setDOnMeasurement(false);
		angMotionProfilePID.setDOnMeasurement(false);

		//If the robot turns the wrong way, change the signs of both angEffects. If it goes forward or backwards instead
        // of turning, change the sign on one of the angEffects.
		motionProfile = new DualPIDMotionProfile(linMotionProfilePID, angMotionProfilePID, 1, velGain, accelGain, profileGen);

        resetAngle();
	}

	public double getWheelVelocity() {
		double vl = Math.abs(left.feedbackTalon.getRawVelocity());
		double vr = Math.abs(right.feedbackTalon.getRawVelocity());
		double v = Math.min(Math.max(vl,vr),MAX_VELOCITY)/MAX_VELOCITY;
		v = Utilities.expInput(v,1.4);
		return v;
	}

	//TODO Implement this with gyro instead of encoders

    /**
     * @return the angle of the robot's drivebase.
     */
    public double getAngle(){
        //Just find based on encoder stuff. THIS IS BAD! CHANGE THIS!
        return Math.toRadians(IMU.pigeon.getFusedHeading()) - angleOffset;
    }

    private double getRawAngle(){
        return Math.toRadians(IMU.pigeon.getFusedHeading());
    }

    public void resetAngle(){
        angleOffset = getRawAngle();
    }

	public void update(double dT) {
	    motionProfile.setState((left.getPosition() + right.getPosition()) / 2.0, getAngle());
		super.update(dT);
		SmartDashboard.putNumber("left current", left.feedbackTalon.talon.getOutputCurrent());
		SmartDashboard.putNumber("right current", right.feedbackTalon.talon.getOutputCurrent());
		SmartDashboard.putNumber("left position", left.getPosition());
		SmartDashboard.putNumber("right position", right.getPosition());
		motionProfile.writeErrorToDashboard("MP error");
		if(SmartDashboard.getBoolean("Calibrate pigeon IMU", false)){
			SmartDashboard.putBoolean("Calibrate pigeon IMU", false);
			IMU.calibrateTemp();
		}
		SmartDashboard.putNumber("Heading", getAngle());

        if(isFeedbackActive){
            double[] pidOuts = motionProfile.run(0,dT);
            left.setPower(pidOuts[0] - pidOuts[1]);
            right.setPower(pidOuts[0] + pidOuts[1]);
        }
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
	
	@Override
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
		motionProfile.reset();

		// generate profiles
		motionProfile.generateProfileFromPath(p, isBackwards);

		// set offsets
        double offset = (left.getPosition() + right.getPosition()) / 2.0;
		motionProfile.setOffset(offset);

		// enable them
		//left.setFeedbackController(motionProfile);
		//right.setFeedbackController(rightMotionProfile);
		//left.setFeedbackActive(true);
		//right.setFeedbackActive(true);
        isFeedbackActive = true;
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
}
