package org.theflyingtoasters.kalman;

import Jama.Matrix;

public class RobotPoseKF {
    //TODO set the mass & moment of inertia properly
    static final double robotMassKg = 54;
    // Assumes the bot is a 1m diameter cylinder for next two:
    static final double robotMomentKgM2 = 6.75;
    static final double robotRadius = 0.5;

    // The time stepped forward in each update step
    static final double deltaTime = 0.01;

    KalmanFilter kf;

    Matrix latestState = new Matrix(1,5);

    /**
     * This matrix is multiplied by the previous state to get the current state. It needs
     * to update every time before it is used based on the current robot state.
      */
    static final KalmanFilter.StateDependantModel A = (X, dT) -> new Matrix(new double[][]{
            {1, 0, 0, dT * Math.cos(X.get(0,2)), 0},
            {0, 1, 0, dT * Math.sin(X.get(0,2)), 0},
            {0, 0, 1, 0, dT},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 1}},
            5, 5);
    /**
     * This matrix is multiplied by the control input (Force from each side of the drivetrain)
     * each update step to model motor forces on the robot.
     */
    static final KalmanFilter.StateDependantModel B = (X, dT) -> new Matrix(new double[][]{
            {0, 0},
            {0, 0},
            {0, 0},
            {1/robotMassKg, 1/robotMassKg},
            {-Math.PI * robotRadius / robotMomentKgM2, Math.PI * robotRadius / robotMomentKgM2}});

    public RobotPoseKF() {
        kf = new KalmanFilter(latestState, null, null, null, A, B, deltaTime);
    }
}
