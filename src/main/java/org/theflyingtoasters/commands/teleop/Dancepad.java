package org.theflyingtoasters.commands.teleop;

import edu.wpi.first.wpilibj.Joystick;

public class Dancepad {
    private static final double deadZone = 0.1;
    private static final double minOutput = 0.2;
    private static final double rampUpRate = 0.5;
    private static final double rampDownRate = 1.5;
    private static final double maxOutput = 0.7;

    private Joystick rawJoystick;

    double currentVertical;
    double currentHorizontal;

    private interface Poller{
        boolean poll(Joystick joystick);
    }
    private enum directions{
        LEFT((j)->
            j.getRawButton(1) || j.getRawButton(3) || j.getPOV() > 180),
        RIGHT((j)->
            j.getRawButton(0) || j.getRawButton(2) || (j.getPOV() >= 0 && j.getPOV() < 180)),
        UP((j)->
            j.getRawButton(0) || j.getRawButton(1) || (j.getPOV() != -1 && (j.getPOV() < 90 || j.getPOV() > 270))),
        DOWN((j)->
            j.getRawButton(2) || j.getRawButton(3) || (j.getPOV() > 90 && j.getPOV() < 180)),
        
        CLIMB_UP((j)-> j.getRawButton(7)),
        CLIMB_DOWN((j)-> j.getRawButton(8));
        
        private Poller p;
        directions(Poller p){
            this.p = p;
        }

        boolean poll(Joystick joystick){
            return p.poll(joystick);
        }
    }

    enum Axis{
        HORIZONTAL, VERTICAL;
    }


    public Dancepad(int port){
        rawJoystick = new Joystick(port);
    }

    public void poll(double dT){
        if(directions.LEFT.poll(rawJoystick)&& !directions.RIGHT.poll(rawJoystick)) {
            if(currentHorizontal >= 0){
                currentHorizontal = -minOutput;
            }else{
                currentHorizontal -= rampUpRate * dT;
            }
        }else if(directions.RIGHT.poll(rawJoystick) && !directions.LEFT.poll(rawJoystick)){
            if(currentHorizontal >= 0){
                currentHorizontal = minOutput;
            }else {
                currentHorizontal += rampUpRate * dT;
            }
        }else{
            if(currentHorizontal > deadZone) currentHorizontal -= rampDownRate * dT;
            else if(currentHorizontal < -deadZone) currentHorizontal += rampDownRate * dT;
            else currentHorizontal = 0;
        }

        if(directions.DOWN.poll(rawJoystick)&& !directions.UP.poll(rawJoystick)) {
            if(currentVertical >= 0){
                currentHorizontal = -minOutput;
            }else{
                currentHorizontal -= rampUpRate * dT;
            }
        }else if(directions.RIGHT.poll(rawJoystick) && !directions.DOWN.poll(rawJoystick)){
            if(currentHorizontal >= 0){
                currentHorizontal = minOutput;
            }else {
                currentHorizontal += rampUpRate * dT;
            }
        }else{
            if(currentVertical > deadZone) currentVertical -= rampDownRate * dT;
            else if(currentVertical < -deadZone) currentVertical += rampDownRate * dT;
            else currentVertical = 0;
        }

        currentHorizontal = clamp(currentHorizontal, maxOutput);

        currentVertical = clamp(currentVertical, maxOutput);
    }

    private double clamp(double val, double range){
        if(val > range) return range;
        else if(val < -range) return -range;
        else return val;
    }

    public double getAxis(Axis axis){
        switch (axis){
            case VERTICAL:
                return currentVertical;

            case HORIZONTAL:
                return currentHorizontal;

            default: return 0;
        }
    }
}
