package org.theflyingtoasters.toaster_commands;

public interface CommandRunner {
	public CommandScheduler getScheduler();
	public void commandEnded(Command cmd);
}
