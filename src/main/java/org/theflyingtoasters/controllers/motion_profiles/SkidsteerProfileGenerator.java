package org.theflyingtoasters.controllers.motion_profiles;

import org.theflyingtoasters.path_generation.*;

/**
 * wheel profile generator for robots with wheels on either side
 * 
 * @author jack
 *
 */
public class SkidsteerProfileGenerator extends WheelProfileGenerator {
	/**
	 * how far to the right the wheel is, negative for left
	 */
	private double rightOffset;

	/**
	 * constructor
	 * 
	 * @param rightOffset
	 *            how far to the right the wheel is (negative for wheels on the
	 *            left)
	 * 
	 */
	public SkidsteerProfileGenerator(double rightOffset) {
		this.rightOffset = rightOffset;
	}

	@Override
	public Profile genPoints(Path p, boolean isBackwards) {
		Profile outProfile = new Profile(p.waypoints.size());
		Waypoint firstWP = p.waypoints.get(0);
		
		MPPoint startMPPoint = new MPPoint(0, 0, 0);
		outProfile.setPoint(0, startMPPoint);
		
		double totalDist = 0;
		Waypoint lastWP = firstWP;
		for (int i = 1; i < p.waypoints.size(); i++) {
			// get the current waypoint
			Waypoint wp = p.waypoints.get(i);

			// get the velocity and distance
			double dT = wp.time - p.waypoints.get(i - 1).time;
			double arcDist = getSignedArcDistance(lastWP, wp, isBackwards);
			double linDist = wp.distance - lastWP.distance;
			double dist = arcDist + linDist;
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

			lastWP = wp;
		}
		outProfile.getPoint(0).velocity = outProfile.getPoint(1).velocity;
		// return it!
		return outProfile;
	}
	
	/**
	 * used to generate the position of a given point when offset to the positio of
	 * the wheel.
	 * secondsecond
	 * @param wp
	 *            the waypoint to offset
	 * @param backwards
	 *            whether the robot is moving backwards
	 * @return the position of the offset point
	 */
	//Unused for now.
	/*
	private Point getOffsetPoint(Waypoint wp, boolean backwards) {
		if (backwards) {
			return wp.position.sum(Point.PolarPoint(-rightOffset, 3 * Math.PI / 2 + wp.rotation));
		} else {
			return wp.position.sum(Point.PolarPoint(rightOffset, 3 * Math.PI / 2 + wp.rotation));
		}
	}*/

	/**
	 * Used to generate the distance a wheel travels on a circular arc from the two
	 * points.
	 * 
	 * @param first
	 *            the first waypoint
	 * @param second
	 *            the second waypoint
	 * @param backwards
	 *            whether the robot is moving backwards
	 * @return
	 */
	private double getSignedArcDistance(Waypoint first, Waypoint second, boolean backwards) {
		double radians = angleBetween(first.rotation, second.rotation);
		double arcDist = radians * rightOffset;
		if (backwards) {
			return -arcDist;
		} else {
			return arcDist;
		}
	}
	
	private double angleBetween(double angle1, double angle2) {
		double angleDif = angle2 - angle1;
		if(angleDif > Math.PI) angleDif -= 2 * Math.PI;
		else if(angleDif < -Math.PI) angleDif += 2 * Math.PI;
		return angleDif;
	}
}