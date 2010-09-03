package org.reprap.machines;

import org.reprap.utilities.Debug;

/**
 * Small class to compute the optimum velocity profile given a starting and an ending 
 * velocity, a maximum velocity that cannot be exceeded, and an acceleration.
 * 
 * The result is either a single maximum velocity, v, inbetween the ends (in which case flat will be 1), or
 * an acceleration to maxSpeed at s1 from the start, movement at that velocity to s2, then deceleration to 
 * the end (in which case flat will be 2).
 * 
 * If flat is 0 on return, the starting and ending speeds are greater than the maximum allowed...
 * 
 * @author Adrian
 *
 */

public class VelocityProfile 
{
	private double v, s1, s2;
	private int flat;
	
	public VelocityProfile(double s, double vStart, double maxSpeed, double vEnd, double acceleration)
	{
		if(maxSpeed <= vStart && maxSpeed <= vEnd)
		{
			flat = 0;
			return;
		}
		
		s1 = (2*acceleration*s - vStart*vStart + vEnd*vEnd)/(4*acceleration);
		v = Math.sqrt(2*acceleration*s1 + vStart*vStart);
		double f = s1/s;
		if(f < 0 || f > 1)
		{
			Debug.d("VelocityProfile - sm/s: " + f);
			s1 = Math.max(Math.min(s, s1), 0);
		} 
		if(v <= maxSpeed)
			flat = 1;
		else
		{
			s2 = s - 0.5*(maxSpeed*maxSpeed - vEnd*vEnd)/acceleration;
			f = s2/s;
			if(f < 0 || f > 1)
			{
				Debug.d("VelocityProfile - s2/s: " + f);
				s2 = Math.max(Math.min(s, s2), 0);
			}
			s1 = 0.5*(maxSpeed*maxSpeed - vStart*vStart)/acceleration;
			f = s1/s;
			if(f < 0 || f > 1)
			{
				Debug.d("VelocityProfile - s1/s: " + f);
				s1 = Math.max(Math.min(s, s1), 0);
			}
			flat = 2;
		}
	}

	public double v() { return v; }
	public double s1() { return s1; }
	public double s2() { return s2; }
	public int flat() { return flat; }
}
