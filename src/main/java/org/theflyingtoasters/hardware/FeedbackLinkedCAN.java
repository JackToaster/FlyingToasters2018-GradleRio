package org.theflyingtoasters.hardware;


import org.theflyingtoasters.controllers.AbstractFeedbackController;
import org.theflyingtoasters.hardware.interfaces.CANMotorController;
import org.theflyingtoasters.hardware.interfaces.FeedbackMotorController;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;

/**
 * Linked can motor controllers with feedback, works in master/slave mode
 * @author jack
 *
 */
public class FeedbackLinkedCAN extends LinkedCANMotorControllers implements FeedbackMotorController {
	public FeedbackTalon feedbackTalon;
	private AbstractFeedbackController feedbackController;
	private boolean feedbackActive = false;
	/**
	 * Creates a new set of linked talons.
     *
     * @param controllers each motor controller to follow the feedback talon
	 */
	public FeedbackLinkedCAN(FeedbackTalon feedback, CANMotorController... controllers) {
		//initializes the controllers and stuff
		super(controllers);
		feedbackTalon = feedback;
		//set the motor controllers to follow the master talon
		for(CANMotorController mc : motorControllers) {
            mc.followMaster(feedbackTalon);
		}
	}
	
	public FeedbackLinkedCAN(FeedbackDevice device, int feedbackTalonID, CANMotorController... controllers){
		this(new FeedbackTalon(feedbackTalonID, device), controllers);
		feedbackTalon.setFeedbackDevice(device);
	}

	@Override
	/**
	 * Set output power of all of the talons.
	 * 
	 * @param power
	 *            The power to set each of the talons to.
	 */
	public void setPower(double power) {
		if(isReversed) {
			feedbackTalon.setPower(-power);
		} else {
			feedbackTalon.setPower(power);
		}
	}

	@Override
	public double getPower() {
		
		return currentPower;
	}

	@Override
	public double getPosition() {
		return feedbackTalon.getPosition();
	}

	@Override
	public void setFeedbackDevice(FeedbackDevice device) {
		feedbackTalon.setFeedbackDevice(device);
	}

	@Override
	//set the feedback controller,
	public void setFeedbackController(AbstractFeedbackController controller) {
		feedbackController = controller;
		feedbackTalon.setFeedbackController(controller);
	}


	@Override
	public void setFeedbackActive(boolean active) {
		feedbackTalon.feedbackActive = active;
		feedbackActive = active;
	}

	@Override
	public boolean getFeedbackActive() {
		return feedbackActive;
	}

	@Override
	public void setSetpoint(double setpoint) {
		feedbackTalon.setSetpoint(setpoint);		
	}

	@Override
	public double getSetpoint() {
		return feedbackTalon.getSetpoint();
	}

	@Override
	public void runFeedback(double deltaTime) {
		feedbackTalon.runFeedback(deltaTime);
	}
	
	public void setCurrentLimit(int amps){
		super.setCurrentLimit(amps);
		feedbackTalon.setCurrentLimit(amps);
	}
	
	public void enableCurrentLimit(boolean enable){
		super.enableCurrentLimit(enable);
		feedbackTalon.enableCurrentLimit(enable);
	}

	@Override
	public AbstractFeedbackController getFeedbackController() {
		return feedbackController;
	}

	@Override
	public void setEncoderReversed(boolean reversed) {
		feedbackTalon.setEncoderReversed(reversed);		
	}

	@Override
	public void resetEncoders() {
		feedbackTalon.resetEncoders();
	}
	@Override
	public void setInverted(boolean inverted) {
		feedbackTalon.setInverted(inverted);
	}
}
