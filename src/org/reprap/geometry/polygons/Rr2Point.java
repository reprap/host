/*
 
 RepRap
 ------
 
 The Replicating Rapid Prototyper Project
 
 
 Copyright (C) 2005
 Adrian Bowyer & The University of Bath
 
 http://reprap.org
 
 Principal author:
 
 Adrian Bowyer
 Department of Mechanical Engineering
 Faculty of Engineering and Design
 University of Bath
 Bath BA2 7AY
 U.K.
 
 e-mail: A.Bowyer@bath.ac.uk
 
 RepRap is free; you can redistribute it and/or
 modify it under the terms of the GNU Library General Public
 Licence as published by the Free Software Foundation; either
 version 2 of the Licence, or (at your option) any later version.
 
 RepRap is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Library General Public Licence for more details.
 
 For this purpose the words "software" and "library" in the GNU Library
 General Public Licence are taken to mean any and all computer programs
 computer files data results documents and other copyright information
 available from the RepRap project.
 
 You should have received a copy of the GNU Library General Public
 Licence along with RepRap; if not, write to the Free
 Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA,
 or see
 
 http://www.gnu.org/
 
 =====================================================================
 
 
 Rr2Point: 2D vectors
 
 First version 20 May 2005
 This version: 1 May 2006 (Now in CVS - no more comments here)
 
 
 */

package org.reprap.geometry.polygons;

/**
 * Class for (x, y) points and vectors
 */
public class Rr2Point
{
	/**
	 * 
	 */
	private double x, y;
	
	/**
	 * Destroy me and all I point to
	 */
	public void destroy()
	{
		// I don't point to anything
	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		super.finalize();
//	}
	
	
	/**
	 * Default to the origin
	 */
	public Rr2Point()
	{
		x = 0;
		y = 0;
	}
	
	/**
	 * Usual constructor
	 * @param a
	 * @param b
	 */
	public Rr2Point(double a, double b)
	{
		x = a;
		y = b;
	}
	
	/**
	 * Copy
	 * @param r Rr2Point to copy from
	 */
	public Rr2Point(Rr2Point r)
	{
		x = r.x;
		y = r.y;
	}
	
	/**
	 * Overwrite
	 * @param p Rr2Point containing values to take
	 */
	public void set(Rr2Point p)
	{
		x = p.x;
		y = p.y;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return Double.toString(x) + " " + Double.toString(y);
	}
	
	/**
	 * Coordinates
	 */
	public double x() { return x; }
	public double y() { return y; }
	
	/**
	 * Arithmetic
	 * @return neg of point
	 */
	public Rr2Point neg()
	{
		return new Rr2Point(-x, -y);
	}
	
	/**
	 * @return orthogonal of (this) point
	 */
	public Rr2Point orthogonal()
	{
		return new Rr2Point(y, -x);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return a new point based on a vector addition of points a and b
	 */
	public static Rr2Point add(Rr2Point a, Rr2Point b)
	{
		Rr2Point r = new Rr2Point(a);
		r.x += b.x;
		r.y += b.y;
		return r;
	}
	
	/**
	 * @param a
	 * @param b
	 * @return a new point based on a vector subtraction of a - b
	 */
	public static Rr2Point sub(Rr2Point a, Rr2Point b)
	{
		return add(a, b.neg());
	}
	
	
	/**
	 * Scale a point
	 * @param b An R2rPoint
	 * @param factor A scale factor
	 * @return The point Rr2Point scaled by a factor of factor
	 */
	public static Rr2Point mul(Rr2Point b, double factor)
	{
		return new Rr2Point(b.x*factor, b.y*factor);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return the point Rr2Point scaled by a factor of a
	 */
	public static Rr2Point mul(double a, Rr2Point b)
	{
		return mul(b, a);
	}
	
	/**
	 * Downscale a point
	 * @param b An R2rPoint
	 * @param factor A scale factor
	 * @return The point Rr2Point divided by a factor of a
	 */
	public static Rr2Point div(Rr2Point b, double factor)
	{
		return mul(b, 1/factor);
	}
	
	/**
	 * Inner product
	 * @param a
	 * @param b
	 * @return The scalar product of the points
	 */
	public static double mul(Rr2Point a, Rr2Point b)
	{
		return a.x*b.x + a.y*b.y;
	}
	
	
	/**
	 * Modulus
	 * @return modulus
	 */
	public double mod()
	{
		return Math.sqrt(mul(this, this));
	}
	
	
	/**
	 * Unit length normalization
	 * @return normalized unit lenght 
	 */
	public Rr2Point norm()
	{
		return div(this, mod());
	}
	
	
	/**
	 * Outer product
	 * @param a
	 * @param b
	 * @return oute product
	 */
	public static double op(Rr2Point a, Rr2Point b)
	{
		return a.x*b.y - a.y*b.x;
	}
	
	/**
	 * Gradient
	 * @return gradient
	 */
	public double gradient()
	{
		double g;
		if(x == 0)
		{
			if(y > 0)
				g = Double.POSITIVE_INFINITY;
			else
				g = Double.NEGATIVE_INFINITY;
		} else
			g = y/x;
		return g;
	}
	
	/**
	 * Squared distance
	 * @param a
	 * @param b
	 * @return squared distance
	 */
	public static double dSquared(Rr2Point a, Rr2Point b)
	{
		Rr2Point c = sub(a, b);
		return mul(c, c);
	}
	
	/**
	 * distance
	 * @param a
	 * @param b
	 * @return distance
	 */
	public static double d(Rr2Point a, Rr2Point b)
	{
		return Math.sqrt(dSquared(a, b));
	}
	
	/**
	 * The same, withing tolerance?
	 * @param a
	 * @param b
	 * @param tol_2
	 * @return true if the squared distance between points a and b 
	 * is within tolerance tol_2, otherwise false
	 */
	public static boolean same(Rr2Point a, Rr2Point b, double tol_2)
	{
		return dSquared(a, b) < tol_2;
	}
}


