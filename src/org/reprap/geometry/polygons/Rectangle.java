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
 
 RrRectangle: 2D rectangles
 
 First version 20 May 2005
 This version: 1 May 2006 (Now in CVS - no more comments here)
 
 */

package org.reprap.geometry.polygons;

import org.reprap.utilities.Debug;

/**
 * A 2D box is an X and a Y interval
 */
public class Rectangle
{
	/**
	 * Compass directions
	 */
	public static final byte rr_N = 0x01;
	public static final byte rr_E = 0x02;
	public static final byte rr_S = 0x04;
	public static final byte rr_W = 0x08;
	
	/**
	 * X interval
	 */
	private Interval x = null;
	
	/**
	 * Y interval
	 */
	private Interval y = null;
	
	/**
	 * Anyone home?
	 */
	private boolean empty;
	
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
		if(x != null)
			x.destroy();
		x = null;
		if(y != null)
			y.destroy();		
		y = null;
		beingDestroyed = false;
	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		x = null;
//		y = null;
//		super.finalize();
//	}
	
	/**
	 * Default is empty
	 */ 
	public Rectangle()
	{
		empty = true;
	}
	
//	/**
//	 * For when we need one that has just something in
//	 * @param a
//	 */
//	public RrRectangle(double a)
//	{
//		x = new RrInterval(0, a);
//		y = new RrInterval(0, a);
//		empty = false;
//	}
	
	/**
	 * Copy constructor
	 */
	public Rectangle(Rectangle b)
	{
		x = new Interval(b.x);
		y = new Interval(b.y);
		empty = b.empty;
	}
	
	/**
	 * Make from any diagonal corners
	 * @param sw
	 * @param ne
	 */
	public Rectangle(Point2D a, Point2D b)
	{
		x = new Interval(Math.min(a.x(), b.x()), Math.max(a.x(), b.x()));
		y = new Interval(Math.min(a.y(), b.y()), Math.max(a.y(), b.y()));
		empty = x.empty() || y.empty();
	}
	
	/**
	 * Make from X and Y intervals
	 * @param sw
	 * @param ne
	 */
	public Rectangle(Interval xi, Interval yi)
	{
		x = new Interval(xi);
		y = new Interval(yi);
		empty = x.empty() || y.empty();
	}
	
	
	/**
	 * @return Return the x interval
	 */
	public Interval x() { return x; }
	
	
	/**
	 * @return Return the y interval
	 * 
	 */
	public Interval y() { return y; }
	
	/**
	 * 
	 * @return
	 */
	public boolean empty() { return empty; }
	
	
	/**
	 * Expand the box to incorporate another box or a point
	 * @param a
	 */
	public void expand(Rectangle a)
	{
		if(a.empty)
			return;
		if(empty)
		{
			empty = false;
			x = new Interval(a.x);
			y = new Interval(a.y);
		} else
		{
			x.expand(a.x);
			y.expand(a.y);
		}
	}
	
	/**
	 * Shrink or grow by a given distance
	 * @param dist
	 * @return
	 */
	public Rectangle offset(double dist)
	{
		return new Rectangle(new Interval(x.low() - dist, x.high() + dist), 
				new Interval(y.low() - dist, y.high() + dist));
	}
	
	/**
	 * Move somewhere else
	 * @param p
	 * @return
	 */
	public Rectangle translate(Point2D p)
	{
		return new Rectangle(new Interval(x.low() + p.x(), x.high() + p.x()), 
				new Interval(y.low() + p.y(), y.high() + p.y()));
	}
	
	/**
	 * @param a
	 */
	public void expand(Point2D a)
	{
		if(empty)
		{
			empty = false;
			x = new Interval(a.x(), a.x());
			y = new Interval(a.y(), a.y());
		} else
		{
			x.expand(a.x());
			y.expand(a.y());
		}
	}
	
	/**
	 * Corner points and center
	 * @return NE cornerpoint
	 */
	public Point2D ne()
	{
		return new Point2D(x.high(), y.high());
	}
	
	/**
	 * @return SW cornerpoint
	 */
	public Point2D sw()
	{
		return new Point2D(x.low(), y.low());
	}
	
	/**
	 * @return SE cornerpoint
	 */
	public Point2D se()
	{
		return new Point2D(x.high(), y.low());
	}
	
	/**
	 * @return NW cornerpoint
	 */
	public Point2D nw()
	{
		return new Point2D(x.low(), y.high());
	}   
	
	/**
	 * @return Centre point
	 */
	public Point2D centre()
	{
		return new Point2D(x.cen(), y.cen());
	}	
	/**
	 * Scale the box by a factor about its center
	 * @param f
	 * @return scaled box object
	 */
	public Rectangle scale(double f)
	{
		Rectangle r = new Rectangle();
		if(empty)
			return r;
		f = 0.5*f;
		Point2D p = new Point2D(x.length()*f, y.length()*f);
		Point2D c = centre();
		r.expand(Point2D.add(c, p));
		r.expand(Point2D.sub(c, p));
		return r;
	}
	
	/**
	 * Convert to a string
	 */
	public String toString()
	{
		if(empty)
			return "<empty>";
		return "<BOX x:" + x.toString() + ", y:" + y.toString() + ">";
	}
	
	/**
	 * Squared diagonal
	 * @return squared diagonal of the box
	 */
	public double dSquared()
	{
		if(empty)
			return 0;
		return Point2D.dSquared(sw(), ne());
	}

	/**
	 * Squared distance to a point
	 * @return minimal squared distance to a point from one of the corners of the box
	 */
	public double dSquared(Point2D p)
	{
		if(empty)
			return Double.POSITIVE_INFINITY;
		byte b = pointRelative(p);
		double d1 = Double.POSITIVE_INFINITY, d2 = Double.POSITIVE_INFINITY;
		switch(b)
		{
		case 0:
			return 0;
			
		case 1:
			d1 = Point2D.dSquared(p, nw());
			d2 = Point2D.dSquared(p, ne());
			break;
			
		case 2:
			d1 = Point2D.dSquared(p, ne());
			d2 = Point2D.dSquared(p, se());
			break;
			
		case 3:
			return Point2D.dSquared(p, ne());
			
		case 4:
			d1 = Point2D.dSquared(p, sw());
			d2 = Point2D.dSquared(p, se());
			break;
			
		case 6:
			return Point2D.dSquared(p, se());
			
		case 8:
			d1 = Point2D.dSquared(p, sw());
			d2 = Point2D.dSquared(p, nw());
			break;
			
		case 9:
			return Point2D.dSquared(p, nw());
			
		case 12:
			return Point2D.dSquared(p, sw());
			
		default:
			Debug.e("RrRectangle.dSquared(): dud value from point_relative()!");	
		}
		
		return Math.min(d1, d2);

	}
	

	/**
	 * Take a range of parameter values and a line, and find
     * the intersection of that range with the part of the line
     * (if any) in the box.
	 * @param a
	 * @param oldRange
	 * @return intersection interval
	 */
	public Interval wipe(Line a, Interval oldRange)
	{
		if(oldRange.empty()) return oldRange;
		
		Interval range = new Interval(oldRange);
		
		HalfPlane hp = new HalfPlane(sw(), nw());
		range = hp.wipe(a, range);
		if(range.empty()) return range;
		
		hp = new HalfPlane(nw(), ne());
		range = hp.wipe(a, range);
		if(range.empty()) return range;
		
		hp = new HalfPlane(ne(), se());
		range = hp.wipe(a, range);
		if(range.empty()) return range;
		
		hp = new HalfPlane(se(), sw());
		range = hp.wipe(a, range);
		return range;
	}
	
	/**
	 * Where is a point relative to a box?
	 * @param p 
	 * @return relative position of a point p (E, W, N or S)
	 */
	public byte pointRelative(Point2D p)
	{
		byte result = 0;
		if(p.x() >= x.high())
			result |= rr_E;
		if(p.x() < x.low())
			result |= rr_W;
		if(p.y() >= y.high())
			result |= rr_N;
		if(p.y() < y.low())
			result |= rr_S;        
		return result;
	}
	
	/**
	 * Intersection
	 * @param a
	 * @param b
	 * @return
	 */
	public static Rectangle intersection(Rectangle a, Rectangle b)
	{
		if(a.empty)
			return a;
		if(b.empty)
			return b;
		return new Rectangle(Interval.intersection(a.x, b.x), Interval.intersection(a.y, b.y));	
	}
	
	/**
	 * Union
	 * @param a
	 * @param b
	 * @return
	 */
	public static Rectangle union(Rectangle a, Rectangle b)
	{
		if(a.empty)
			return b;
		if(b.empty)
			return a;
		return new Rectangle(Interval.union(a.x, b.x), Interval.union(a.y, b.y));	
	}
}

