package org.theflyingtoasters.controllers;


import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.theflyingtoasters.utilities.Coords;

/**
 * A PID Controller that goes the correct direction around a circle.
 */
public class AngularPID extends PIDcontroller {

    public AngularPID(double pGain, double iGain, double dGain) {
        super(pGain, iGain, dGain);
    }


    @Override
    public double run(double current, double deltaTime) {
        double error = Coords.calcAngleErrorRad(setpoint,current);
        //Logging.h("Angular error: " + error);
        //Logging.h("Current angle: " + current + ", target angle: " + setpoint);
        SmartDashboard.putNumber("Angular MP Error", error);
        integral -= error * deltaTime;
        if (limitIntegral) {
            if (Math.abs(integral) > maxIntegral / kI) {
                if (integral > 0) {
                    integral = maxIntegral / kI;
                } else {
                    integral = -maxIntegral / kI;
                }
            }
        }

        double deltaError;

        deltaError = (error - lastError) / deltaTime;

        // calculate the proportional + FF part of the PID
        double proportionalValue = error * kP;

        // calculate the integral + derivative parts
        double integralValue = -integral * kI;
        double derivativeValue = deltaError * kD;

        // set the last error for next loop
        lastReading = current;
        lastError = error;
        // return the value
        return proportionalValue + integralValue + derivativeValue;
    }
}
