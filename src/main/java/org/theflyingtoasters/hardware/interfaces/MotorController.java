package org.theflyingtoasters.hardware.interfaces;

//TODO implement current limits
/**
 * abstract interface for a motor controller.
 * 
 * @author jack
 *
 */
public interface MotorController {
	/**
	 * set the voltage of the motor controller
	 * 
	 * @param power
	 *            the power (-1 to 1) of the motor controller.
	 */
	void setPower(double power);

	/**
	 * 
	 * @return the power last set to the motor controller
	 */
	double getPower();

	/**
	 * sets the motor controller to reversed/forward
	 * 
	 * @param inverted
	 *            whether the motor is reversed or not.
	 */
	void setInverted(boolean inverted);
}
