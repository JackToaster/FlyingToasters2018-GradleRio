package commands.autonomous;

import org.usfirst.frc.team3641.robot.Robot;

import commands.*;
import commands.interfaces.*;
import path_generation.Point;
import path_generation.Waypoint;
import utilities.Logging;

public class MotionProfileTest extends OpMode {
	final static double dist_m = 4.0;
	
	Waypoint start1 = new Waypoint(new Point(0,0),0);
	Waypoint mid1 = new Waypoint(new Point(6,0.2), 0);
	Waypoint end1 = new Waypoint(new Point(7.5,-0.5), -Math.PI / 2.0);
	MotionProfileCommand driveToScale;
	
	Waypoint start2 = new Waypoint(new Point(7.5,-.5), Math.PI / 2.0);
	Waypoint end2 = new Waypoint(new Point(7.75,0.2), Math.PI / 4.0);
	MotionProfileCommand backUp;
	
	Waypoint start3 = new Waypoint(new Point(7.75, 0.2), 5 * Math.PI / 4.0);
	Waypoint end3 = new Waypoint(new Point(5.312, -0.714), 5 * Math.PI / 4.0);
	MotionProfileCommand driveToCube;
	
	public MotionProfileTest(Robot bot) {
		super(bot, "Motion Profile Auton");

		driveToScale = new MotionProfileCommand(this, robot, "Drive to cube", false, start1, mid1, end1);
		backUp = new MotionProfileCommand(this, robot, "Back up", true, start2, end2);
		driveToCube = new MotionProfileCommand(this, robot, "Drive to cube", false, start3, end3);
	}
	
	public void init() {
		super.init();
		addCommand(driveToScale);
	}
	public void periodic(double deltaTime) {
		super.periodic(deltaTime);
		Logging.h("Left pos: " + robot.driveBase.left.getPosition() + ", right pos: " + robot.driveBase.right.getPosition());
	}
	public void stop() {
		robot.driveBase.setFeedbackActive(false);
	}
	
	public void commandFinished(Command cmd) {
		super.commandFinished(cmd);
		if(cmd == driveToScale) {
			addCommand(backUp);
		}else if(cmd == backUp) {
			addCommand(driveToCube);
		}else if(cmd == driveToCube) {
			endCommand();
		}
	}
}
