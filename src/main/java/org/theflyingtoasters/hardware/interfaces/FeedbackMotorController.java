package org.theflyingtoasters.hardware.interfaces;

import org.theflyingtoasters.controllers.AbstractFeedbackController;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;

/**
 * Abstract interface for a motor controller with an encoder/potentiometer for
 * feedback. Allows for use of an AbstractFeedbackController for closed loop
 * control.
 * 
 * @author jack
 *
 */
public interface FeedbackMotorController extends MotorController {
	/**
	 * 
	 * @return the position from the encoder
	 */
	double getPosition();

	/**
	 * resets the encoder position to zero.
	 */
	void resetEncoders();

	/**
	 * sets the feedback device.
	 * 
	 * @param device
	 *            The feedback device to set.
	 */
	void setFeedbackDevice(FeedbackDevice device);

	/**
	 * sets an AbstractFeedbackController to control the motor controller
	 * 
	 * @param controller
	 *            the feedback controller to use for closed loop control
	 */
	void setFeedbackController(AbstractFeedbackController controller);

	/**
	 * 
	 * @return the feedback controller used for closed loop control
	 */
	public AbstractFeedbackController getFeedbackController();

	/**
	 * enables/disables closed loop control
	 * 
	 * @param active
	 */
	void setFeedbackActive(boolean active);

	/**
	 * 
	 * @return whether closed loop control is active
	 */
	boolean getFeedbackActive();

	/**
	 * sets the setpoint of the feedback controller
	 * 
	 * @param setpoint
	 *            the setpoint to set to the feedback controller
	 */
	void setSetpoint(double setpoint);

	/**
	 * 
	 * @return the setpoint of the feedback controller
	 */
	double getSetpoint();

	/**
	 * runs the closed loop feedback control.
	 * 
	 * @param deltaTime
	 *            the amount of time elapsed since the last run of runFeedback
	 */
	void runFeedback(double deltaTime);

	/**
	 * sets the encoder to be forward/reversed/
	 * 
	 * @param reversed
	 *            whether or not to reverse the encoder output.
	 */
	void setEncoderReversed(boolean reversed);
}
