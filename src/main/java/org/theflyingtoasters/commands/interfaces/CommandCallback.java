package org.theflyingtoasters.commands.interfaces;

/**
 * abstract interface for a command callback. This is used to detect when a
 * command has ended. It is implemented by Robot.java to detect when opmodes end
 * and the OpMode abstract class to remove subcommands which have ended.
 * 
 * @author jackf
 *
 */
public interface CommandCallback {
	/**
	 * called once the command ends on its own.
	 * 
	 * @param cmd
	 *            the command that has ended.
	 */
	public void commandFinished(Command cmd);
}
