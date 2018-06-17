package org.theflyingtoasters.kalman;


import Jama.Matrix;
import com.sun.org.apache.xalan.internal.lib.ExsltDatetime;
import org.theflyingtoasters.hardware.Intake;

/**
 * Implements the original Kalman Filter using Jama's Matrix API
 * @author Vy Nguyen
 */
public class KalmanFilter
{
    public interface StateDependantModel{
        Matrix getModel(Matrix stateVector, double dT);
    }

    // How long each update step is
    double dT;

    // variable states (Output of the Kalman filter)
    private Matrix X; // state vector
    private Matrix P; // state covariance matrix

    // constant parameters
    private final Matrix I; // the identity matrix of the proper size for the state covariance matrix
    private final Matrix R; // measurement noise covariance matrix
    private final Matrix Q; // Process covariance matrix. If elements of the state vector are
                            // unrelated, then this is usually just a diagonal matrix of small values.


    // Model parameters
    private final StateDependantModel A; // The model of the system, applied during update steps.
    private final StateDependantModel B; // The matrix to apply control inputs to the model
    private final Matrix H; // observation matrix. X * H = expected sensor readings.

    /**
     * Create a Kalman filter with the given parameters
     * @param X The starting state of the system
     * @param H The observation matrix
     * @param R The measurement covariance matrix
     * @param Q The process covariance matrix
     * @param A The model matrix
     * @param B The input matrix
     * @param initVariance the initial covariance of the state matrix. Must be non-zero!
     */
    public KalmanFilter(Matrix X, Matrix H, Matrix R, Matrix Q, StateDependantModel A, StateDependantModel B, double initVariance, double timeStep)
    {
        this.dT = timeStep;
        this.X = X;
        this.H = H;
        this.R = R;
        this.Q = Q;
        this.A = A;
        this.B = B;
        I = Matrix.identity(X.getRowDimension(), X.getColumnDimension());
        P = Matrix.identity(X.getRowDimension(), X.getColumnDimension()).times(initVariance);
    }

    /**
     * Predict the state and covariance after one timestep based on the current estimate of the state.
     * @param u the control input vector
     */
    public void predictionUpdate(Matrix u)
    {
        // Get current model & input matrices
        final Matrix Ak = A.getModel(X, dT);
        final Matrix Bk = B.getModel(X, dT);

        // State update equation
        // Updates estimated state vector (X) based on model (A), control matrix (B), and input vector (u).
        // Xk = A Xk-1 + B uk
        X = (Ak.times(X)).plus(Bk.times(u));

        // Updates covariance matrix (P) based on model (A) and process covariance (Q)
        // Pk = A Pk-1 At + Q
        P = Ak.times(P).times(Ak.transpose()).plus(Q);
    }


    // TODO Actually fix the measurementUpdate, because it's wrong.
    /**
     * Update the state estimate based on sensor readings.
     * @param Xk: the new measurement (sensor reading)
     */
    public void measurementUpdate(Matrix Xk)
    {
        //Matrix Hk = H.getModel(X, dT);
        // compute kalman gain
        //Kk+1 = PkHT(H PkHT + R)-1
        Matrix K = (P.times(H.transpose())).times((H.times(P).times(H.transpose()).plus(R)).inverse());

        // update x
        // xk+1 = xk + K(zk+1 − Hxk )
        X = X.plus(K.times((H.times(Xk)).minus(H.times(X))));

        // update P
        P = (I.minus(K.times(H))).times(P);
    }

    public Matrix getState(){
        return X;
    }

    public Matrix getCovarianceMatrix(){
        return P;
    }
}