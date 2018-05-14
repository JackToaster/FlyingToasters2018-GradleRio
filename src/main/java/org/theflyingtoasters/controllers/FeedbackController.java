package org.theflyingtoasters.controllers;

/**
 * abstract interface for any feedback controller, whether it's just a
 * feedforward or a PID or something else.
 * 
 * @author jackf
 *
 */
public interface FeedbackController {
	/**
	 * sets the gains of the controller
	 * 
	 * @param gains
	 *            the gains of the different terms of the feedback/forward control.
	 */
	void setGains(double... gains);

	/**
	 * sets the setpoint of the controller
	 * 
	 * @param setpoint
	 *            the point to target with the controller
	 */
	void setSetpoint(double setpoint);

	/**
	 * get the setpoint
	 * 
	 * @return the setpoint
	 */
	double getSetpoint();
	/**
	 * returns the output given the current position and time since last run
	 * @param current the current measurement from the system
	 * @param deltaTime the time elapsed since the last run
	 * @return the output that should be applied to the system
	 */
	double run(double current, double deltaTime);
	
	/**
	 * resets the controller.
	 */
	void reset();
}
