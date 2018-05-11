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
public abstract class OpMode extends CommandGroup{
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
	public OpMode(Robot bot, String name) {
		super(bot, name);
		commands = new ArrayList<Command>();
		robot = bot;
	}

	/**
	 * Constructor using no defined name. Avoid this.
	 * 
	 * @param bot
	 *            the robot the opmode is running on
	 */
	public OpMode(Robot bot) {
		super(bot, "Default opmode name");
		commands = new ArrayList<Command>();
		robot = bot;
	}

	/**
	 * called once when the command is started (during firstPeriodic in Robot)
	 */
	public void init() {
		// No auto-initing of commands, since they may not start immediately.
	}

	/**
	 * called periodically during teleop/autonomous periodic.
	 */
	public void periodic(double deltaTime) {
		Command[] cmdArray = commands.toArray(new Command[commands.size()]);
		for (Command cmd : cmdArray) {
			cmd.periodic(deltaTime);
		}
	}

	/**
	 * called once or never, to stop the opmode.
	 */
	public void stop() {
		Command[] cmdArray = commands.toArray(new Command[commands.size()]);
		for (Command cmd : cmdArray) {
			cmd.stop();
			commands.remove(cmd);
		}
	}

	/**
	 * Add a new command to the list of commands
	 * 
	 * @param cmd
	 *            the command to add
	 */
	protected void addCommand(Command cmd) {
		Logging.h("Added command: " + cmd);
		commands.add(cmd);
		cmd.init();
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
