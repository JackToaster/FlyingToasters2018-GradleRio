package org.theflyingtoasters.utilities;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;

public class Logging {
	// priority of a message to be logged
	public enum Priority {
		LOW(0), MID(1), HIGH(2), WARN(3), ERROR(4);

		private int intValue;

		Priority(int val) {
			this.intValue = val;
		}
	}
	
	private static DriverStation DS = DriverStation.getInstance();
	private static Timer uptime = new Timer();

	private static Priority minPriority = Priority.HIGH;
	private static boolean enableWarn = true;
	private static boolean enableError = true;
	
	public static void init() {
		uptime.start();
		uptime.reset();
	}
	
	/**
	 * Sets minimum priority
	 * 
	 * @param p
	 *            minimum priority to log
	 */
	public static void setMinPriority(Priority p) {
		minPriority = p;
	}

	/**
	 * Enables printing of warnings
	 */
	public static void showWarnings() {
		enableWarn = true;
	}

	/**
	 * Enables printing errors
	 */
	public static void showErrors() {
		enableError = true;
	}

	/**
	 * Disables printing of warnings
	 */
	public static void hideWarnings() {
		enableWarn = false;
	}

	/**
	 * Disables printing errors
	 */
	public static void hideErrors() {
		enableError = false;
	}

	/**
	 * Logs a message.
	 * 
	 * @param message
	 *            the message to log
	 */
	public static void logMessage(Object message) {
		System.out.println(message.toString());
	}
	
	/**
	 * logs a warning
	 */
	public static void logWarning(Object message) {
		DriverStation.reportWarning(message.toString(), false);
	}
	
	public static void logError(Object message) {
		DriverStation.reportError(message.toString(), false);
	}
	
	
	/**
	 * Logs a message if the priority is high enough.
	 * 
	 * @param message
	 *            the message to log
	 * @param p
	 *            the priority of the message
	 */
	public static void logMessage(Object message, Priority p) {
		
		message = message.toString();
		switch (p) {
		case WARN:
			if (enableWarn)
				logWarning(getPrefix() + "[WARNING] " + message);
			break;
		case ERROR:
			if (enableError)
				logError(getPrefix() + "[ERROR] " + message);
			break;
		default:
			if (p.intValue >= minPriority.intValue) {
				logMessage(getPrefix() + message);
			}
			break;
		}
	}
	/**
	 * Logs a message if the priority is high enough.
	 * 
	 * @param message
	 *            the message to log
	 * @param p
	 *            the priority of the message, 0 = low, 1 = mid, 2 = high, 3 =
	 *            warn, 4 = error
	 */
	public static void logMessage(Object message, int p) {
		// Priority to be converted into
		Priority enumValue;

		switch (p) {
		case 0:
			enumValue = Priority.LOW;
			break;
		case 1:
			enumValue = Priority.MID;
			break;
		case 2:
			enumValue = Priority.HIGH;
			break;
		case 3:
			enumValue = Priority.WARN;
			break;
		case 4:
			enumValue = Priority.ERROR;
			break;
		default:
			enumValue = Priority.LOW;
			logMessage("Priority value out of range: " + p, Priority.ERROR);
			logMessage("(The message was \"" + message.toString() + "\")", Priority.ERROR);
			break;
		}

		logMessage(message, enumValue);
	}
	
	public static void l(Object message){
		logMessage(message, Priority.LOW);
	}
	public static void m(Object message){
		logMessage(message, Priority.MID);
	}
	public static void h(Object message){
		logMessage(message, Priority.HIGH);
	}
	public static void w(Object message){
		logMessage(message, Priority.WARN);
	}
	public static void e(Object message){
		logMessage(message, Priority.ERROR);
	}
	
	public static String getPrefix() {
		String mode =                   "  [Teleop]";
		if(DS.isAutonomous()) mode =    "   [Auton]";
		else if(DS.isTest()) mode =     "    [Test]";
		else if(DS.isDisabled()) mode = "[Disabled]";
		
		return mode + " [" + String.format("%.2f", uptime.get()) + "] ";
	}
	
	public static void resetUptime() {
		uptime.reset();
	}
}
