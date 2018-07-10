package org.theflyingtoasters.controllers.motion_profiles;

import org.theflyingtoasters.path_generation.Path;
import org.theflyingtoasters.path_generation.Waypoint;

/**
 * Converts a path to forward or backwards profile
 */
public class CenterProfileGenerator/* extends WheelProfileGenerator */{
    //@Override
    public CenterProfile genPoints(Path p, boolean isBackwards) {
        CenterProfile outProfile = new CenterProfile(p.waypoints.size());

        for (int i = 0; i < p.waypoints.size(); i++) {
            // get the current waypoint
            Waypoint wp = p.waypoints.get(i);

            CenterMPPoint currentMPPoint;
            if (isBackwards) {
                currentMPPoint = new CenterMPPoint(-wp.velocity, -wp.distance, wp.time, wp.rotation);
            } else {
                currentMPPoint = new CenterMPPoint(wp.velocity, wp.distance, wp.time, wp.rotation + Math.PI);
            }
            outProfile.setPoint(i, currentMPPoint);
        }

        // return it!
        return outProfile;
    }
}