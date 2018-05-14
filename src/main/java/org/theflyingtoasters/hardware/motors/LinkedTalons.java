package org.theflyingtoasters.hardware.motors;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;

public class LinkedTalons extends Talon {
	public LinkedTalons(int feedbackDeviceNumber, CanMotorController... followers) {
		super(feedbackDeviceNumber);
		for (CanMotorController follower : followers) {
			follower.followMaster(this);
		}
	}
	public LinkedTalons(FeedbackDevice feedbackDevice, int feedbackDeviceNumber, CanMotorController... followers) {
		super(feedbackDevice, feedbackDeviceNumber);
		for (CanMotorController follower : followers) {
			follower.followMaster(this);
		}
	}
}