package org.theflyingtoasters.hardware;

import org.theflyingtoasters.utilities.Logging;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Class for the lift mechanism.
 * 
 * @author jack
 *
 */
public class Lift {
	/**
	 * Id of the talon driving the lift motor
	 */
	static final int LIFT_TALON_ID = 5;
	static final int LIFT_FOLLOWER_ID = 8;

	/**
	 * Id of the talon driving the flip motor
	 */
	static final int FLIP_TALON_ID = 7;

	static final int LIMIT_SWITCH_PORT = 1;
	
	/**
	 * The parameters used in initialization of the lift talon, like PID values
	 * 
	 * @author jack
	 *
	 */
	private static class LiftTalonParams {
		double kF = 0;
		double kP = 40;
		double kI = 0.02;
		double kD = 3;
		int vel = 100;
		int accel = 110;
	}

	/**
	 * The parameters used in initialization of the lift talon, like PID values
	 * 
	 * @author jack
	 *
	 */
	private class FlipTalonParams {
		double kF = 0;
		double kP = 40;
		double kI = 0;
		double kD = 4;
		int vel = 25;
		int accel = 30;
	}

	/**
	 * The minimum position at which the thing can flip at.
	 */
	static final double FLIP_MIN_POS = -418;

	private static final double DOWN_SLOW_SPEED = -.15;

	/**
	 * The different positions to go to, in raw potentiometer values.
	 * 
	 * @author jack
	 *
	 */
	 public enum Positions {
		//old
		/*GROUND(-98, 537),
		GROUND_TILT(-98, 515),
		SWITCH(-365, 537),
		H_SWITCH(-475, 537),
		L_SCALE(-695, 400),
		H_SCALE(-737, 419),
		CLIMB(-777, 389),
		CLIMB_ENGAGED(-550, 389),
		STARTING(-418, 389),
		STARTING_FLIP(-418, 537);*/
		//new
		GROUND(-98, 667),
		GROUND_TILT(-112, 657),
		SWITCH(-365, 667),
		H_SWITCH(-475, 667),
		L_SCALE(-695, 621),
		LL_SCALE(-610, 621),
		H_SCALE(-737, 627),
		CLIMB(-777, 618),
		CLIMB_ENGAGED(-475, 618),
		STARTING(-418, 618),
		STARTING_FLIP(-418, 667);

        double liftPos;
		double flipPos;

		Positions(double lP, double fP) {
			liftPos = lP;
			flipPos = fP;
		}
	}

	/**
	 * The current position of the stuff
	 */
	public Positions currentPos = Positions.STARTING;

	private boolean resettingDown = false;
	private boolean lastResettingDown = false;
	/**
	 * the motor which drives the lift
	 */
	private FeedbackLinkedCAN liftMotor;
	private FeedbackTalon flipMotor;
	private DigitalInput limitSwitch;
	private boolean limSwitchVal = false;
	private boolean lastSwitchVal = false;
	/**
	 * Whether the lift is active. If true, feedback loop is run, if false it's set
	 * to 0 all the time.
	 */
	private boolean active = true;

	/**
	 * Stores the parameters for the lift talon
	 */
	LiftTalonParams liftParams = new LiftTalonParams();
	/**
	 * Stores the parameters for the flip talon
	 */
	FlipTalonParams flipParams = new FlipTalonParams();

	/**
	 * Constructor. creates a new lift object and initializes the motors
	 */
	public Lift() {
		// Puts the things for tuning PIDs / vel / accel on dashboard
		addTuningToDashboard();

		// Create the talons for the lift and flipper and invert them if necessary (May
		// change with string pot for lift talon)
		FeedbackTalon liftFeedbackTalon = new FeedbackTalon(LIFT_TALON_ID, FeedbackDevice.Analog);
		Talon liftFollowerTalon = new Talon(LIFT_FOLLOWER_ID);
		liftFollowerTalon.talon.setInverted(true);
		liftFeedbackTalon.talon.setInverted(true);
		liftMotor = new FeedbackLinkedCAN(liftFeedbackTalon, liftFollowerTalon);
		// Set up flip motor and limit switch
		flipMotor = new FeedbackTalon(FLIP_TALON_ID, FeedbackDevice.Analog);
		limitSwitch = new DigitalInput(LIMIT_SWITCH_PORT);

		// Sets up motion magic constants with pid/va values.
		setupMotionMagic();
		// Set the motion magic to stay at the starting setpoint
		trackToPos(Positions.STARTING);
	}

	/**
	 * sets up motion magic on motor controllers
	 */
	private void setupMotionMagic() {
		flipMotor.setupTalonPIDVA(flipParams.kF, flipParams.kP, flipParams.kI, flipParams.kD, flipParams.vel,
				flipParams.accel);
		liftMotor.feedbackTalon.setupTalonPIDVA(liftParams.kF, liftParams.kP, liftParams.kI, liftParams.kD,
				liftParams.vel, liftParams.accel);
	}

	/**
	 * Disables motion magic and puts motors in percent output mode.
	 */
	public void disableMotionMagic() {
		flipMotor.stopMotionMagic();
		liftMotor.feedbackTalon.stopMotionMagic();
	}

	/**
	 * Sets the lift to be active(runs feedback control stuff) or inactive (motor at
	 * 0)
	 * 
	 * @param isActive
	 *            whether to be active
	 */
	public void setActive(boolean isActive) {
		active = isActive;
	}

	/**
	 * Drive the lift with no feedback control.
	 * 
	 * @param liftPow
	 *            the raw power to the lift
	 * @param flipPow
	 *            the raw power to the flipper
	 */
	public void driveNoFeedback(double liftPow, double flipPow) {
		liftMotor.setPower(liftPow);
		flipMotor.setPower(flipPow);
	}

	/**
	 * Drives the lift to the given position.
	 * 
	 * @param position
	 *            The position to go to.
	 */
	public void trackToPos(Positions position) {
		Logging.h("Tracking to: " + position.toString());
		// The lift should only be able to go to l/h scale from starting, otherwise go
		// to starting flip position.
		if (currentPos == Positions.STARTING && position.liftPos > Positions.STARTING.liftPos) {
			currentPos = Positions.STARTING_FLIP;
		} else {
			currentPos = position;
		}
		// Set the setpoint of the lift motor
		liftMotor.feedbackTalon.setSetpoint(currentPos.liftPos);

		// The flip motor can't be up higher than ground_tilt if the lift is too low.
		// Mostly a sanity check for invalid setpoints to avoid damage.
		if (liftMotor.feedbackTalon.getRawPosition() < FLIP_MIN_POS
				|| currentPos.flipPos >= Positions.GROUND_TILT.flipPos) {
			flipMotor.setSetpoint(currentPos.flipPos);
		} else {
			flipMotor.setSetpoint(Positions.GROUND.flipPos);
		}
	}

	/**
	 * Runs the closed loop motion magic controller thingy on the lift
	 */
	public void periodic() {
		// Set lastSwitchVal to do edge detection and update switch value
		lastSwitchVal = limSwitchVal;
		// Invert value from limit switch
		limSwitchVal = !limitSwitch.get();
		// If a rising edge is detected, call limSwitchPressed
		//if (limSwitchVal && !lastSwitchVal)
		//	limSwitchPressed();

		// Debugging stuff
		/*
		 * if (lastResettingDown != resettingDown) {
		 * Logging.h("Switched resettingDown to " + resettingDown); }
		 */

		// Run if the resetDown function is called. Shouldn't be needed with a string
		// pot.
		if (resettingDown) {
			if (lastResettingDown != resettingDown) {
				Logging.h("Lift is no longer active.");
			}
			liftMotor.setPower(-DOWN_SLOW_SPEED);
		} else if (active) {
			// Starting pos is a special case, since it's right at the minimum flip
			// position.
			if (currentPos == Positions.STARTING) {
				flipMotor.setSetpoint(currentPos.flipPos);
			} else {
				if (lastResettingDown != resettingDown) {
					Logging.h("Lift is active again.");
				}

				// Ensure that the flipper won't flip until it's above the top of the first
				// stage.
				if (liftMotor.feedbackTalon.getRawPosition() < FLIP_MIN_POS
						|| currentPos.flipPos >= Positions.GROUND_TILT.flipPos) {
					flipMotor.setSetpoint(currentPos.flipPos);
				} else {
					flipMotor.setSetpoint(Positions.GROUND.flipPos);
				}
			}
			// Run feedback to make sure motion magic keeps happening.
			liftMotor.runFeedback(0);
			flipMotor.runFeedback(0);
		}

		// Used to detect when resettingDown turns on/off.
		lastResettingDown = resettingDown;
	}

	/**
	 * Zero both POTs to the current position.
	 */
	public void limSwitchPressed() {
		// Special case if reset down thing is used.
		if (resettingDown) {
			stopResettingDown();
			resetError();
			Logging.h("Manual Down Hit Switch");
			Logging.h("Current Target: " + currentPos.toString());
		}
		// If resetDown isn't used, only zero for ground position.
		if (currentPos == Positions.GROUND) {
			resetError();
		}
		// Go to ground position.
		trackToPos(Positions.GROUND);
		Logging.h("Current Target: still " + currentPos.toString());
	}
	
	/**
	 * Sets the sensor position for the talons to make the closed loop error 0
	 */
	private void resetError() {
		//Set sensor postion
		liftMotor.feedbackTalon.talon.setSelectedSensorPosition((int) Positions.GROUND.liftPos, 0, 20);
		flipMotor.talon.setSelectedSensorPosition((int) Positions.GROUND.flipPos, 0, 20);
		
		//Reset momentary button on dashboard
		SmartDashboard.putBoolean("Reset Error", false);
		Logging.h("Reset Lift and Flip Error");
		//Make sure motion magic won't freak out
		flipMotor.talon.set(ControlMode.PercentOutput, 0);
	}
	
	/**
	 * Start moving the lift down to zero the pot if it's not in scale position or tilted.
	 */
	public void resetDown() {
		if (currentPos != Positions.H_SCALE && currentPos != Positions.L_SCALE && currentPos != Positions.STARTING && currentPos != Positions.GROUND_TILT) {
			resettingDown = true;
		}
	}
	
	/**
	 * Stops moving the lift downards to reset error.
	 */
	public void stopResettingDown() {
		resettingDown = false;
	}

	/**
	 * Get the total absolute error of the lift and flip motor to see if it has reached its setpoint.
	 * @return the total error of the lift and flipper
	 */
	public double getTotalError() {
		return Math.abs(liftMotor.feedbackTalon.getRawPosition() - currentPos.liftPos)
				+ Math.abs(flipMotor.getRawPosition() - currentPos.flipPos);
	}
	
	/**
	 * Writes the current position, velocity, and closed loop error to the
	 * dashboard.position
	 */
	public void logToDashboard() {
		SmartDashboard.putString("Lift setpoint name", currentPos.name());
		SmartDashboard.putNumber("lift encoder pos", liftMotor.feedbackTalon.getRawPosition());
		SmartDashboard.putNumber("lift closed loop error", liftMotor.feedbackTalon.getRawCLError());
		SmartDashboard.putNumber("flip encoder pos", flipMotor.getRawPosition());
		SmartDashboard.putNumber("flip closed loop error", flipMotor.getRawCLError());
		SmartDashboard.putNumber("flip talon output voltage", flipMotor.talon.getMotorOutputVoltage());
		SmartDashboard.putNumber("Lift motor output voltage", liftMotor.feedbackTalon.talon.getMotorOutputVoltage());
		SmartDashboard.putBoolean("Lift lim switch", limSwitchVal);
		SmartDashboard.putBoolean("Resetting Down", resettingDown);
	}

	/**
	 * Read PIDF values from the dashboard
	 */
	public void readTuningValuesFromDashboard() {
		Logging.h("Reading pid tuning values");
		liftParams.kP = SmartDashboard.getNumber("lift_kp", liftParams.kP);
		liftParams.kI = SmartDashboard.getNumber("lift_ki", liftParams.kI);
		liftParams.kD = SmartDashboard.getNumber("lift_kd", liftParams.kD);
		liftParams.kF = SmartDashboard.getNumber("lift_kf", liftParams.kF);
		liftParams.vel = (int) SmartDashboard.getNumber("lift_vel", liftParams.vel);
		liftParams.accel = (int) SmartDashboard.getNumber("lift_accel", liftParams.accel);

		flipParams.kP = SmartDashboard.getNumber("flip_kp", flipParams.kP);
		flipParams.kI = SmartDashboard.getNumber("flip_ki", flipParams.kI);
		flipParams.kD = SmartDashboard.getNumber("flip_kd", flipParams.kD);
		flipParams.kF = SmartDashboard.getNumber("flip_kf", flipParams.kF);
		flipParams.vel = (int) SmartDashboard.getNumber("flip_vel", flipParams.vel);
		flipParams.accel = (int) SmartDashboard.getNumber("flip_accel", flipParams.accel);

		setupMotionMagic();
	}

	/**
	 * write PIDF values to the dashboard
	 */
	public void addTuningToDashboard() {
		SmartDashboard.putNumber("lift_kp", liftParams.kP);
		SmartDashboard.putNumber("lift_ki", liftParams.kI);
		SmartDashboard.putNumber("lift_kd", liftParams.kD);
		SmartDashboard.putNumber("lift_kf", liftParams.kF);
		SmartDashboard.putNumber("lift_vel", liftParams.vel);
		SmartDashboard.putNumber("lift_accel", liftParams.accel);

		SmartDashboard.putNumber("flip_kp", flipParams.kP);
		SmartDashboard.putNumber("flip_ki", flipParams.kI);
		SmartDashboard.putNumber("flip_kd", flipParams.kD);
		SmartDashboard.putNumber("flip_kf", flipParams.kF);
		SmartDashboard.putNumber("flip_vel", flipParams.vel);
		SmartDashboard.putNumber("flip_accel", flipParams.accel);

		SmartDashboard.putBoolean("Reset Error", false);
	}
}
