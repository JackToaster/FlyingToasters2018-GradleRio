package org.theflyingtoasters.teleop;

import java.util.EnumMap;

import org.theflyingtoasters.utilities.Logging;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;

public class XPlorer
{
	private EnumMap<Button, Boolean> current, last;
	private EnumMap<Axis, Double> axes, lastAxes;
	private Joystick rawJoystick;

	/**
	 * Initialize the joysitck with the port.
	 * 
	 * @param port The port it uses on the driver station.
	 */
	public XPlorer(int port)
	{
		rawJoystick= new Joystick(port);
		current = new EnumMap<Button, Boolean>(Button.class);
		last = new EnumMap<Button, Boolean>(Button.class);
		axes = new EnumMap<Axis, Double>(Axis.class);
		poll(); //Populate the current EnumMap so the last EnumMap won't be null when the user polls for the first time.
	}
	
	/**
	 * The buttons it supports
	 */
	public static enum Button
	{
		BLUE, GREEN, RED, YELLOW, ORANGE,
		LOWER, BACK, START,
		STRUM, STRUM_UP, STRUM_DOWN,
		WHAMMY;
	}
	
	/**
	 * The axes it supports
	 */
	public enum Axis
	{
		STRUM, WHAMMY_BAR, BUTTONS, TILT;
	}

	/**
	 * Returns the value of the specified axis.
	 * 
	 * @param axis The axis to read.
	 * @return The value of said axis.
	 */
	public double getAxis(Axis axis)
	{
		return axes.get(axis);
	}
		
	/**
	 * Checks if the specified button is down at all.
	 * 
	 * @param button The button to read.
	 * @return True if the button is down.
	 */
	public boolean isDown(Button button)
	{
		return current.get(button);
	}
	
	/**
	 * Checks if the specified button has just been pressed.
	 * 
	 * @param button The button to read.
	 * @return True on the rising edge of the button.
	 */
	public boolean isPressed(Button button)
	{
		//Strum is special, because two states count as pressed.
		//If we switch between those, it passed over the unpressed state without polling,
		//so we want it to count as pressed anyway.
		if(button == Button.STRUM) return (isDown(button) && lastAxes.get(Axis.STRUM) != axes.get(Axis.STRUM));
		else return (current.get(button) && !last.get(button));
	}
	
	/**
	 * Checks if the specified button has just been released.
	 * 
	 * @param button The button to read.
	 * @return True on the falling edge of the button.
	 */
	public boolean isReleased(Button button)
	{
		return (!current.get(button) && last.get(button));
	}
	
	/**
	 * Read the current state of each button and axis.
	 */
	public void poll()
	{
		last = current.clone();
		lastAxes = axes.clone();

		if(rawJoystick.getPOV(0) == 0) axes.put(Axis.STRUM, 1.0);
		else if(rawJoystick.getPOV(0) == 180) axes.put(Axis.STRUM, -1.0);
		else axes.put(Axis.STRUM, 0.0);
		

		double wb = rawJoystick.getRawAxis(4);
		/*if(wb == -0.0078125) wb = 0;
		else wb = (wb+1)/2;*/
		
		axes.put(Axis.WHAMMY_BAR, (wb + 1)/2);
		axes.put(Axis.TILT, rawJoystick.getRawAxis(5));
		current.put(Button.WHAMMY, axes.get(Axis.WHAMMY_BAR) > 0.5);
		
		current.put(Button.STRUM, !(axes.get(Axis.STRUM) == 0));
		current.put(Button.STRUM_UP, axes.get(Axis.STRUM) > 0.5);
		current.put(Button.STRUM_DOWN, axes.get(Axis.STRUM) < -0.5);
		current.put(Button.GREEN, rawJoystick.getRawButton(1));
		current.put(Button.RED, rawJoystick.getRawButton(2));
		current.put(Button.BLUE, rawJoystick.getRawButton(3));
		current.put(Button.YELLOW, rawJoystick.getRawButton(4));
		current.put(Button.ORANGE, rawJoystick.getRawButton(5));
		current.put(Button.BACK, rawJoystick.getRawButton(7));
		current.put(Button.START, rawJoystick.getRawButton(8));
		
		current.put(Button.LOWER, rawJoystick.getRawButton(7));
		
		axes.put(Axis.BUTTONS, buttonsToAxis());
	}
	
	public void setRumble(double rumble)
	{
		rumble = Math.abs(rumble);
		Logging.h("Rumble: " + rumble);
		rawJoystick.setRumble(RumbleType.kLeftRumble, rumble);
		rawJoystick.setRumble(RumbleType.kRightRumble, rumble);
	}
	
	/**
	 * Converts the current value of the frets to an axis.
	 * 
	 * @return A number based on which frets are down.
	 */
	public double buttonsToAxis()
	{
		double rotation = 0;
		double numberOfButtons = 0;
		if(!isDown(Button.LOWER))
		{
			if(isDown(Button.GREEN))
			{
				rotation += 1;
				numberOfButtons++;
			}
			if(isDown(Button.RED))
			{
				rotation += .5;
				numberOfButtons++;
			}
			if(isDown(Button.YELLOW))
			{
				rotation += 0;
				numberOfButtons++;
			}
			if(isDown(Button.BLUE))
			{
				rotation -= .5;
				numberOfButtons++;
			}
			if(isDown(Button.ORANGE))
			{
				rotation -= 1;
				numberOfButtons++;
			}
			if(numberOfButtons != 0) rotation /= numberOfButtons;
		}
		return rotation;
	}
}
