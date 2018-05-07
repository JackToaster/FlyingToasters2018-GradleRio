package org.theflyingtoasters.commands.autonomous;

import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

/**
 * Simplest auton. Does nothing, prints strings.
 * 
 * @author jack
 *
 */
public class TestAuton extends OpMode {
	public TestAuton(Robot callback, String name) {
		super(callback, name);
	}

	public void init() {
		Logging.h("Init called!");
	}

	public void periodic(double deltaTime) {
		Logging.h("Periodic called!");
	}

	public void stop() {
		Logging.h("Stop called!");
	}
}
