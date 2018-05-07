package org.theflyingtoasters.hardware;

import org.theflyingtoasters.hardware.interfaces.CANMotorController;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class Victor implements CANMotorController {
	protected VictorSPX victor;
	protected boolean isReversed = false;
	protected double currentPower;
	public Victor(int victorID) {
		victor = new VictorSPX(victorID);
		currentPower = 0;
	}
	
	public void setInverted(boolean inverted) {
		isReversed = inverted;
	}

	@Override
	public void setPower(double power) {
		currentPower = power;
		if(isReversed) {
			victor.set(ControlMode.PercentOutput, -currentPower);
		} else {
			victor.set(ControlMode.PercentOutput, currentPower);
		}
	}

	@Override
	public double getPower() {
		return currentPower;
	}


	@Override
    public void followMaster(CANMotorController master) {
		victor.follow(master.getMotorController());
	}

	@Override
	public IMotorController getMotorController() {
		return victor;
	}
}
