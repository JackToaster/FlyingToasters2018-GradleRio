package org.theflyingtoasters.hardware.motors;

import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import hardware.interfaces.CanMotorController;

public class Victor extends VictorSPX implements CanMotorController{

	public Victor(int deviceNumber) {
		super(deviceNumber);
		// TODO Auto-generated constructor stub
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
