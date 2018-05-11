package org.theflyingtoasters.toaster_commands;

/**
 * abstract interface for a command.
 * 
 * @author jackf
 *
 */
public abstract class Command {
	/**
	 * the callback to call upon ending
	 */
	private CommandCallback start;
	private CommandCallback end;
	
	/**
	 * the readable name of the command
	 */
	protected String readableName = "Unnamed command";

	/**
	 * Constructor for a command. Takes a callback which is to notify the
	 * opmode/robot that the command is finished, and a readable name for the
	 * command.
	 * 
	 * @param callback
	 *            the opmode/robot that the command is run by
	 */
	public Command(CommandCallback callback) {
		this.callback = callback;
	}

	/**
	 * called once when the command starts.
	 */
	public void init() {
	}

	/**
	 * called periodically when the command is run.
	 * 
	 * @param deltaTime
	 *            the amount of time passed since the call of periodic or init.
	 */
	public void periodic(double deltaTime) {
	}

	/**
	 * called once or never, to stop the command.
	 */
	public void stop() {
	}

	/**
	 * called when the command ends and calls back. This is used to end the command
	 * before stop is called.
	 */
	protected void endCommand() {
		end.call(this);
	}
	
	public DelayedCommand delay(double delayTime) {
		DelayedCommand delayed = new DelayedCommand(callback, delayTime);
		delayed.setCommand(this);
		return delayed;
	}
	
	/**
	 * get the readable name of the command. Identical to toString().
	 * @return the readable name of the command
	 */
	public String getName() {
		return readableName;
	}

	// toString used for printing
	@Override
	public String toString() {
		return readableName;
	}
}
