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
 
 RrLine: 2D parametric line
 
 First version 20 May 2005
 This version: 1 May 2006 (Now in CVS - no more comments here)
 
 */

package org.reprap.geometry.polygons;


/**
 * Class to hold and manipulate parametric lines
 */
public class RrLine
{
	/**
	 * direction 
	 */
	private Rr2Point direction = null;
	
	/**
	 * origin
	 */
	private Rr2Point origin = null;
	
	/**
	 * Flag to prevent cyclic graphs going round forever
	 */
	private boolean beingDestroyed = false;
	
	/**
	 * Destroy me and all that I point to
	 */
	public void destroy() 
	{
		if(beingDestroyed) // Prevent infinite loop
			return;
		beingDestroyed = true;
		if(direction != null)
			direction.destroy();
		direction = null;
		if(origin != null)
			origin.destroy();
		origin = null;
		beingDestroyed = false;
	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		direction = null;
//		origin = null;
//		super.finalize();
//	}
	
	/**
	 * Line between two points
	 * @param a
	 * @param b
	 */
	public RrLine(Rr2Point a, Rr2Point b)
	{
		origin = new Rr2Point(a);
		direction = Rr2Point.sub(b, a);
	}
	
	/**
	 * Copy constructor
	 * @param r
	 */
	public RrLine(RrLine r)
	{
		origin = new Rr2Point(r.origin);
		direction = new Rr2Point(r.direction);
	}
	
	/**
	 * Make from an implicit half-plane
	 */
//	public RrLine(RrHalfPlane p)
//	{
//		origin = new Rr2Point(p.pLine().origin);
//		direction = new Rr2Point(p.pLine().direction);		
////		origin = new Rr2Point(-p.normal().x()*p.offset(), 
////				-p.normal().y()*p.offset());
////		direction = new Rr2Point(p.normal().y(), -p.normal().x());
//	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "<" + origin.toString() + ", " + direction.toString() + ">";
	}
	
	/**
	 * @return Return the contents
	 */
	public Rr2Point direction() { return direction; }
	public Rr2Point origin() { return origin; }
	
	/**
	 * The point at a given parameter value
	 * @param t
	 * @return point at parameter value t
	 */
	public Rr2Point point(double t)
	{
		return Rr2Point.add(origin, Rr2Point.mul(direction, t));
	}
	
	
	/**
	 * Normalise the direction vector
	 */
	public void norm()
	{
		direction = direction.norm();
	}
	
	
	/**
	 * Arithmetic
	 * @return inverted direction of this line
	 */
	public RrLine neg()
	{
		RrLine a = new RrLine(this);
		a.direction = direction.neg();
		return a;
	}
	
	/**
	 * Move the origin
	 * @param b
	 * @return translated line by value b
	 */
	public RrLine add(Rr2Point b)
	{
		Rr2Point a = Rr2Point.add(origin, b);
		RrLine r = new RrLine(a, Rr2Point.add(a, direction));
		return r;
	}
	
	/**
	 * @param b
	 * @return ??
	 */
	public RrLine sub(Rr2Point b)
	{
		Rr2Point a = Rr2Point.sub(origin, b);
		RrLine r = new RrLine(a, Rr2Point.add(a, direction));
		return r;
	}
	
	/**
	 * Offset by a distance
	 * @param d
	 * @return translated line by distance d
	 */
	public RrLine offset(double d)
	{
		RrLine result = new RrLine(this);
		Rr2Point n = Rr2Point.mul(-d, direction.norm().orthogonal());
		result.origin = Rr2Point.add(origin, n);
		return result;
	}
	
	/**
	 * The parameter value where another line crosses
	 * @param a
	 * @return parameter value
	 * @throws rr_ParallelLineException
	 */
	public double cross_t(RrLine a) throws RrParallelLineException 
	{
		double det = Rr2Point.op(a.direction, direction);
		if (det == 0)
			throw new RrParallelLineException("cross_t: parallel lines.");  
		Rr2Point d = Rr2Point.sub(a.origin, origin);
		return Rr2Point.op(a.direction, d)/det;
	}
	
	
	/**
	 * The point where another line crosses
	 * @param a
	 * @return crossing point 
	 * @throws rr_ParallelLineException
	 */
	public Rr2Point cross_point(RrLine a) throws RrParallelLineException
	{
		return point(cross_t(a));
	}
	
	/**
	 * The nearest point on a line to another as a line parameter
	 * @param p
	 * @return nearest point on the eline
	 */
	public double nearest(Rr2Point p)
	{
		return Rr2Point.mul(direction, p) - Rr2Point.mul(direction, origin);
	}
	
	/**
	 * The squared distance of a point from a line
	 * @param p
	 * @return squared distance between point p and the line
	 */
	public Rr2Point d_2(Rr2Point p)
	{
		double fsq = direction.x()*direction.x();
		double gsq = direction.y()*direction.y();
		double finv = 1.0/(fsq + gsq);
		Rr2Point j0 = Rr2Point.sub(p, origin);
		double fg = direction.x()*direction.y();
		double dx = gsq*j0.x() - fg*j0.y();
		double dy = fsq*j0.y() - fg*j0.x();
		double d2 = (dx*dx + dy*dy)*finv*finv;
		double t = Rr2Point.mul(direction, j0)*finv;
		return new Rr2Point(d2, t);
	}
	
	/**
	 * The parameter value of the point on the line closest to point p
	 * @param p
	 * @return
	 */
	public double projection(Rr2Point p)
	{
		Rr2Point s = Rr2Point.sub(p, origin);
		return Rr2Point.mul(direction, s);
	}
}
