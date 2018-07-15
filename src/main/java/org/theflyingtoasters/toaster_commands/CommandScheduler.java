package org.theflyingtoasters.toaster_commands;

public interface CommandScheduler extends CommandRunner {
	/**
	 * Add the command to be run periodically and call its init().
	 * @param cmd the command to add and start
	 */
	void addCommand(Command cmd);
	/**
	 * Stops the command and removes it.
	 * @param cmd
	 */
	void removeCommand(Command cmd);

	/**
	 * Runs all commands that are currently added to this scheduler. This should be called periodically.
	 * @param deltaTime the amount of time that has passed since the last call to run().
	 */
	void run(double deltaTime);

    /**
     * Called back when a command ends. The default method removes the command.
     * @param cmd
     */
	default void commandEnded(Command cmd) {
		removeCommand(cmd);
	}

	default CommandScheduler getScheduler() {
		return this;
	}
}
