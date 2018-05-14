package org.theflyingtoasters.hardware;

import org.theflyingtoasters.hardware.Lift.Positions;
import org.theflyingtoasters.hardware.motors.Talon;
import org.theflyingtoasters.utilities.Logging;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Intake class
 * 
 * @author ben
 *
 */
public class Intake {

	/* 2 motors for intake */
	static final int leftMotorID = 9;
	static final int rightMotorID = 6;
	static final int cubeSwitchPort = 0;

	private Talon leftTalon;
	private Talon rightTalon;
	private DigitalInput cubeSwitch;
	private boolean currentSwitchStatus = false;
	
	boolean autoliftEnabled = true;
	
	//Used to delay lifting
	//private DelayedCommand delayLift;
	
	//Used for lifting slightly when a cube is gotten
	private Lift lift;

	private State currentState = State.RESTING;
	private double time, timeWithCube;
	private final double timeWithoutCube = .548;
	private final double maxRecoveryTime = 1;

	private final double defaultInSpeed = 1.0;
	private double defaultOutSpeed = 0.5;
	private double manualOutSpeed = defaultOutSpeed;
	
	private final int MAX_INTAKE_VELOCITY = 13500;
	
	private class IntakeTalonParams {
		double kP = 0.03;
		double kI = 0.0001;
		double kD = 0;
		double kF = 0.03;
		int vel = 0;
		int accel = 0;
	}
	
	IntakeTalonParams intakeParams = new IntakeTalonParams();
	
	public static enum State {
		INTAKING, OUTPUTTING, RESTING, RESTING_WITH_CUBE, HAS_CUBE, RESET, RECOVERY, OUTPUTTING_MANUAL, FLUTTER_OUT, FLUTTER_PAUSE, START_FLUTTER_OUT,
	}

	public Intake(Lift lift) {
		addTuningToDashboard();
	
		leftTalon = new Talon(leftMotorID);
		rightTalon = new Talon(rightMotorID);
		
		leftTalon.configContinuousCurrentLimit(20, 10);
		rightTalon.configContinuousCurrentLimit(20, 10);
		
		leftTalon.configPeakCurrentLimit(20, 10);
		rightTalon.configPeakCurrentLimit(20, 10);
		
		leftTalon.configPeakCurrentDuration(1000, 10);
		rightTalon.configPeakCurrentDuration(1000, 10);
		
		leftTalon.enableCurrentLimit(true);
		rightTalon.enableCurrentLimit(true);
		
		leftTalon.setNeutralMode(NeutralMode.Brake);
		rightTalon.setNeutralMode(NeutralMode.Brake);
		
		leftTalon.setInverted(false);
		rightTalon.setInverted(true);
		//rightTalon.talon.setInverted(true);
		
		leftTalon.setupSensor(FeedbackDevice.CTRE_MagEncoder_Absolute);
		rightTalon.setupSensor(FeedbackDevice.CTRE_MagEncoder_Absolute);
		
		leftTalon.setupPID(intakeParams.kP, intakeParams.kI, intakeParams.kD, intakeParams.kF); 
		leftTalon.setupMotionMagic(intakeParams.vel, intakeParams.accel);
		rightTalon.setupPID(intakeParams.kP, intakeParams.kI, intakeParams.kD, intakeParams.kF);
		rightTalon.setupMotionMagic(intakeParams.vel, intakeParams.accel);

		
		intakeParams = new IntakeTalonParams();
		
		cubeSwitch = new DigitalInput(cubeSwitchPort);
		this.lift = lift;
	}

	/**
	 * Method that sets power to the motor
	 * 
	 * @param power
	 *            assigned to the motor
	 */
	public void setPower(double power) {
		leftTalon.setPower(power);
		rightTalon.setPower(power);
	}
	
	public void setVelocity(double velocity) {
		velocity *= MAX_INTAKE_VELOCITY;
		leftTalon.set(ControlMode.Velocity, velocity);
		rightTalon.set(ControlMode.Velocity, -velocity);
		SmartDashboard.putNumber("Intake Target Velocity", velocity);
		SmartDashboard.putNumber("Left Intake Velocity Error", velocity - leftTalon.getSelectedSensorVelocity(0));
		SmartDashboard.putNumber("Right Intake Velocity Error", velocity - rightTalon.getSelectedSensorVelocity(0));
	}
	
	public void periodic(double deltaTime) {
		pollSwitch();
		switch (currentState) {
		case RECOVERY:
			time += deltaTime;
			if (time >= maxRecoveryTime) {
				setState(State.RESET);
			}
		case INTAKING:
			setPower(-defaultInSpeed);
			if(!hasCube()) {
				timeWithCube = 0;
			} else {
				timeWithCube += deltaTime;
			}
			if (hasCube() && timeWithCube >= 0.25) {
				setState(State.HAS_CUBE);
			}
			break;
		case OUTPUTTING:
			setPower(defaultOutSpeed);
			if (hasCube()) {
				time = 0;
			} else {
				time += deltaTime;
			}
			if (time >= timeWithoutCube) {
				setState(State.RESET);
			}
			break;
		case OUTPUTTING_MANUAL:
			setVelocity(manualOutSpeed);
			break;
		case RESET:
			setPower(0);
			time = 0;
			//If we lose the cube in GROUND_TILT for any reason, return to GROUND
			if(lift.currentPos == Positions.GROUND_TILT) lift.trackToPos(Positions.GROUND);
			setState(State.RESTING);
			break;
		case HAS_CUBE:
			setVelocity(0);
			//Lift up a bit if it's at the ground to avoid damage or losing the cube
			if(lift.currentPos == Positions.GROUND && autoliftEnabled) lift.trackToPos(Positions.GROUND_TILT);
			setState(State.RESTING_WITH_CUBE);
			break;
		case RESTING_WITH_CUBE:
			time = 0;
			if (!hasCube()) {
				setState(State.RECOVERY);
			}
			break;
		case START_FLUTTER_OUT:
			time = 0;
			setState(State.FLUTTER_OUT);
		case FLUTTER_OUT:
			time += deltaTime;
			if(time < .1) {
				setVelocity(.37);
			} else {
				time = 0;
				setState(State.FLUTTER_PAUSE);
			}
			break;
		case FLUTTER_PAUSE:
			time += deltaTime;
			if(time < .1) {
				setPower(0);
			} else {
				time = 0;
				setState(State.FLUTTER_OUT);
			}
			break;
		case RESTING:
			time = 0;
			break;
		}
		SmartDashboard.putString("Intake State", currentState.toString());
		SmartDashboard.putBoolean("Has Cube?", hasCube());
		SmartDashboard.putNumber("Intake Time", time);
		SmartDashboard.putNumber("Left Intake Velocity", leftTalon.getSelectedSensorVelocity(0));
		SmartDashboard.putNumber("Right Intake Velocity", rightTalon.getSelectedSensorVelocity(0));
	}

	
	public void enableAutolift(boolean e) {
		autoliftEnabled = e;
	}

	public void setState(State newState) {
		Logging.h("Switching Intake from " + currentState.toString() + " to " + newState.toString());
		currentState = newState;
		time = 0;
	}
	
	public void setOutputSpeed(double speed) {
		//Logging.h("Set Intake output velocity to " + speed);
		manualOutSpeed = speed;
	}

	public void setOutputPower(double speed) {
		//Logging.h("Set Intake output power to " + speed);
		defaultOutSpeed = speed;
	}

	public void pollSwitch() {
		currentSwitchStatus = !cubeSwitch.get();
	}

	public State getState() {
		return currentState;
	}

	public boolean hasCube() {
		return currentSwitchStatus;
	}
	
	public void readTuningValuesFromDashboard() {
		Logging.h("Reading pid tuning values");
		
		intakeParams.kP = SmartDashboard.getNumber("intake_kp", intakeParams.kP);
		intakeParams.kI = SmartDashboard.getNumber("intake_ki", intakeParams.kI);
		intakeParams.kD = SmartDashboard.getNumber("intake_kd", intakeParams.kD);
		intakeParams.kF = SmartDashboard.getNumber("intake_kf", intakeParams.kF);
		intakeParams.vel = (int) SmartDashboard.getNumber("intake_vel", intakeParams.vel);
		intakeParams.accel = (int) SmartDashboard.getNumber("intake_accel", intakeParams.accel);
		
		leftTalon.setupPID(intakeParams.kP, intakeParams.kI, intakeParams.kD, intakeParams.kF); 
		leftTalon.setupMotionMagic(intakeParams.vel, intakeParams.accel);
		rightTalon.setupPID(intakeParams.kP, intakeParams.kI, intakeParams.kD, intakeParams.kF);
		rightTalon.setupMotionMagic(intakeParams.vel, intakeParams.accel);
	}
	
	public void addTuningToDashboard() {
		SmartDashboard.putNumber("intake_kp", intakeParams.kP);
		SmartDashboard.putNumber("intake_ki", intakeParams.kI);
		SmartDashboard.putNumber("intake_kd", intakeParams.kD);
		SmartDashboard.putNumber("intake_kf", intakeParams.kF);
		SmartDashboard.putNumber("intake_vel", intakeParams.vel);
		SmartDashboard.putNumber("intake_accel", intakeParams.accel);
	}
	
}
