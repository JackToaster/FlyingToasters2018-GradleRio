package org.theflyingtoasters.hardware.interfaces;

import com.ctre.phoenix.motorcontrol.IMotorController;

/**
 * An abstract interface for talon SRX's and Victor SPX's, so they can work
 * together in linked things
 * 
 * @author jack
 *
 */
public interface CANMotorController extends MotorController {
	/**
	 * sets the motor controller as a follower of another can motor controller
	 * 
	 * @param master
	 *            the motor controller to follow
	 */
	public void followMaster(CANMotorController master);

	/**
	 * returns the actual motor controller object (TalonSRX or VictorSPX)
	 * 
	 * @return the IMotorController object of the controller
	 */
	public IMotorController getMotorController();
}
