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

package org.reprap.geometry.polyhedra;

/**
 * Class for (x, y, z) points and vectors
 */
public class Point3D
{
	/**
	 * 
	 */
	private double x, y, z;
	
	/**
	 * Default to the origin
	 */
	public Point3D()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	/**
	 * Usual constructor
	 * @param a
	 * @param b
	 */
	public Point3D(double a, double b, double c)
	{
		x = a;
		y = b;
		z = c;
	}
	
	/**
	 * Copy
	 * @param r Rr2Point to copy from
	 */
	public Point3D(Point3D r)
	{
		x = r.x;
		y = r.y;
		z = r.z;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return Double.toString(x) + " " + Double.toString(y) + " " + Double.toString(z);
	}
	
	/**
	 * Coordinates
	 */
	public double x() { return x; }
	public double y() { return y; }
	public double z() { return z; }
	
	/**
	 * Arithmetic
	 * @return neg of point
	 */
	public Point3D neg()
	{
		return new Point3D(-x, -y, -z);
	}
	
	
	/**
	 * @param a
	 * @param b
	 * @return a new point based on a vector addition of points a and b
	 */
	public static Point3D add(Point3D a, Point3D b)
	{
		Point3D r = new Point3D(a);
		r.x += b.x;
		r.y += b.y;
		r.z += b.z;
		return r;
	}
	
	/**
	 * @param a
	 * @param b
	 * @return a new point based on a vector subtraction of a - b
	 */
	public static Point3D sub(Point3D a, Point3D b)
	{
		return add(a, b.neg());
	}
	
	
	/**
	 * Scale a point
	 * @param b An R2rPoint
	 * @param factor A scale factor
	 * @return The point Rr2Point scaled by a factor of factor
	 */
	public static Point3D mul(Point3D b, double factor)
	{
		return new Point3D(b.x*factor, b.y*factor, b.z*factor);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return the point Rr2Point scaled by a factor of a
	 */
	public static Point3D mul(double a, Point3D b)
	{
		return mul(b, a);
	}
	
	/**
	 * Downscale a point
	 * @param b An R2rPoint
	 * @param factor A scale factor
	 * @return The point Rr2Point divided by a factor of a
	 */
	public static Point3D div(Point3D b, double factor)
	{
		return mul(b, 1/factor);
	}
	
	/**
	 * Inner product
	 * @param a
	 * @param b
	 * @return The scalar product of the points
	 */
	public static double mul(Point3D a, Point3D b)
	{
		return a.x*b.x + a.y*b.y + a.z*b.z;
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
	public Point3D norm()
	{
		return div(this, mod());
	}
	
	
	/**
	 * Outer product
	 * @param a
	 * @param b
	 * @return oute product
	 */
	public static Point3D op(Point3D a, Point3D b)
	{
		return new Point3D(a.y*b.z - a.z*b.y, a.z*b.x - a.x*b.z,  a.x*b.y - a.y*b.x);
	}
	
	/**
	 * Squared distance
	 * @param a
	 * @param b
	 * @return squared distance
	 */
	public static double dSquared(Point3D a, Point3D b)
	{
		Point3D c = sub(a, b);
		return mul(c, c);
	}
	
	/**
	 * distance
	 * @param a
	 * @param b
	 * @return distance
	 */
	public static double d(Point3D a, Point3D b)
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
	public static boolean same(Point3D a, Point3D b, double tol_2)
	{
		return dSquared(a, b) < tol_2;
	}
}


