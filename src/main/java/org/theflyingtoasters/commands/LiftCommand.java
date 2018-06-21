package org.theflyingtoasters.commands;

import org.theflyingtoasters.commands.interfaces.Command;
import org.theflyingtoasters.commands.interfaces.CommandCallback;
import org.theflyingtoasters.hardware.Lift;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

public class LiftCommand extends Command{
	static final double START_TIME = 0.5;
	static final double MAX_ERROR = 5;
	Lift.Positions pos;
	Robot bot;
	double startTimeout;
	public LiftCommand(CommandCallback opMode, Robot robot, Lift.Positions position) {
		super(opMode, "Intake Command: " + position.name());
		pos = position;
		bot = robot;
	}
	
	public void init() {
		bot.lift.trackToPos(pos);
		startTimeout = 0;
	}
	
	public void periodic(double deltaTime) {
		startTimeout += deltaTime;
		Logging.l("Error: " + bot.lift.getTotalError());
		Logging.l("Timeout: " + startTimeout);
		if(startTimeout > START_TIME && bot.lift.getTotalError() < MAX_ERROR) {
			Logging.l("EndCommand called!");
			endCommand();
		}
	}
}
