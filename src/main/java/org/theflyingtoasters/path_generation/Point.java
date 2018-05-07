package org.theflyingtoasters.path_generation;

public class Point {
	/**
	 * the coordinates of the point
	 */
	public double x, y;

	/**
	 * create a point from X/Y coordinates
	 * 
	 * @param xPosition
	 *            the x coordinate of the point
	 * @param yPosition
	 *            the y coordinate of the point
	 */
	public Point(double xPosition, double yPosition) {
		x = xPosition;
		y = yPosition;
	}

	/**
	 * create a point from polar coordinates
	 * 
	 * @param r
	 *            distance from origin
	 * @param theta
	 *            angle in radians
	 * @return the point specified
	 */
	public static Point PolarPoint(double r, double theta) {
		double xPosition = r * Math.cos(theta);
		double yPosition = r * Math.sin(theta);
		return new Point(xPosition, yPosition);

	}

	/**
	 * linear interpolation between two points
	 * 
	 * @param startPoint
	 *            the starting point of the interpolation
	 * @param endPoint
	 *            the ending point of the interpolation
	 * @param alpha
	 *            How far along the line. 0 returns startPoint, 1 returns endPoint,
	 *            other values from 0 to 1 are in between.
	 * @return the interpolated point
	 */
	public static Point lerp(Point startPoint, Point endPoint, double alpha) {
		double xPosition = (endPoint.x - startPoint.x) * alpha + startPoint.x;
		double yPosition = (endPoint.y - startPoint.y) * alpha + startPoint.y;
		return new Point(xPosition, yPosition);
	}

	/**
	 * get the distance to another point
	 * 
	 * @param p2
	 *            the other point
	 * @return the distance between the points
	 */
	public double distance(Point p2) {
		return Math.sqrt(Math.pow(x - p2.x, 2) + Math.pow(y - p2.y, 2));
	}

	/**
	 * add two points together
	 * 
	 * @param p2
	 *            the other point
	 * @return the sum of the two points (X1 + X2, Y1 + Y2)
	 */
	public Point sum(Point p2) {
		return new Point(x + p2.x, y + p2.y);
	}

	/**
	 * offset this point by another one. This modifies the original point.
	 * 
	 * @param p2
	 *            the other point.
	 */
	public void add(Point p2) {
		x += p2.x;
		y += p2.y;
	}
}
