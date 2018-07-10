package org.theflyingtoasters.hardware;

import com.ctre.phoenix.sensors.PigeonIMU;

public class IMU {
    public static PigeonIMU pigeon;
    public static void calibrateTemp(){
        pigeon.enterCalibrationMode(PigeonIMU.CalibrationMode.Temperature, 100);
    }
}
