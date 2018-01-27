package controllers.motion_profiles;

import path_generation.*;
import utilities.Logging;

//wheel profile generator for robots with wheels on either side
public class SkidsteerProfileGenerator extends WheelProfileGenerator {
	// how far to the right the wheel is, negative for left
	private double rightOffset;

	// constructor
	public SkidsteerProfileGenerator(double rightOffset) {
		this.rightOffset = rightOffset;
	}

	// generate the motion profile for the path
	@Override
	public Profile genPoints(Path p, boolean isBackwards) {
		Profile outProfile = new Profile(p.waypoints.size());
		Waypoint firstWP = p.waypoints.get(0);
		Point firstPoint = getOffsetPoint(firstWP, isBackwards);
		MPPoint startMPPoint = new MPPoint(0, 0, 0);

		outProfile.setPoint(0, startMPPoint);

		// loop through every other point and add it to the profile
		double totalDist = 0;
		Point lastOffsetPoint = firstPoint;
		for (int i = 1; i < p.waypoints.size(); i++) {
			// get the current waypoint
			Waypoint wp = p.waypoints.get(i);

			// get the position of this wheel at the point
			Point offsetPoint = getOffsetPoint(wp, isBackwards);

			// get the velocity and distance
			double dT = wp.time - p.waypoints.get(i - 1).time;
			double dist = lastOffsetPoint.distance(offsetPoint);
			totalDist += dist;
			double vel = dist / dT;
			// create the profile point and add it
			MPPoint currentMPPoint;
			if (isBackwards) {
				currentMPPoint = new MPPoint(-vel, -totalDist, wp.time);
			} else {
				currentMPPoint = new MPPoint(vel, totalDist, wp.time);
			}
			outProfile.setPoint(i, currentMPPoint);

			lastOffsetPoint = offsetPoint;
		}
		outProfile.getPoint(0).velocity = outProfile.getPoint(1).velocity;
		// return it!
		return outProfile;
	}

	private Point getOffsetPoint(Waypoint wp, boolean backwards) {
		if (backwards) {
			return wp.position.sum(Point.PolarPoint(-rightOffset, 3 * Math.PI / 2 + wp.rotation));
		} else {
			return wp.position.sum(Point.PolarPoint(rightOffset, 3 * Math.PI / 2 + wp.rotation));
		}
	}
}