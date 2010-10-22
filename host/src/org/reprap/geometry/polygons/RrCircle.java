package org.reprap.geometry.polygons;

import org.reprap.Preferences;

/**
 * Small class to hold circles (centre and squared radius)
 * 
 */

public class RrCircle {
	private Rr2Point centre;
	private double radius2;
	
	/**
	 * Constructor makes a circle from three points on its
	 * circumference.
	 * (See "A Programmer's Geometry" p 65, by Adrian Bowyer and John Woodwark)
	 * @param pk
	 * @param pl
	 * @param pm
	 */
	public RrCircle(Rr2Point k, Rr2Point l, Rr2Point m) throws RrParallelLineException
	{
		Rr2Point lk = Rr2Point.sub(l, k);
		Rr2Point mk = Rr2Point.sub(m, k);
		double det = Rr2Point.op(lk, mk);
		if(Math.abs(det) < Preferences.tiny())
			throw new RrParallelLineException("RrCircle: colinear points.");
		double lk2 = Rr2Point.mul(lk, lk);
		double mk2 = Rr2Point.mul(mk, mk);
		Rr2Point lkt = new Rr2Point(lk2, lk.y());
		Rr2Point mkt = new Rr2Point(mk2, mk.y());
		double x = 0.5*Rr2Point.op(lkt, mkt)/det;
		lkt = new Rr2Point(lk.x(), lk2);
		mkt = new Rr2Point(mk.x(), mk2);
		double y = 0.5*Rr2Point.op(lkt, mkt)/det;
		radius2 = x*x + y*y;
		centre = new Rr2Point(x + k.x(), y + k.y());
	}
	
	public Rr2Point centre()
	{
		return centre;
	}
	
	public double radiusSquared()
	{
		return radius2;
	}

}
