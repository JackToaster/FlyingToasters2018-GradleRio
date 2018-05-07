package org.theflyingtoasters.path_generation;

/**
 * A waypoint holds a position and orientation, and maybe distance along a path,
 * velociy, or time. Used for motion profile generation.
 * 
 * @author jack
 *
 */
public class Waypoint {
	public Point position;
	public double distance;
	public double velocity;
	public double rotation;
	public double time;

	/**
	 * Create a point from a position and rotation
	 * 
	 * @param waypointPosition
	 *            the position of the point
	 * @param wpRotation
	 *            the orientation (Angle in radians from positive X axis)
	 */
	public Waypoint(Point waypointPosition, double wpRotation) {
		position = waypointPosition;
		rotation = wpRotation;
		time = 0;
	}

	/**
	 * Sets the X and Y coordinates of the waypoint
	 * 
	 * @param xPosition
	 *            the x coordinate
	 * @param yPosition
	 *            the y coordinate
	 */
	public void setPosition(double xPosition, double yPosition) {
		position.x = xPosition;
		position.y = yPosition;
	}

	/**
	 * Sets the angle of the waypoint
	 * 
	 * @param wpRotation
	 *            Angle of the waypoint in radians
	 */
	public void setRotation(double wpRotation) {
		rotation = wpRotation;
	}

	/**
	 * Translates the waypoint by an X and Y amount
	 * 
	 * @param xOffset
	 *            the amount to offset in the X axis
	 * @param yOffset
	 *            amount in Y axis
	 */
	public void translateWaypoint(double xOffset, double yOffset) {
		position.add(new Point(xOffset, yOffset));
	}

	/**
	 * Change the waypoint's rotation so that it is oriented towards the given
	 * point.
	 * 
	 * @param target
	 *            the point to face towards
	 * @return the new rotation of the point
	 */
	public double pointTowards(Point target) {
		double xOffset = target.x - position.x;
		double yOffset = target.y - position.y;
		if (xOffset == 0) {
			if (yOffset > 0) {
				rotation = Math.PI / 2.0;
			} else {
				rotation = 3.0 * Math.PI / 2.0;
			}
		} else {
			rotation = Math.atan(yOffset / xOffset);
			if (xOffset < 0) {
				rotation += Math.PI;
			}
		}
		return rotation;
	}
	
	public Waypoint backwards() {
		return new Waypoint(this.position, this.rotation + Math.PI);
	}
	
	/**
	 * 
	 * @return the position of the point
	 */
	public Point getPoint() {
		return position;
	}

	@Override
	public String toString() {
		return "X: " + position.x + ", Y: " + position.y + ", Angle: " + rotation + ", distance: " + distance
				+ ", time: " + time + ", velocity:" + velocity;
	}
}
