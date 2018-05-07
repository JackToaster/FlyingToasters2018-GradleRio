package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

/**
 * Time based auton to cross the auto line. this is terrible and should never be
 * used in competition.
 * 
 * @author jack
 *
 */
public class TimeBasedAuton extends OpMode {
	final static double DRIVE_TIME = 1.35;
	final static double DRIVE_SPEED = 0.75;
	final static double DRIVE_TURN = -0.05;

	private double timer = 0;

	public TimeBasedAuton(Robot bot) {
		super(bot, "Line Auton");
	}

	public TimeBasedAuton(Robot bot, String name) {
		super(bot, name);
	}

	public void init() {
		super.init();
		timer = 0;
	}

	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
		Logging.l("timer = " + timer);
		timer += deltaTime;
		if (timer >= DRIVE_TIME) {
			earlyStop();
			endCommand();
		} else {
			robot.driveBase.drive(DRIVE_SPEED, DRIVE_TURN);
		}
	}

	private void earlyStop() {
		robot.driveBase.drive(0, 0);
	}

	public void stop() {
		super.stop();
		earlyStop();
		timer = 0;
	}

}
