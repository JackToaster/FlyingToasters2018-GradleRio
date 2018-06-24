package org.theflyingtoasters.controllers.motion_profiles;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.theflyingtoasters.controllers.AbstractFeedbackController;
import org.theflyingtoasters.controllers.PIDcontroller;
import org.theflyingtoasters.path_generation.Path;
import org.theflyingtoasters.utilities.Logging;

public class DualPIDMotionProfile implements AbstractFeedbackController {
    private double distance, angle;
    private double angEffect;


    private double error = 0;
    private Profile profile;
    private double totalTime = 0;
    private MPPoint lastTarget;
    private WheelProfileGenerator wpg;
    // the amount to offset the encoder value by
    private double offset;

    public PIDcontroller distPid;
    public PIDcontroller angPid;

    public double kV = 0;
    public double kA = 0;

    /**
     * Constructor without profile
     *
     * @param pidController
     *            the PID controller for the motion profile
     * @param velGain
     *            the velocity gain
     * @param accelGain
     *            the acceleration gain (note: not necesary with a D gain in the
     *            PID)
     * @param wheelProfileGen
     *            the wheelProfileGenerator to use to transform the path
     */
    public DualPIDMotionProfile(PIDcontroller pidController, PIDcontroller angPid, double angEffect, double velGain, double accelGain,
                         WheelProfileGenerator wheelProfileGen) {
        distPid = pidController;
        this.angPid = angPid;
        this.angEffect = angEffect;
        kV = velGain;
        kA = accelGain;
        wpg = wheelProfileGen;
    }

    /**
     * Constructor with profile
     *
     * @param distPid
     *            the PID controller for the motion profile
     * @param velGain
     *            the velocity gain
     * @param accelGain
     *            the acceleration gain (note: not necesary with a D gain in the
     *            PID)
     * @param wheelProfileGen
     *            the wheelProfileGenerator to use to transform the path
     * @param p
     *            the profile to follow
     */
    public DualPIDMotionProfile(PIDcontroller distPid, PIDcontroller angPid, double angEffect, double velGain, double accelGain,
                                WheelProfileGenerator wheelProfileGen, Profile p) {
        this.distPid = distPid;
        this.angPid = angPid;
        this.angEffect = angEffect;
        kV = velGain;
        kA = accelGain;
        profile = p;
        wpg = wheelProfileGen;
        lastTarget = p.start();
    }

    /**
     * sets the starting encoder offset
     *
     * @param encoderOffset
     *            the starting encoder offset of the profile
     */
    public void setOffset(double encoderOffset) {
        offset = encoderOffset;
    }

    /**
     * sets the points of the profile
     *
     * @param points
     *            the points to put in the profile
     */
    public void setPoints(MPPoint... points) {
        if (points.length < 2) {
            Logging.w("Useless motion profile - less than 2 points");
        } else {
            profile.setPoints(points);
            lastTarget = profile.start();
        }
    }

    /**
     * creates a Profile from the given path
     *
     * @param path
     *            the path to generate the profile from
     * @param isBackwards
     *            whether to generate the points for a backwards profile
     */
    public void generateProfileFromPath(Path path, boolean isBackwards) {
        profile = wpg.genPoints(path, isBackwards);

        lastTarget = profile.start();
    }

    @Override
    public void setGains(double... gains) {
        if (gains.length != 5) {// check to see if there are the right number
            // of values
            Logging.logMessage("Invalid number of parameters for MotionProfile.setGains", Logging.Priority.ERROR);

        } else {
            distPid.setGains(gains[0], gains[1], gains[2]);
            kV = gains[3];
            kA = gains[4];
        }
    }

    @Override
    public void readFromPrefs(String name) {
        // TODO make read from prefs
    }

    // setting/getting the setpoint of a motion profile makes no sense.
    @Override
    public void setSetpoint(double setpoint) {
    }

    @Override
    public double getSetpoint() {
        return 0;
    }


    public void setState(double dist, double angle) {
        this.distance = dist;
        this.angle = angle;
    }
    /**
     * run the closed loop control
     */
    public double run(double current, double deltaTime) {
        double offsetCurrent = distance - offset;
        // update current time
        totalTime += deltaTime;

        // stores the target position/velocity
        CenterMPPoint target;
        double accel;
        if (totalTime >= profile.getEndTime()) {
            Logging.l("Motion profile finished running");
            target = (CenterMPPoint) profile.end();
            accel = 0;
        } else {
            target = (CenterMPPoint) profile.getInterpolatedPoint(totalTime);
            accel = (target.velocity - lastTarget.velocity) / deltaTime;
        }

        // calculate error for logging
        error = offsetCurrent - target.position;

        // set up the PIDs
        distPid.setSetpoint(target.position);
        angPid.setSetpoint(target.orientation);
        double pidOut = distPid.run(offsetCurrent, deltaTime) + angEffect * angPid.run(angle, deltaTime);

        double velOut = kV * target.velocity;
        double accelOut = kA * accel;

        return pidOut + velOut + accelOut;
    }

    /**
     * resets the offset and time
     */
    public void reset() {
        totalTime = 0;
        offset = 0;
        distPid.reset();
        angPid.reset();
    }

    /**
     * writes out the current time elapsed to the console
     */
    public void logStatus() {
        Logging.l("Motion profile - current time: " + totalTime);
    }

    public String toString() {
        if (profile != null) {
            return "Generator: " + wpg.toString() + "\n Profile:\n" + profile.toString();
        } else {
            return "Generator: " + wpg.toString() + ", no profile generated";
        }
    }

    /**
     * writes the current closed loop error to the smartDashboard
     *
     * @param key
     *            the name of the item to put on the dashboard
     */
    public void writeErrorToDashboard(String key) {
        SmartDashboard.putNumber(key, error);
    }
}

