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
 
 */

package org.reprap.geometry.polygons;

import java.util.ArrayList;

import org.reprap.CSGOp;
import org.reprap.geometry.polyhedra.CSG3D;
import org.reprap.geometry.polyhedra.Point3D;
import org.reprap.utilities.Debug;

/**
 * RepRap Constructive Solid Geometry class
 * 
 * RrCSG: 2D polygons as boolean combinations of half-planes
 * First version 14 November 2005 
 */
public class CSG2D
{
	
	/**
	 * Universal set 
	 */
	private static final CSG2D u = new CSG2D(true);  
	
	/**
	 * Null set  
	 */
	private static final CSG2D n = new CSG2D(false); 
	
	/**
	 * Leaf half plane 
	 */
	private HalfPlane hp = null;
	
	/**
	 * Type of set
	 */
	private CSGOp op;
	
	/**
	 * Non-leaf child operands 
	 */
	private CSG2D c1 = null;
	private CSG2D c2 = null; 
	
	/**
	 * The complement (if there is one) 
	 */
	private CSG2D comp = null;        
	
	/**
	 * How much is in here (leaf count)?
	 */
	private int complexity;
	
	/**
	 * Flag to prevent cyclic graphs going round forever
	 */
	private boolean beingDestroyed = false;
	
//	/**
//	 * Destroy me and all that I point to
//	 */
//	public void destroy() 
//	{
//		if(beingDestroyed) // Prevent infinite loop
//			return;
//		beingDestroyed = true;
//		if(c1 != null)
//			c1.destroy();
//		c1 = null;
//		if(c2 != null)
//			c2.destroy();
//		c2 = null;
//		if(comp != null)
//			comp.destroy();
//		comp = null;
//		if(hp != null)
//			hp.destroy();
//		hp = null;
//		beingDestroyed = false;
//	}
	
	/**
	 * Destroy just me
	 */
//	protected void finalize() throws Throwable
//	{
//		c1 = null;
//		c2 = null;
//		comp = null;
//		hp = null;
//		super.finalize();
//	}
	
	private CSG2D()
	{
		hp = null;
		op = CSGOp.LEAF;
		c1 = null;
		c2 = null;
		comp = null;
		complexity = 1;
	}	
	
	/**
	 * Make a leaf from a single half-plane
	 * @param h
	 */
	public CSG2D(HalfPlane h)
	{
		hp = new HalfPlane(h);
		op = CSGOp.LEAF;
		c1 = null;
		c2 = null;
		comp = null;
		complexity = 1;
	}
	
	/**
	 * One off constructor for the universal and null sets
	 * @param b
	 */
	private CSG2D(boolean b)
	{
		hp = null;
		if(b)
			op = CSGOp.UNIVERSE;
		else
			op = CSGOp.NULL;
		c1 = null;
		c2 = null;
		comp = null;   // Resist temptation to be clever here
		complexity = 0;
	}
	
	/**
	 * Universal or null set
	 * @return universal or null set
	 */
	public static CSG2D universe()
	{
		return u;
	}
	
	/**
	 * @return nothing/null set
	 */
	public static CSG2D nothing()
	{
		return n;
	}
	
	/**
	 * Deep copy
	 * @param c
	 */
	public CSG2D(CSG2D c)
	{
		if(c == u || c == n)
			Debug.e("RrCSG deep copy: copying null or universal set.");
		
		if(c.hp != null)
			hp = new HalfPlane(c.hp);
		else
			hp = null;
		
		if(c.c1 != null)
			c1 = new CSG2D(c.c1);
		else
			c1 = null;
		
		if(c.c2 != null)
			c2 = new CSG2D(c.c2);
		else
			c2 = null;
		
		comp = null;  // This'll be built if it's needed
		
		op = c.op;
		complexity = c.complexity;
	}
	
	/**
	 * Compute a 2D slice of a 3D CSG at a given Z value
	 * @param t
	 * @param z
	 * @return
	 */
	public static CSG2D slice(CSG3D t, double z)
	{
		CSG2D r = new CSG2D();	
		
		switch(t.operator())
		{
		case LEAF:
			r.op = CSGOp.LEAF;
			r.complexity = 1;
			try 
			{
				r.hp = new HalfPlane(t.hSpace(), z);
			} catch (ParallelException e) 
			{
				if(t.hSpace().value(new Point3D(0,0,z)) <= 0)
					return universe();
				else
					return nothing();
			}
			break;
			
		case NULL:
				Debug.e("CSG2D constructor from CSG3D: null set in tree!");
			break;
			
		case UNIVERSE:
				Debug.e("CSG2D constructor from CSG3D: universal set in tree!");
			break;
			
		case UNION:  
			return CSG2D.union(slice(t.c_1(), z), slice(t.c_2(), z));
			
		case INTERSECTION:
			return CSG2D.intersection(slice(t.c_1(), z), slice(t.c_2(), z));			
			
		default:
			Debug.e("CSG2D constructor from CSG3D: invalid operator " + t.operator());
		}
		return r;
	}
	
	/**
	 * Get children, operator etc
	 * @return children
	 */
	public CSG2D c_1() { return c1; }
	public CSG2D c_2() { return c2; }
	public CSGOp operator() { return op; }
	public HalfPlane hPlane() { return hp; }
	public int complexity() { return complexity; }
	
	/**
	 * Convert to a string
	 * @param result
	 * @param white
	 * @return string representation
	 */
	private String toString_r(String result, String white)
	{
		switch(op)
		{
		case LEAF:
			result = result + white + hp.toString() + "\n";
			break;
			
		case NULL:
			result = result + white + "0\n";
			break;
			
		case UNIVERSE:
			result = result + white + "U\n";
			break;
			
		case UNION:
			result = result + white + "+\n";
			white = white + " ";
			result = c1.toString_r(result, white);
			result = c2.toString_r(result, white);
			break;
			
		case INTERSECTION:
			result = result + white + "&\n";
			white = white + " ";
			result = c1.toString_r(result, white);
			result = c2.toString_r(result, white);
			break;
			
		default:
			Debug.e("toString_r(): invalid operator.");
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		String result = "2D RrCSG: complexity = " + 
			Integer.toString(complexity) + "\n";
		result = toString_r(result, " ");
		return result;
	}
	
	/**
	 * Private constructor for common work setting up booleans
	 * @param a
	 * @param b
	 */
	private CSG2D(CSG2D a, CSG2D b)
	{
		hp = null;
		comp = null;
		if(a.complexity <= b.complexity) // So we know the 1st child is the simplest
		{
			c1 = a;
			c2 = b;
		} else
		{
			c1 = b;
			c2 = a;
		}
		complexity = c1.complexity + c2.complexity;
	}
	
	/**
	 * Boolean operations, with de Morgan simplifications
	 * @param a
	 * @param b
	 * @return union of passed CSG objects a and b
	 */
	public static CSG2D union(CSG2D a, CSG2D b)
	{
		if(a == b)
			return a;
		if(a.op == CSGOp.NULL)
			return b;
		if(b.op == CSGOp.NULL)
			return a;
		if((a.op == CSGOp.UNIVERSE) || (b.op == CSGOp.UNIVERSE))
			return universe();
		
		if(a.comp != null && b.comp != null)
			if(a.comp == b)
				return universe();
		
		CSG2D r = new CSG2D(a, b);
		r.op = CSGOp.UNION;
		return r;
	}
	
	/**
	 * Boolean operation to perform an intersection
	 * @param a
	 * @param b
	 * @return intersection of passed CSG objects a and b
	 */
	public static CSG2D intersection(CSG2D a, CSG2D b)
	{
		if(a == b)
			return a;
		if(a.op == CSGOp.UNIVERSE)
			return b;
		if(b.op == CSGOp.UNIVERSE)
			return a;
		if((a.op == CSGOp.NULL) || (b.op == CSGOp.NULL))
			return nothing();
		
		if(a.comp != null && b.comp != null)
			if(a.comp == b)
				return nothing();
		
		CSG2D r = new CSG2D(a, b);
		r.op = CSGOp.INTERSECTION;
		return r;
	}
	
	/**
	 * Lazy evaluation for complement.
	 * @return complement
	 */
	public CSG2D complement()
	{		
		if(comp != null)
			return comp;
		
		CSG2D result;
		
		switch(op)
		{
		case LEAF:
			result = new CSG2D(hp.complement());
			break;
			
		case NULL:
			return universe();
			
		case UNIVERSE:
			return nothing();
			
		case UNION:
			result = intersection(c1.complement(), c2.complement());
			break;
			
		case INTERSECTION:
			result = union(c1.complement(), c2.complement());
			break;
			
		default:
			Debug.e("complement(): invalid operator.");
			return nothing();
		}
		
		// Remember, so we don't have to do it again.
		// (I do hope that the Java garbage collector is up to 
		// spotting this deadly embrace, or we - I mean it - has
		// a memory leak.)
		// It turned out it was dumb.  Hence addition of destroy() above...
		
		comp = result;
		result.comp = this;
		
		return comp;
	}
	
	/**
	 * Set difference is intersection with complement
	 * @param a
	 * @param b
	 * @return set difference as CSG object based on input CSG objects a and b
	 */		
	public static CSG2D difference(CSG2D a, CSG2D b)
	{
		return intersection(a, b.complement());
	}
	
	/**
	 * Make a rectangle
	 */
	public static CSG2D RrCSGFromBox(Rectangle b)
	{
		CSG2D r = new CSG2D(new HalfPlane(b.nw(), b.ne()));
		r = CSG2D.intersection(r, new CSG2D(new HalfPlane(b.ne(), b.se())));
		r = CSG2D.intersection(r, new CSG2D(new HalfPlane(b.se(), b.sw())));
		r = CSG2D.intersection(r, new CSG2D(new HalfPlane(b.sw(), b.nw())));
		return r;
	}
	
	/**
	 * This takes a complicated expression assumed to contain multiple instances of leafA
	 * and returns the equivalent CSG expression involving just leafA.
	 * @param leafA
	 * @return equivalent CSG expression involving just leafA
	 */
	private CSG2D categorise(CSG2D leafA)
	{
		HalfPlane a = leafA.hPlane();
		Point2D an = a.normal();
		Point2D x = Point2D.add(a.pLine().origin(), an);
		if(value(x) <= 0)
			return leafA.complement();
		else
			return leafA;
	}
	
	/**
	 * This takes a complicated expression assumed to contain multiple instances of leafA
	 * and leafB and returns the equivalent CSG expression involving at most leafA and leafB once 
	 * (except for non-manifold shapes).
	 * @param leafA
	 * @param leafB
	 * @return equivalent CSG expression involving at most leafA and leafB once
	 */
	private CSG2D crossCategorise(CSG2D leafA, CSG2D leafB)
	{
		HalfPlane a = leafA.hPlane();
		HalfPlane b = leafB.hPlane();
		Point2D an = a.normal();
		Point2D bn = b.normal();
		Point2D v02 = Point2D.add(an, bn);
		Point2D v31 = Point2D.sub(bn, an);
		Point2D x, x0, x1, x2, x3;
		int category = 0;
		try
		{
			x = a.cross_point(b);
			v02 = v02.norm();
			v31 = v31.norm();
			x2 = Point2D.add(x, v02);
			x0 = Point2D.sub(x, v02);
			x1 = Point2D.add(x, v31);
			x3 = Point2D.sub(x, v31);
			if(value(x0) <= 0)
				category |= 1;
			if(value(x1) <= 0)
				category |= 2;
			if(value(x2) <= 0)
				category |= 4;
			if(value(x3) <= 0)
				category |= 8;
			
			switch(category)
			{
			case 0:
				return nothing();
			case 1:
				return intersection(leafA, leafB);
			case 2:
				return intersection(leafA, leafB.complement());
			case 3:
				return leafA;
			case 4:
				return intersection(leafA.complement(), leafB.complement());
			case 5:
				Debug.e("RrCSG crossCategorise: non-manifold shape (case 0101)!");
				return union(intersection(leafA, leafB), intersection(leafA.complement(), leafB.complement()));
			case 6:
				return leafB.complement();
			case 7:
				return union(leafA, leafB.complement());
			case 8:
				return intersection(leafA.complement(), leafB);
			case 9:
				return leafB;
			case 10:
				Debug.e("RrCSG crossCategorise: non-manifold shape (case 1010)!");
				return union(CSG2D.intersection(leafA.complement(), leafB), intersection(leafA, leafB.complement()));
			case 11:
				return union(leafA, leafB);
			case 12:
				return leafA.complement();
			case 13:
				return union(leafA.complement(), leafB);
			case 14:
				return union(leafA.complement(), leafB.complement());
			case 15:
				return universe();
			default:
				Debug.e("RrCSG crossCategorise: bitwise | doesn't seem to work...");
				return this;
			}
		} catch (Exception e)
		{
			// leafA and leafB are parallel
			
			x0 = Point2D.mul(Point2D.add(a.pLine().origin(), b.pLine().origin()), 0.5);
			x1 = Point2D.mul(Point2D.sub(a.pLine().origin(), b.pLine().origin()), 3);
			x2 = Point2D.add(x0, x1);
			x1 = Point2D.sub(x0, x1);
			if(value(x0) <= 0)
				category |= 1;
			if(value(x1) <= 0)
				category |= 2;
			if(value(x2) <= 0)
				category |= 4;
			
			if(leafA.value(x0) <= 0)
				leafA = leafA.complement();
			
			if(leafB.value(x0) <= 0)
				leafB = leafB.complement();			
			
			switch(category)
			{
			case 0:
				return nothing();
			case 1:
				return intersection(leafA.complement(), leafB.complement());
			case 2:
				return leafB;
			case 3:
				return leafA.complement();
			case 4:
				return leafA;
			case 5:
				return union(leafA, leafB);
			case 6:
				return leafB.complement();
			case 7:
				return universe();
			default:
				Debug.e("RrCSG crossCategorise: bitwise | doesn't seem to work...");
				return this;	
			}
		}


	}
	
	/**
	 * Run through a GSG expression looking at its leaves and return
	 * a list of the distinct leaves.  Note: leaf and leaf.complement() are
	 * not considered distinct.
	 * Recursive internal call.
	 * @param expression
	 * @return
	 */
	private void uniqueList_r(ArrayList<CSG2D> list)
	{
		switch(op)
		{
		case LEAF:
			CSG2D entry;
			for(int i = 0; i < list.size(); i++)
			{
				entry = list.get(i);
				if(this == entry || complement() == entry)
					return;
			}
			list.add(this);
			break;

		case NULL:			
		case UNIVERSE:
			Debug.e("uniqueList_r: null or universe at a leaf.");
			break;
			
		case UNION:
		case INTERSECTION:
			c1.uniqueList_r(list);
			c2.uniqueList_r(list);
			break;
			
		default:
			Debug.e("uniqueList_r: invalid operator.");
		}		
		
		return;
	}
	
	/**
	 * Run through a GSG expression looking at its leaves and return
	 * a list of the distinct leaves.  Note: leaf and leaf.complement() are
	 * not considered distinct.
	 * @param expression
	 * @return
	 */
	private ArrayList<CSG2D> uniqueList()
	{
		ArrayList<CSG2D> result = new ArrayList<CSG2D>();
		uniqueList_r(result);
		return result;
	}	
	

	
//	private RrCSG reg_3()
//	{
//		
//		RrCSG r = this;
//		
//		if(complexity != 3)
//			return r;
//		
//		RrCSG a = c1;
//		//RrCSG b = c2.c1;
//		//RrCSG c = c2.c2;
//		
//		RrCSG c = c2.c1;
//		RrCSG b = c2.c2;
//		
//		int caseVal = 0;
//		boolean noEquals = true;
//
//		if(a == b)
//			noEquals = false;
//		if(a == c)
//		{
//			noEquals = false;
//			caseVal |= 4;
//		}
//		if(b == c)
//		{
//			noEquals = false;
//			caseVal |= 2;
//			caseVal |= 4;
//		}
//		if(a == b.comp)
//		{
//			noEquals = false;
//			caseVal |= 1;
//		}
//		if(a == c.comp)
//		{
//			noEquals = false;
//			caseVal |= 1;
//			caseVal |= 4;
//		}
//		if(b == c.comp)
//		{
//			noEquals = false;
//			caseVal |= 1;
//			caseVal |= 2;
//			caseVal |= 4;
//		}
//		
//		if(noEquals)
//			return r;
//		
//		if(op == RrCSGOp.INTERSECTION)
//			caseVal |= 8;
//		if(c2.op == RrCSGOp.INTERSECTION)
//			caseVal |= 16;
//		
//		// The code in the following switch is automatically
//		// generated by the program CodeGenerator.java
//		
//		switch(caseVal)
//		{
//		case 0: 
//			// r = RrCSG.union(a, RrCSG.union(b, c));
//			// a = b ->
//
//			// a c 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.union(a, c);
//				break;
//
//			case 1: 
//			// r = RrCSG.union(a, RrCSG.union(b, c));
//			// a = !b ->
//
//			// a c 
//			// 0 0 | 1 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.universe();
//				break;
//
//			case 4: 
//			// r = RrCSG.union(a, RrCSG.union(b, c));
//			// a = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.union(a, b);
//				break;
//
//			case 5: 
//			// r = RrCSG.union(a, RrCSG.union(b, c));
//			// a = !c ->
//
//			// a b 
//			// 0 0 | 1 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.universe();
//				break;
//
//			case 6: 
//			// r = RrCSG.union(a, RrCSG.union(b, c));
//			// b = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.union(b, a);
//				break;
//
//			case 7: 
//			// r = RrCSG.union(a, RrCSG.union(b, c));
//			// b = !c ->
//
//			// a b 
//			// 0 0 | 1 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.universe();
//				break;
//
//			case 8: 
//			// r = RrCSG.union(a, RrCSG.intersection(b, c));
//			// a = b ->
//
//			// a c 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = a;
//				break;
//
//			case 9: 
//			// r = RrCSG.union(a, RrCSG.intersection(b, c));
//			// a = !b ->
//
//			// a c 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.union(a, c);
//				break;
//
//			case 12: 
//			// r = RrCSG.union(a, RrCSG.intersection(b, c));
//			// a = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = a;
//				break;
//
//			case 13: 
//			// r = RrCSG.union(a, RrCSG.intersection(b, c));
//			// a = !c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.union(a, b);
//				break;
//
//			case 14: 
//			// r = RrCSG.union(a, RrCSG.intersection(b, c));
//			// b = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 1 
//			// 1 1 | 1 
//				r = RrCSG.union(b, a);
//				break;
//
//			case 15: 
//			// r = RrCSG.union(a, RrCSG.intersection(b, c));
//			// b = !c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = a;
//				break;
//
//			case 16: 
//			// r = RrCSG.intersection(a, RrCSG.union(b, c));
//			// a = b ->
//
//			// a c 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = a;
//				break;
//
//			case 17: 
//			// r = RrCSG.intersection(a, RrCSG.union(b, c));
//			// a = !b ->
//
//			// a c 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = RrCSG.intersection(a, c);
//				break;
//
//			case 20: 
//			// r = RrCSG.intersection(a, RrCSG.union(b, c));
//			// a = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = a;
//				break;
//
//			case 21: 
//			// r = RrCSG.intersection(a, RrCSG.union(b, c));
//			// a = !c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = RrCSG.intersection(a, b);
//				break;
//
//			case 22: 
//			// r = RrCSG.intersection(a, RrCSG.union(b, c));
//			// b = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = RrCSG.intersection(b, a);
//				break;
//
//			case 23: 
//			// r = RrCSG.intersection(a, RrCSG.union(b, c));
//			// b = !c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 1 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = a;
//				break;
//
//			case 24: 
//			// r = RrCSG.intersection(a, RrCSG.intersection(b, c));
//			// a = b ->
//
//			// a c 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = RrCSG.intersection(a, c);
//				break;
//
//			case 25: 
//			// r = RrCSG.intersection(a, RrCSG.intersection(b, c));
//			// a = !b ->
//
//			// a c 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 0 
//				r = RrCSG.nothing();
//				break;
//
//			case 28: 
//			// r = RrCSG.intersection(a, RrCSG.intersection(b, c));
//			// a = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = RrCSG.intersection(a, b);
//				break;
//
//			case 29: 
//			// r = RrCSG.intersection(a, RrCSG.intersection(b, c));
//			// a = !c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 0 
//				r = RrCSG.nothing();
//				break;
//
//			case 30: 
//			// r = RrCSG.intersection(a, RrCSG.intersection(b, c));
//			// b = c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 1 
//				r = RrCSG.intersection(b, a);
//				break;
//
//			case 31: 
//			// r = RrCSG.intersection(a, RrCSG.intersection(b, c));
//			// b = !c ->
//
//			// a b 
//			// 0 0 | 0 
//			// 1 0 | 0 
//			// 0 1 | 0 
//			// 1 1 | 0 
//				r = RrCSG.nothing();
//				break;
//		default:
//			Debug.e("RrCSG.reg_3(): dud case value: " + caseVal);
//		}
//		
//		return r;
//	}
	
	
//	/**
//	 * FIXME: there's a bug in reg_4 somewhere.  So it's not called at present.
//	 * Regularise a set with a contents of 4
//	 * This assumes simplify has been run over the set
//	 * @return regularised CSG object
//	 */	
//	private RrCSG reg_4()
//	{            
//		RrCSG result = this;
//		
//		if(complexity != 4)
//			return result;
//		
//		RrCSG temp;	
//		
//		if(c1.complexity == 1)
//		{
//			temp = c2.reg_3();
//			if(temp.complexity <= 2)
//			{
//				if(op == RrCSGOp.UNION)
//					result = union(c1, temp).reg_3();
//				else
//					result = intersection(c1, temp).reg_3();
//			}else
//			{
//				// c1 can only equal at most one leaf of c2 as all three c2 leaves
//				// must be distinct because reg_3() didn't simplify c2.
//				
//				if(c1 == c2.c1)
//				{
//					result = c1;
//				}
//				else if(c1 == c2.c2.c1 || c1 == c2.c2.c2)
//				{
//					if(c1 == c2.c2.c2)
//						c2.c2.c2 = c2.c2.c1;
//					int ops = 0;
//					if(op == RrCSGOp.UNION)
//						ops++;
//					if(c2.op == RrCSGOp.UNION)
//						ops += 2;
//					if(c2.c2.op == RrCSGOp.UNION)
//						ops += 4;
//					switch(ops)
//					{
//					case 0:
//						result = c2;
//						break;
//					case 1:
//					case 6:
//						result = c1;
//						break;
//					case 2:
//					case 5:
//					case 7:
//						result.c2.c2 = c2.c2.c2;
//						break;                            
//					case 3:
//					case 4:
//						result.c2 = c2.c1;
//						break;
//					default:
//						Debug.e("reg_4() 1: addition doesn't work...");
//					}  
//				}
//			}
//		} else
//		{
//			int type = 0;
//			if(c1.c1 == c2.c1)
//				type++;
//			else if(c1.c1 == c2.c2)
//			{
//				type++;
//				temp = c2.c2;
//				c2.c2 = c2.c1;
//				c2.c1 = temp;
//			}
//			if(c1.c2 == c2.c2)
//				type++;
//			else if(c1.c2 == c2.c1)
//			{
//				type++;
//				temp = c1.c2;
//				c1.c2 = c1.c1;
//				c1.c1 = temp;
//			}
//			
//			int ops = 0;
//			if(op == RrCSGOp.UNION)
//				ops += 4;
//			if(c1.op == RrCSGOp.UNION)
//				ops++;
//			if(c2.op == RrCSGOp.UNION)
//				ops += 2;
//			
//			switch(type)
//			{
//			case 0:
//				break;
//				
//			case 1:
//				switch(ops)
//				{
//				case 0:
//					result = intersection(c1, c2.c2);
//					break;
//				case 1:
//					result = intersection(c1.c1, c2.c2);
//					break;
//				case 2:
//				case 5:
//					result = c1;
//				case 3:
//					result = union(c1.c1, intersection(c1.c2, c2.c2));
//					break;
//				case 4:
//					result = intersection(c1.c1, union(c1.c2, c2.c2));
//					break;
//				case 6:
//					result = union(c1.c1, c2.c2);
//					break;
//				case 7:
//					result = union(c1, c2.c2);
//					break;
//				default:
//					Debug.e("reg_4() 2: addition doesn't work...");
//				}
//				break;
//				
//			case 2:		// Pick the child that's an intersection (if there is one)		
//				if(c1.op == RrCSGOp.UNION)
//					result = c2;
//				else
//					result = c1;
//				break;
//				
//			default:
//				Debug.e("reg_4() 4: addition doesn't work...");
//			}
//		}
//		
//		return result;
//	}

	/**
	 * Regularise a set with simple contents ( <= 4 )
	 * This assumes simplify has been run over the set
	 * @return regularised CSG object
	 */	
	public CSG2D regularise()
	{	
		CSG2D r = this;

		if(complexity < 3 || complexity > 4)
			return r;

		ArrayList<CSG2D> list = uniqueList();
		if(list.size() == 1)
			return categorise(list.get(0));
		if(list.size() == 2)
			return crossCategorise(list.get(0), list.get(1));
		
		return r;
	}
	
	/**
	 * Force an approximation to the regulariseing of a set
	 * This assumes simplify has been run over the set
	 * @return regularised CSG object
	 */	
	public CSG2D forceRegularise()
	{	
		ArrayList<CSG2D> list = uniqueList();
		if(list.size() == 1)
			return categorise(list.get(0));
		
		return crossCategorise(list.get(0), list.get(1));
	}
	
	/**
	 * Regularise a set with simple contents ( < 4 )
	 * This assumes simplify has been run over the set
	 * @return regularised CSG object
	 */	
//	public RrCSG regularise()
//	{
//		RrCSG result = this;
//
//		switch(complexity)
//		{
//		case 0:
//		case 1:
//		case 2:
//			break;
//		case 3:
//			result = reg_3();
////			if(result.complexity < complexity)
////				System.out.println("regularise: \n" + toString() + " > " + 
////						result.toString());
//			break;
//			
//		case 4:
//			//result = reg_4();
////			if(result.complexity < complexity)
////				System.out.println("regularise: \n" + toString() + " > " + 
////						result.toString());
//			break;
//			
//		default:
//			Debug.e("regularise(): set too complicated.");
//		}
//		
//		return result;
//	}
	
	/**
	 * Replace duplicate of leaf with leaf itself
	 * TODO: this should also use known complements
	 * @param leaf
	 * @param tolerance
	 */		
	private void replaceAllSameLeaves(CSG2D leaf, double tolerance)
	{	
		int same;
		switch(op)
		{
		case LEAF:
		case NULL:   
		case UNIVERSE:
			//System.out.println("replace_all_same_leaves(): at a leaf!");
			break;
			
		case UNION:
		case INTERSECTION:    
			HalfPlane hp = leaf.hp;
			if(c1.op == CSGOp.LEAF)
			{
				same = HalfPlane.same(hp, c1.hp, tolerance);
				if(same == 0)
					c1 = leaf;
				if(same == -1)
					c1 = leaf.complement();
			} else
				c1.replaceAllSameLeaves(leaf, tolerance);
			
			if(c2.op == CSGOp.LEAF)
			{
				same = HalfPlane.same(hp, c2.hp, tolerance);
				if(same == 0)
					c2 = leaf;
				if(same == -1)
					c2 = leaf.complement();
			} else
				c2.replaceAllSameLeaves(leaf, tolerance);
			break;
			
		default:
			Debug.e("replace_all_same(): invalid operator.");		
		}
	}
	
	/**
	 * Replace duplicate of all leaves with the last instance of each; also
	 * link up complements.
	 * @param root
	 * @param tolerance
	 * @return simplified CSG object
	 */		
	private void simplify_r(CSG2D root, double tolerance)
	{
		switch(op)
		{
		case LEAF:
			root.replaceAllSameLeaves(this, tolerance);
			break;
			
		case NULL:   
		case UNIVERSE:
			//System.out.println("simplify_r(): at a leaf!");
			break;
			
		case UNION:
		case INTERSECTION:    
			c1.simplify_r(root, tolerance);
			c2.simplify_r(root, tolerance);
			break;
			
		default:
			Debug.e("simplify_r(): invalid operator.");
		
		}
	}
	
	/**
	 * Replace duplicate of all leaves with the last instance of each
	 * @param tolerance
	 * @return simplified CSG object
	 */		
	public CSG2D simplify(double tolerance)
	{
		if(this == u || this == n)
			return this;
		
		CSG2D root = new CSG2D(this);
		simplify_r(root, tolerance);
		return root;
	}
	
//	/**
//	 * For each half plane remove any existing crossing list.
//     */
//    public void clearCrossings()
//    {
//    	if(complexity() > 1)
//    	{
//    		c_1().clearCrossings();
//    		c_2().clearCrossings();
//    	} else
//    	{
//    		if(operator() == RrCSGOp.LEAF)
//    			plane().removeCrossings();
//    	}
//    }
    
    
//    /**
//	 * For each half plane sort the crossing list.
//     */
//    public void sortCrossings(boolean up, RrCSGPolygon q)
//    {
//    	if(complexity() > 1)
//    	{
//    		c_1().sortCrossings(up, q);
//    		c_2().sortCrossings(up, q);
//    	} else
//    	{
//    		if(operator() == RrCSGOp.LEAF)
//    			plane().sort(up, q);
//    	}	
//    }
	
	/**
	 * Offset by a distance (+ve or -ve)
	 * TODO: this should keep track of complements
	 * @param d
	 * @return offset CSG object by distance d
	 */
	public CSG2D offset(double d)
	{
		CSG2D result;
		
		switch(op)
		{
		case LEAF:
			result = new CSG2D(hp.offset(d));
			break;
			
		case NULL:
		case UNIVERSE:
			result = this;
			break;
			
		case UNION:
			result = union(c1.offset(d), c2.offset(d));
			break;
			
		case INTERSECTION:
			result = intersection(c1.offset(d), c2.offset(d));
			break;
			
		default:
			Debug.e("offset(): invalid operator.");
			result = nothing();
		}
		return result;
	}
	
	
	/**
	 * leaf find the half-plane that generates the value for a point
	 * @param p
	 * @return leaf?
	 */
	public CSG2D leaf(Point2D p)
	{
		CSG2D result, r1, r2;
		
		switch(op)
		{
		case LEAF:
			result = this;
			break;
			
		case NULL:
			result = this;
			break;
			
		case UNIVERSE:
			result = this;
			break;
			
		case UNION:
			r1 = c1.leaf(p);
			r2 = c2.leaf(p);
			if(r1.value(p) < r2.value(p))
				return r1;
			else
				return r2;
			
		case INTERSECTION:
			r1 = c1.leaf(p);
			r2 = c2.leaf(p);
			if(r1.value(p) > r2.value(p))
				return r1;
			else
				return r2;
			
		default:
			Debug.e("leaf(Rr2Point): invalid operator.");
			result = nothing();
		}
		return result;
	}
	
	/**
	 * "Potential" value of a point; i.e. a membership test
	 * -ve means inside; 0 means on the surface; +ve means outside
	 *
	 * @param p
	 * @return potential value of a point
	 */
	public double value(Point2D p)
	{
		double result = 1;
//		RrCSG c = leaf(p);
		switch(op)
		{
		case LEAF:
			result = hp.value(p);
			break;
			
		case NULL:
			result = 1;
			break;
			
		case UNIVERSE:
			result = -1;
			break;
			
		case UNION:
			result = Math.min(c1.value(p), c2.value(p));
			break;
			
		case INTERSECTION:
			result = Math.max(c1.value(p), c2.value(p));
			break;
			
		default:
			Debug.e("RrCSG.value(): dud operator.");
		}
		return result;
	}
	
//	/**
//	 * "Potential" value of a point; i.e. a membership test
//	 * -ve means inside; 0 means on the surface; +ve means outside
//	 * TODO - this should work independently of a call to leaf(); that's more efficient
//	 * @param p
//	 * @return value of a point
//	 */
//	public double value(Rr2Point p)
//	{
//		double result = 1;
//		RrCSG c = leaf(p);
//		switch(c.op)
//		{
//		case LEAF:
//			result = c.hp.value(p);
//			break;
//			
//		case NULL:
//			result = 1;
//			break;
//			
//		case UNIVERSE:
//			result = -1;
//			break;
//			
//		case UNION:
//		case INTERSECTION:
//			
//		default:
//			Debug.e("value(Rr2Point): non-leaf operator.");
//		}
//		return result;
//	}
	
	/**
	 * The interval value of a box (analagous to point)
	 * @param b
	 * @return value of a box
	 */
	public Interval value(Rectangle b)
	{
		Interval result;
		
		switch(op)
		{
		case LEAF:
			result = hp.value(b);
			break;
			
		case NULL:
			result = new Interval(1, 1.01);  // Is this clever?  Or dumb?
			break;
			
		case UNIVERSE:
			result = new Interval(-1.01, -1);  // Ditto.
			break;
			
		case UNION:
			result = Interval.min(c1.value(b), c2.value(b));
			break;
			
		case INTERSECTION:
			result = Interval.max(c1.value(b), c2.value(b));
			break;
			
		default:
			Debug.e("value(RrBox): invalid operator.");
			result = new Interval();
		}
		
		return result;
	}
	
	/**
	 * Prune the set to a box
	 * @param b
	 * @return pruned box as new CSG object
	 */
	public CSG2D prune(Rectangle b)
	{
		CSG2D result = this;
		
		switch(op)
		{
		case LEAF:            
			Interval i = hp.value(b);
			if (i.empty())
				Debug.e("RrCSG.prune(RrBox): empty interval!");
			else if(i.neg())
				result = universe();
			else if (i.pos())
				result = nothing();
			break;
			
		case NULL:
		case UNIVERSE:
			break;
			
		case UNION:
			result =  union(c1.prune(b), c2.prune(b));
			break;
			
		case INTERSECTION:
			result = intersection(c1.prune(b), c2.prune(b));
			break;
			
		default:
			Debug.e("RrCSG.prune(RrBox): dud op value!");
		}
		
		return result;
	}
}
