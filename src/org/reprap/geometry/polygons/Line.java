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
public class Line
{
	/**
	 * direction 
	 */
	private Point2D direction = null;
	
	/**
	 * origin
	 */
	private Point2D origin = null;
	
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
	public Line(Point2D a, Point2D b)
	{
		origin = new Point2D(a);
		direction = Point2D.sub(b, a);
	}
	
	/**
	 * Copy constructor
	 * @param r
	 */
	public Line(Line r)
	{
		origin = new Point2D(r.origin);
		direction = new Point2D(r.direction);
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
	public Point2D direction() { return direction; }
	public Point2D origin() { return origin; }
	
	/**
	 * The point at a given parameter value
	 * @param t
	 * @return point at parameter value t
	 */
	public Point2D point(double t)
	{
		return Point2D.add(origin, Point2D.mul(direction, t));
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
	public Line neg()
	{
		Line a = new Line(this);
		a.direction = direction.neg();
		return a;
	}
	
	/**
	 * Move the origin
	 * @param b
	 * @return translated line by value b
	 */
	public Line add(Point2D b)
	{
		Point2D a = Point2D.add(origin, b);
		Line r = new Line(a, Point2D.add(a, direction));
		return r;
	}
	
	/**
	 * @param b
	 * @return ??
	 */
	public Line sub(Point2D b)
	{
		Point2D a = Point2D.sub(origin, b);
		Line r = new Line(a, Point2D.add(a, direction));
		return r;
	}
	
	/**
	 * Offset by a distance
	 * @param d
	 * @return translated line by distance d
	 */
	public Line offset(double d)
	{
		Line result = new Line(this);
		Point2D n = Point2D.mul(-d, direction.norm().orthogonal());
		result.origin = Point2D.add(origin, n);
		return result;
	}
	
	/**
	 * The parameter value where another line crosses
	 * @param a
	 * @return parameter value
	 * @throws rr_ParallelLineException
	 */
	public double cross_t(Line a) throws ParallelException 
	{
		double det = Point2D.op(a.direction, direction);
		if (det == 0)
			throw new ParallelException("cross_t: parallel lines.");  
		Point2D d = Point2D.sub(a.origin, origin);
		return Point2D.op(a.direction, d)/det;
	}
	
	
	/**
	 * The point where another line crosses
	 * @param a
	 * @return crossing point 
	 * @throws rr_ParallelLineException
	 */
	public Point2D cross_point(Line a) throws ParallelException
	{
		return point(cross_t(a));
	}
	
	/**
	 * The nearest point on a line to another as a line parameter
	 * @param p
	 * @return nearest point on the eline
	 */
	public double nearest(Point2D p)
	{
		return Point2D.mul(direction, p) - Point2D.mul(direction, origin);
	}
	
	/**
	 * The squared distance of a point from a line
	 * @param p
	 * @return squared distance between point p and the line
	 */
	public Point2D d_2(Point2D p)
	{
		double fsq = direction.x()*direction.x();
		double gsq = direction.y()*direction.y();
		double finv = 1.0/(fsq + gsq);
		Point2D j0 = Point2D.sub(p, origin);
		double fg = direction.x()*direction.y();
		double dx = gsq*j0.x() - fg*j0.y();
		double dy = fsq*j0.y() - fg*j0.x();
		double d2 = (dx*dx + dy*dy)*finv*finv;
		double t = Point2D.mul(direction, j0)*finv;
		return new Point2D(d2, t);
	}
	
	/**
	 * The parameter value of the point on the line closest to point p
	 * @param p
	 * @return
	 */
	public double projection(Point2D p)
	{
		Point2D s = Point2D.sub(p, origin);
		return Point2D.mul(direction, s);
	}
}
