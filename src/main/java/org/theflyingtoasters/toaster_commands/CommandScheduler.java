package org.theflyingtoasters.toaster_commands;

public interface CommandScheduler extends CommandRunner {
	/**
	 * Add the command to be run periodically and call its init().
	 * @param cmd the command to add and start
	 */
	public void addCommand(Command cmd);
	/**
	 * Stops the command and removes it.
	 * @param cmd
	 */
	public void removeCommand(Command cmd);
	public void run(double deltaTime);
	default void commandEnded(Command cmd) {
		removeCommand(cmd);
	}
	default CommandScheduler getScheduler() {
		return this;
	}
}
