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
 
 
 RrPolygonList: A collection of 2D polygons
 
 First version 20 May 2005
 This version: 1 May 2006 (Now in CVS - no more comments here)
 
 */

package org.reprap.geometry.polygons;

import java.util.ArrayList;
import java.util.List;

import org.reprap.Extruder;
import org.reprap.geometry.LayerRules;
import org.reprap.utilities.Debug;

/**
 * Small class to hold a polygon index and the index of a point within it
 * @author ensab
 *
 */
class PolPoint
{
	private int pNear;
	private int pEnd;
	private int pg;
	private double d2;
	private RrPolygon pol;
	
	public PolPoint(int pnr, int pgn, RrPolygon poly, double s)
	{
		set(pnr, pgn, poly, s);
	}
	
	public int near() { return pNear; }
	public int end() { return pEnd; }
	public int pIndex() { return pg; }
	public RrPolygon polygon() { return pol; }
	public double dist2() { return d2; }
	
	public void set(int pnr, int pgn, RrPolygon poly, double s)
	{
		pNear = pnr;
		pg = pgn;
		pol = poly;
		d2 = s;
	}
	
	private void midPoint(int i, int j)
	{
		if(i > j)
		{
			int temp = i;
			i = j;
			j = temp;
		}
		
		if(i < 0 || i > pol.size() -1 || j < 0 || j > pol.size() -1)
			Debug.e("RrPolygonList.midPoint(): i and/or j wrong: i = " + i + ", j = " + j);
		
		Rr2Point p = Rr2Point.add(pol.point(i), pol.point(j));
		p = Rr2Point.mul(p, 0.5);
		pol.add(j, p);
		pEnd = j;
	}
	
	/**
	 * Find the a long-enough polygon edge away from point pNear
	 * and put its index in pNext.
	 */
	public void findLongEnough(double longEnough, double searchFor)
	{
		double d;
		double sum = 0;
		double longest = -1;
		Rr2Point p = pol.point(pNear);
		Rr2Point q;
		int inc = 1;
		if(pNear > pol.size()/2 - 1)
			inc = -1;
		int i = pNear;
		int iLongest = i;
		int jLongest = i;
		int j = i;
		while(i > 0 && i < pol.size() - 1 && sum < searchFor)
		{
			i += inc;
			q = pol.point(i);
			d = Rr2Point.d(p, q);
			if(d >= longEnough)
			{
				midPoint(i, j);
				return;
			}
			if(d > longest)
			{
				longest = d;
				iLongest = i;
				jLongest = j;
			}
			sum += d;
			p = q;
			j = i;
		}
		midPoint(iLongest, jLongest);
	}
}

/**
 * chPair - small class to hold double pointers for convex hull calculations.
 */
class chPair
{
	/**
	 * 
	 */
	public int polygon;
	
	/**
	 * 
	 */
	public int vertex;
	
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
	 * @param p
	 * @param v
	 */
	chPair(int p, int v)
	{
		polygon = p;
		vertex = v;
	}
}

/**
 * tree - class to hold lists to build a containment tree
 * (that is a representation of which polygon is inside which,
 * like a Venn diagram).
 */
class treeList
{
	/**
	 * Index of this polygon in the list
	 */
	private int index;
	
	/**
	 * The polygons inside this one
	 */
	private List<treeList> children = null;
	
	/**
	 * The polygon that contains this one
	 */
	private treeList parent = null;
	
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
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).destroy();
				children.set(i, null);
			}
		}
		children = null;
		if(parent != null)
			parent.destroy();
		parent = null;	
	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		children = null;
//		parent = null;	
//		super.finalize();
//	}
	
	/**
	 * Constructor builds from a polygon index
	 * @param i
	 */
	public treeList(int i)
	{
		index = i;
		children = null;
		parent = null;
	}
	
	/**
	 * Add a polygon as a child of this one
	 * @param t
	 */
	public void addChild(treeList t)
	{
		if(children == null)
			children = new ArrayList<treeList>();
		children.add(t);
	}
	
	/**
	 * Get the ith polygon child of this one
	 * @param i
	 * @return
	 */
	public treeList getChild(int i)
	{
		if(children == null)
		{
			Debug.e("treeList: attempt to get child from null list!");
			return null;
		}
		return children.get(i);
	}
	
	/**
	 * Get the parent
	 * @return
	 */
	public treeList getParent()
	{
		return parent;
	}
	
	/**
	 * How long is the list (if any)
	 * @return
	 */
	public int size()
	{
		if(children != null)
			return children.size();
		else
			return 0;
	}
	
	/**
	 * Printable form
	 */
	public String toString()
	{
		String result;
		
		if(parent != null)
			result = Integer.toString(index) + "(^" + parent.index + "): ";
		else
			result = Integer.toString(index) + "(^null): ";
		
		for(int i = 0; i < size(); i++)
		{
			result += getChild(i).polygonIndex() + " ";
		}
		result += "\n";
		for(int i = 0; i < size(); i++)
		{
			result += getChild(i).toString();
		}
		return result;
	}
	
	
	/**
	 * Remove every instance of polygon t from the list
	 * @param t
	 */
	public void remove(treeList t)
	{		
		for(int i = size() - 1; i >= 0; i--)
		{
			if(getChild(i) == t)
			{
				children.remove(i);
			}
		}
	}
	

	/**
	 * Recursively walk the tree from here to find polygon target.
	 * @param node
	 * @param target
	 * @return
	 */
	public treeList walkFind(int target)
	{
		if(polygonIndex() == target)
			return this;
				
		for(int i = 0; i < size(); i++)
		{
			treeList result = getChild(i).walkFind(target);
			if(result != null)
				return result;
		}
		
		return null;
	}
	
	/**
	 * Walk the tree building a CSG expression to represent all
	 * the polygons as one thing.
	 * @param csgPols
	 * @return
	 */
	public RrCSG buildCSG(List<RrCSG> csgPols)
	{
		if(size() == 0)
			return csgPols.get(index);
		
		RrCSG offspring = RrCSG.nothing();
		
		for(int i = 0; i < size(); i++)
		{
			treeList iEntry = getChild(i);
			RrCSG iCSG = iEntry.buildCSG(csgPols);
			offspring = RrCSG.union(offspring, iCSG);
		}
		
		if(index < 0)
			return offspring;
		else
			return RrCSG.difference(csgPols.get(index), offspring);
	}
	
	/**
	 * Do a depth-first walk setting parents.  Any node that appears
	 * in more than one list should have the deepest possible parent 
	 * set as its parent, which is what we want.
	 * @param node
	 */
	public void setParents()
	{
		treeList child;
		int i;
		for(i = 0; i < size(); i++)
		{
			child = getChild(i);
			child.parent = this;
		}
		for(i = 0; i < size(); i++)
		{
			child = getChild(i);
			child.setParents();
		}		
	}
	
	/**
	 * get the index of the polygon
	 * @return
	 */
	public int polygonIndex()
	{
		return index;
	}
}

/**
 * RrPolygonList: A collection of 2D polygons
 * List of polygons class.  This too maintains a maximum enclosing rectangle.
 */
public class RrPolygonList
{
	/**
	 * 
	 */
	private List<RrPolygon> polygons = null;
	
	/**
	 * 
	 */
	private RrRectangle box = null;
	
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
		if(polygons != null)
		{
			for(int i = 0; i < size(); i++)
			{
				polygons.get(i).destroy();
				polygons.set(i,null);
			}
			polygons = null;
		}
		if(box != null)
			box.destroy();
		box = null;
		beingDestroyed = false;
	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		polygons = null;
//		box = null;
//		super.finalize();
//	}
	
	/**
	 * Empty constructor
	 */
	public RrPolygonList()
	{
		polygons = new ArrayList<RrPolygon>();
		box = new RrRectangle();
	}
	
	/**
	 * Get the data
	 * @param i index of polygon to return
	 * @return polygon at index i
	 */
	public RrPolygon polygon(int i)
	{
		return polygons.get(i);
	}
	
	/**
	 * @return number of polygons in the list
	 */
	public int size()
	{
		return polygons.size();
	}
	
	/**
	 * @return the current enclosing box
	 */
	public RrRectangle getBox() { return box; }
	
	/**
	 * Overwrite one of the polygons
	 * @param i index of polygon to overwrite
	 * @param p polygon to set at index i
	 */
	public void set(int i, RrPolygon p)
	{
		polygons.set(i, p);
	}
	
	/**
	 * Remove one from the list
	 * @param i index of polygon to remove
	 */
	public void remove(int i)
	{
		polygons.remove(i);
	}
	
	/**
	 * Deep copy
	 * @param lst list of polygons to copy
	 */
	public RrPolygonList(RrPolygonList lst)
	{
		polygons = new ArrayList<RrPolygon>();
		box = new RrRectangle(lst.box);
		for(int i = 0; i < lst.size(); i++)
			polygons.add(new RrPolygon(lst.polygon(i)));
	}
	
	/**
	 * Put a new list on the end
	 * @param lst list to append to existing polygon list
	 */
	public void add(RrPolygonList lst)
	{
		if(lst.size() == 0)
			return;
		for(int i = 0; i < lst.size(); i++)
			polygons.add(new RrPolygon(lst.polygon(i)));
		box.expand(lst.box);
	}
	
	/**
	 * Add one new polygon to the list
	 * @param p polygon to add to the list
	 */
	public void add(RrPolygon p)
	{
		polygons.add(p);
		box.expand(p.getBox());
	}
	
	/**
	 * Add one new polygon to the list at location i
	 * @param p polygon to add to the list
	 */
	public void add(int i, RrPolygon p)
	{
		polygons.add(i, p);
		box.expand(p.getBox());
	}
	
	/**
	 * Swap two in the list
	 * @param i
	 * @param j
	 */
	private void swap(int i, int j)
	{
		RrPolygon p = polygons.get(i);
		polygons.set(i, polygons.get(j));
		polygons.set(j, p);
	}
	
	/**
	 * Negate all the polygons
	 * @return negated polygons
	 */
	public RrPolygonList negate()
	{
		RrPolygonList result = new RrPolygonList();
		for(int i = 0; i < size(); i++)
			result.polygons.add(polygon(i).negate());
		result.box = new RrRectangle(box);
		return result;
	}
	
//	/**
//	 * Set whether we loop back on ourself.
//	 * @param c
//	 */
//	public void setClosed(boolean c)
//	{
//		for(int i = 0; i < size(); i++)
//			polygon(i).setClosed(c);		
//	}
	
	/**
	 * Create a new polygon list with a random start vertex for each 
	 * polygon in the list
	 * @return new polygonlist
	 */
	public RrPolygonList randomStart()
	{
		RrPolygonList result = new RrPolygonList();
		for(int i = 0; i < size(); i++)
			result.add(polygon(i).randomStart());
		return result;
	}
	
	/**
	 * Negate one of the polygons
	 * @param i
	 */
	private void negate(int i)
	{
		RrPolygon p = polygon(i).negate();
		polygons.set(i, p);
	}
	
	/**
	 * As a string
	 * @return string representation of polygon list
	 */
	public String toString()
	{
		String result = "Polygon List - polygons: ";
		result += size() + ", enclosing box: ";
		result += box.toString();
		for(int i = 0; i < size(); i++)
			result += "\n" + polygon(i).toString();
		return result;
	}
	
	/**
	 * Turn into SVG xml
	 * @param opf
	 */
	public String svg()
	{
		String result = "<?xml version=\"1.0\" standalone=\"no\"?>" +
		"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"" +
		"\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">" +
		"<svg" +
		" width=\"" + Double.toString(box.x().length()) + "mm\"" +
		" height=\""  + Double.toString(box.y().length()) +  "mm\"" +
		" viewBox=\"" + Double.toString(box.x().low()) +
		" " + Double.toString(box.y().low()) +
		" " + Double.toString(box.x().high()) +
		" " + Double.toString(box.y().high()) + "\"" +
		" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">" +
		" <desc>RepRap polygon list - http://reprap.org</desc>";
		
		int leng = size();
		for(int i = 0; i < leng; i++)
			result += polygon(i).svg();
		
		result += "</svg>";
		return result;
	}
	
	/**
	 * Simplify all polygons by length d
	 * N.B. this may throw away small ones completely
	 * @param d
	 * @return simplified polygon list
	 */
	public RrPolygonList simplify(double d)
	{
		RrPolygonList r = new RrPolygonList();
		double d2 = d*d;
		
		for(int i = 0; i < size(); i++)
		{
			RrPolygon p = polygon(i);
			if(p.getBox().dSquared() > 2*d2)
				r.add(p.simplify(d));
		}
		
		return r;
	}
	
	
	/**
	 * Re-order and (if need be) reverse the order of the polygons
	 * in a list so the end of the first is near the start of the second and so on.
	 * This is a heuristic - it does not do a full travelling salesman...
	 * This deals with both open and closed polygons, but it only allows closed ones to
	 * be re-ordered if reOrder is true.  If any point on a closed polygon is closer to 
	 * any point on any other than linkUp, the two polygons are merged at their closest
	 * points.  This is suppressed by setting linkUp negative.
	 * 
	 * @param startNearHere
	 * @param reOrder
	 * @param linkUp
	 * @return new ordered polygon list
	 */
	public RrPolygonList nearEnds(Rr2Point startNearHere, boolean reOrder, double linkUp)
	{
		RrPolygonList r = new RrPolygonList();
		if(size() <= 0)
			return r;
		
		int i, j;
		
		for(i = 0; i < size(); i++)
			r.add(polygon(i));
		
		// Make the nearest end point on any polygon to startNearHere
		// go to polygon 0 and get it the right way round if it's open.
		
		boolean neg = false;
		double d = Double.POSITIVE_INFINITY;
		
		double d2;
		int near = -1;
		int nearV = -1;
		
		// Begin by moving the polygon nearest the specified start point to the head
		// of the list.
		
		if(startNearHere != null)
		{
			for(i = 0; i < size(); i++)
			{
				if(r.polygon(i).isClosed() && reOrder)
				{
					int nv = polygon(i).nearestVertex(startNearHere);
					d2 = Rr2Point.dSquared(startNearHere, polygon(i).point(nv));
					if(d2 < d)
					{
						near = i;
						nearV = nv;
						d = d2;
						neg = false;
					}
				} else
				{
					d2 = Rr2Point.dSquared(startNearHere, r.polygon(i).point(0));
					if(d2 < d)
					{
						near = i;
						nearV = -1;
						d = d2;
						neg = false;
					}
					if(!r.polygon(i).isClosed())
					{
						d2 = Rr2Point.dSquared(startNearHere, r.polygon(i).point(r.polygon(i).size() - 1));
						if(d2 < d)
						{
							near = i;
							nearV = -1;
							d = d2;
							neg = true;
						}
					}
				}
			}

			if(near < 0)
			{
				Debug.e("RrPolygonList.nearEnds(): no nearest end found to start point!");
				return r;
			}

			r.swap(0, near);
			if(reOrder && nearV >= 0)
				set(0, polygon(0).newStart(nearV));
			if(neg)
				r.negate(0);
		}
		
		if(reOrder && linkUp >= 0)
			for(i = 0; i < r.size() - 1; i++)
				for(j = i+1; j < r.size(); j++)
					if(r.polygon(j).isClosed())
						if(r.polygon(i).nearestVertexReorderMerge(r.polygon(j), linkUp))
							r.remove(j);
		
		// Go through the rest of the polygons getting them as close as
		// reasonable.
		
		for(i = 0; i < r.size() - 1; i++)
		{
			Rr2Point end;
			if(r.polygon(i).isClosed())
				end = r.polygon(i).point(0);
			else
				end = r.polygon(i).point(r.polygon(i).size() - 1);
			neg = false;
			near = -1;
			d = Double.POSITIVE_INFINITY;
			for(j = i+1; j < r.size(); j++)
			{	
				d2 = Rr2Point.dSquared(end, r.polygon(j).point(0));
				if(d2 < d)
				{
					near = j;
					d = d2;
					neg = false;
				}

				if(!r.polygon(j).isClosed())
				{
					d2 = Rr2Point.dSquared(end, r.polygon(j).point(r.polygon(j).size() - 1));
					if(d2 < d)
					{
						near = j;
						d = d2;
						neg = true;
					}
				}
			}

			if(near > 0)
			{
				if(neg)
					r.negate(near);
				r.swap(i+1, near);
			}
		}
		
		return r;
	}
	
	/**
	 * Remove polygon pol from the list, replacing it with two polygons, the
	 * first being pol's vertices from 0 to st inclusive, and the second being
	 * pol's vertices from en to its end inclusive.  It is permissible for 
	 * st == en, but if st > en, then they are swapped.
	 * 
	 * The two new polygons are put on the end of the list.
	 * 
	 * @param pol
	 * @param st
	 * @param en
	 */
	private void cutPolygon(int pol, int st, int en)
	{
		RrPolygon old = polygon(pol);
		RrPolygon p1 = new RrPolygon(old.getAttributes(), old.isClosed());
		RrPolygon p2 = new RrPolygon(old.getAttributes(), old.isClosed());
		if(st > en)
		{
			int temp = st;
			st = en;
			en = temp;
		}
		if(st > 0)
		{
			for(int i = 0; i <= st; i++)
				p1.add(old.point(i));
		}
		if(en < old.size() - 1)
		{
			for(int i = en; i < old.size(); i++)
				p2.add(old.point(i));
		}
		remove(pol);
		if(p1.size() > 1)
			add(p1);
		if(p2.size() > 1)
			add(p2);
	}
	
	/**
	 * Search a polygon list to find the nearest point on all the polygons within it
	 * to the point p.  If omit is non-negative, ignore that polygon in the search.
	 * 
	 * @param p
	 * @param omit
	 * @return
	 */
	private PolPoint ppSearch(Rr2Point p, int omit)
	{
		double d = Double.POSITIVE_INFINITY;
		PolPoint result = null;
		
		if(size() <= 0)
			return result;
		
		for(int i = 0; i < size(); i++)
		{
			if(i != omit)
			{
				RrPolygon pgon = polygon(i);
				int n = pgon.nearestVertex(p);
				double d2 = Rr2Point.dSquared(p, pgon.point(n));
				if(d2 < d)
				{
					if(result == null)
						result = new PolPoint(n, i, pgon, d2);
					else
						result.set(n, i, pgon, d2);
					d = d2;
				}
			}
		}
		
		if(result == null)
			Debug.e("RrPolygonList.ppSearch(): no point found!");
		
		return result;
	}
	
	
	/**
	 * This assumes that the RrPolygonList for which it is called is all the outline
	 * polygons, and that hatching is their infill hatch.  It goes through the outlines
	 * and the hatch modifying both so that that outlines actually start and end half-way 
	 * along a hatch line (that half of the hatch line being deleted).  When the outlines
	 * are then printed, they start and end in the middle of a solid area, thus minimising dribble.
	 * 
	 * The outline polygons are re-ordered before the start so that their first point is
	 * the most extreme one in the current hatch direction.
	 * 
	 * @param hatching
	 * @param lc
	 */
	public void middleStarts(RrPolygonList hatching, LayerRules lc)
	{
		for(int i = 0; i < size(); i++)
		{
			RrPolygon outline = polygon(i);
			Extruder ex = outline.getAttributes().getExtruder();
			if(ex.getMiddleStart())
			{
				RrLine l = lc.getHatchDirection(ex).pLine();
				if(i%2 != 0 ^ lc.getMachineLayer()%4 > 1)
					l = l.neg();
				outline = outline.newStart(outline.maximalVertex(l));

				Rr2Point start = outline.point(0);
				PolPoint pp = hatching.ppSearch(start, -1);
				if(pp != null)
				{
					pp.findLongEnough(10, 30);

					int st = pp.near();
					int en = pp.end();

					RrPolygon pg = pp.polygon();

					outline.add(start);
					outline.setExtrudeEnd(outline.size() - 1);

					if(en >= st)
					{
						for(int j = st; j <= en; j++)
						{
							outline.add(0, pg.point(j));  // Put it on the beginning...
							if(j < en)
								outline.add(pg.point(j));     // ...and the end.
						}
					} else
					{
						for(int j = st; j >= en; j--)
						{
							outline.add(0, pg.point(j));
							if(j > en)
								outline.add(pg.point(j));
						}
					}

					set(i, outline);

					hatching.cutPolygon(pp.pIndex(), st, en);
				}
			}
		}
	}
	
	/**
	 * Offset (some of) the points in the polygons to allow for the fact that extruded
	 * circles otherwise don't come out right.  See http://reprap.org/bin/view/Main/ArcCompensation.
	 *
	 * @param es
	 */	
	public RrPolygonList arcCompensate()
	{
		RrPolygonList r = new RrPolygonList();
		
		for(int i = 0; i < size(); i++)
		{
			RrPolygon p = polygon(i);
			r.add(p.arcCompensate());
		}
		
		return r;		
	}
	
	/**
	 * Remove edges that are shorter than tiny from the
	 *   polygons in the list if those edges are preceeded 
	 *   by gap material.  
	 * @param tiny
	 * @return filtered polygon list
	 */
//	public RrPolygonList filterShorts(double tiny)
//	{
//		RrPolygonList r = new RrPolygonList();
//		int i;
//		RrPolygon p;
//		
//		for(i = 0; i < size(); i++)
//		{
//			p = polygon(i).filterShort(tiny);
//			if(p.size() > 0)
//				r.add(polygon(i));
//		}
//		return r;
//	}
	
	/**
	 * Is polygon i inside CSG polygon j?
	 * (Check twice to make sure...)
	 * @param i
	 * @param j
	 * @param csgPols
	 * @return true if the polygon is inside the CSG polygon, false if otherwise
	 */
	private boolean inside(int i, int j, List<RrCSG> csgPols)
	{
		RrCSG exp = csgPols.get(j);
		Rr2Point p = polygon(i).point(0);
		boolean a = (exp.value(p) <= 0);
		p = polygon(i).point(polygon(i).size()/2);
		boolean b = (exp.value(p) <= 0);
		if (a != b)
		{
			Debug.e("RrPolygonList:inside() - i is both inside and outside j!");
			// casting vote...
			p = polygon(i).point(polygon(i).size()/3);
			return exp.value(p) <= 0;
		}
		return a;
	}
		
	/**
	 * Take a list of CSG expressions, each one corresponding with the entry of the same 
	 * index in this class, classify each as being inside other(s)
	 * (or not), and hence form a single CSG expression representing them all.
	 * @param csgPols
	 * @param polAttributes
	 * @return single CSG expression based on csgPols list 
	 */
	private RrCSG resolveInsides(List<RrCSG> csgPols)
	{
		int i, j;
		
		treeList universe = new treeList(-1);
		universe.addChild(new treeList(0));
		
		// For each polygon construct a list of all the others that
		// are inside it (if any).
		
		for(i = 0; i < size() - 1; i++)
		{
			treeList isList = universe.walkFind(i);
			if(isList == null)
			{
				isList = new treeList(i);
				universe.addChild(isList);
			}

			for(j = i + 1; j < size(); j++)
			{
				treeList jsList = universe.walkFind(j);
				if(jsList == null)
				{
					jsList = new treeList(j);
					universe.addChild(jsList);
				}


				if(inside(j, i, csgPols))  // j inside i?
					isList.addChild(jsList);

				if(inside(i, j, csgPols))  // i inside j?
					jsList.addChild(isList);						
			}
		}
		
		// Set all the parent pointers
		
		universe.setParents();
		//System.out.println("---\n" + universe.toString() + "\n---\n");
		
		// Eliminate each leaf from every part of the tree except the node immediately above itself
		
		for(i = 0; i < size(); i++)
		{		
			treeList isList = universe.walkFind(i);
			if(isList == null)
				Debug.e("RrPolygonList.resolveInsides() - can't find list for polygon " + i);
			treeList parent = isList.getParent();
			if(parent != null)
			{
				parent = parent.getParent();
				while(parent != null)
				{
					parent.remove(isList);
					parent = parent.getParent();
				}
			}
		}
		//System.out.println("---\n" + universe.toString() + "\n---\n");
		
		// We now have a tree of containment.  universe is the root.
		// Walk the tree turning it into a single CSG expression
		
		RrCSG expression = universe.buildCSG(csgPols);
		
		//RrCSGPolygon res = new RrCSGPolygon(expression, box.scale(1.1), polygon(0).getAttributes());
		//res.divide(0.0001, 0);
		//RrGraphics g2 = new RrGraphics(res, true);
		return expression;		
	}
	
	/**
	 * Compute the CSG representation of all the polygons in the list
	 * @return CSG representation
	 */
	public RrCSG toCSG(double tolerance)
	{	
		if(size() == 0)
		{
			return RrCSG.nothing();
		}
		if(size() == 1)
		{
			return polygon(0).toCSG(tolerance);
		}
		
		List<RrCSG> csgPols = new ArrayList<RrCSG>();
		
		for(int i = 0; i < size(); i++)
			csgPols.add(polygon(i).toCSG(tolerance));
		
		RrCSG polygons = resolveInsides(csgPols);
		//expression = expression.simplify(tolerance);
		
		return polygons;
	}
		
}
