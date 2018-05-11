package org.theflyingtoasters.hardware.motors;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Talon extends TalonSRX implements MotorController, CanMotorController{
	static final int configTimeoutMs = 20;

	public Talon(int deviceNumber) {
		super(deviceNumber);
		// TODO Auto-generated constructor stub
	}

	public Talon(CanMotorID.TalonID talonID) {
		this(talonID.canID);
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

	@Override
	public void followMaster(CanMotorController master) {
		follow(master.getIMotorController());
	}

	@Override
	public IMotorController getIMotorController() {
		return this;
	}
}
