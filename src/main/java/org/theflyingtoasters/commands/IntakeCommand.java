package org.theflyingtoasters.commands;

import org.theflyingtoasters.hardware.Intake;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.toaster_commands.Command;
import org.theflyingtoasters.toaster_commands.CommandCallback;

/**
 * A command to intake/output a power cube.
 *
 */
public class IntakeCommand extends Command {
	static final double defaultIntakeForwardSpeed = 0.2;
	private double intakeForwardSpeed = defaultIntakeForwardSpeed;
	/**
	 * The robot the command is running on.
	 */
	private Robot bot;
	
	/**
	 * The mode to start the intake in when the command is run.
	 */
	private Intake.State mode;
	/**
	 * Constructor for the intake command.
	 * 
	 * @param opMode
	 *            the opmode calling the command. used for callback.
	 * @param robot
	 *            the robot the command is running on
	 * @param name
	 *            the name of the command
	 * @param mode
	 *            whether the command is intaking or outputting
	 */
	public IntakeCommand(CommandCallback opMode, Robot robot, String name, Intake.State mode) {
		super(opMode, name);
		bot = robot;
		this.mode = mode;
	}

	/**
	 * Constructor for the intake command using default name.
	 * 
	 * @param opMode
	 *            the opmode calling the command. used for callback.
	 * @param robot
	 *            the robot the command is running on
	 * @param mode
	 *            whether the command is intaking or outputting
	 */
	public IntakeCommand(CommandCallback opMode, Robot robot, Intake.State mode) {
		this(opMode, robot, "Intake command", mode);
	}
	
	public IntakeCommand(CommandCallback opMode, Robot robot, Intake.State mode, double speed) {
		this(opMode, robot, "Intake command", mode);
		intakeForwardSpeed = speed;
	}

	/**
	 * Called when the command is initialized.
	 */
	public void init() {
		bot.intake.setPower(0);
		bot.intake.setState(mode);
	}

	/**
	 * run continuously when the command is running. Updates the time, checks the
	 * sensor, and sets the motor/ends the command appropriately.
	 */
	public void periodic(double deltaTime) {
		if(mode == Intake.State.INTAKING) {
			bot.driveBase.setFeedbackActive(false);
			bot.driveBase.driveArcade(intakeForwardSpeed, 0);
		}
		// Check if the cube is gotten, and and the command if so.
		Intake.State intakeState = bot.intake.getState();
		if (intakeState == Intake.State.RESTING || intakeState == Intake.State.RESTING_WITH_CUBE) {
			endCommand();
			if(mode == Intake.State.INTAKING) {
				bot.driveBase.driveArcade(0, 0);
			}
		}
	}

	/**
	 * called when the command is stopped externally. Sets motors to off.
	 */
	public void stop() {
		bot.intake.setPower(0);
		super.stop();
		;
	}
}
