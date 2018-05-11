package org.theflyingtoasters.hardware.motors;

import com.ctre.phoenix.motorcontrol.IMotorController;

public interface CanMotorController {
	void followMaster(CanMotorController master);
	IMotorController getIMotorController();
}
