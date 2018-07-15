package org.theflyingtoasters.teleop;
import java.util.EnumMap;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;

public class PS4
{
	private EnumMap<Button, Boolean> current, last;
	private EnumMap<Axis, Double> axes;
	private Joystick[] rawJoysticks = new Joystick[2];

	private RumbleThread rumbleThread;
	
	private class RumbleThread extends Thread {
		double value;
		boolean heavy;
		long ms;
		
		RumbleThread(double value, boolean heavy, double time) {
			ms = (long)(1000*time);
			this.value = value;
			this.heavy = heavy;
			this.start();
		}
		
		public void run() {
			rumble(value, heavy);
			try {
				Thread.sleep(ms);
				rumble(0, heavy);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * Initialize the PS4 controller with the port.
	 * 
	 * @param port The port it uses on the driver station.
	 */
	public PS4(int port) {
		rawJoysticks[0] = new Joystick(port);
		rawJoysticks[1] = new Joystick(port+1);
		current = new EnumMap<Button, Boolean>(Button.class);
		last = new EnumMap<Button, Boolean>(Button.class);
		axes = new EnumMap<Axis, Double>(Axis.class);
		poll(); //Populate the current EnumMap so the last EnumMap won't be null when the user polls for the first time.
		rumbleThread = new RumbleThread(0,true,0); //Create the thread so it exists.
	}
	
	/**
	 * The buttons it supports
	 */
	public static enum Button {
		SQUARE(1,0),
		X(2,0),
		CIRCLE(3,0),
		TRIANGLE(4,0),
		LEFT_BUMPER(5,0),
		RIGHT_BUMPER(6,0),
		LEFT_TRIGGER_BUTTON(7,0),
		RIGHT_TRIGGER_BUTTON(8,0),
		SHARE(9,0),
		OPTIONS(10,0),
		LEFT_STICK_BUTTON(11,0),
		RIGHT_STICK_BUTTON(12,0),
		PLAYSTATION_BUTTON(13,0),
		TOUCHPAD_BUTTON(14,0),
		DPAD_LEFT(0,0,270),
		DPAD_RIGHT(0,0,90),
		DPAD_UP(0,0,0),
		DPAD_DOWN(0,0,180);
		
		int number;
		int joystickNumber;
		boolean dpad;
		int position;
		
		Button(int number, int joystickNumber) {
			this.number = number;
			this.joystickNumber = joystickNumber;
			this.dpad = false;
		}
		
		Button(int number, int joystickNumber, int position) {
			this.dpad = true;
			this.position = position;
		}
	}
	
	/**
	 * The axes it supports
	 */
	public enum Axis {
		LEFT_X(0,0),
		LEFT_Y(1,0,true),
		RIGHT_X(2,0),
		LEFT_TRIGGER(3,0,.5,.5),
		RIGHT_TRIGGER(4,0,.5,.5),
		RIGHT_Y(5,0,true),
		TILT_ROLL(0,1),
		TILT_PITCH(1,1,true);
		
		int number;
		int joystickNumber;
		double multiplier;
		double offset;
		
		Axis(int number, int joystickNumber) {
			this.number = number;
			this.joystickNumber = joystickNumber;
			this.multiplier = 1;
			this.offset = 0;
		}
		
		Axis(int number, int joystickNumber, boolean inverted) {
			this.number = number;
			this.joystickNumber = joystickNumber;
			this.multiplier = inverted ? -1 : 1;
			this.offset = 0;
		}
		
		Axis(int number, int joystickNumber, double multiplier, double constant) {
			this.number = number;
			this.joystickNumber = joystickNumber;
			this.multiplier = multiplier;
			this.offset = constant;
		}
}
		
	/**
	 * Returns the value of the specified axis.
	 * 
	 * @param axis The axis to read.
	 * @return The value of said axis.
	 */
	public double getAxis(Axis axis) {
		return axes.get(axis);
	}
	
	/**
	 * Checks if the specified button is down at all.
	 * 
	 * @param button The button to read.
	 * @return True if the button is down.
	 */
	public boolean isDown(Button button) {
		return current.get(button);
	}
	
	/**
	 * Checks if the specified button has just been pressed.
	 * 
	 * @param button The button to read.
	 * @return True on the rising edge of the button.
	 */
	public boolean isPressed(Button button) {
		return (current.get(button) && !last.get(button));
	}
	
	/**
	 * Checks if the specified button has just been released.
	 * 
	 * @param button The button to read.
	 * @return True on the falling edge of the button.
	 */
	public boolean isReleased(Button button) {
		return (!current.get(button) && last.get(button));
	}
	
	public void rumble(double value, boolean heavy) {
		RumbleType type = (heavy) ? RumbleType.kLeftRumble : RumbleType.kRightRumble;
		rawJoysticks[1].setRumble(type, value);
	}
	
	public void rumbleForTime(double value, boolean heavy, double time) {
		rumbleThread.interrupt();
		rumbleThread = new RumbleThread(value, heavy, time);
	}
	
	/**
	 * Read the current state of each button and axis.
	 */
	public void poll() {
		last = current.clone();
		
		for(Button button : Button.values()) {
			if(button.dpad)	{
				current.put(button, rawJoysticks[button.joystickNumber].getPOV(button.number) == button.position);
			} else {
				current.put(button, rawJoysticks[button.joystickNumber].getRawButton(button.number));
			}
		}
		
		for(Axis axis : Axis.values()) {
			axes.put(axis, rawJoysticks[axis.joystickNumber].getRawAxis(axis.number) * axis.multiplier + axis.offset );
		}
	}
}
