package org.theflyingtoasters.toaster_commands;

/**
 * A command runner simply runs commands. It si different from a scheduler because it only handles one command
 *  and may be inside of a command scheduler.
 */
public interface CommandRunner {
    /**
     * Returns the scheduler running this CommandRunner. In the case of a CommandScheduler, this returns this.
     * @return the scheduler running the commands.
     */
    CommandScheduler getScheduler();
	void commandEnded(Command cmd);
}
