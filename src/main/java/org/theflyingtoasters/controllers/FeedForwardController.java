package org.theflyingtoasters.controllers;

import org.theflyingtoasters.utilities.Logging;

/**
 * FeedForward controller class, returns a constant value with the opposite sign
 * as the error.
 * 
 * @author jackf
 *
 */
public class FeedForwardController implements FeedbackController {
	protected double kFeedForward = 0;
	protected double setpoint;

	/**
	 * create a new feedforward controller with the proper gain
	 * 
	 * @param feedForwardGain
	 */
	public FeedForwardController(double feedForwardGain) {
		kFeedForward = feedForwardGain;
	}

	@Override
	/**
	 * set the gains for the feedforward controller, logs an error if the wrong
	 * number are given.
	 */
	public void setGains(double... gains) {
		if (gains.length != 1) { // check to see if there are the right number
									// of values
			Logging.logMessage("Invalid number of parameters for FeedForwardController.setGains",
					Logging.Priority.ERROR);
		} else {// set the value
			kFeedForward = gains[0];
		}
	}

	@Override
	public double run(double current, double deltaTime) {
		double error = current - setpoint;
		boolean sign = error > 0;
		if (sign)
			return -kFeedForward;
		else
			return kFeedForward;
	}

	@Override
	/**
	 * does literally nothing.
	 */
	public void reset() {
	}

	@Override
	public void setSetpoint(double setpoint) {
		this.setpoint = setpoint;
	}

	@Override
	public double getSetpoint() {
		return setpoint;
	}
}
