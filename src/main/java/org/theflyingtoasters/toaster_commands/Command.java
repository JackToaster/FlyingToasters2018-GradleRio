package org.theflyingtoasters.toaster_commands;

/**
 * abstract interface for a command.
 * 
 * @author jackf
 *
 */
public abstract class Command {

    /**
     * Command behavior that does nothing.
     */
    protected static final CommandBehavior NONE = scheduler -> {};



	/**
	 * the callback to call upon starting/ending
	 */
	private CommandBehavior start = NONE;
	private CommandBehavior end = NONE;


    /**
	 * The callback to stop calling the command periodically
	 */
	protected CommandRunner callback;
	
	/**
	 * Constructor for a command. Takes a callback which is to notify the
	 * opmode/robot that the command is finished, and a readable name for the
	 * command.
	 * 
	 * @param callback
	 *            the opmode/robot that the command is run by
	 */
	public Command(CommandScheduler callback) {
		this.callback = callback;
	}

	/**
	 * called once when the command starts.
	 */
	public final void commandInit() {
		init();
		if(start != null) {
			start.run(callback.getScheduler());
		}
	}
	
	public abstract void init();

	/**
	 * called periodically when the command is run.
	 * 
	 * @param deltaTime
	 *            the amount of time passed since the call of periodic or init.
	 */
	public abstract void periodic(double deltaTime);

	/**
	 * called once or never, to stop the command.
	 */
	public final void commandStop() {
		stopped();
		if(end != null) {
			end.run(callback.getScheduler());
		}
	}

    /**
     * Called when a command gets stopped by the scheduler. This is not run when endCommand is called.
     */
	public abstract void stopped();
	/**
	 * called when the command ends and calls back. This is used to end the command
	 * before stop is called.
	 */
	protected void endCommand() {
		callback.commandEnded(this);
	}
	
	public DelayedCommand delay(double delayTime) {
		DelayedCommand delayed = new DelayedCommand(callback.getScheduler(), delayTime);
		delayed.setCommand(this);
		return delayed;
	}

	// toString used for printing
	@Override
	public String toString() {
		return getClass().getSimpleName() + " run by " + callback.getClass().getSimpleName() + " scheduled in " + getScheduler().toString();
	}
	
	public CommandScheduler getScheduler() {
		return callback.getScheduler();
	}

    public CommandBehavior getStart() {
        return start;
    }

    public void setStart(CommandBehavior start) {
        this.start = start;
    }

    public CommandBehavior getEnd() {
        return end;
    }

    public void setEnd(CommandBehavior end) {
        this.end = end;
    }

    public CommandRunner getCallback() {
        return callback;
    }

    public void setCallback(CommandRunner callback) {
        this.callback = callback;
    }
}
