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
 
 RrHalfPlane: 2D planar half-spaces
 
 First version 20 May 2005
 This version: 9 March 2006
 
 */

package org.reprap.geometry.polygons;

import org.reprap.geometry.polyhedra.HalfSpace;

//import java.util.ArrayList;
//import java.util.List;

///**
// * Small class to hold parameter/quad pairs
// * @author Adrian
// *
// */
//class lineIntersection
//{
//	/**
//	 * The line's parameter 
//	 */
//	private double t;
//	
//	/**
//	 * Quad containing hit plane 
//	 */
//	//private RrCSGPolygon quad = null;
//	
//	/**
//	 * Flag to prevent cyclic graphs going round forever
//	 */
//	private boolean beingDestroyed = false;
//	
//	/**
//	 * Destroy me and all that I point to
//	 */
//	public void destroy() 
//	{
//		if(beingDestroyed) // Prevent infinite loop
//			return;
//		beingDestroyed = true;
////		if(quad != null)
////			quad.destroy();
////		quad = null;
//	}
//	
//	/**
//	 * Destroy just me
//	 */
//	protected void finalize() throws Throwable
//	{
////		quad = null;
////		super.finalize();
//	}
//	
////	/**
////	 * @param v
////	 * @param q
////	 */
////	public lineIntersection(double v, RrCSGPolygon q)
////	{
////		t = v;
////		quad = q;
////	}
//	
//	/**
//	 * @return
//	 */
//	public double parameter() { return t; }
//	
//	/**
//	 * @return
//	 */
////	public RrCSGPolygon quad() { return quad; }
//}


/**
 * Class to hold and manipulate linear half-planes
 */
public class HalfPlane
{
	
	/**
	 * The half-plane is normal*(x, y) + offset <= 0 
	 */
	private Point2D normal = null; 
	private double offset;
	
	/**
	 * Keep the parametric equivalent to save computing it
	 */
	private Line p = null;
	
	/**
	 * List of intersections with others
	 */
	//private List<lineIntersection> crossings = null;
	
	/**
	 * Flag to prevent cyclic graphs going round forever
	 */
	private boolean beingDestroyed = false;
	
	/**
	 * Destroy me and all that I point to
	 */
//	public void destroy() 
//	{
//		if(beingDestroyed) // Prevent infinite loop
//			return;
//		beingDestroyed = true;
//		if(normal != null)
//			normal.destroy();
//		normal = null;
//		if(p != null)
//			p.destroy();
//		p = null;
////		if(crossings != null)
////		{
////			for(int i = 0; i < size(); i++)
////			{
////				crossings.get(i).destroy();
////				crossings.set(i, null);
////			}
////		}
////		crossings = null;
//		beingDestroyed = false;
//	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		normal = null;
//		p = null;
//		//crossings = null;
//		super.finalize();
//	}
	
	/**
	 * Convert a parametric line
	 * @param l
	 */
	public HalfPlane(Line l)
	{
		p = new Line(l);
		p.norm();
		normal = new Point2D(-p.direction().y(), p.direction().x());
		offset = -Point2D.mul(l.origin(), normal());
		//crossings = new ArrayList<lineIntersection>();
	}
	
	/**
	 * Make one from two points on its edge
	 * @param a
	 * @param b
	 */
	public HalfPlane(Point2D a, Point2D b)
	{
		this(new Line(a, b));
	}   
	
	/**
	 * Deep copy
	 * @param a
	 */
	public HalfPlane(HalfPlane a)
	{
		normal = new Point2D(a.normal);
		offset = a.offset;
		p = new Line(a.p);
		//crossings = new ArrayList<lineIntersection>(); // No point in deep copy -
		                             // No pointers would match
	}
	
	/**
	 * Construct a half-plane from a 3D half-space cutting across a z plane
	 * @param hs
	 * @param z
	 */
	public HalfPlane(HalfSpace hs, double z) throws ParallelException
	{
		normal = new Point2D(hs.normal().x(), hs.normal().y());
		double m = normal.mod();
		if(m < 1.0e-10)
			throw new ParallelException("HalfPlane from HalfSpace - z parallel");
		offset = (hs.normal().z()*z + hs.offset())/m;
		normal = Point2D.div(normal, m);
		Point2D p0, p1;
		if(Math.abs(normal.x()) < 0.1)
			p0 = new Point2D(0, -offset/normal.y());
		else
			p0 = new Point2D(-offset/normal.x(), 0);
		p1 = Point2D.add(p0, normal.orthogonal());
		p = new Line(p0, p1);
		p.norm();
	}
	
	/**
	 * Get the parametric equivalent
	 * @return parametric equivalent of a line
	 */
	public Line pLine()
	{
		return p;
	}
	
//	/**
//	 * The number of crossings
//	 * @return number of crossings
//	 */
//	public int size()
//	{
//		return crossings.size();
//	}
	
//	/**
//	 * Get the i-th crossing parameter
//	 * @param i
//	 * @return i-th crossing parameter
//	 */
//	public double getParameter(int i)
//	{
//		return (crossings.get(i)).parameter();
//	}
	
//	/**
//	 * i-th point from the crossing list
//	 * @param i
//	 * @return i-th point
//	 */
//	public Rr2Point getPoint(int i)
//	{
//		return pLine().point(getParameter(i));
//	}
	
//	/**
//	 * Get the i-th quad
//	 * @param i
//	 * @return i-th quad
//	 */
//	public RrCSGPolygon getQuad(int i)
//	{
//		return (crossings.get(i)).quad();
//	}
	
//	/**
//	 * Get the i-th CSG for the plane
//	 * @param i
//	 * @return i-th CSG
//	 */
//	public RrCSG getCSG(int i)
//	{
//		RrCSGPolygon q = getQuad(i);
//		if(q.csg().complexity() == 1)
//			return q.csg();
//		else if(q.csg().complexity() == 2)
//		{
//			if(q.csg().c_1().plane() == this)
//				return q.csg().c_2();
//			if(q.csg().c_2().plane() == this)			
//				return q.csg().c_1();
//			
//			double t = getParameter(i);
//			double v = Math.abs(q.csg().c_1().plane().value(pLine().point(t)));
//			if(Math.abs(q.csg().c_2().plane().value(pLine().point(t))) < v)
//				return q.csg().c_2();
//			else
//				return q.csg().c_1();
//		}
//		
//		System.err.println("RrHalfPlane.getCSG(): complexity: " + q.csg().complexity());
//		return RrCSG.nothing();
//	}
	
//	/**
//	 * Get the i-th plane.
//	 * @param i
//	 * @return i-th plane
//	 */
//	public RrHalfPlane getPlane(int i)
//	{
//		return getCSG(i).plane();
//	}
	
//	/**
//	 * Take the sorted list of parameter values and a shape, and
//	 * make sure they alternate solid/void/solid etc.  Insert
//	 * duplicate parameter values if need be to ensure this,
//	 * or - if two are very close - delete one. 
//	 * @param p
//	 */
//	public void solidSet(RrCSGPolygon p)
//	{
//		double v;
//		boolean odd = true;
//		int i = 0;
//		while(i < size() - 1)
//		{
//			double pi = getParameter(i);
//			double pi1 = getParameter(i+1);
//			v = 0.5*(pi + pi1);
//			boolean tiny = Math.abs(pi1 - pi) < 2*Math.sqrt(p.box().dSquared()); // Is this too coarse a limit?
//			v = p.value(pLine().point(v));
//			if(odd)
//			{
//				if(v > 0)
//				{
//					if(tiny)
//						crossings.remove(i);
//					else
//						crossings.add(i, crossings.get(i));
//				}
//			} else
//			{
//				if(v <= 0)
//				{
//					if(tiny)
//						crossings.remove(i);
//					else
//						crossings.add(i, crossings.get(i));
//				}	
//			}
//			odd = !odd;
//			i++;
//		}
//		if (size()%2 != 0)    // Nasty hack that seems to work...
//		{
//			System.err.println("RrHalfPlane.solidSet(): odd number of crossings: " +
//					size());
//			crossings.remove(size() - 1);
//		}
//	}

	
//	/**
//	 * Add ??? if it contains ??? with a parameter within bounds.
//	 * @param p
//	 * @param q
//	 * @param range
//	 * @param me
//	 * @return true if ??? may be added, otherwise false
//	 */
//	private boolean maybeAdd(RrHalfPlane p, RrCSGPolygon q, RrInterval range, boolean me)
//	{	
//		// Ensure no duplicates
//		
//		for(int i = 0; i < size(); i++)
//		{
//			if(getPlane(i) == p)
//				return false;     // Because we've already got it
//		}
//		
//		RrInterval newRange = q.box().wipe(pLine(), range);
//		if(!newRange.empty())
//			try
//		{
//				double v = p.cross_t(pLine());
//				if(v >= newRange.low() && v < newRange.high())
//				{
//					if(me)
//					{
//						crossings.add(new lineIntersection(v, q));
//						return true;						
//					} else
//					{
//						Rr2Point x = pLine().point(v);
//						double r = Math.sqrt(q.resolution2());
//						double pot = q.csg().value(x);
//						if(pot > -r && pot < r)
//						{
//							crossings.add(new lineIntersection(v, q));
//							return true;
//						}
//					}
//				}
//		} catch (RrParallelLineException ple)
//		{}
//		return false;
//	}
	
//	/**
//	 * Add quad q if it contains a half-plane with an 
//	 * intersection with a parameter within bounds.
//	 * @param q
//	 * @param range
//	 * @return true if quad q may be added, otherwise false
//	 */
//	public boolean maybeAdd(RrCSGPolygon q, RrInterval range)
//	{		
//		switch(q.csg().operator())
//		{
//		case NULL:
//		case UNIVERSE:
//			return false;
//		
//		case LEAF:
//			return maybeAdd(q.csg().plane(), q, range, false);
//			
//		case INTERSECTION:
//		case UNION:	
//			if(q.csg().complexity() != 2)
//			{
//				System.err.println("RrHalfPlane.maybeAdd(): too complex: " + q.csg().complexity());
//				return false;
//			}
//			RrHalfPlane p1 = q.csg().c_1().plane();
//			RrHalfPlane p2 = q.csg().c_2().plane();
//			if(p1 == this)
//				return maybeAdd(p2, q, range, true);
//			if(p2 == this)
//				return maybeAdd(p1, q, range, true);
//			
//			boolean b = maybeAdd(p1, q, range, false); 
//			b = b | maybeAdd(p2, q, range, false);
//			return b;
//			
//		default:
//			System.err.println("RrHalfPlane.maybeAdd(): invalid CSG operator!");
//		}
//		
//		return false;
//	}
	
//	/**
//	 * Add a crossing
//	 * @param qc
//	 */
//	public static boolean cross(RrCSGPolygon qc)
//	{		
//		if(qc.corner())
//		{
//			RrInterval range = RrInterval.bigInterval();
//			boolean b = qc.csg().c_1().plane().maybeAdd(qc, range);
//			range = RrInterval.bigInterval();
//			b = b & qc.csg().c_2().plane().maybeAdd(qc, range);
//			return (b);
//		}
//		System.err.println("RrHalfPlane.cross(): called for non-corner!");
//		return false;
//	}
	
//	/**
//	 * Find a crossing
//	 * @param q
//	 * @return the index of the quad
//	 */
//	public int find(RrCSGPolygon q)
//	{	
//		for(int i = 0; i < size(); i++)
//		{
//			if(getQuad(i) == q)
//				return i;
//		}
//		System.err.println("RrHalfPlane.find(): quad not found!");
//		return -1;
//	}
	
//	/**
//	 * Find the index of a crossing plane
//	 * @param h
//	 * @return index of the plane
//	 */
//	public int find(RrHalfPlane h)
//	{	
//		for(int i = 0; i < size(); i++)
//		{
//			if(getPlane(i) == h)
//				return i;
//		}
//		System.err.println("RrHalfPlane.find(): plane not found!");
//		return -1;
//	}
	
//	/**
//	 * Remove all crossings
//	 */
//	public void removeCrossings()
//	{
//		crossings = new ArrayList<lineIntersection>();
//	}
//		
//	/**
//	 * Remove a crossing from the list
//	 * @param i identifier of the crossing to be removed from the list 
//	 */
//	public void remove(int i)
//	{
//		crossings.remove(i);
//	}
	
//	/**
//	 * Sort on ascending parameter value.
//	 * @param up use an ascending sort when true, descending when false
//	 */
//	public void sort(boolean up, RrCSGPolygon q)
//	{
//		if(up)
//		{
//			java.util.Collections.sort(crossings, 
//					new java.util.Comparator<lineIntersection>() 
//					{
//				public int compare(lineIntersection a, lineIntersection b)
//				{
//					if(((lineIntersection)a).parameter() < 
//							((lineIntersection)b).parameter())
//						return -1;
//					else if (((lineIntersection)a).parameter() > 
//					((lineIntersection)b).parameter())
//						return 1;
//					return 0;
//				}
//					}
//			);
//		} else
//		{
//			java.util.Collections.sort(crossings, 
//					new java.util.Comparator<lineIntersection>() 
//					{
//				public int compare(lineIntersection a, lineIntersection b)
//				{
//					if(((lineIntersection)a).parameter() > 
//							((lineIntersection)b).parameter())
//						return -1;
//					else if (((lineIntersection)a).parameter() < 
//					((lineIntersection)b).parameter())
//						return 1;
//					return 0;
//				}
//					}
//			);
//		}		
//		if(size()%2 != 0)
//		{
//			//System.err.println("RrHalfPlane.sort(): odd number of crossings: " +
//					//size());
//			solidSet(q);
//		}
//	}
	
	
	
	/**
	 * Return the plane as a string
	 * @return string representation
	 */
	public String toString()
	{
		return "|" + normal.toString() + ", " + Double.toString(offset) + "|";
	} 
	
	
	/**
	 * Get the components
	 * @return components?
	 */
	public Point2D normal() { return normal; }
	public double offset() { return offset; }
	
	/**
	 * Is another line the same within a tolerance?
	 * @param a
	 * @param b
	 * @param tolerance
	 * @return 0 if the distance between halfplane a and b is less then the tolerance, -1 if one
	 * is the complement of the other within the tolerance, otherwise 1
	 */
	public static int same(HalfPlane a, HalfPlane b, double tolerance)
	{
		if(a == b)
			return 0;
		
		int result = 0;
		if(Math.abs(a.normal.x() - b.normal.x()) > tolerance)
		{
			if(Math.abs(a.normal.x() + b.normal.x()) > tolerance)
				return 1;
			result = -1;
		}
		if(Math.abs(a.normal.y() - b.normal.y()) > tolerance)
		{
			if(Math.abs(a.normal.y() + b.normal.y()) > tolerance || result != -1)
				return 1;
		}
		double rms = Math.sqrt((a.offset*a.offset + b.offset*b.offset)*0.5);
		if(Math.abs(a.offset - b.offset) > tolerance*rms)
		{
			if(Math.abs(a.offset + b.offset) > tolerance*rms || result != -1)
				return 1;
		}
		
		return result;
	}
	
//	public static boolean same(RrHalfPlane a, RrHalfPlane b, double tolerance)
//	{
//		if(Math.abs(a.normal.x() - b.normal.x()) > tolerance)
//			return false;
//		if(Math.abs(a.normal.y() - b.normal.y()) > tolerance)
//			return false;
//		double rms = Math.sqrt((a.offset*a.offset + b.offset*b.offset)*0.5);
//		if(Math.abs(a.offset - b.offset) > tolerance*rms)
//			return false;
//		
//		return true;
//	}
	
	/**
	 * Change the sense
	 * @return complent of half plane
	 */
	public HalfPlane complement()
	{
		HalfPlane r = new HalfPlane(this);
		r.normal = r.normal.neg();
		r.offset = -r.offset;
		r.p = r.p.neg();
		return r;
	}
	
	/**
	 * Move
	 * @param d
	 * @return offset halfplane
	 */
	public HalfPlane offset(double d)
	{
		HalfPlane r = new HalfPlane(this);
		r.offset = r.offset - d;
		r.p = p.offset(d);
		return r;
	}
	
	
	/**
	 * Find the potential value of a point
	 * @param p
	 * @return potential value of point p
	 */
	public double value(Point2D p)
	{
		return offset + Point2D.mul(normal, p);
	}
	
	
	/**
	 * Find the potential interval of a box
	 * @param b
	 * @return potential interval of box b
	 */
	public Interval value(Rectangle b)
	{
		return Interval.add(Interval.add((Interval.mul(b.x(), normal.x())), 
				(Interval.mul(b.y(), normal.y()))), offset);
	}
	
	/**
	 * The point where another line crosses
	 * @param a
	 * @return cross point
	 * @throws ParallelException
	 */
	public Point2D cross_point(HalfPlane a) throws ParallelException
	{
		double det = Point2D.op(normal, a.normal);
		if(det == 0)
			throw new ParallelException("cross_point: parallel lines.");
		det = 1/det;
		double x = normal.y()*a.offset - a.normal.y()*offset;
		double y = a.normal.x()*offset - normal.x()*a.offset;
		return new Point2D(x*det, y*det);
	}
	
	/**
	 * Parameter value where a line crosses
	 * @param a
	 * @return parameter value
	 * @throws ParallelException
	 */
	public double cross_t(Line a) throws ParallelException 
	{
		double det = Point2D.mul(a.direction(), normal);
		if (det == 0)
			throw new ParallelException("cross_t: parallel lines.");  
		return -value(a.origin())/det;
	}
	
	/**
	 * Point where a parametric line crosses
	 * @param a
	 * @return cross point
	 * @throws ParallelException
	 */
	public Point2D cross_point(Line a) throws ParallelException
	{
		return a.point(cross_t(a));
	}
	
	/**
	 * Take a range of parameter values and a line, and find
	 * the intersection of that range with the part of the line
	 * (if any) on the solid side of the half-plane.
	 * @param a
	 * @param range
	 * @return intersection interval
	 */
	public Interval wipe(Line a, Interval range)
	{
		if(range.empty()) return range;
		
		// Which way is the line pointing relative to our normal?
		
		boolean wipe_down = (Point2D.mul(a.direction(), normal) >= 0);
		
		double t;
		
		try
		{
			t = cross_t(a);
			if (t >= range.high())
			{
				if(wipe_down)
					return range;
				else
					return new Interval();
			} else if (t <= range.low())
			{
				if(wipe_down)
					return new Interval();
				else
					return range;                
			} else
			{
				if(wipe_down)
					return new Interval(range.low(), t);
				else
					return new Interval(t, range.high());                 
			}
		} catch (ParallelException ple)
		{
			t = value(a.origin());
			if(t <= 0)
				return range;
			else
				return new Interval();  
		}
	}
}
