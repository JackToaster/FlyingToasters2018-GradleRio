package org.theflyingtoasters.controllers.motion_profiles;

import org.theflyingtoasters.utilities.Logging;

/**
 * stores a list of points and allows reading an interpolated point at any time
 * through the profile.
 * 
 * @author jack
 *
 */
public class Profile {
	/**
	 * the list of points.
	 */
	private MPPoint[] trajectory;

	/**
	 * Used to dramatically speed up searching through points by starting from this
	 * index.
	 */
	private int lastLowerIndex = 0;

	/**
	 * creates a profile with the given length
	 * 
	 * @param length
	 *            the number of points in the profile
	 */
	public Profile(int length) {
		trajectory = new MPPoint[length];
	}

	/**
	 * creates a profile from the given points
	 * 
	 * @param points
	 *            the points to store in the profile
	 */
	public void setPoints(MPPoint... points) {
		if (points.length < 2) {
			Logging.e("Useless motion profile - less than 2 points");
		} else {
			trajectory = points;
		}
	}

	/**
	 * sets the given point of the profile
	 * 
	 * @param index
	 *            the index of the point
	 * @param point
	 *            what to put at the given index
	 */
	public void setPoint(int index, MPPoint point) {
		trajectory[index] = point;
	}

	/**
	 * gets a point at an integer index
	 * 
	 * @param index
	 *            the index of the point
	 * @return the requested point
	 */
	public MPPoint getPoint(int index) {
		return trajectory[index];
	}

	/**
	 * returns an interpolated point at the given time.
	 * 
	 * @param time
	 *            the time to get the point at
	 * @return the point
	 */
	public MPPoint getInterpolatedPoint(double time) {
		// Start searching at the last lower index. This only one or two points will
		// usually have to be searched through. This method is to avoid doing something
		// that requires more effort like a binary search, and it's faster for the case
		// that is typically used.
		int upperIndex = lastLowerIndex;

		// check for start/end
		if (time == 0)
			return start();
		else if (time >= getEndTime())
			return end();
		
		// loop through until a set of two points that contain the given time are found
		while (!(getPoint(upperIndex).time > time && getPoint(upperIndex - 1).time < time)) {
			upperIndex++;
			if (upperIndex > trajectory.length)
				upperIndex = 1;
		}

		int lowerIndex = upperIndex - 1;
		lastLowerIndex = lowerIndex;
		MPPoint upper = getPoint(upperIndex);
		MPPoint lower = getPoint(lowerIndex);

		// find what fraction of the way from upper to lower the time is
		double alpha = (time - lower.time) / (upper.time - lower.time);

		return lower.lerp(upper, alpha);
	}

	/**
	 * returns the first point
	 * 
	 * @return the first point
	 */
	public MPPoint start() {
		return getPoint(0);
	}

	/**
	 * returns the last point
	 * 
	 * @return the last point
	 */
	public MPPoint end() {
		return getPoint(trajectory.length - 1);
	}

	/**
	 * returns the time of the last point
	 * 
	 * @return the time of the last point
	 */
	public double getEndTime() {
		return end().time;
	}

	public String toString() {
		String out = "";
		for (MPPoint getPoint : trajectory) {
			out += getPoint.toString() + "\n";
		}
		return out;
	}
}