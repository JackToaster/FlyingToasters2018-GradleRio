package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.controllers.PIDcontroller;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

/**
 * An autonomous mode which tries to drive a distance using only a PID
 * controller. This is terrible and should never be used in competition.
 * 
 * @author jack
 *
 */
public class PIDDistanceAuton extends OpMode {
	final static double dist_m = 4.0;

	public PIDcontroller leftPID;
	public PIDcontroller rightPID;

	public PIDDistanceAuton(Robot bot) {
		super(bot, "PID distance auton");
		leftPID = new PIDcontroller(0.5, 0.1, .1);
		rightPID = new PIDcontroller(0.5, 0.1, .1);
	}

	public void init() {
		super.init();
		robot.driveBase.left.setFeedbackController(leftPID);
		robot.driveBase.right.setFeedbackController(rightPID);
		double initLeft = robot.driveBase.left.getPosition();
		robot.driveBase.left.setSetpoint(initLeft + dist_m);
		double initRight = robot.driveBase.right.getPosition();
		robot.driveBase.right.setSetpoint(initRight + dist_m);
		robot.driveBase.left.setFeedbackActive(true);
		robot.driveBase.right.setFeedbackActive(true);
	}

	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
		Logging.l("Left pos: " + robot.driveBase.left.getPosition() + ", right pos: "
				+ robot.driveBase.right.getPosition());
	}

	public void stop() {
		robot.driveBase.left.setFeedbackActive(false);
		robot.driveBase.right.setFeedbackActive(false);
	}
}
