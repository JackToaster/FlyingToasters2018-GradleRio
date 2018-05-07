package org.theflyingtoasters.controllers.motion_profiles;

import org.theflyingtoasters.utilities.Utilities;

/**
 * Motion profile point class, has a position, velocity, and time (in ms)
 * 
 * @author jackf
 *
 */
public class MPPoint {
	public double position;
	public double velocity;
	public double time;

	/**
	 * create a motion profile point
	 * 
	 * @param vel
	 *            the point's velocity
	 * @param pos
	 *            the point's position
	 * @param t
	 *            the time at which the point should be reached
	 */
	public MPPoint(double vel, double pos, double t) {
		velocity = vel;
		position = pos;
		time = t;
	}

	/**
	 * linear interpolation between this point and another
	 * 
	 * @param p2
	 *            the other point
	 * @param alpha
	 *            how far between this and p2 (0 returns this point, 1 returns p2,
	 *            0.5 is halfway between, etc.)
	 * @return the interpolated point
	 */
	public MPPoint lerp(MPPoint p2, double alpha) {
		double newVel = Utilities.lerp(this.velocity, p2.velocity, alpha);
		double newPos = Utilities.lerp(this.position, p2.position, alpha);
		double newTime = Utilities.lerp(this.time, p2.time, alpha);
		return new MPPoint(newVel, newPos, newTime);
	}
	/**
	 * returns a readable string of the point.
	 */
	public String toString() {
		return "Position: " + position + ", velocity: " + velocity + ", time: " + time;
	}
}
