package org.theflyingtoasters.path_generation;

import java.util.ArrayList;

import org.theflyingtoasters.hardware.DriveBase2018;
import org.theflyingtoasters.utilities.Logging;

/**
 * A path of waypoints with times, velocities, and positions.
 *
 * @author jack
 */
public class Path {
    // default values for path generation
    public static final double maxVel = 2;
    public static final double maxAccel = 3;
    public static final int defaultPoints = 100;
    public static final VelocityMode defaultVelocityMode = VelocityMode.TRAPAZOIDAL;
    public static final SplineMode defaultSplineMode = SplineMode.BEZIER;

    /**
     * a path consists of a list of waypoints.
     */
    public ArrayList<Waypoint> waypoints;
    /**
     * The time at which the robot should be at the endpoint of the path
     */
    public double endTime;
    /**
     * The total distance of the path
     */
    public double endPos;

    /**
     * the different velocity profile modes.
     *
     * @author jack
     */
    public enum VelocityMode {
        TRIANGULAR, TRAPAZOIDAL, CONSTANT,
    }

    /**
     * The type of spline to use to generate curved paths.
     */
    public enum SplineMode {
        BEZIER, CATMULL_ROM, CUBIC_HERMITE, QUINTIC_HERMITE,
    }

    /**
     * Creates a path between the given waypoints
     *
     * @param points
     */
    public Path(Waypoint... points) {
        this(maxVel, maxAccel, defaultVelocityMode, points);
    }

    public Path(double velocity, double accel, Waypoint... points) {
        this(velocity, accel, defaultVelocityMode, points);
    }

    /**
     * creates a longer path between multiple points
     *
     * @param velocity       the max velocity
     * @param accel          the max acceleration
     * @param velMode           the velocity profile mode
     * @param points         the waypoints to make a path between
     */
    public Path(double velocity, double accel, VelocityMode velMode, Waypoint... points) {
        waypoints = new ArrayList<Waypoint>();
        genChainPath(defaultSplineMode, 0.8, points);
        alignWaypoints();
        getPositions();
        getVelocities(velocity, accel, velMode);
        getTimes();
    }

    /**
     * creates a longer path between multiple points
     *
     * @param velocity       the max velocity
     * @param accel          the max acceleration
     * @param velMode           the velocity profile mode
     * @param points         the waypoints to make a path between
     */
    public Path(double velocity, double accel, VelocityMode velMode, SplineMode splineMode, Waypoint... points) {
        waypoints = new ArrayList<Waypoint>();
        genChainPath(splineMode, 0.8, points);
        alignWaypoints();
        getPositions();
        getVelocities(velocity, accel, velMode);
        getTimes();
    }

    /**
     * creates a path using default values from start to end
     *
     * @param start
     * @param end
     */
    public Path(Waypoint start, Waypoint end) {
        this(start, end, maxVel, maxAccel, defaultVelocityMode);
    }

    /**
     * create a path from two waypoints
     *
     * @param start          starting waypoints
     * @param end            ending waypoint
     * @param velocity       the max velocity
     * @param accel          the max acceleration
     * @param mode           the velocity profile mode
     */
    public Path(Waypoint start, Waypoint end, double velocity, double accel, VelocityMode mode) {
        waypoints = new ArrayList<Waypoint>();
        genPath(start, end, defaultSplineMode, 1);
        alignWaypoints();
        getPositions();
        getVelocities(velocity, accel, mode);
        getTimes();
    }

    /**
     * calculates the distances of each point and sets end position
     */
    private void getPositions() {
        // stores the total distance
        double distanceAccumulator = 0;
        // get the position of the first point, and set its distance to 0
        Point lastPoint = waypoints.get(0).position;
        waypoints.get(0).time = 0;
        // loop through all other points
        for (int i = 1; i < waypoints.size(); i++) {
            Point wpPosition = waypoints.get(i).position;
            double dist = lastPoint.distance(wpPosition);
            lastPoint = wpPosition;
            distanceAccumulator += dist;
            waypoints.get(i).distance = distanceAccumulator;
        }
        endPos = distanceAccumulator;
    }

    /**
     * Sets the velocities of the path's points
     *
     * @param vel   the maximum velocity to use
     * @param accel the maximum acceleration to use
     * @param mode  the velocity profile mode
     */
    private void getVelocities(double vel, double accel, VelocityMode mode) {
        switch (mode) {
            case TRIANGULAR:
                for (Waypoint wp : waypoints) {
                    double distance = wp.distance;
                    double accelVelocity = Math.sqrt(2 * distance * accel);
                    double decelVelocity = Math.sqrt(2 * (endPos - distance) * accel);
                    wp.velocity = Math.min(accelVelocity, decelVelocity);
                }
                break;
            case CONSTANT:
                for (Waypoint wp : waypoints) {
                    wp.velocity = vel;
                }
                break;
            case TRAPAZOIDAL:
                waypoints.get(0).velocity = 0;
                for (int i = 1; i < waypoints.size(); i++) {
                    Waypoint wp = waypoints.get(i);
                    // Distance of this WP along the path
                    double distance = wp.distance;

                    /* Calculate the maximum velocity given the sharpness of the turn */
                    // Length of the arc the wheel travels, disregarding linear velocity (m)
                    double arcDistance = DriveBase2018.wheelDistance
                            * Math.abs(angleBetween(waypoints.get(i - 1).rotation, wp.rotation));
                    // Length of the straight line the wheel moves, disregarding turning (m)
                    double linearDistance = distance - waypoints.get(i - 1).distance;
                    // Total distance the wheel travels including arc and linear distance (m)
                    double totalWheelDist = arcDistance + linearDistance;
                    // Time for center of bot moving at max velocity (m/(m/s) = s)
                    double uncorrectedDeltaTime = linearDistance / vel;
                    // Velocity of fastest wheel given robot is at max velocity (m/s)
                    double uncorrectedWheelVel = totalWheelDist / uncorrectedDeltaTime;
                    // Calculate the max velocity of the center of the bot to not violate the max
                    // velocity with any wheel. ((m2/s2)/(m/s) = m/s)
                    double correctedMaxVel = vel * vel / uncorrectedWheelVel;

                    /* Calculate the maximum acceleration/deceleration velocity*/
                    //Max velocity for accelerating entire profile with no max vel
                    double accelVelocity = Math.sqrt(2 * distance * accel);
                    //Same for decelerating entire profile
                    double decelVelocity = Math.sqrt(2 * (endPos - distance) * accel);
                    //Maximum for a triangular profile
                    double triangularVelocity = Math.min(accelVelocity, decelVelocity);

                    /*Calculate the final velocity at this point*/
                    wp.velocity = Math.min(triangularVelocity, correctedMaxVel);
                }
                break;
            default:
                Logging.e("Couldn't find velocity profile mode.");
        }
    }

    private double angleBetween(double angle1, double angle2) {
        double angleDif = angle2 - angle1;
        if (angleDif > Math.PI)
            angleDif -= 2 * Math.PI;
        else if (angleDif < -Math.PI)
            angleDif += 2 * Math.PI;
        return angleDif;
    }

    /**
     * Calculates the time of each waypoint by determining the average velocity and
     * distance and determining the time between waypoints then adding the times up.
     */
    private void getTimes() {
        // starting point is at time 0
        waypoints.get(0).time = 0;
        // set previous
        Waypoint lastWaypoint = waypoints.get(0);
        // loop through all but first
        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint currentWaypoint = waypoints.get(i);
            // calculate the distance,velocity, and time between the last and current
            // waypoint
            double distance = currentWaypoint.distance - lastWaypoint.distance;
            double averageVel = (currentWaypoint.velocity + lastWaypoint.velocity) / 2.0;
            double deltaTime = distance / averageVel;
            // calculate the time based on the delta time and last time
            currentWaypoint.time = lastWaypoint.time + deltaTime;

            lastWaypoint = currentWaypoint;
        }
        endTime = waypoints.get(waypoints.size() - 1).time;
    }

    void genPath(Waypoint start, Waypoint end, SplineMode mode, double tightness){
        switch(mode){
            case BEZIER:
                genCubicBezierPath(start,end,defaultPoints,tightness);
                break;
            case CATMULL_ROM:
                //Catmull-Rom is a special case of cubic Bezier or Hermite,
                // so use Hermite.
                genCubicHermitePath(start, end, defaultPoints,0.5);
                break;
            case CUBIC_HERMITE:
                genCubicHermitePath(start,end,defaultPoints,tightness);
                break;
            case QUINTIC_HERMITE:
                genQuinticHermitePath(start,end,defaultPoints,tightness);
                break;
            default:
                Logging.e("Failed to find spline mode.");
                break;
        }
    }

    void genChainPath(SplineMode mode, double tightness, Waypoint... waypoints){
        switch(mode){
            case BEZIER:
                genCubicBezierChainPath(defaultPoints,tightness,waypoints);
                break;
            case CATMULL_ROM:
                //Catmull-Rom is a special case of cubic Bezier or Hermite,
                // so use Hermite.
                genCatmullRomPath(defaultPoints, waypoints);
                break;
            case CUBIC_HERMITE:
                //Unsuported path type
                Logging.e("Cubic Hermite paths not supported for more than 2 waypoints");
                break;
            case QUINTIC_HERMITE:
                genQuinticHermiteChainPath(defaultPoints,tightness,waypoints);
                break;
            default:
                Logging.e("Failed to find spline mode.");
                break;
        }
    }

    /**
     * generates a path with a bezier curve
     *
     * @param start          the starting point
     * @param end            the ending point
     * @param numberOfPoints the number of points between the start and the end
     * @param tightness      how close the guide points are put to the start/end
     */
    void genCubicBezierPath(Waypoint start, Waypoint end, int numberOfPoints, double tightness) {
        // get the location of the start and end points
        Point startPoint = start.getPoint();
        Point endPoint = end.getPoint();
        double distance = startPoint.distance(endPoint);
        double gpLength = distance / 2 * tightness;

        Point startOffset = Point.PolarPoint(gpLength, start.rotation);
        Point endOffset = Point.PolarPoint(-gpLength, end.rotation);

        Point gp1 = startPoint.sum(startOffset);
        Point gp2 = endPoint.sum(endOffset);

        waypoints.add(start);
        for (int i = 1; i < numberOfPoints; i++) {
            double alpha = (double) i / (double) numberOfPoints;
            Point p1 = Point.lerp(startPoint, gp1, alpha);
            Point p2 = Point.lerp(gp1, gp2, alpha);
            Point p3 = Point.lerp(gp2, endPoint, alpha);

            Point p5 = Point.lerp(p1, p2, alpha);
            Point p6 = Point.lerp(p2, p3, alpha);

            Point p = Point.lerp(p5, p6, alpha);
            Waypoint wp = new Waypoint(p, 0);
            waypoints.add(wp);
        }
        waypoints.add(end);
    }

    /**
     * Generates a longer path by chaining together more waypoints.
     *
     * @param pointsPerCurve the number of points in each curve
     * @param tightness      how close the guide points are to the start/end of each individual
     *                       curve.
     * @param points         the waypoints to build the curve from
     */
    void genCubicBezierChainPath(int pointsPerCurve, double tightness, Waypoint... points) {
        if (points.length < 2) {
            Logging.e("Not enough waypoints to make a path!");
            return;
        }

        // add first point
        waypoints.add(points[0]);
        // iterates throught the waypoints in pairs.
        for (int wp = 0; wp < points.length - 1; wp++) {
            Waypoint start = points[wp];
            Waypoint end = points[wp + 1];
            Point startPoint = start.getPoint();
            Point endPoint = end.getPoint();
            double distance = startPoint.distance(endPoint);
            double gpLength = distance / 2 * tightness;

//            Note: The following code may produce slightly smoother paths, but is not necessary
//             as bezier splines will NOT have matched second derivatives unless other special cases
//             are met that will interfere with setting the direction of start/end segments.
//             This code only matches the LENGTH of the guide segments.
//            double startGPLength;
//            double endGPLength;
//
//            if (wp > 0) {
//                Waypoint lastWP = points[wp - 1];
//                double lastDist = startPoint.distance(lastWP.getPoint());
//                double lastGPLength = lastDist / 2 * tightness;
//                startGPLength = Math.min(lastGPLength, gpLength);
//            } else {
//                startGPLength = gpLength;
//            }
//            // correct end guide point
//            if (wp < points.length - 2) {
//                Waypoint nextWP = points[wp + 2];
//                double nextDist = startPoint.distance(nextWP.getPoint());
//                double nextGPLength = nextDist / 2 * tightness;
//                endGPLength = Math.min(gpLength, nextGPLength);
//            } else {
//                endGPLength = gpLength;
//            }


            Point startOffset = Point.PolarPoint(gpLength, start.rotation);
            Point endOffset = Point.PolarPoint(-gpLength, end.rotation);

            Point gp1 = startPoint.sum(startOffset);
            Point gp2 = endPoint.sum(endOffset);

            for (int i = 1; i <= pointsPerCurve; i++) {
                double alpha = (double) i / (double) pointsPerCurve;
                Point p1 = Point.lerp(startPoint, gp1, alpha);
                Point p2 = Point.lerp(gp1, gp2, alpha);
                Point p3 = Point.lerp(gp2, endPoint, alpha);

                Point p5 = Point.lerp(p1, p2, alpha);
                Point p6 = Point.lerp(p2, p3, alpha);

                Point p = Point.lerp(p5, p6, alpha);
                waypoints.add(new Waypoint(p, 0));
            }
        }
        // set last point
        waypoints.set(waypoints.size() - 1, points[points.length - 1]);
    }

    /**
     * Generates a longer path by chaining together more waypoints. This method uses Catmull-Rom splines.
     *
     * @param pointsPerCurve the number of points in each curve
     * @param points         the waypoints to build the curve from
     */
    void genCatmullRomPath(int pointsPerCurve, Waypoint... points) {
        if (points.length < 2) {
            Logging.e("Not enough waypoints to make a path!");
            return;
        }

        //'Tightness' for a Catmull-Rom spline
        double tightness = 0.5;

        // add first point
        waypoints.add(points[0]);
        // iterates throught the waypoints in pairs.
        for (int wp = 0; wp < points.length - 1; wp++) {
            Waypoint start = points[wp];
            Waypoint end = points[wp + 1];
            Point startPoint = start.getPoint();
            Point endPoint = end.getPoint();
            double distance = startPoint.distance(endPoint);
            double gpLength = distance / 2 * tightness;

            double startGPLength;
            double endGPLength;

            if (wp > 0) {
                Waypoint lastWP = points[wp - 1];
                double lastDist = startPoint.distance(lastWP.getPoint());
                double lastGPLength = lastDist / 2 * tightness;
                startGPLength = Math.min(lastGPLength, gpLength);
            } else {
                startGPLength = gpLength;
            }
            // correct end guide point
            if (wp < points.length - 2) {
                Waypoint nextWP = points[wp + 2];
                double nextDist = startPoint.distance(nextWP.getPoint());
                double nextGPLength = nextDist / 2 * tightness;
                endGPLength = Math.min(gpLength, nextGPLength);
            } else {
                endGPLength = gpLength;
            }


            Point startOffset = Point.PolarPoint(startGPLength, start.rotation);
            Point endOffset = Point.PolarPoint(-endGPLength, end.rotation);

            Point gp1 = startPoint.sum(startOffset);
            Point gp2 = endPoint.sum(endOffset);

            for (int i = 1; i <= pointsPerCurve; i++) {
                double alpha = (double) i / (double) pointsPerCurve;
                Point p1 = Point.lerp(startPoint, gp1, alpha);
                Point p2 = Point.lerp(gp1, gp2, alpha);
                Point p3 = Point.lerp(gp2, endPoint, alpha);

                Point p5 = Point.lerp(p1, p2, alpha);
                Point p6 = Point.lerp(p2, p3, alpha);

                Point p = Point.lerp(p5, p6, alpha);
                waypoints.add(new Waypoint(p, 0));
            }
        }
        // set last point
        waypoints.set(waypoints.size() - 1, points[points.length - 1]);
    }

    /**
     * generates a path with a hermite spline.
     *
     * @param start          the starting point
     * @param end            the ending point
     * @param numberOfPoints the number of points between the start and the end
     * @param tightness      how close the guide points are put to the start/end
     */
    void genCubicHermitePath(Waypoint start, Waypoint end, int numberOfPoints, double tightness) {
        // get the location of the start and end points
        Point startPoint = start.getPoint();
        Point endPoint = end.getPoint();
        double distance = startPoint.distance(endPoint);
        double gpLength = distance / 2 * tightness;

        Point startVel = Point.PolarPoint(gpLength, start.rotation);
        Point endVel = Point.PolarPoint(gpLength, end.rotation);

        waypoints.add(start);
        for (int i = 1; i < numberOfPoints; i++) {
            // Calculate the value (0 to 1) representing the distance along the spline
            double s = (double) i / (double) numberOfPoints;

            //Calculate the four cubic hermite basis functions
            // h1(s) =  2s^3 - 3s^2 + 1
            // h2(s) = -2s^3 + 3s^2
            // h3(s) =   s^3 - 2s^2 + s
            // h4(s) =   s^3 -  s^2
            double b1 = 2 * Math.pow(s, 3) - 3 * s * s + 1;
            double b2 = -2 * Math.pow(s, 3) + 3 * s * s;
            double b3 = Math.pow(s,3) - 2 * s * s + s;
            double b4 = Math.pow(s,3) - s * s;

            // Calculate the spline value
            double x = b1 * startPoint.x + b2 * endPoint.x + b3 * startVel.x + b4 * endVel.x;
            double y = b1 * startPoint.y + b2 * endPoint.y + b3 * startVel.y + b4 * endVel.y;
            //Create a new point (x,y) and add it
            Point p = new Point(x,y);
            Waypoint wp = new Waypoint(p, 0);
            waypoints.add(wp);
        }
        waypoints.add(end);
    }

    /**
     * generates a path with a quintic hermite spline and 0 acceleration at start and end
     * to remove infinite sideways jerk where paths meet (and thus eliminate infinite acceleration)
     * Note that this is VERY similar to genCubicHermitePath, but uses 5th order basis functions.
     *
     * @param start          the starting point
     * @param end            the ending point
     * @param numberOfPoints the number of points between the start and the end
     * @param tightness      how close the guide points are put to the start/end
     */
    void genQuinticHermitePath(Waypoint start, Waypoint end, int numberOfPoints, double tightness) {
        // get the location of the start and end points
        Point startPoint = start.getPoint();
        Point endPoint = end.getPoint();
        double distance = startPoint.distance(endPoint);
        double gpLength = distance / 2 * tightness;

        Point startVel = Point.PolarPoint(gpLength, start.rotation);
        Point endVel = Point.PolarPoint(gpLength, end.rotation);

        waypoints.add(start);
        for (int i = 1; i < numberOfPoints; i++) {
            // Calculate the value (0 to 1) representing the distance along the spline
            double s = (double) i / (double) numberOfPoints;

            //Calculate the six quintic hermite basis functions (H2 and H3 aren't used
            //h0(s) = 1 - 10s^3 + 15s^4 - 6s^5
            //h1(s) = s - 6s^3 + 8s^4 - 3s^5
            //h2(s) Unused = 1/2 s^2 - 3/2 s^3 + 3/2 s^4 - 1/2 s^5
            //h3(s) Unused = 1/2 s^3 - s^4 + 1/2 s^5
            //h4(s) = -4s^3 + 7s^4 - 3s^5
            //h5(s) = 10s^3 - 15s^4 + 6s^5
            double b0 = 1 - 10 * Math.pow(s, 3) + 15 * Math.pow(s, 4) - 6 * Math.pow(s, 5);
            double b1 = s - 6  * Math.pow(s,3) + 8 * Math.pow(s,4) - 3 * Math.pow(s,5);
            double b4 = -4 * Math.pow(s,3) + 7 * Math.pow(s,4) - 3 * Math.pow(s,5);
            double b5 = 10 * Math.pow(s,3) - 15 * Math.pow(s,4) + 6 * Math.pow(s,5);

            // Calculate the spline value
            double x = b0 * startPoint.x + b1 * startVel.x + b4 * endVel.x + b5 * endPoint.x;
            double y = b0 * startPoint.y + b1 * startVel.y + b4 * endVel.y + b5 * endPoint.y;
            //Create a new point (x,y) and add it
            Point p = new Point(x,y);
            Waypoint wp = new Waypoint(p, 0);
            waypoints.add(wp);
        }
        waypoints.add(end);
    }

    /**
     * generates a path with a quintic hermite spline and 0 acceleration at start and end
     * to remove infinite sideways jerk where paths meet (and thus eliminate infinite acceleration)
     * Note that this is VERY similar to genCubicHermitePath, but uses 5th order basis functions. This
     * could be optimized in the future by adding starting/ending accelerations that are calculated smartly.
     *
     * @param pointsPerCurve the number of points in each segment
     * @param tightness      how close the guide points are put to the start/end
     */
    void genQuinticHermiteChainPath(int pointsPerCurve, double tightness, Waypoint... points) {
        if (points.length < 2) {
            Logging.e("Not enough waypoints to make a path!");
            return;
        }

        // add first point
        waypoints.add(points[0]);
        // iterates throught the waypoints in pairs.
        for (int wp = 0; wp < points.length - 1; wp++) {
            Waypoint start = points[wp];
            Waypoint end = points[wp + 1];
            Point startPoint = start.getPoint();
            Point endPoint = end.getPoint();
            double distance = startPoint.distance(endPoint);
            double gpLength = distance / 2 * tightness;

            Point startVel = Point.PolarPoint(gpLength, start.rotation);
            Point endVel = Point.PolarPoint(gpLength, end.rotation);

            for (int i = 1; i <= pointsPerCurve; i++) {
                // Calculate the value (0 to 1) representing the distance along the spline
                double s = (double) i / (double) pointsPerCurve;

                //Calculate the six quintic hermite basis functions (H2 and H3 aren't used
                //h0(s) = 1 - 10s^3 + 15s^4 - 6s^5
                //h1(s) = s - 6s^3 + 8s^4 - 3s^5
                //h2(s) Unused = 1/2 s^2 - 3/2 s^3 + 3/2 s^4 - 1/2 s^5
                //h3(s) Unused = 1/2 s^3 - s^4 + 1/2 s^5
                //h4(s) = -4s^3 + 7s^4 - 3s^5
                //h5(s) = 10s^3 - 15s^4 + 6s^5
                double b0 = 1 - 10 * Math.pow(s, 3) + 15 * Math.pow(s, 4) - 6 * Math.pow(s, 5);
                double b1 = s - 6  * Math.pow(s,3) + 8 * Math.pow(s,4) - 3 * Math.pow(s,5);
                double b4 = -4 * Math.pow(s,3) + 7 * Math.pow(s,4) - 3 * Math.pow(s,5);
                double b5 = 10 * Math.pow(s,3) - 15 * Math.pow(s,4) + 6 * Math.pow(s,5);

                // Calculate the spline value
                double x = b0 * startPoint.x + b1 * startVel.x + b4 * endVel.x + b5 * endPoint.x;
                double y = b0 * startPoint.y + b1 * startVel.y + b4 * endVel.y + b5 * endPoint.y;
                //Create a new point (x,y) and add it
                Point p = new Point(x,y);
                waypoints.add(new Waypoint(p, 0));
            }
        }
        // set last point
        waypoints.set(waypoints.size() - 1, points[points.length - 1]);
    }

    /**
     * aligns the waypoints all pointing to the next waypoint.
     */
    void alignWaypoints() {
        for (int i = 1; i < waypoints.size() - 1; i++) {
            waypoints.get(i).pointTowards(waypoints.get(i + 1).getPoint());
        }
    }

    @Override
    public String toString() {
        String out = "";
        for (Waypoint wp : waypoints) {
            out = out + wp.toString() + "\n";
        }
        return out;
    }
}
