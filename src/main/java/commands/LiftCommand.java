package commands;

import robot.Robot;

import commands.interfaces.Command;
import commands.interfaces.CommandCallback;
import hardware.Lift;
import utilities.Logging;

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
		Logging.h("Error: " + bot.lift.getTotalError());
		Logging.h("Timeout: " + startTimeout);
		if(startTimeout > START_TIME && bot.lift.getTotalError() < MAX_ERROR) {
			Logging.h("EndCommand called!");
			endCommand();
		}
	}
}
