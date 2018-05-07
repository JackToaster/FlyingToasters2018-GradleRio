package org.theflyingtoasters.hardware;

import org.theflyingtoasters.hardware.interfaces.CANMotorController;
import org.theflyingtoasters.hardware.interfaces.MotorController;

/**
 * Linked can motor controllers, works by setting all motor controllers since there is no one slave.
 * @author jack
 *
 */
public class LinkedCANMotorControllers implements MotorController {
	protected boolean isReversed = false;
	protected int numberOfTalons;
	protected CANMotorController[] motorControllers;
	protected double currentPower = 0;
	
	/**
	 * Creates a new set of linked talons.
	 * 
	 * @param talonIDs
	 *            Each of the IDs you want to control.
	 */
	public LinkedCANMotorControllers(CANMotorController... controllers) {
		
		numberOfTalons = controllers.length;
		motorControllers = controllers;
	}

	@Override
	/**
	 * Set output power of all of the talons.
	 * 
	 * @param power
	 *            The power to set each of the talons to.
	 */
	public void setPower(double power) {
		currentPower = power;
		for (CANMotorController controller : motorControllers) {
			if(isReversed) {
				controller.setPower(-power);
			} else {
				controller.setPower(power);
			}
		}
	}

	@Override
	public double getPower() {
		return currentPower;
	}
	
	/**
	 * Sets the current limit for every TalonSRX. ONLY WORKS FOR TALONS!!!
	 * @param amps
	 */
	public void setCurrentLimit(int amps){
		for(CANMotorController ct : motorControllers){
			if(ct instanceof Talon) {
				((Talon)ct).setCurrentLimit(amps);
			}
		}
	}
	
	/**
	 * enables/disables the current limit for every TalonSRX. ONLY WORKS FOR TALONS!!!
	 * @param enable
	 */
	public void enableCurrentLimit(boolean enable){
		for(CANMotorController ct : motorControllers){
			if(ct instanceof Talon) {
				((Talon)ct).enableCurrentLimit(enable);
			}
		}
	}

	@Override
	public void setInverted(boolean inverted) {
		isReversed = inverted;
	}
}
