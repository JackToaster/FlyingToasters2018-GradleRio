package org.theflyingtoasters.hardware.motors;

import org.theflyingtoasters.controllers.FeedbackController;
import org.theflyingtoasters.utilities.Logging;
import org.theflyingtoasters.utilities.Utilities.Conversions;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Talon extends TalonSRX implements MotorController, CanMotorController{
	static final int configTimeoutMs = 20;
	
	private FeedbackController feedbackController;
	
	public Talon(int deviceNumber) {
		super(deviceNumber);
	}
	public Talon(FeedbackDevice feedbackDevice, int deviceNumber) {
		super(deviceNumber);
		setupSensor(feedbackDevice);
	}

	public void setupSensor(FeedbackDevice device) {
		configSelectedFeedbackSensor(device, 0, configTimeoutMs);
	}

	public void setupPID(double kP, double kI, double kD, double kF) {
		config_kP(0, kP, configTimeoutMs);
		config_kI(0, kI, configTimeoutMs);
		config_kD(0, kD, configTimeoutMs);
		config_kF(0, kF, configTimeoutMs);
	}
	
	public void setupMotionMagic(double maxVel, double accel) {
		//Multiply by 10 to convert from units/s^2 to units/100ms/s
		configMotionCruiseVelocity((int) (maxVel * 10), configTimeoutMs);
		configMotionAcceleration((int) (accel * 10), configTimeoutMs);
	}
	
	public void setPower(double pctPower) {
		set(ControlMode.PercentOutput, pctPower);
	}
	
	public void setMotionMagic(double targetPosition) {
		set(ControlMode.MotionMagic, targetPosition);
	}
	
	public double getPosition() {
		return getSelectedSensorPosition(0);
	}
	
	public void setCurrentLimit(double maxCurrent) {
		enableCurrentLimit(true);
		configContinuousCurrentLimit((int)maxCurrent, configTimeoutMs);
		configPeakCurrentLimit((int)maxCurrent, configTimeoutMs);
		configPeakCurrentDuration(1, configTimeoutMs);
	}
	
	public void setCurrentLimit(double peakCurrent, double peakTimeSec, double continuousCurrent) {
		enableCurrentLimit(true);
		configContinuousCurrentLimit((int)continuousCurrent, configTimeoutMs);
		configPeakCurrentLimit((int)peakCurrent, configTimeoutMs);
		configPeakCurrentDuration((int) peakTimeSec * 1000, configTimeoutMs);
	}
	
	@Override
	public void followMaster(CanMotorController master) {
		follow(master.getIMotorController());
	}

	@Override
	public IMotorController getIMotorController() {
		return this;
	}
	
	public int getRawSensorPosition() {
		return getSelectedSensorPosition(0);
	}
	
	public int getRawSensorVelocity() {
		return getSelectedSensorVelocity(0);
	}
	
	public double getSensorPosition(Conversions.Distance distUnit) {
		return Conversions.Distance.ENCODER_TICK.convert(getRawSensorPosition(), distUnit);
	}
	
	public double getSensorVelocity(Conversions.Velocity velUnit) {
		return Conversions.Velocity.ENCODER_TPS.convert(getRawSensorPosition(), velUnit);
	}
	
	public double getSensorPosition() {
		return Conversions.Distance.ENCODER_TICK.convert(getRawSensorPosition(), Conversions.Distance.M);
	}
	
	public double getSensorVelocity() {
		return Conversions.Velocity.ENCODER_TPS.convert(getRawSensorPosition(), Conversions.Velocity.M_S);
	}
	
	public double getRawCLError() {
		return getClosedLoopError(0);
	}
	
	public void setFeedbackController(FeedbackController controller) {
		feedbackController = controller;
	}
	
	public void runFeedback(double input, double setpoint, double deltaTime) {
		if(feedbackController != null) {
		feedbackController.setSetpoint(setpoint);
		set(ControlMode.PercentOutput, feedbackController.run(input, deltaTime));
		} else {
			Logging.e("Feedback controller not set, cannot run feedback");
		}
	}
	public void runFeedback(double setpoint, double deltaTime) {
		if(feedbackController != null) {
		feedbackController.setSetpoint(setpoint);
		set(ControlMode.PercentOutput, feedbackController.run(getSensorPosition(), deltaTime));
		} else {
			Logging.e("Feedback controller not set, cannot run feedback");
		}
	}
}
