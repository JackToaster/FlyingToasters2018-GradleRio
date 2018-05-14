package org.theflyingtoasters.toaster_commands;

import java.util.ArrayList;

import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;

/**
 * abstract interface for a command
 * 
 * @author jackf
 *
 */
public abstract class OpMode implements CommandScheduler {
	/**
	 * A list of running commands. On periodic, all commands are updated.
	 */
	protected ArrayList<Command> commands;
	/**
	 * The robot that the command is running on.
	 */
	protected Robot robot;

	/**
	 * Constructor for a defined name.
	 * 
	 * @param bot
	 *            The robot the command is running in
	 * @param name
	 *            the name of the opmode.
	 */
	public OpMode(Robot bot) {
		commands = new ArrayList<Command>();
		robot = bot;
	}

	/**
	 * called once when the opmode is started (during firstPeriodic in Robot)
	 */
	public final void opModeInit() {
		init();
	}

	/**
	 * called once when the opmode is started (during firstPeriodic in Robot)
	 */
	public abstract void init();

	/**
	 * called periodically during teleop/autonomous periodic.
	 */
	public void opModePeriodic(double deltaTime) {
		Command[] cmdArray = commands.toArray(new Command[commands.size()]);
		for (Command cmd : cmdArray) {
			cmd.periodic(deltaTime);
		}
		periodic(deltaTime);
	}

	public abstract void periodic(double deltaTime);

	/**
	 * called once or never, to stop the opmode.
	 */
	public void stop() {
		Command[] cmdArray = commands.toArray(new Command[commands.size()]);
		for (Command cmd : cmdArray) {
			cmd.commandStop();
			commands.remove(cmd);
		}
	}

	/**
	 * Add a new command to the list of commands
	 * 
	 * @param cmd
	 *            the command to add
	 */
	public void addCommand(Command cmd) {
		Logging.h("Added command: " + cmd);
		commands.add(cmd);
		cmd.commandInit();
	}

	/**
	 * stops and removes a command.
	 * 
	 * @param cmd
	 *            the command to remove
	 */
	public void removeCommand(Command cmd) {
		commands.remove(cmd);
		cmd.commandStop();
	}

	/**
	 * Callback for commands that finish. Removes the command from the list of
	 * commands.
	 */
	public void called(Command cmd) {
		Logging.h("Removed command: " + cmd);
		commands.remove(cmd);
	}
}
