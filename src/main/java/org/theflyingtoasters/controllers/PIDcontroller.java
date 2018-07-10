package org.theflyingtoasters.controllers;

import org.theflyingtoasters.utilities.Logging;

/**
 * PID controller class. Most basic possible, no deadbands or anything silly
 * like that. If you want fancy stuff like deadbands, create a new PID class
 * that extends this one.
 *
 * @author jackf
 *
 */

// TODO add fancy class that does all the stuff the old PID did
public class PIDcontroller extends ProportionalController implements AbstractFeedbackController {
	protected double kI;
	protected double kD;
	private boolean dOnMeasurement = false;
	protected double maxIntegral;
	protected boolean limitIntegral = false;
	protected double integral = 0;
	protected double lastReading = 0;
	protected double lastError = 0;

	/**
	 * create a PID controller with no feedforward
	 * 
	 * @param pGain
	 * @param iGain
	 * @param dGain
	 */
	public PIDcontroller(double pGain, double iGain, double dGain) {
		this(pGain, iGain, dGain, 0);
	}

	/**
	 * create a pid with no feedforward and an I limit
	 * 
	 * @param pGain
	 * @param iGain
	 * @param dGain
	 * @param maxI
	 * @param limitI
	 *            whether to use an I limit. Probably should be true if you're using
	 *            this constructor
	 */
	public PIDcontroller(double pGain, double iGain, double dGain, double maxI, boolean limitI) {
		this(pGain, iGain, dGain, 0);
		maxIntegral = maxI;
		limitIntegral = limitI;
	}

	/**
	 * create a pid with no feedforward and an I limit and a feedforward gain
	 * 
	 * @param pGain
	 * @param iGain
	 * @param dGain
	 * @param ffGain
	 * @param maxI
	 * @param limitI
	 *            whether to use an I limit.
	 */
	public PIDcontroller(double pGain, double iGain, double dGain, double ffGain, double maxI, boolean limitI) {
		this(pGain, iGain, dGain, ffGain);
		maxIntegral = maxI;
		limitIntegral = limitI;
	}

	/**
	 * set the I limit
	 * 
	 * @param maxI
	 *            the maximum output from the integral term
	 */
	public void setILimit(double maxI) {
		maxIntegral = maxI;
		limitIntegral = true;
	}

	/**
	 * set whether to use the integral term limit
	 * 
	 * @param limit
	 *            whether or not to limit it
	 */
	public void limitI(boolean limit) {
		limitIntegral = limit;
	}

	/**
	 * set whether to calculate the derivative term from the derivative of the error
	 * or the derivative of the measurement.
	 * 
	 * @param onMeasurement
	 *            set true to use the derivative of the measurement, set false for
	 *            derivative of error.
	 */
	public void setDOnMeasurement(boolean onMeasurement) {
		dOnMeasurement = onMeasurement;
	}

	/**
	 * create a PID controller with feedforward
	 * 
	 * @param pGain
	 * @param iGain
	 * @param dGain
	 * @param ffGain
	 */
	public PIDcontroller(double pGain, double iGain, double dGain, double ffGain) {
		super(pGain, ffGain);
		kI = iGain;
		kD = dGain;
	}

	@Override
	/**
	 * set the gains for the proportional controller, logs an error if the wrong
	 * number are given.
	 */
	public void setGains(double... gains) {
		if (gains.length != 3 && gains.length != 4) {// check to see if there are the right number
			// of values
			Logging.logMessage("Invalid number of parameters for PIDcontroller.setGains", Logging.Priority.ERROR);

		} else {
			if (gains.length == 4) {
				super.setGains(gains[0], gains[1]);
				kI = gains[2];
				kD = gains[3];
			} else {
				super.setGains(0, gains[0]);
				kI = gains[1];
				kD = gains[2];
			}
		}
	}

	@Override
	/**
	 * calculate the output of the PID loop
	 */
	public double run(double current, double deltaTime) {
		double error = current - setpoint;
		// continue integrating
		integral += error * deltaTime;
		if (limitIntegral) {
			if (Math.abs(integral) > maxIntegral / kI) {
				if (integral > 0) {
					integral = maxIntegral / kI;
				} else {
					integral = -maxIntegral / kI;
				}
			}
		}

		double deltaError;
		if (dOnMeasurement) {
			// find the change in reading / time (dError / dT)
			deltaError = (current - lastReading) / deltaTime;
		} else {
			deltaError = (error - lastError) / deltaTime;
		}

		// calculate the proportional + FF part of the PID
		double proportionalValue = super.run(current, deltaTime);

		// calculate the integral + derivative parts
		double integralValue = -integral * kI;
		double derivativeValue = -deltaError * kD;

		// set the last error for next loop
		lastReading = current;
		lastError = error;
		// return the value
		return proportionalValue + integralValue + derivativeValue;
	}


	@Override
	/**
	 * reset the integral & derivative terms
	 */
	public void reset() {
		integral = 0;
		lastReading = 0;
	}

	@Override
	/**
	 * log the current status of the controller
	 */
	public void logStatus() {
		Logging.logMessage("Proportional controller running with gains: kP=" + kP + ", kI=" + kI + ", kD=" + kD
				+ ", kFeedForward=" + kFeedForward, Logging.Priority.LOW);
	}
}
