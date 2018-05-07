package org.theflyingtoasters.hardware.interfaces;

import java.util.ArrayList;


/**
 * Drivebase class for all of your driving needs
 * 
 * @author jackf
 *
 */

public abstract class DriveBase {
	protected boolean isFeedbackActive = false;
	// these two should be mutually exclusive.
	private ArrayList<MotorController> controllers;
	private ArrayList<FeedbackMotorController> feedbackControllers;

	/**
	 * constructor. It creates the arraylists of motor controllers to update every
	 * loop.
	 */
	public DriveBase() {
		controllers = new ArrayList<MotorController>();
		feedbackControllers = new ArrayList<FeedbackMotorController>();
	}

	/**
	 * override this with whatever stuff
	 * 
	 * @param inputs
	 *            the inputs used to drive
	 */
	abstract public void drive(double... inputs);

	/**
	 * add a motor controller to the list of motor controllers. This should be
	 * called for every drivebase motor controller.
	 * 
	 * @param mc
	 *            the motor controller to add
	 */
	protected void registerMotorController(MotorController mc) {
		if (mc instanceof FeedbackMotorController) {
			FeedbackMotorController fmc = (FeedbackMotorController) mc;
			feedbackControllers.add(fmc);
		} else {
			controllers.add(mc);
		}
	}

	/**
	 * udpate should be called periodically. it will update the feedback control
	 * loops for the motor controllers.
	 * 
	 * @param dT
	 *            the time elapsed since the last call to this method
	 */
	public void update(double dT) {
		for (FeedbackMotorController c : feedbackControllers) {
			if (c.getFeedbackActive()) {
				c.runFeedback(dT);
			}
		}
	};

	/**
	 * activate or deactivate the feedback control on ALL motor controllers in the
	 * drivebase
	 * 
	 * @param active
	 *            whether feedback should be active or deactive
	 */
	public void setFeedbackActive(boolean active) {
		isFeedbackActive = active;
		for (FeedbackMotorController c : feedbackControllers) {
			c.setFeedbackActive(active);
		}
		update(0);
	}

	/**
	 * gets the state of the feedback control.
	 * 
	 * @return whether the feedback control is active.
	 */
	public boolean getFeedbackActive() {
		return isFeedbackActive;
	}
}
