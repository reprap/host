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
 
 
 RrPolygon: 2D polygons
 
 First version 20 May 2005
 This version: 1 May 2006 (Now in CVS - no more comments here)
 
 A polygon is an auto-extending list of Rr2Points.  Its end is 
 sometimes considered to join back to its beginning, depending
 on context.
 
 It also keeps its enclosing box.  
 
 Each point is stored with a flag value.  This can be used to flag the
 point as visited, or to indicate if the subsequent line segment is to
 be plotted etc.
 
 java.awt.Polygon is no use for this because it has integer coordinates.
 
 */

package org.reprap.geometry.polygons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.reprap.Attributes;
import org.reprap.Preferences;
import org.reprap.Extruder;
import org.reprap.geometry.LayerRules;
import org.reprap.machines.VelocityProfile;
import org.reprap.utilities.Debug;

/**
 * The main boundary-representation polygon class
 */
public class RrPolygon
{
	/**
	 * End joined to beginning?
	 */
	private boolean closed = false;
	
	/**
	 * Used to choose the starting point for a randomized-start copy of a polygon
	 */
	private static Random rangen = new Random(918273);
	
	/**
	 * The (X, Y) points round the polygon as Rr2Points
	 */
	private List<Rr2Point> points = null;
	
	/**
	 * The speed of the machine at each corner
	 */
	private List<Double> speeds = null;
	
	/**
	 * The atributes of the STL object that this polygon represents
	 */
	private Attributes att = null;
	
	/**
	 * The minimum enclosing X-Y box round the polygon
	 */
	private RrRectangle box = null;
	
	/**
	 * Flag to prevent cyclic graphs going round forever
	 */
	private boolean beingDestroyed = false;
	
	/**
	 * The index of the last point to draw to, if there are more that should just be moved over
	 */
	private int extrudeEnd;
	
	/**
	 * The index of the last point at which the valve (if any) is open.
	 */
	private int valveEnd;
	
	/**
	 * Destroy me and all that I point to
	 */
	public void destroy() 
	{
		if(beingDestroyed) // Prevent infinite loop
			return;
		beingDestroyed = true;
		
		if(speeds != null)
		{
			for(int i = 0; i < size(); i++)
				speeds.set(i, null);
		}
		speeds = null;
		
		if(points != null)
		{
			for(int i = 0; i < size(); i++)
			{
				points.get(i).destroy();
				points.set(i, null);
			}
		}
		points = null;
		
		if(box != null)
			box.destroy();
		box = null;
		
		// Don't destroy the attribute - that may still be needed
		
		//if(att != null)
		//	att.destroy();
		att = null;
		beingDestroyed = false;
	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		points = null;
//		speeds = null;
//		att = null;
//		box = null;
//		super.finalize();
//	}
	
	
	/**
	 * Make an empty polygon
	 */
	public RrPolygon(Attributes a, boolean c)
	{
		if(a == null)
			Debug.e("RrPolygon(): null attributes!");
		points = new ArrayList<Rr2Point>();
		speeds = null;
		att = a;
		box = new RrRectangle();
		closed = c;
		extrudeEnd = -1;
		valveEnd = -1;
	}
	
	/**
	 * Get the data
	 * @param i
	 * @return i-th point object of polygon
	 */
	public Rr2Point point(int i)
	{
		//return new Rr2Point(points.get(i));
		return points.get(i);
	}
	
	/**
	 * Get the speed
	 * @param i
	 * @return i-th point object of polygon
	 */
	public double speed(int i)
	{
		if(speeds == null)
		{
			Debug.e("Rr2Point.speed(int i): speeds null!");
			return 0;
		}
		return speeds.get(i).doubleValue();
	}

	
	/**
	 * As a string
	 * @return string representation of polygon
	 */
	public String toString()
	{
		String result = " Polygon -  vertices: ";
		result += size() + ", enclosing box: ";
		result += box.toString();
		result += "\n";
		for(int i = 0; i < size(); i++)
		{
			result += point(i).toString();
			if(speeds != null)
				result += "(" + speed(i) + "); ";
			else
				result += "; ";
		}
		
		return result;
	}
	
	/**
	 * Do we loop back on ourself?
	 * @return
	 */
	public boolean isClosed()
	{
		return closed;
	}
	
	/**
	 * What's the last point to plot to?
	 * @return
	 */
	public int extrudeEnd()
	{
		if(extrudeEnd < 0)
			return size() - 1;
		else
			return extrudeEnd;
	}
	
	/**
	 * What's the last point at which the valve should be open to?
	 * @return
	 */
	public int valveEnd()
	{
		if(valveEnd < 0)
			return size() - 1;
		else
			return valveEnd;
	}
		
	/**
	 * Length
	 * @return number of points in polygon
	 */
	public int size()
	{
		return points.size();
	}
	
	/**
	 * Deep copy - NB: attributes _not_ deep copied
	 * @param p
	 */
	public RrPolygon(RrPolygon p)
	{
		this(p.att, p.closed);
		for(int i = 0; i < p.size(); i++)
			add(new Rr2Point(p.point(i)));
		if(p.speeds != null)
		{
			speeds = new ArrayList<Double>();
			for(int i = 0; i < p.size(); i++)
				speeds.add(new Double(p.speed(i)));
		}
		closed = p.closed;
		extrudeEnd = p.extrudeEnd;
		valveEnd  = p.valveEnd;
	}
	
	/**
	 * Add a new point to the polygon
	 * @param p
	 * @param f
	 */
	public void add(Rr2Point p)
	{
		if(speeds != null)
			Debug.e("Rr2Point.add(): adding a point to a polygon with its speeds set.");
		points.add(new Rr2Point(p));
		box.expand(p);
	}
	
	/**
	 * Insert a new point into the polygon
	 * @param i
	 * @param p
	 */
	public void add(int i, Rr2Point p)
	{
		if(speeds != null)
			Debug.e("Rr2Point.add(): adding a point to a polygon with its speeds set.");
		points.add(i, new Rr2Point(p));
		box.expand(p);
		if(i <= extrudeEnd)
			extrudeEnd++;
		if(i <= valveEnd)
			valveEnd++;
	}
	
	/**
	 * Set a point to be p
	 * @param i
	 * @param p
	 */
	public void set(int i, Rr2Point p)
	{
		if(speeds != null)
			Debug.e("Rr2Point.set(): adding a point to a polygon with its speeds set.");
		points.set(i, new Rr2Point(p));
		box.expand(p);  // Note if the old point was on the convex hull, and the new one is within, box will be too big after this
	}

	/**
	 * Insert a new point and speed into the polygon
	 * @param i
	 * @param p
	 * @param s
	 */
	public void add(int i, Rr2Point p, double s)
	{
		if(speeds == null)
			Debug.e("Rr2Point.add(): adding a point and a speed to a polygon without its speeds set.");
		points.add(i, new Rr2Point(p));
		speeds.add(i, s);
		box.expand(p);
		if(i <= extrudeEnd)
			extrudeEnd++;
		if(i <= valveEnd)
			valveEnd++;
	}		
	
	/**
	 * Set a new point and speed
	 * @param i
	 * @param p
	 * @param s
	 */
	public void set(int i, Rr2Point p, double s)
	{
		if(speeds == null)
			Debug.e("Rr2Point.set(): adding a point and a speed to a polygon without its speeds set.");
		points.set(i, new Rr2Point(p));
		speeds.set(i, s);
		box.expand(p); // Note if the old point was on the convex hull, and the new one is within, box will be too big after this
	}
	
	/**
	 * Add a speed to the polygon
	 * @param p
	 * @param f
	 */
	public void setSpeed(int i, double s)
	{
		// Lazy initialization
		if(speeds == null)
		{
			speeds = new ArrayList<Double>();
			for(int j = 0; j < size(); j++)
				speeds.add(new Double(0));
		}
		speeds.set(i, new Double(s));
	}
	
	/**
	 * Eet the last point to plot to
	 * @param d
	 */
	public void setExtrudeEnd(int d)
	{
		extrudeEnd = d;
	}
	
	/**
	 * Eet the last point to valve-open to
	 * @param d
	 */
	public void setValveEnd(int d)
	{
		valveEnd = d;
	}
	
	/**
	 * @return the attributes
	 */
	public Attributes getAttributes() { return att; }
	
	/**
	 * @return the current surrounding box
	 */
	public RrRectangle getBox() { return box; }
	
	/**
	 * Sum of the edge lengths
	 * @return
	 */
	public double getLength()
	{
		double len = 0;
		for(int i = 1; i < size(); i++)
			len = len + Rr2Point.d(point(i), point(i-1));
		if(closed)
			len = len + Rr2Point.d(point(0), point(size()-1));
		return len;
	}
	
	/**
	 * Put a new polygon on the end
	 * (N.B. Attributes of the new polygon are ignored)
	 * @param p
	 */
	public void add(RrPolygon p)
	{
		if(p.size() == 0)
			return;
		if(extrudeEnd >= 0 || valveEnd >= 0)
			Debug.e("Rr2Point.add(): adding a polygon to another polygon with its extrude or valve ending set.");
		for(int i = 0; i < p.size(); i++)
		{
			if(i == p.extrudeEnd)
				extrudeEnd = size();
			if(i == p.valveEnd)
				valveEnd = size();
			points.add(new Rr2Point(p.point(i)));
		}
		box.expand(p.box);
		if(speeds == null)
		{
			if(p.speeds != null)
				Debug.e("Rr2Point.add(): adding a polygon to another polygon but discarding it's speeds.");
			return;
		}
		if(p.speeds == null)
		{
			Debug.e("Rr2Point.add(): adding a polygon to another polygon, but it has no needed speeds.");
			return;
		}
		for(int i = 0; i < p.size(); i++)
		{
			speeds.add(new Double(p.speed(i)));
		}
	}
	
	/**
	 * Put a new polygon in the middle (at vertex k, which will be at
	 * the end of the inserted polygon afterwards).
	 * (N.B. Attributes of the new polygon are ignored)
	 * @param k
	 * @param p
	 */
	public void add(int k, RrPolygon p)
	{
		if(p.size() == 0)
			return;
		if(speeds != p.speeds)
		{
			Debug.e("Rr2Point.add(): attempt to add a polygon to another polygon when one has speeds and the other doesn't.");
			return;
		}
		if(k <= extrudeEnd || k <= valveEnd)
			Debug.e("Rr2Point.add(): adding a polygon to another polygon with its extrude or valve ending set.");
		int de = -1;
		int dv = -1;
		if (extrudeEnd >= 0)
			de = extrudeEnd + p.size();
		if (valveEnd >= 0)
			dv = valveEnd + p.size();		
		for(int i = 0; i < p.size(); i++)
		{
			if(i == p.extrudeEnd)
				extrudeEnd = size();
			if(i == p.valveEnd)
				valveEnd = size();
			if(speeds != null)
				add(k, new Rr2Point(p.point(i)), p.speed(i));
			else
				points.add(k, new Rr2Point(p.point(i)));
			k++;
		}
		extrudeEnd = Math.max(extrudeEnd, de);
		valveEnd = Math.max(valveEnd, dv);
		box.expand(p.box);
	}
	
	/**
	 * Remove a point.
	 * N.B. This does not amend the enclosing box
	 * @param i
	 */
	public void remove(int i)
	{
		points.remove(i);
		if(speeds != null)
			speeds.remove(i);
	}
	
	/**
	 * Recompute the box (sometimes useful if points have been deleted) 
	 */
	public void re_box()
	{
		box = new RrRectangle();
		int leng = size();
		for(int i = 0; i < leng; i++)
		{
			box.expand(points.get(i)); 
		}
	}
	
	
	/**
	 * Output the polygon in SVG XML format
	 * This ignores any speeds
	 * @param opf
	 */
	public String svg()
	{
		String result = "<polygon points=\"";
		int leng = size();
		for(int i = 0; i < leng; i++)
			result += Double.toString((point(i)).x()) + "," 
					+ Double.toString((point(i)).y());
		result +="\" />";
		return result;
	}
		
	/**
	 * Negate (i.e. reverse cyclic order)
	 * @return reversed polygon object
	 */
	public RrPolygon negate()
	{
		if(extrudeEnd >= 0 || valveEnd >= 0)
			Debug.e("Rr2Point.negate(): negating a polygon with its extrude or valve ending set.");
		RrPolygon result = new RrPolygon(att, closed);
		for(int i = size() - 1; i >= 0; i--)
		{
			result.add(point(i)); 
		}
		if(speeds == null)
			return result;
		for(int i = size() - 1; i >= 0; i--)
		{
			result.setSpeed(i, speed(i)); 
		}
		return result;
	}
	
	/**
	 * @return same polygon starting at a random vertex
	 */
	public RrPolygon randomStart()
	{
		if(extrudeEnd >= 0 || valveEnd >= 0)
			Debug.e("Rr2Point.randomStart(: randomizing a polygon with its extrude or valve ending set.");
		return newStart(rangen.nextInt(size()));
	}
	
	/**
	 * @return same polygon, but starting at vertex i
	 */
	public RrPolygon newStart(int i)
	{
		if(!isClosed())
			Debug.e("RrPolygon.newStart(i): reordering an open polygon!");
		if(extrudeEnd >= 0 || valveEnd >= 0)
			Debug.e("Rr2Point.newStart(i): reordering a polygon with its extrude or valve ending set.");		
		if(i < 0 || i >= size())
		{
			Debug.e("RrPolygon.newStart(i): dud index: " + i);
			return this;
		}
		RrPolygon result = new RrPolygon(att, closed);
		for(int j = 0; j < size(); j++)
		{
			result.add(point(i));
			if(speeds != null)
				result.setSpeed(j, speed(i));
			i++;
			if(i >= size())
				i = 0;
		}
		
		return result;
	}
	
	/**
	 * @return same polygon starting at point incremented from last polygon
	 */
	public RrPolygon incrementedStart(LayerRules lc)
	{
		if(size() == 0 || lc.getModelLayer() < 0)
			return this;
		if(extrudeEnd >= 0 || valveEnd >= 0)
			Debug.e("Rr2Point.incrementedStart(): incrementing a polygon with its extrude or valve ending set.");	
		int i = lc.getModelLayer() % size();
		return newStart(i);
	}
	
	/**
	 * Find the nearest vertex on a polygon to a given point
	 * @param p
	 * @return
	 */
	public int nearestVertex(Rr2Point p)
	{
		double d = Double.POSITIVE_INFINITY;
		int result = -1;
		for(int i = 0; i < size(); i++)
		{
			double d2 = Rr2Point.dSquared(point(i), p);
			if(d2 < d)
			{
				d = d2;
				result = i;
			}
		}
		if(result < 0)
			Debug.e("RrPolygon.nearestVertex(): no point found!");
		return result;
	}
	
	/**
	 * Find the nearest vertex on this polygon to any on polygon p,
	 * reorder p so that its nearest is its first one, then merge that polygon
	 * into this one.  The reordering is only done if the distance^2 is less 
	 * than linkUp.  If no reordering and merging are done false is returned, 
	 * otherwise true is returned.
	 * 
	 * @param p
	 * @param linkUp
	 * @return
	 */
	public boolean nearestVertexReorderMerge(RrPolygon p, double linkUp)
	{
		if(!p.isClosed())
			Debug.e("RrPolygon.nearestVertexReorder(): called for non-closed polygon.");
		if(extrudeEnd >= 0 || p.extrudeEnd >= 0 || valveEnd >= 0 || p.valveEnd >= 0)
			Debug.e("Rr2Point.nearestVertexReorderMerge(): merging polygons with a extrude or valve ending set.");
		double d = Double.POSITIVE_INFINITY;
		int myPoint = -1;
		int itsPoint = -1;
		for(int i = 0; i < size(); i++)
		{
			int j = p.nearestVertex(point(i));
			double d2 = Rr2Point.dSquared(point(i), p.point(j));
			if(d2 < d)
			{
				d = d2;
				myPoint = i;
				itsPoint = j;
			}
		}
		if(itsPoint >= 0 && d < linkUp*linkUp)
		{
			RrPolygon ro = p.newStart(itsPoint);
			ro.add(0, point(myPoint));
			add(myPoint, ro);
			return true;
		} else
			return false;
	}
	
	/**
	 * Find the index of the polygon point that has the maximal parametric projection
	 * onto a line.
	 * @param ln
	 * @return
	 */
	public int maximalVertex(RrLine ln)
	{
		double d = Double.NEGATIVE_INFINITY;
		int result = -1;
		for(int i = 0; i < size(); i++)
		{
			double d2 = ln.projection(point(i));
			if(d2 > d)
			{
				d = d2;
				result = i;
			}
		}
		if(result < 0)
			Debug.e("RrPolygon.maximalVertex(): no point found!");
		return result;		
	}
	
	/**
	 * Find the index of the polygon point that is at the start of the polygon's longest edge.
	 * @param ln
	 * @return
	 */
	public int longestEdgeStart()
	{
		double d = Double.NEGATIVE_INFINITY;
		int result = -1;
		int lim = size();
		if(!closed)
			lim--;
		for(int i = 0; i < lim; i++)
		{
			int j = (i + 1)%size();
			double d2 = Rr2Point.dSquared(point(i), point(j));
			if(d2 > d)
			{
				d = d2;
				result = i;
			}
		}
		if(result < 0)
			Debug.e("RrPolygon.longestEdgeStart(): no point found!");
		return result;		
	}
	
	/**
	 * Signed area (-ve result means polygon goes anti-clockwise)
	 * @return signed area
	 */
	public double area()
	{
		double a = 0;
		Rr2Point p, q;
		int j;
		for(int i = 1; i < size() - 1; i++)
		{
			j = i + 1;
			p = Rr2Point.sub(point(i), point(0));
			q = Rr2Point.sub(point(j), point(0));
			a += Rr2Point.op(q, p);
		} 
		return a*0.5;
	}
	
	/**
	 * Backtrack a given distance, inserting a new point there and set extrudeEnd to it.
	 * If drawEnd is already set, backtrack from that.
	 * @param distance to backtrack
	 * @return index of the inserted point
	 */
	public void backStepExtrude(double d)
	{
		if(d <= 0)
			return;
		
		Rr2Point p, q;
		int start, last;
		
		if(extrudeEnd >= 0)
			start = extrudeEnd;
		else
			start = size() - 1;					

		if(!isClosed() && extrudeEnd < 0)
				start--;
		
		if (start >= size() - 1)
			last = 0;
		else
			last = start + 1;
		
		double sum = 0;
		for(int i = start; i >= 0; i--)
		{
			sum += Rr2Point.d(point(i), point(last));
			if(sum > d)
			{
				sum = sum - d;
				q = Rr2Point.sub(point(last), point(i));
				p = Rr2Point.add(point(i), Rr2Point.mul(sum/q.mod(), q));
				double s = 0;
				if(speeds != null)
				{
					s = speeds.get(last) - speeds.get(i);
					s = speeds.get(i) + s*sum/q.mod();
				}
				int j = i + 1;
				if(j < size())
				{
					points.add(j, p);
					if(speeds != null)
						speeds.add(j, new Double(s)); 
				} else
				{
					points.add(p);
					if(speeds != null)						
						speeds.add(new Double(s)); 
				}
				extrudeEnd = j;
				return;
			}
			last = i;
		}
		extrudeEnd = 0;
	}
	
	
	/**
	 * Backtrack a given distance, inserting a new point there and set valveEnd to it.
	 * If drawEnd is already set, backtrack from that.
	 * @param distance to backtrack
	 * @return index of the inserted point
	 */
	public void backStepValve(double d)
	{
		if(d <= 0)
			return;
		
		Rr2Point p, q;
		int start, last;
		
		if(valveEnd >= 0)
			start = valveEnd;
		else
			start = size() - 1;					

		if(!isClosed() && valveEnd < 0)
				start--;
		
		if (start >= size() - 1)
			last = 0;
		else
			last = start + 1;
		
		double sum = 0;
		for(int i = start; i >= 0; i--)
		{
			sum += Rr2Point.d(point(i), point(last));
			if(sum > d)
			{
				sum = sum - d;
				q = Rr2Point.sub(point(last), point(i));
				p = Rr2Point.add(point(i), Rr2Point.mul(sum/q.mod(), q));
				double s = 0;
				if(speeds != null)
				{
					s = speeds.get(last) - speeds.get(i);
					s = speeds.get(i) + s*sum/q.mod();
				}
				int j = i + 1;
				if(j < size())
				{
					points.add(j, p);
					if(speeds != null)
						speeds.add(j, new Double(s)); 
				} else
				{
					points.add(p);
					if(speeds != null)						
						speeds.add(new Double(s)); 
				}
				valveEnd = j;
				return;
			}
			last = i;
		}
		valveEnd = 0;
	}
	
	/**
	 * Search back from the end of the polygon to find the vertex nearest to d back from the end
	 * @param d
	 * @return the index of the nearest vertex
	 */
	public int findBackPoint(double d)
	{
		Rr2Point last, p;
		int start = size() - 1;
		if(isClosed())
			last = point(0);
		else
		{
			last = point(start);
			start--;
		}
		double sum = 0;
		double lastSum = 0;
		int lasti = 0;
		for(int i = start; i >= 0; i--)
		{
			p = point(i);
			sum += Rr2Point.d(p, last);
			if(sum > d)
			{
				if(sum - d < d - lastSum)
					return i;
				else
					return lasti;
			}
			last = p;
			lastSum = sum;
			lasti = i;
		}
		return 0;
	}
	
	/**
	 * @param v1
	 * @param d2
	 * @return the vertex at which the polygon deviates from a (nearly) straight line from v1
	 */
	private int findAngleStart(int v1, double d2)
	{
		int leng = size();
		Rr2Point p1 = point(v1%leng);
		int v2 = v1;
		for(int i = 0; i <= leng; i++)
		{
			v2++;
			RrLine line = new RrLine(p1, point(v2%leng));
			for (int j = v1+1; j < v2; j++)
			{
				if (line.d_2(point(j%leng)).x() > d2)
					return v2 - 1;
			}	
		}
		Debug.d("RrPolygon.findAngleStart(): polygon is all one straight line!");
		return -1;
	}
	
	/**
	 * Simplify a polygon by deleting points from it that
	 * are closer than d to lines joining other points
	 * NB - this ignores speeds
	 * @param d
	 * @return simplified polygon object
	 */
	public RrPolygon simplify(double d)
	{
		int leng = size();
		if(leng <= 3)
			return new RrPolygon(this);
		RrPolygon r = new RrPolygon(att, closed);
		double d2 = d*d;

		int v1 = findAngleStart(0, d2);
		// We get back -1 if the points are in a straight line.
		if (v1<0)
		{
			r.add(point(0));
			r.add(point(leng-1));
			return r;
		}
		
		if(!isClosed())
			r.add(point(0));

		r.add(point(v1%leng));
		int v2 = v1;
		while(true)
		{
			// We get back -1 if the points are in a straight line. 
			v2 = findAngleStart(v2, d2);
			if(v2<0)
			{
				Debug.e("RrPolygon.simplify(): points were not in a straight line; now they are!");
				return(r);
			}
			
			if(v2 > leng || (!isClosed() && v2 == leng))
			{
				return(r);
			}
			
			if(v2 == leng && isClosed())
			{
				r.points.add(0, point(0));
				r.re_box();
				return r;
			}
			r.add(point(v2%leng));
		}
		// The compiler is very clever to spot that no return
		// is needed here...
	}
	
	/**
	 * Remove solitary edges that are shorter than tiny from the
	 * polygon if they are preceeded and followed by gap material.
	 * @param tiny
	 * @return filtered polygon object
	 */
	
//	public RrPolygon filterShort(double tiny)
//	{
//		RrPolygon r = new RrPolygon(att);
//		int oldEdgeFlag = flag(size()-1);
//		int i, ii;
//		
//		for(i = 1; i <= size(); i++)
//		{
//			ii = i%size();
//			if(oldEdgeFlag == LayerProducer.gapMaterial() && flag(ii) == LayerProducer.gapMaterial())
//			{
//				double d = Rr2Point.sub(point(ii), point(i - 1)).mod();
//				if(d > tiny)
//					r.add(point(i - 1), flag(i - 1));
//				//else
//					//System.out.println("Tiny edge removed.");
//			} else
//				r.add(point(i - 1), flag(i - 1));
//			oldEdgeFlag = flag(i - 1);
//		}
//		
//		// Anything left?
//		
//		for(i = 0; i < r.size(); i++)
//		{
//			if(r.flag(i) != LayerProducer.gapMaterial())
//				return r;
//		}
//		
//		// Nothing left
//		
//		return new RrPolygon(att);
//	}
	
	// ****************************************************************************
	
	/**
	 * Offset (some of) the points in the polygon to allow for the fact that extruded
	 * circles otherwise don't come out right.  See http://reprap.org/bin/view/Main/ArcCompensation.
	 * If the extruder for the polygon's arc compensation factor is 0, return the polygon unmodified.
	 * 
	 * This ignores speeds
	 * @param es
	 */
	public RrPolygon arcCompensate()
	{
		Extruder e = att.getExtruder();
		
		// Multiply the geometrically correct result by factor
		
		double factor = e.getArcCompensationFactor();
		if(factor < Preferences.tiny())
			return this;
		
		// The points making the arc must be closer than this together
		
		double shortSides = e.getArcShortSides();
		
		double thickness = e.getExtrusionSize();
		
		RrPolygon result = new RrPolygon(att, closed);
		
		Rr2Point previous = point(size() - 1);
		Rr2Point current = point(0);
		Rr2Point next;
		Rr2Point offsetPoint;
		
		double d1 = Rr2Point.dSquared(current, previous);
		double d2;
		double short2 = shortSides*shortSides;
		double t2 = thickness*thickness;
		double offset;
		
		for(int i = 0; i < size(); i++)
		{
			if(i == size() - 1)
				next = point(0);
			else
				next = point(i + 1);
			
			d2 = Rr2Point.dSquared(next, current);
			if(d1 < short2 && d2 < short2)
			{
				try
				{
					RrCircle c = new RrCircle(previous, current, next);
					offset = factor*(Math.sqrt(t2 + 4*c.radiusSquared())*0.5 - Math.sqrt(c.radiusSquared()));
					//System.out.println("Circle r: " + Math.sqrt(c.radiusSquared()) + " offset: " + offset);
					offsetPoint = Rr2Point.sub(current, c.centre());
					offsetPoint = Rr2Point.add(current, Rr2Point.mul(offsetPoint.norm(), offset));
					result.add(offsetPoint);
				} catch (Exception ex)
				{
					result.add(current);
				}
			} else
				result.add(current);
			
			d1 = d2;
			previous = current;
			current = next;
		}
		
		
		return result;
	}
	
	// *****************************************************************************************************
	//
	// Speed and acceleration calculations
	
	
	private RrInterval accRange(double startV, double s, double acc)
	{
		double vMax = Math.sqrt(2*acc*s + startV*startV);
		double vMin = -2*acc*s + startV*startV;
		if(vMin <= 0)
			vMin = 0; //-Math.sqrt(-vMin);
		else
			vMin = Math.sqrt(vMin);
		return new RrInterval(vMin, vMax);
	}
	
	private void backTrack(int j, double v, double vAccMin, double minSpeed, double acceleration, boolean fixup[])
	{
		Rr2Point a, b, ab;
		double backV, s;
		int i = j - 1;
		b = point(j);
		while(i >= 0)
		{
			a = point(i);
			ab = Rr2Point.sub(b, a);
			s = ab.mod();
			ab = Rr2Point.div(ab, s);
			backV = Math.sqrt(v*v + 2*acceleration*s);
			setSpeed(j, v);
			if(backV > speed(i))
			{
				fixup[j] = true;
				return;
			}
			setSpeed(i, backV);
			v = backV;
			fixup[j] = false;
			b = a;
			j = i;
			i--;
		}
	}
	
	/**
	 * Set the speeds at each vertex so that the polygon can be plotted as fast as possible
	 * 
	 * @param minSpeed
	 * @param maxSpeed
	 * @param maxAcceleration
	 */
	public void setSpeeds(double minSpeed, double maxSpeed, double acceleration)
	{
		//if(isClosed())System.out.println(toString());
		//RrPolygon pg = simplify(Preferences.gridRes());

		//points = pg.points;
		//box = pg.box;
		//if(isClosed())System.out.println(toString());
		
		boolean fixup[] = new boolean[size()];
		setSpeed(0, minSpeed);
		Rr2Point a, b, c, ab, bc;
		double oldV, vCorner, s, newS;
		int next;
		a = point(0);
		b = point(1);
		ab = Rr2Point.sub(b, a);
		s = ab.mod();
		ab = Rr2Point.div(ab, s);
		oldV = minSpeed;
		fixup[0] = true;
		for(int i = 1; i < size(); i++)
		{
			next = (i+1)%size();
			c = point(next);
			bc = Rr2Point.sub(c, b);
			newS = bc.mod();
			bc = Rr2Point.div(bc, newS);
			vCorner = Rr2Point.mul(ab, bc);
			if(vCorner >= 0)
				vCorner = minSpeed + (maxSpeed - minSpeed)*vCorner;
			else
				vCorner = 0.5*minSpeed*(2 + vCorner);
			
			if(!isClosed() && i == size() - 1)
				vCorner = minSpeed;
			
			RrInterval aRange = accRange(oldV, s, acceleration);
			
			if(vCorner <= aRange.low())
			{
				backTrack(i, vCorner, aRange.low(), minSpeed, acceleration, fixup);
			} else if(vCorner < aRange.high())
			{
				setSpeed(i, vCorner);
				fixup[i] = true;				
			} else
			{
				setSpeed(i, aRange.high());
				fixup[i] = false;
			}
			b = c;
			ab = bc;
			oldV = speed(i);
			s = newS;
		}
		
		
		for(int i = isClosed()?size():size() - 1; i > 0; i--)
		{
			int ib= i;
			if(ib == size())
				ib = 0;
			
			if(fixup[ib])
			{
				int ia = i - 1;
				a = point(ia);
				b = point(ib);
				ab = Rr2Point.sub(b, a);
				s = ab.mod();
				double va = speed(ia);
				double vb = speed(ib);
				
				VelocityProfile vp = new VelocityProfile(s, va, maxSpeed, vb, acceleration);
				switch(vp.flat())
				{
				case 0:
					break;
					
				case 1:
					add(i, Rr2Point.add(a, Rr2Point.mul(ab, vp.s1()/s)), vp.v());
					break;
					
				case 2:
					add(i, Rr2Point.add(a, Rr2Point.mul(ab, vp.s2()/s)), maxSpeed);
					add(i, Rr2Point.add(a, Rr2Point.mul(ab, vp.s1()/s)), maxSpeed);	
					break;
					
				default:
					Debug.e("RrPolygon.setSpeeds(): dud VelocityProfile flat value.");	
				}
			} 
		}

		
		if(speeds.size() != points.size())
			Debug.e("Speeds and points arrays different: " + speeds.size() + ", " + points.size());
	}
	
	// ****************************************************************************
	
	// Convex hull code - this uses the QuickHull algorithm
	// It finds the convex hull of a list of points from the polygon
	// (which can be the whole polygon if the list is all the points.
	// of course).
	// This completely ignores speeds
	
	/**
	 * @return Convex hull as a polygon
	 */
	public RrPolygon convexHull()
	{
		List<Integer> ls = listConvexHull();
		RrPolygon result = new RrPolygon(att, true);
		for(int i = 0; i < ls.size(); i++)
			result.add(listPoint(i, ls));
		return result;
	}
	
	/**
	 * @return Convex hull as a CSG expression
	 */
	public RrCSG CSGConvexHull()
	{
		List<Integer> ls = listConvexHull();
		return toCSGHull(ls);
	}
	
	/**
	 * @return Convex hull as a list of point indices
	 */
	private List<Integer> listConvexHull()
	{
		RrPolygon copy = new RrPolygon(this);
		if(copy.area() < 0)
			copy = copy.negate();

		List<Integer> all = copy.allPoints();
		return convexHull(all);
	}
	
	/**
	 * find a point from a list of polygon points
	 * @Param i
	 * @param a
	 * @return the point
	 */
	private Rr2Point listPoint(int i, List<Integer> a)
	{
		return point((a.get(i)).intValue());
	}

		
	/**
	 * find the top (+y) point of a polygon point list
	 * @return the index in the list of the point
	 */
	private int topPoint(List<Integer> a)
	{
		int top = 0;
		double yMax = listPoint(top, a).y();
		double y;

		for(int i = 1; i < a.size(); i++)
		{
			y = listPoint(i, a).y();
			if(y > yMax)
			{
				yMax = y;
				top = i;
			}
		}
		
		return top;
	}
	
	/**
	 * find the bottom (-y) point of a polygon point list
	 * @return the index in the list of the point
	 */
	private int bottomPoint(List<Integer> a)
	{
		int bot = 0;
		double yMin = listPoint(bot, a).y();
		double y;

		for(int i = 1; i < a.size(); i++)
		{
			y = listPoint(i, a).y();
			if(y < yMin)
			{
				yMin = y;
				bot = i;
			}
		}
		
		return bot;
	}

	/**
	 * Put the points on a triangle (list a) in the right order
	 * @param a
	 */
	private void clockWise(List<Integer> a)
	{
		if(a.size() == 3)
		{
			Rr2Point q = Rr2Point.sub(listPoint(1, a), listPoint(0, a));
			Rr2Point r = Rr2Point.sub(listPoint(2, a), listPoint(0, a));
			if(Rr2Point.op(q, r) > 0)
			{
				Integer k = a.get(0);
				a.set(0, a.get(1));
				a.set(1, k);
			}
		} else
			Debug.e("clockWise(): not called for a triangle!");
	}
	
	
	/**
	 * Turn the list of hull points into a CSG convex polygon
	 * @param hullPoints
	 * @return CSG representation
	 */	
	private RrCSG toCSGHull(List<Integer> hullPoints)
	{
		Rr2Point p, q;
		RrCSG hull = RrCSG.universe();
		p = listPoint(hullPoints.size() - 1, hullPoints);
		for(int i = 0; i < hullPoints.size(); i++)
		{
			q = listPoint(i, hullPoints);
			hull = RrCSG.intersection(hull, new RrCSG(new RrHalfPlane(p, q)));
			p = q;
		}

		return hull;
	}
	
	/**
	 * Remove all the points in a list that are within or on the hull
	 * @param inConsideration
	 * @param hull
	 */		
	private void outsideHull(List<Integer> inConsideration, RrCSG hull)
	{
		Rr2Point p;
		double small = Math.sqrt(Preferences.tiny());
		for(int i = inConsideration.size() - 1; i >= 0; i--)
		{
			p = listPoint(i, inConsideration);
			if(hull.value(p) <= small)	
			{
				inConsideration.remove(i);
			}
		}
	}
	
	/**
	 * Compute the convex hull of all the points in the list
	 * @param points
	 * @return list of point index pairs of the points on the hull
	 */
	private List<Integer> convexHull(List<Integer> points)
	{	
		if(points.size() < 3)
		{
			Debug.e("convexHull(): attempt to compute hull for " + points.size() + " points!");
			return new ArrayList<Integer>();
		}
		
		List<Integer> inConsideration = new ArrayList<Integer>(points);
		
		int i;

		// The top-most and bottom-most points must be on the hull
		
		List<Integer> result = new ArrayList<Integer>();
		int t = topPoint(inConsideration);
		int b = bottomPoint(inConsideration);
		result.add(inConsideration.get(t));
		result.add(inConsideration.get(b));
		if(t > b)
		{
			inConsideration.remove(t);
			inConsideration.remove(b);
		} else
		{
			inConsideration.remove(b);
			inConsideration.remove(t);			
		}
			
		// Repeatedly add the point that's farthest outside the current hull
		
		int corner, after;
		RrCSG hull;
		double v, vMax;
		Rr2Point p, q;
		RrHalfPlane hp;
		while(inConsideration.size() > 0)
		{
			vMax = 0;   // Need epsilon?
			corner = -1;
			after = -1;
			for(int testPoint = inConsideration.size() - 1; testPoint >= 0; testPoint--)
			{
				p = listPoint(result.size() - 1, result);
				for(i = 0; i < result.size(); i++)
				{
					q = listPoint(i, result);
					hp = new RrHalfPlane(p, q);
					v = hp.value(listPoint(testPoint, inConsideration));
					if(result.size() == 2)
						v = Math.abs(v);
					if(v >= vMax)
					{
						after = i;
						vMax = v;
						corner = testPoint;
					}
					p = q;
				}
			}
			
			if(corner >= 0)
			{
				result.add(after, inConsideration.get(corner));
				inConsideration.remove(corner);
			} else if(inConsideration.size() > 0)
			{
				Debug.e("convexHull(): points left, but none included!");
				return result;
			}
			
			// Get the first triangle in the right order
			
			if(result.size() == 3)
				clockWise(result);

			// Remove all points within the current hull from further consideration
			
			hull = toCSGHull(result);
			outsideHull(inConsideration, hull);
		}
		
		return result;
	}
	
	// **************************************************************************
	
	// Convert polygon to CSG form 
	// using Kai Tang and Tony Woo's algorithm.
	// This completely ignores speeds
	
	/**
	 * Construct a list of all the points in the polygon
	 * @return list of indices of points in the polygons
	 */
	private List<Integer> allPoints()
	{
		List<Integer> points = new ArrayList<Integer>();
		for(int i = 0; i < size(); i++)
				points.add(new Integer(i));
		return points;
	}
	
	/**
	 * Set all the flag values in a list the same
	 * @param f
	 */
	private void flagSet(int f, List<Integer> a, int[] flags)
	{
			for(int i = 0; i < a.size(); i++)
				flags[(a.get(i)).intValue()] = f;
	}	
	
	/**
	 * Get the next whole section to consider from list a
	 * @param a
	 * @param level
	 * @return the section (null for none left)
	 */
	private List<Integer> polSection(List<Integer> a, int level, int[] flags)
	{
		int flag, oldi;
		oldi = a.size() - 1;
		int oldFlag = flags[((Integer)a.get(oldi)).intValue()];

		int ptr = -1;
		for(int i = 0; i < a.size(); i++)
		{
			flag = flags[((Integer)a.get(i)).intValue()];

			if(flag < level && oldFlag >= level) 
			{
				ptr = oldi;
				break;
			}
			oldi = i;
			oldFlag = flag;
		}
		
		if(ptr < 0)
			return null;
		
		List<Integer> result = new ArrayList<Integer>();
		result.add(a.get(ptr));
		ptr++;
		if(ptr > a.size() - 1)
			ptr = 0;
		while(flags[((Integer)a.get(ptr)).intValue()] < level)
		{
			result.add(a.get(ptr));
			ptr++;
			if(ptr > a.size() - 1)
				ptr = 0;
		}

		result.add(a.get(ptr));

		return result;
	}
	
	/**
	 * Compute the CSG representation of a (sub)list recursively
	 * @param a
	 * @param level
	 * @return CSG representation
	 */
	private RrCSG toCSGRecursive(List<Integer> a, int level, boolean closed, int[] flags)
	{	
		flagSet(level, a, flags);	
		level++;
		List<Integer> ch = convexHull(a);
		if(ch.size() < 3)
		{
			Debug.e("toCSGRecursive() - null convex hull: " + ch.size() +
					" points.");
			return RrCSG.nothing();
		}
		
		flagSet(level, ch, flags);
		RrCSG hull;


		if(level%2 == 1)
			hull = RrCSG.universe();
		else
			hull = RrCSG.nothing();

		// Set-theoretically combine all the real edges on the convex hull

		int i, oldi, flag, oldFlag, start;
		
		if(closed)
		{
			oldi = a.size() - 1;
			start = 0;
		} else
		{
			oldi = 0;
			start = 1;
		}
		
		for(i = start; i < a.size(); i++)
		{
			oldFlag = flags[((Integer)a.get(oldi)).intValue()]; //listFlag(oldi, a);
			flag = flags[((Integer)a.get(i)).intValue()]; //listFlag(i, a);

			if(oldFlag == level && flag == level)
			{
				RrHalfPlane hp = new RrHalfPlane(listPoint(oldi, a), listPoint(i, a));
				if(level%2 == 1)
					hull = RrCSG.intersection(hull, new RrCSG(hp));
				else
					hull = RrCSG.union(hull, new RrCSG(hp));
			} 
			
			oldi = i;
		}
		
		// Finally deal with the sections on polygons that form the hull that
		// are not themselves on the hull.
		
		List<Integer> section = polSection(a, level, flags);
		while(section != null)
		{
			if(level%2 == 1)
				hull = RrCSG.intersection(hull,
						toCSGRecursive(section, level, false, flags));
			else
				hull = RrCSG.union(hull, 
						toCSGRecursive(section, level, false, flags));
			section = polSection(a, level, flags);
		}
		
		return hull;
	}
	
	/**
	 * Convert a polygon to CSG representation
	 * @param tolerance
	 * @return CSG polygon object based on polygon and tolerance 
	 */
	public RrCSG toCSG(double tolerance)
	{
		
		RrPolygon copy = new RrPolygon(this);
		if(copy.area() < 0)
			copy = copy.negate();

		List<Integer> all = copy.allPoints();
		int [] flags = new int[copy.size()];
		RrCSG expression = copy.toCSGRecursive(all, 0, true, flags);

		//RrRectangle b = copy.box.scale(1.1);
		//expression = expression.simplify(tolerance);
		//if(att == null)
		//	Debug.e("toCSG(): null attribute!");
		//RrCSGPolygon result = new RrCSGPolygon(expression, b, att);
		
		return expression;
	}
	
}


