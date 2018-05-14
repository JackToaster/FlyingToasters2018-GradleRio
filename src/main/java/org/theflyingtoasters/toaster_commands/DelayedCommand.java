package org.theflyingtoasters.toaster_commands;

public class DelayedCommand extends Command implements CommandRunner{
	private double delay;
	private double timer;
	private Command command;
	private boolean commandRunning = false;
	
	public DelayedCommand (CommandScheduler callback, double delayTime) {
		super(callback);
		delay = delayTime;
	}
	
	public void setCommand(Command command) {
		this.command = command;
	}
	
	public void init() {
		timer = delay;
	}
	
	public void periodic (double deltaTime) {
		if(commandRunning) {
			command.periodic(deltaTime);
		} else {
			timer -= deltaTime;
			if(timer <= 0) {
				commandRunning = true;
				command.init();
			}
		}
	}
	
	public void stop() {
		command.stop();
	}

	@Override
	public void commandEnded(Command cmd) {
		endCommand();
	}

	@Override
	public CommandScheduler getScheduler() {
		return callback.getScheduler();
	}
}
