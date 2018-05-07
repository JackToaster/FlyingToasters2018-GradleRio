package org.theflyingtoasters.hardware;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.theflyingtoasters.utilities.Logging;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;

public class PDP extends PowerDistributionPanel
{
	private PrintStream logStream;
	
	private static final int PORTS = 16;
	private static String DIR = "/home/lvuser/current_log/";
	private static final double LOG_FREQUENCY = 0.1;
	private static final int PERCISION = 2;
	private double time = 0;
	private double lastTime = -LOG_FREQUENCY;
	private DriverStation DS;
	
	/**
	 * Create the pdp object
	 */
	public PDP() {
		super();
		String logName = null;
		DS = DriverStation.getInstance();
		if(DS.isFMSAttached()) {
			DIR += DS.getEventName().replace(' ', '_').replace('/','-') + "/" + DS.getMatchType().toString() + "/";
			logName = DS.getMatchType().toString().charAt(0) + DS.getMatchNumber() + ".csv";
		} else { 
		}
		
		File dir = new File(DIR);
		int count = 0;
		dir.mkdirs();
		String[] files = dir.list();
		if(files != null) {
			if(logName == null) {
				for(String filename : files) {
					if(filename.contains(".csv")) {
						count ++;
					}
				}
				logName = DIR + "log" + count + ".csv";
			}
			SmartDashboard.putString("logName", logName);
			try {
				File logFile = new File(logName);
				logFile.createNewFile();
				logStream = new PrintStream(logName);
				String outputString = "Time,\tVoltage,\tTemperature,\t";
				for(int i = 0; i<PORTS; i++) {
					outputString += "Port " + i + " Current,\t";
				}
				outputString += "Mode";
				logStream.println(outputString);
				logStream.flush();
			} catch (IOException e) { 
				Logging.h("Could not create new current log \"" + logName + "\"");
			}
		} else { 
			SmartDashboard.putString("logName", "NONE!");
			Logging.h("\"" + DIR + "\" is not a directory...");
		}
	}
	
	public void periodic(double deltaTime) {
		time += deltaTime;
		SmartDashboard.putNumber("PDP Log time", time);
	}
	
	private String getMode() {
		String mode;
		if(DS.isAutonomous()) {
			mode = "Auton";
		} else if(DS.isEnabled()) {
			mode = "Teleop";
		} else {
			mode = "Disabled";
		}
		return mode;
	}
	
	public void forceLogCurrent() {
		SmartDashboard.putBoolean("LogStream", (logStream != null));
		if(logStream != null) {
			String outputString = String.format("%." + PERCISION + "f", time) + ",\t" + String.format("%.2f", this.getVoltage()) + ",\t" + String.format("%.2f", this.getTemperature()) + ",\t";
			for(int i = 0; i<PORTS; i++) {
				outputString += String.format("%." + PERCISION + "f", this.getCurrent(i)) + ",\t";
			}
			outputString += getMode();
			SmartDashboard.putString("PDP Data", outputString);
			logStream.println(outputString);
			logStream.flush();
			lastTime = time;
		}
	}
	
	public void logCurrent() {
		if(time >= lastTime + LOG_FREQUENCY) {
			forceLogCurrent();
		}
	}
}