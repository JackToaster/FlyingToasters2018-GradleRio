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
            {0, 0, 1, 0,                              dT},
            {0, 0, 0, 1,                              0},
            {0, 0, 0, 0,                              1}},
            5, 5);
    /**
     * This matrix is multiplied by the control input (Force from each side of the drivetrain)
     * each update step to model motor forces on the robot.
     */
    static final KalmanFilter.StateDependantModel B = (X, dT) -> new Matrix(new double[][]{
            {0,                                        0},
            {0,                                        0},
            {0,                                        0},
            {1/robotMassKg,                            1/robotMassKg},
            {-Math.PI * robotRadius / robotMomentKgM2, Math.PI * robotRadius / robotMomentKgM2}},
            5, 2);

    /**
     * This matrix maps measurements of the system to the state.
     *
     * Sensor measurements:
     * [left displacement, right displacement, gyro angle, gyro rate]
     */
    static final Matrix H = new Matrix(new double[][]{
            {0,                       0,                       0, 0},
            {0,                       0,                       0, 0},
            {0,                       0,                       1, 0},
            {deltaTime,               deltaTime,               0, 0},
            {deltaTime * robotRadius, deltaTime * robotRadius, 0, 1}},
            5, 4);

    /**
     * Process covariance matrix. Represents the error of the model of the system.
     */
    private static final Matrix Q = Matrix.identity(5,5).times(0.1);


    //TODO determine std. dev of each sensor and input them to R matrix
    /**
     * Measurement covariance matrix. Represents the variance (std. deviation squared)
     * of each sensor and covariance between them.
     */
    private static final Matrix R = new Matrix(new double[][]{
            {1,0,0,0},
            {0,1,0,0},
            {0,0,1,0},
            {0,0,0,1}
    });
    public RobotPoseKF() {
        kf = new KalmanFilter(latestState, H, R, Q, A, B,  1, deltaTime);
    }
}
