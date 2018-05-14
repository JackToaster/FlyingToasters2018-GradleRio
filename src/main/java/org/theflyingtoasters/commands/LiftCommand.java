package org.theflyingtoasters.commands;

import org.theflyingtoasters.hardware.Lift;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.toaster_commands.Command;
import org.theflyingtoasters.toaster_commands.CommandScheduler;
import org.theflyingtoasters.utilities.Logging;

public class LiftCommand extends Command{
	static final double START_TIME = 0.5;
	static final double MAX_ERROR = 5;
	Lift.Positions pos;
	Robot bot;
	double startTimeout;
	public LiftCommand(CommandScheduler opMode, Robot robot, Lift.Positions position) {
		super(opMode);
		pos = position;
		bot = robot;
	}
	
	public void init() {
		bot.lift.trackToPos(pos);
		startTimeout = 0;
	}
	
	public void periodic(double deltaTime) {
		startTimeout += deltaTime;
		Logging.h("Error: " + bot.lift.getTotalError());
		Logging.h("Timeout: " + startTimeout);
		if(startTimeout > START_TIME && bot.lift.getTotalError() < MAX_ERROR) {
			Logging.h("EndCommand called!");
			endCommand();
		}
	}

	@Override
	public void stop() {
		
	}
}
