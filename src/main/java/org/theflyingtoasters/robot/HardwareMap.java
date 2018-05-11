package org.theflyingtoasters.robot;

public class HardwareMap {
	public enum CanMotorID {
		test(0);
		
		int canID;
		CanMotorID(int canID) {
			this.canID = canID;
		}
	}
}
