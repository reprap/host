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
 
 RrInterval: 1D intervals
 
 First version 20 May 2005
 This version: 1 May 2006 (Now in CVS - no more comments here)
 
 */

package org.reprap.geometry.polygons;

/**
 * Real 1D intervals
 */
public class RrInterval
{
	private double low;
	private double high;
	private boolean empty;
	
	/**
	 * Destroy me and all that I point to
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
	
	public RrInterval()
	{
		empty = true;
	}
	
	/**
	 * Two ends...
	 * @param l
	 * @param h
	 */
	public RrInterval(double l, double h)
	{
		low = l;
		high = h;
		empty = (low > high);
	}
	
	/**
	 * Deep copy
	 * @param i
	 */
	public RrInterval(RrInterval i)
	{
		low = i.low;
		high = i.high;
		empty = i.empty;
	}
	
	/**
	 * @return Return contents
	 */
	public double low() { return low; }
	public double high() { return high; }
	public boolean empty() { return empty; }
	
	/**
	 * The biggest possible
	 * @return biggest possible interval
	 */
	public static RrInterval bigInterval()
	{
		return new RrInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		if(empty)
			return "[empty]";
		return "[l:" + Double.toString(low) + ", h:" + Double.toString(high) + "]";
	}
	
	/**
	 * Accomodate v
	 * @param v
	 */
	public void expand(double v)
	{
		if(empty)
		{
			low = v;
			high = v;
		} else
		{
			if(v < low)
				low = v;
			if(v > high)
				high = v;
		}
	}
	
	/**
	 * Accommodate another interval
	 * @param i
	 */
	public void expand(RrInterval i)
	{
		expand(i.low);
		expand(i.high);
	}
	
	/**
	 * Size
	 * @return size
	 */
	public double length()
	{
		return high - low;
	}
	
	/**
	 * Middle
	 * @return
	 */
	public double cen()
	{
		return (high + low)*0.5;
	}	
	
	/**
	 * Interval addition
	 * @param a
	 * @param b
	 * @return new interval based on addition of intervals a and b
	 */
	public static RrInterval add(RrInterval a, RrInterval b)
	{
		if(a.empty || b.empty)
			System.err.println("add(...): adding empty interval(s).");	    
		return new RrInterval(a.low + b.low, a.high + b.high);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return new interval based on addition of interval a and value b
	 */
	public static RrInterval add(RrInterval a, double b)
	{
		if(a.empty)
			System.err.println("add(...): adding an empty interval.");	    
		return new RrInterval(a.low + b, a.high + b);
	}
	
	/**
	 * @param b
	 * @param a
	 * @return new interval based on addition of interval a and value b
	 */
	public static RrInterval add(double b, RrInterval a)
	{	    
		return add(a, b);
	}
	
	
	/**
	 * Interval subtraction
	 * @param a
	 * @param b
	 * @return new interval based on subtraction of interval a and value b
	 */
	public static RrInterval sub(RrInterval a, RrInterval b)
	{
		if(a.empty || b.empty)
			System.err.println("difference(...): subtracting empty interval(s).");
		return new RrInterval(a.low - b.high, a.high - b.low);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return new interval based on subtraction of interval a and value b
	 */
	public static RrInterval sub(RrInterval a, double b)
	{
		if(a.empty)
			System.err.println("difference(...): subtracting an empty interval.");
		return new RrInterval(a.low - b, a.high - b);
	}
	
	/**
	 * @param b
	 * @param a
	 * @return new interval based on subtraction of interval a and value b
	 */
	public static RrInterval sub(double b, RrInterval a)
	{
		if(a.empty)
			System.err.println("difference(...): subtracting an empty interval.");
		return new RrInterval(b - a.high, b - a.low);
	}   
	
	/**
	 * Interval multiplication
	 * @param a
	 * @param b
	 * @return new interval based on interval multiplication of intervals a and b
	 */
	public static RrInterval mul(RrInterval a, RrInterval b)
	{
		if(a.empty || b.empty)
			System.err.println("multiply(...): multiplying empty intervals.");
		double d = a.low*b.low;
		RrInterval r = new RrInterval(d, d);
		r.expand(a.low*b.high);
		r.expand(a.high*b.low);
		r.expand(a.high*b.high);
		return r;
	}
	
	/**
	 * @param a
	 * @param f
	 * @return new interval based on interval multiplication of interval a by factor f
	 */
	public static RrInterval mul(RrInterval a, double f)
	{
		if(a.empty)
			System.err.println("multiply(...): multiplying an empty interval.");
		if(f > 0)
			return new RrInterval(a.low*f, a.high*f);
		else
			return new RrInterval(a.high*f, a.low*f);	    
	}
	
	/**
	 * @param f
	 * @param a
	 * @return new interval based on interval multiplication of interval a by factor f
	 */
	public static RrInterval mul(double f, RrInterval a)
	{
		return mul(a, f);	    
	}
	
	/**
	 * Negative, zero, or positive?
	 * @return true if interval is negative (?)
	 */
	public boolean neg()
	{
		return high < 0;
	}
	
	/**
	 * @return true if intervale is positive (?)
	 */
	public boolean pos()
	{
		return low >= 0;
	}
	
	/**
	 * Does the interval _contain_ zero?
	 * @return true if zero is within the interval
	 */
	public boolean zero()
	{
		return(!neg() && !pos());
	}
	
	/**
	 * In or out
	 * @param v
	 * @return true if v is within the interval
	 */
	public boolean in(double v)
	{
		return v >= low && v <= high;
	}
	
	/**
	 * Identical within tolerance
	 * @param a
	 * @param b
	 * @param tolerance
	 * @return true if intervals a and b are identical within the tolerance
	 */
	public static boolean same(RrInterval a, RrInterval b, double tolerance)
	{
		if(a.empty() && b.empty())   //??? !!!
			return true;
		
		if( Math.abs(a.low - b.low) > tolerance)
			return false;
		if (Math.abs(a.high - b.high) > tolerance)
			return false;
		return true;
	}
	
	/**
	 * Absolute value of an interval
	 * @return absolute value of the interval
	 */
	public RrInterval abs()
	{
		RrInterval result = new RrInterval(this);
		double p;
		
		if (low < 0)
		{
			if (high <= 0)
			{
				result = new RrInterval(-high, -low);
			} else
			{
				result = new RrInterval(0, result.high);
				p = -low;
				if ( p > high ) result = new RrInterval(result.low, p);
			}
		}
		return(result);
	}
	
	/**
	 * Sign of an interval
	 * @param x
	 * @return sign of the interval
	 */
	public RrInterval sign()
	{
		return( new RrInterval(Math.signum(low), Math.signum(high)) );
	}	
	
	/**
	 * Max
	 * @param a
	 * @param b
	 * @return max value of the interval
	 */
	public static RrInterval max(RrInterval a, RrInterval b)
	{
		RrInterval result = new RrInterval(b);
		if (a.low > b.low) result = new RrInterval(a.low, result.high);
		if (a.high > b.high) result = new RrInterval(result.low, a.high);
		return(result);
	}
	
	/**
	 * Min
	 * @param a
	 * @param b
	 * @return minimal value of the interval
	 */
	public static RrInterval min(RrInterval a, RrInterval b)
	{
		RrInterval result = new RrInterval(b);
		if (a.low < b.low) result = new RrInterval(a.low, result.high);
		if (a.high < b.high) result = new RrInterval(result.low, a.high);
		return(result);
	}
	
	
	/**
	 * Intersection
	 * @param a
	 * @param b
	 * @return
	 */
	public static RrInterval intersection(RrInterval a, RrInterval b)
	{
		if(a.empty())
			return a;
		if(b.empty())
			return b;
		return new RrInterval(Math.max(a.low, b.low), Math.min(a.high, b.high));	
	}
	
	/**
	 * Union
	 * @param a
	 * @param b
	 * @return
	 */
	public static RrInterval union(RrInterval a, RrInterval b)
	{
		if(a.empty())
			return b;
		if(b.empty())
			return a;
		return new RrInterval(Math.min(a.low, b.low), Math.max(a.high, b.high));	
	}
}
