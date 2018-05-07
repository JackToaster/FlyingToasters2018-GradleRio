package org.theflyingtoasters.hardware;

import org.theflyingtoasters.hardware.interfaces.CANMotorController;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Talon implements CANMotorController {
	public TalonSRX talon;
	protected boolean isReversed = false;
	protected double currentPower;
	public Talon(int talonID) {
		talon = new TalonSRX(talonID);
		currentPower = 0;
	}
	
	public void setInverted(boolean inverted) {
		isReversed = inverted;
	}

	@Override
	public void setPower(double power) {
		currentPower = power;
		if(isReversed) {
			talon.set(ControlMode.PercentOutput, -currentPower);
		} else {
			talon.set(ControlMode.PercentOutput, currentPower);
		}
	}

	@Override
	public double getPower() {
		return currentPower;
	}

	public void setCurrentLimit(int amps){
		talon.configContinuousCurrentLimit(amps, 100);
		talon.configPeakCurrentLimit(amps, 100);
		talon.configPeakCurrentDuration(100, 100);
	}
	
	
	public void enableCurrentLimit(boolean enable){
		talon.enableCurrentLimit(enable);
	}

	@Override
	public void followMaster(CANMotorController master) {
		talon.follow(master.getMotorController());
	}

	@Override
	public IMotorController getMotorController() {
		return talon;
	}
}
