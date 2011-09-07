package org.reprap.geometry.polygons;

import org.reprap.Preferences;

/**
 * Small class to hold circles (centre and squared radius)
 * 
 */

public class Circle {
	private Point2D centre;
	private double radius2;
	
	/**
	 * Constructor makes a circle from three points on its
	 * circumference.
	 * (See "A Programmer's Geometry" p 65, by Adrian Bowyer and John Woodwark)
	 * @param pk
	 * @param pl
	 * @param pm
	 */
	public Circle(Point2D k, Point2D l, Point2D m) throws ParallelException
	{
		Point2D lk = Point2D.sub(l, k);
		Point2D mk = Point2D.sub(m, k);
		double det = Point2D.op(lk, mk);
		if(Math.abs(det) < Preferences.tiny())
			throw new ParallelException("RrCircle: colinear points.");
		double lk2 = Point2D.mul(lk, lk);
		double mk2 = Point2D.mul(mk, mk);
		Point2D lkt = new Point2D(lk2, lk.y());
		Point2D mkt = new Point2D(mk2, mk.y());
		double x = 0.5*Point2D.op(lkt, mkt)/det;
		lkt = new Point2D(lk.x(), lk2);
		mkt = new Point2D(mk.x(), mk2);
		double y = 0.5*Point2D.op(lkt, mkt)/det;
		radius2 = x*x + y*y;
		centre = new Point2D(x + k.x(), y + k.y());
	}
	
	public Point2D centre()
	{
		return centre;
	}
	
	public double radiusSquared()
	{
		return radius2;
	}

}
