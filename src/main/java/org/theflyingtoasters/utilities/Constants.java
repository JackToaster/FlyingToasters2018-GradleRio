package org.theflyingtoasters.utilities;

/**
 * Stores measurements of robot/field.
 * 
 * @author jack
 *
 */
public abstract class Constants {

	/**
	 * Measurements of various robot/field parts
	 * 
	 * @author jackf
	 *
	 */
	public interface Measurments {
		// 4 inch colson wheels
		// double DRIVE_WHEEL_DIAMETER = 0.1016;
		// 6 inch wheels (2018 chassis)
		double DRIVE_WHEEL_DIAMETER = 0.157;
		double DRIVE_WHEEL_CIRCUMFERENCE = Math.PI * DRIVE_WHEEL_DIAMETER;
		// with 2017 drivetrain
		// double DRIVE_ENCODER_TICKS_PER_TURN = 4096 * 1.87;
		// with 2018 drivetrain
		double DRIVE_ENCODER_TICKS_PER_TURN = 4096;
		double FIELD_WIDTH = 1111; // TODO set correct field dimensions
		double FIELD_LENGTH = 1111;
	}
}
