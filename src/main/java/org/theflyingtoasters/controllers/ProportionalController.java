package org.theflyingtoasters.controllers;

import org.theflyingtoasters.utilities.Logging;

/**
 * proportional feedback controller. Sets the output based on a feedforward term
 * and a constant multiplied by the error.
 * 
 * @author jack
 *
 */
public class ProportionalController extends FeedForwardController {

	protected double kP;

	/**
	 * create a proportional controller with the given gains
	 * 
	 * @param proportionalGain
	 * @param feedForwardGain
	 */
	public ProportionalController(double proportionalGain, double feedForwardGain) {
		super(feedForwardGain);
		kP = proportionalGain;
	}

	/**
	 * create a proportional controller with no feedforward gain
	 * 
	 * @param proportionalGain
	 */
	public ProportionalController(double proportionalGain) {
		super(0);
		kP = proportionalGain;
	}

	/**
	 * set the gains for the proportional controller, logs an error if the wrong
	 * number are given.
	 */
	public void setGains(double... gains) {
		if (gains.length != 2) {// check to see if there are the right number
			// of values
			Logging.logMessage("Invalid number of parameters for ProportionalController.setGains",
					Logging.Priority.ERROR);

		} else {
			super.setGains(gains[0]);
			kP = gains[1];
		}
	}


	@Override
	/**
	 * returns the proper output based on the error
	 */
	public double run(double current, double deltaTime) {
		double error = current - setpoint;
		double feedForwardValue = super.run(error, deltaTime);
		double proportional = -error * kP;

		return feedForwardValue + proportional;
	}


}
