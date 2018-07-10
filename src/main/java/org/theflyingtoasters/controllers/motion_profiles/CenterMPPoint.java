package org.theflyingtoasters.controllers.motion_profiles;

import org.theflyingtoasters.utilities.Utilities;

/**
 * Motion profile point for center of bot
 */
public class CenterMPPoint extends MPPoint {
    public double orientation;

    public CenterMPPoint(double vel, double pos, double t, double theta) {
        super(vel,pos,t);
        orientation = theta;
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
    public CenterMPPoint lerp(CenterMPPoint p2, double alpha) {
        double newVel = Utilities.lerp(this.velocity, p2.velocity, alpha);
        double newPos = Utilities.lerp(this.position, p2.position, alpha);
        double newTime = Utilities.lerp(this.time, p2.time, alpha);
        double newOrientation = Utilities.lerp(this.orientation, p2.orientation, alpha);
        return new CenterMPPoint(newVel, newPos, newTime, newOrientation);
    }
}
