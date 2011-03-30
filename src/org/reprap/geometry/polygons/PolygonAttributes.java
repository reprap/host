package org.reprap.geometry.polygons;

/**
 * RepRap Attributes are characteristics of materials and objects made from them.
 * 
 * But sometimes you need to attach some information to an individual polygon to 
 * be plotted.  For example, you might want to know to plot it with less material than
 * normal so it tends to stretch out.
 * 
 * That's what this class is for.
 * 
 * @author ensab
 *
 */
public class PolygonAttributes 
{
	private double bridgeThin;  // Reduce filament feed by this for bridges.
	
	public PolygonAttributes()
	{
		bridgeThin = 1;
	}
	
	public PolygonAttributes(PolygonAttributes pa)
	{
		bridgeThin = pa.bridgeThin;
	}
	
	public double getBridgeThin() { return bridgeThin; }
	public void setBridgeThin(double bt) { bridgeThin = bt; }
	
}
