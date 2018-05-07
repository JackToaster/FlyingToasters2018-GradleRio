package org.theflyingtoasters.hardware;

import org.theflyingtoasters.controllers.AbstractFeedbackController;
import org.theflyingtoasters.hardware.interfaces.FeedbackMotorController;
import org.theflyingtoasters.utilities.Logging;
import org.theflyingtoasters.utilities.Utilities;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

public class FeedbackTalon extends Talon implements FeedbackMotorController, Utilities.Conversions {
	static final int CONFIG_TIMEOUT_MS = 20;
	boolean isEncoderReversed = false;
	private AbstractFeedbackController feedbackController;
	boolean feedbackActive = false;
	private boolean isTalonClosedLoopMode = false;
	private double lastSetpoint = 0;

	public FeedbackTalon(int talonID) {
		super(talonID);
	}

	public FeedbackTalon(int talonID, FeedbackDevice device) {
		super(talonID);
		setFeedbackDevice(device);
	}

	public void setupTalonPIDVA(double kF, double kP, double kI, double kD, int rawVel, int rawAccel) {
		talon.config_kF(0, kF, CONFIG_TIMEOUT_MS);
		talon.config_kP(0, kP, CONFIG_TIMEOUT_MS);
		talon.config_kI(0, kI, CONFIG_TIMEOUT_MS);
		talon.config_kD(0, kD, CONFIG_TIMEOUT_MS);
		talon.configMotionCruiseVelocity(rawVel, CONFIG_TIMEOUT_MS);
		talon.configMotionAcceleration(rawAccel, CONFIG_TIMEOUT_MS);
		isTalonClosedLoopMode = true;
	}

	public void stopMotionMagic() {
		isTalonClosedLoopMode = false;
	}

	public double getRawPosition() {
		return talon.getSelectedSensorPosition(0);
	}

	public double getRawVelocity() {
		return talon.getSelectedSensorVelocity(0);
	}

	public double getRawCLError() {
		return talon.getClosedLoopError(0);
	}

	@Override
	public double getPosition() {
		double d = Distance.ENCODER_TICK.convert(talon.getSelectedSensorPosition(0), Distance.M);
		if (isEncoderReversed) {
			return -d;
		} else {
			return d;
		}
	}

	@Override
	public void setFeedbackController(AbstractFeedbackController controller) {
		feedbackController = controller;
	}

	@Override
	public void setFeedbackActive(boolean active) {
		feedbackActive = active;
	}

	@Override
	public boolean getFeedbackActive() {
		return feedbackActive;
	}

	@Override
	public void runFeedback(double deltaTime) {
		if (isTalonClosedLoopMode) {
			//Logging.h("Motion Magic Mode run!");
			talon.set(ControlMode.MotionMagic, lastSetpoint);
		} else {
			if (feedbackActive) {
				double output = feedbackController.run(getPosition(), deltaTime);
				setPower(output);
			} else {
				//Logging.l("runFeedback run with feedback inactive");
			}
		}
	}

	@Override
	public void setSetpoint(double setpoint) {
		if(isTalonClosedLoopMode) {
			if (feedbackController != null) {
				feedbackController.setSetpoint(setpoint);
			}
		}
		else talon.set(ControlMode.MotionMagic, setpoint);
		lastSetpoint = setpoint;
	}

	@Override
	public double getSetpoint() {
		return feedbackController.getSetpoint();
	}

	@Override
	public void setFeedbackDevice(FeedbackDevice device) {
		talon.configSelectedFeedbackSensor(device, 0, 1000);
	}

	@Override
	public AbstractFeedbackController getFeedbackController() {
		return feedbackController;
	}

	@Override
	public void setEncoderReversed(boolean reversed) {
		isEncoderReversed = reversed;
	}

	@Override
	public void resetEncoders() {
		talon.setSelectedSensorPosition(0, 0, CONFIG_TIMEOUT_MS);
	}

}
