package org.theflyingtoasters.hardware;

import com.ctre.phoenix.motorcontrol.NeutralMode;

import com.ctre.phoenix.sensors.PigeonIMU;
import edu.wpi.first.wpilibj.Servo;

public class Climber {

	private static final int RIGGING_PORT = 2;
	private static final int FORK_PORT = 9;
	
	//TODO: Set the actual servo position values.
	private static final double RIGGING_CLOSED = 0.5;
	private static final double RIGGING_OPEN = 1.5;
	private static final double FORKS_CLOSED = 0.25;
	private static final double FORKS_OPEN = 0.75;
	
	private boolean riggingReleased;
	
	private static final int LEFT = 10;
	private static final int RIGHT = 11;
	
	private Servo riggingServo;
	private Servo forkServo;
	
	private Talon left = new Talon(LEFT);
	private Talon right = new Talon(RIGHT);

	public Climber() {
		IMU.pigeon = new PigeonIMU(right.talon);

		riggingServo = new Servo(RIGGING_PORT);
		forkServo = new Servo(FORK_PORT);
		
		closeServos();

		left.setInverted(false);
		right.setInverted(true);
		
		left.talon.setNeutralMode(NeutralMode.Brake);
		right.talon.setNeutralMode(NeutralMode.Brake);
	}
	
	public void setSpeed(double speed) {
		setSpeed(speed, speed);
	}
	
	public void setSpeed(double left, double right) {
		if(true || riggingReleased) {
			this.left.setPower(left);
			this.right.setPower(right);
		}
	}
	
	public void closeServos() {
		riggingReleased = false;
		forkServo.set(FORKS_CLOSED);
		riggingServo.set(RIGGING_CLOSED);
	}
	
	public void releaseRigging() {
		riggingReleased = true;
		riggingServo.set(RIGGING_OPEN);
	}

	public void releaseForks() {
		forkServo.set(FORKS_OPEN);
	}
}
