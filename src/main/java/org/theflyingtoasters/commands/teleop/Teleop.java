package org.theflyingtoasters.commands.teleop;

import org.theflyingtoasters.commands.interfaces.OpMode;
import org.theflyingtoasters.hardware.Intake;
import org.theflyingtoasters.hardware.Lift;
import org.theflyingtoasters.robot.Robot;
import org.theflyingtoasters.utilities.Logging;
import org.theflyingtoasters.utilities.Utilities;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Teleop class for human-operated control.
 * 
 * @author jack
 *
 */
public class Teleop extends OpMode {
	
	/* 
	 * The rio disables motor controllers at 6.8 and doesn't release until it rises past 7.5, so it
	 * would be pointless to enable current limiting any lower because the Talons would just cut out.
	 */
	private static final double BROWNOUT_TRIGGER = 7.5;
	private static final double BROWNOUT_RELEASE = 9;
	
	private static enum DriveMode {
		TANK,
		ARCADE,
		PURE_CHEESE,
		GRILLED_CHEESE,
		MARIO_KART,
		STOP,
	}
	
	private DriveMode driveMode = DriveMode.GRILLED_CHEESE;
	
	/**
	 * The PS4 controller the driver uses to control the robot
	 */
	private PS4 ps4;
	private Operator op;
	
	private boolean brownout;
	
	/**
	 * Constructor
	 * 
	 * @param bot
	 *            the Robot that's being controlled.
	 */
	public Teleop(Robot bot) {
		super(bot, "Teleop");
		ps4 = new PS4(0);
		op = new Operator(2);
	}

	/**
	 * Called once when the opmode starts. Doesn't really do anything yet.
	 */
	public void init() {
		super.init();
		Logging.h("Starting teleop");
		//Disables feedback control!!!! (OOPS WE DIDNT thiNK OF ThiS UNtiL BAG Day!!!)
		robot.driveBase.setFeedbackActive(false);
		robot.driveBase.left.setFeedbackController(null);
		robot.driveBase.right.setFeedbackController(null);
		
		robot.lift.readTuningValuesFromDashboard();
//		robot.intake.readTuningValuesFromDashboard();
		
		if (SmartDashboard.getBoolean("Manual enabled", true)) {
			robot.lift.disableMotionMagic();
		}

		op.checkControllerType();
		
		
		
		brownout = false;
	}
	
	private void setDriveMode(DriveMode newMode) {
		if(newMode != driveMode) {
			Logging.h("Switching from " + driveMode.toString() + " to " + newMode.toString());
			driveMode = newMode;
		}
	}

	/**
	 * Called periodically during teleop period. Reads user inputs from controllers
	 * and controls the robot.
	 */
	public void periodic(double deltaTime) {
		Logging.l("Teleop periodic run");
		
		// updates all running commands
		super.periodic(deltaTime);
		
		// get input from ps4 controller
		ps4.poll();
		op.poll();
		
		//Set the current drive mode based on the d-pad if the left bumper is pressed for safety.
		if(ps4.isDown(PS4.Button.LEFT_BUMPER)) {
			if(ps4.isPressed(PS4.Button.DPAD_UP)) setDriveMode(DriveMode.GRILLED_CHEESE);
			else if(ps4.isPressed(PS4.Button.DPAD_RIGHT)) setDriveMode(DriveMode.MARIO_KART);
			else if(ps4.isPressed(PS4.Button.DPAD_DOWN)) setDriveMode(DriveMode.ARCADE);
			else if(ps4.isPressed(PS4.Button.DPAD_LEFT)) setDriveMode(DriveMode.TANK);
		}
		
		//Actually drive based on the drive mode
		switch(driveMode) {
		case GRILLED_CHEESE:
			robot.driveBase.driveGrilledCheese(ps4.getAxis(PS4.Axis.LEFT_Y), ps4.getAxis(PS4.Axis.RIGHT_X));
			break;
		case PURE_CHEESE:
			if(ps4.isDown(PS4.Button.RIGHT_BUMPER)) robot.driveBase.driveArcade(ps4.getAxis(PS4.Axis.LEFT_Y), ps4.getAxis(PS4.Axis.RIGHT_X));
			else robot.driveBase.drivePureCheese(ps4.getAxis(PS4.Axis.LEFT_Y), ps4.getAxis(PS4.Axis.RIGHT_X));
			break;
		case ARCADE:
			robot.driveBase.driveArcade(ps4.getAxis(PS4.Axis.LEFT_Y), ps4.getAxis(PS4.Axis.RIGHT_X));
			break;
		case TANK:
			robot.driveBase.driveTank(ps4.getAxis(PS4.Axis.LEFT_Y), ps4.getAxis(PS4.Axis.RIGHT_Y));
			break;
		case MARIO_KART:
			if(ps4.isDown(PS4.Button.LEFT_TRIGGER_BUTTON)) robot.driveBase.driveArcade(ps4.getAxis(PS4.Axis.TILT_PITCH), Utilities.expInput(ps4.getAxis(PS4.Axis.TILT_ROLL), 2));
			else robot.driveBase.driveTank(0, 0);
			break;
		case STOP:
			robot.driveBase.driveArcade(0, 0);
			break;
		}
		
		//Conditional Current Limiting. It's really weird to drive cheesey with current limiting on all the time.
		if(robot.pdp.getVoltage() < BROWNOUT_TRIGGER && !brownout) {
			robot.driveBase.enableCurrentLimiting();
			brownout = true;
		} else if(robot.pdp.getVoltage() > BROWNOUT_RELEASE && brownout) {
			robot.driveBase.lowCurrentLimiting();
			brownout = false;
		}
		
		// Log the encoder positions with low priority
		Logging.l("Left enc.: " + robot.driveBase.left.getPosition());
		Logging.l("Right enc.:" + robot.driveBase.right.getPosition());

		// Set the power of the intake based on the user inputs.
		robot.intake.setOutputSpeed(op.getAxis(Operator.Axis.OUTPUT_SPEED) * 0.4);
		
		if(op.isPressed(Operator.Button.OUTTAKE) && op.getAxis(Operator.Axis.OUTPUT_SPEED) < .1) {
			robot.intake.setState(Intake.State.FLUTTER_OUT);
		} else if(op.isReleased(Operator.Button.OUTTAKE)) {
			robot.intake.setState(Intake.State.RESET);
		} else if (op.isPressed(Operator.Button.INTAKE)) {
			robot.intake.setState(Intake.State.INTAKING);
		} else if (op.isPressed(Operator.Button.OUTTAKE)) {
			robot.intake.setState(Intake.State.OUTPUTTING_MANUAL);
		} else if (op.isReleased(Operator.Button.INTAKE) && robot.intake.getState() == Intake.State.INTAKING) {
			robot.intake.setState(Intake.State.RECOVERY);
		} else if (op.isReleased(Operator.Button.OUTTAKE) && robot.intake.getState() == Intake.State.OUTPUTTING_MANUAL) {
			robot.intake.setState(Intake.State.RESET);
		}
		// else if (robot.intake.getState() == Intake.State.RESTING)
		// robot.intake.setPower(e3d.getAxis(E3D.AxisX));

		// Move the lift on the rising edge of each button
		// Move the lift on the rising edge of each button
		if (op.isPressed(Operator.Button.GROUND)) {
			robot.lift.trackToPos(Lift.Positions.GROUND);
		} else if (op.isPressed(Operator.Button.LOW_SWITCH)) {
			robot.lift.trackToPos(Lift.Positions.SWITCH);
		} else if (op.isPressed(Operator.Button.HIGH_SWITCH)) {
			robot.lift.trackToPos(Lift.Positions.H_SWITCH);
		} else if (op.isPressed(Operator.Button.LOW_SCALE)) {
			if (op.isDown(Operator.Button.HIGH_SCALE))
				robot.lift.trackToPos(Lift.Positions.L_SCALE);
			else
				robot.lift.trackToPos(Lift.Positions.LL_SCALE);
		} else if (op.isPressed(Operator.Button.HIGH_SCALE)) {
			if (op.isDown(Operator.Button.LOW_SCALE))
				robot.lift.trackToPos(Lift.Positions.L_SCALE);
			else
				robot.lift.trackToPos(Lift.Positions.H_SCALE);
		} else if(op.isPressed(Operator.Button.START_CLIMB)) {
			robot.lift.trackToPos(Lift.Positions.CLIMB);
		} else if(op.isPressed(Operator.Button.END_CLIMB)) {
			robot.lift.trackToPos(Lift.Positions.CLIMB_ENGAGED);
		}
		//Reset the lift down slowly until it hits the limit switch and re:zero s
		//		if(op.isPressed(Operator.Button.RESET)) {
		//			robot.lift.resetDown();
		//		} else if(op.isReleased(Operator.Button.RESET)) {
		//			robot.lift.stopResettingDown();
		//		}
		
		//When the whammy bar is pressed, don't tilt the intake up when we get a cube.
		if(op.isPressed(Operator.Button.AUTO_FLIP)) { 
			robot.intake.enableAutolift(false);
		} else if(op.isReleased(Operator.Button.AUTO_FLIP)) {
			robot.intake.enableAutolift(true);
		}
		
		//If we get the cube, let the driver know by activating the heavy rumble
		if(robot.intake.getState() == Intake.State.HAS_CUBE) ps4.rumbleForTime(1, true, .5);
		
		//Set the light rumble based on the drivebase velocity.
		ps4.rumble(robot.driveBase.getWheelVelocity(), false);
		
//		endgame = (ds.getMatchTime() > 105) || (ps4.isPressed(PS4.Button.SHARE));
		
		//Run the climber
		if(ps4.isPressed(PS4.Button.OPTIONS)) {
			robot.climber.releaseRigging();
		}
		if(ps4.isPressed(PS4.Button.SHARE)) {
			robot.climber.closeServos();
		}
		if(driveMode != DriveMode.MARIO_KART) {
			robot.climber.setSpeed(ps4.getAxis(PS4.Axis.RIGHT_TRIGGER) - ps4.getAxis(PS4.Axis.LEFT_TRIGGER));
		}

		
		// Temporary manual lift control code
		// TODO remove temporary lift control code.
//		if (SmartDashboard.getBoolean("Manual enabled", false)) {
//			double liftPower = -op.e3d.getAxis(E3D.Axis.THROTTLE) * op.e3d.getAxis(E3D.Axis.Y);
//			double flipPower = op.e3d.getAxis(E3D.Axis.THROTTLE) * op.e3d.getAxis(E3D.Axis.X);
//			SmartDashboard.putNumber("Lift poweer", liftPower);
//			SmartDashboard.putNumber("Flip power", flipPower);
//			SmartDashboard.putNumber("E3D Throttle axis", op.e3d.getAxis(E3D.Axis.THROTTLE));
//			robot.lift.driveNoFeedback(liftPower, flipPower);
//		}
		SmartDashboard.putString("Match", robot.ds.getEventName() + robot.ds.getMatchNumber());
	}
	
	/**
	 * Called when the opmode is stopped.
	 */
	public void stop() {
		super.stop();
		Logging.h("Stopping teleop");
	}
}
