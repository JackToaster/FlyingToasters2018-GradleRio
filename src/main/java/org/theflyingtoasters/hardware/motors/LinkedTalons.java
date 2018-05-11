package org.theflyingtoasters.hardware.motors;

import hardware.interfaces.CanMotorController;

public class LinkedTalons extends Talon {
	public LinkedTalons(int feedbackDeviceNumber, CanMotorController... followers) {
		super(feedbackDeviceNumber);
		for (CanMotorController follower : followers) {
			follower.followMaster(this);
		}
	}
}