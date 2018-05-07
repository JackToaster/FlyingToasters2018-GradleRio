package org.theflyingtoasters.controllers.motion_profiles;

import org.theflyingtoasters.path_generation.*;

/**
 * Generates a Profile for a given wheel from a Path.
 * 
 * @author jack
 *
 */
public abstract class WheelProfileGenerator {
	/**
	 * generates the profile from the given path.
	 * 
	 * @param p
	 *            the path to generate a profile from
	 * @param isBackwards
	 *            whether to generate a profile for a backwards-driving robot
	 * @return the generated profile
	 */
	public abstract Profile genPoints(Path p, boolean isBackwards);
}
