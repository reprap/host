package org.reprap.geometry.polygons;

import java.util.List;
import java.util.ArrayList;
import org.reprap.geometry.LayerRules;
import org.reprap.gui.STLObject;
import org.reprap.Attributes;
import org.reprap.Extruder;
import org.reprap.Preferences;
import org.reprap.utilities.Debug;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

/**
 * This class holds a list of STLObjects that represents everything that is to be built.
 * 
 * An STLObject may consist of items from several STL files, possible of different materials.
 * But they are all tied together relative to each other in space.
 * 
 * @author Adrian
 *
 */


public class AllSTLsToBuild 
{	
	/**
	 * 3D bounding box
	 * @author ensab
	 *
	 */
	class BoundingBox
	{
		private RrRectangle XYbox;
		private RrInterval Zint;
		
		public BoundingBox(Point3d p0)
		{	
			Zint = new RrInterval(p0.z, p0.z);
			XYbox = new RrRectangle(new RrInterval(p0.x, p0.x), new RrInterval(p0.y, p0.y));
		}
		
		public BoundingBox(BoundingBox b)
		{
			Zint = new RrInterval(b.Zint);
			XYbox = new RrRectangle(b.XYbox);
		}
		
		public void expand(Point3d p0)
		{
			Zint.expand(p0.z);
			XYbox.expand(new Rr2Point(p0.x, p0.y));
		}
		
		public void expand(BoundingBox b)
		{
			Zint.expand(b.Zint);
			XYbox.expand(b.XYbox);
		}
	}
	
	/**
	 * Line segment consisting of two points.
	 * @author Adrian
	 *
	 */
	class LineSegment
	{	
		/**
		 * The ends of the line segment
		 */
		public Rr2Point a = null, b = null;
		
		/**
		 * The attribute (i.e. RepRap material) of the segment.
		 */
		public Attributes att = null;

//		protected void finalize() throws Throwable
//		{
//			a = null;
//			b = null;
//			att = null;
//			super.finalize();
//		}
		
		/**
		 * Constructor takes two intersection points with an STL triangle edge.
		 * @param p
		 * @param q
		 */
		public LineSegment(Rr2Point p, Rr2Point q, Attributes at)
		{
			if(at == null)
				Debug.e("LineSegment(): null attributes!");
			a = p;
			b = q;
			att = at;
		}
	}
	
	/**
	 * Ring buffer cache to hold previously computed slices for doing 
	 * infill and support material calculations.
	 * @author ensab
	 *
	 */
	class SliceCache
	{
		private BooleanGridList[][] sliceRing;
		private BooleanGridList[][] supportRing;
		private int[] layerNumber;
		private int ringPointer;
		private final int noLayer = Integer.MIN_VALUE;
		private final int ringSize = 5;
		
		public SliceCache()
		{
			sliceRing = new BooleanGridList[ringSize][stls.size()];
			supportRing = new BooleanGridList[ringSize][stls.size()];
			layerNumber = new int[ringSize];
			ringPointer = 0;
			for(int layer = 0; layer < ringSize; layer++)
				for(int stl = 0; stl < stls.size(); stl++)
				{
					sliceRing[layer][stl] = null;
					supportRing[layer][stl] = null;
					layerNumber[layer] = noLayer;
				}
		}
		
		private int getTheRingLocationForWrite(int layer)
		{
			for(int i = 0; i < ringSize; i++)
				if(layerNumber[i] == layer)
					return i;

			int rp = ringPointer;
			for(int s = 0; s < stls.size(); s++)
			{
				sliceRing[rp][s] = null;
				supportRing[rp][s] = null;
			}
			ringPointer++;
			if(ringPointer >= ringSize)
				ringPointer = 0;
			return rp;
		}
		
		public void setSlice(BooleanGridList slice, int layer, int stl)
		{
			int rp = getTheRingLocationForWrite(layer);
			layerNumber[rp] = layer;
			sliceRing[rp][stl] = slice;
		}
		
		public void setSupport(BooleanGridList support, int layer, int stl)
		{
			int rp = getTheRingLocationForWrite(layer);
			layerNumber[rp] = layer;
			supportRing[rp][stl] = support;
		}
		
		private int getTheRingLocationForRead(int layer)
		{
			int rp = ringPointer;
			for(int i = 0; i < ringSize; i++)
			{
				rp--;
				if(rp < 0)
					rp = ringSize - 1;
				if(layerNumber[rp] == layer)
					return rp;
			}
			return -1;
		}
		
		public BooleanGridList getSlice(int layer, int stl)
		{
			int rp = getTheRingLocationForRead(layer);
			if(rp >= 0)
				return sliceRing[rp][stl];
			return null;
		}
		
		public BooleanGridList getSupport(int layer, int stl)
		{
			int rp = getTheRingLocationForRead(layer);
			if(rp >= 0)
				return supportRing[rp][stl];
			return null;
		}
	}
	
	/**
	 * The list of things to be built
	 */
	private List<STLObject> stls;
	
	/**
	 * A plan box round each item
	 */
	private List<RrRectangle> rectangles;
	
	/**
	 * New list of things to be built for reordering
	 */
	private List<STLObject> newstls;
	
	/**
	 * The XYZ box around everything
	 */
	//private RrRectangle XYbox;
	private BoundingBox XYZbox;
	
	/**
	 * The lowest and highest points
	 */
	private RrInterval Zrange;
	
	/**
	 * Is the list editable?
	 */
	private boolean frozen;
	
	/**
	 * Recently computed slices
	 */
	private SliceCache cache;
	
	/**
	 * Simple constructor
	 *
	 */
	public AllSTLsToBuild()
	{
		stls = new ArrayList<STLObject>();
		rectangles = null;
		newstls = null;
		XYZbox = null;
		Zrange = null;
		frozen = false;
		cache = null;
	}
	
	/**
	 * Add a new STLObject
	 * @param s
	 */
	public void add(STLObject s)
	{
		if(frozen)
			Debug.e("AllSTLsToBuild.add(): adding an item to a frozen list.");
		stls.add(s);
	}
	
	/**
	 * Add a new collection
	 * @param s
	 */
	public void add(AllSTLsToBuild a)
	{
		if(frozen)
			Debug.e("AllSTLsToBuild.add(): adding a collection to a frozen list.");
		for(int i = 0; i < a.size(); i++)
			stls.add(a.get(i));
	}
	
	/**
	 * Get the i-th STLObject
	 * @param i
	 * @return
	 */
	public STLObject get(int i)
	{
		return stls.get(i);
	}
	
	/**
	 * Delete an object
	 * @param i
	 */
	public void remove(int i)
	{
		if(frozen)
			Debug.e("AllSTLsToBuild.remove(): removing an item from a frozen list.");
		stls.remove(i);
	}
	
	/**
	 * Find an object in the list
	 * @param st
	 * @return
	 */
	private int findSTL(STLObject st)
	{
		if(size() <= 0)
		{
			Debug.e("AllSTLsToBuild.findSTL(): no objects to pick from!");
			return -1;			
		}
		int index = -1;
		for(int i = 0; i < size(); i++)
			if(get(i) == st)
			{
				index = i;
				break;
			}
		if(index < 0)
		{
			Debug.e("AllSTLsToBuild.findSTL(): dud object submitted.");
			return -1;
		}
		return index;
	}
	
	/**
	 * Find an object in the list and return the next one.
	 * @param st
	 * @return
	 */
	public STLObject getNextOne(STLObject st)
	{
		int index = findSTL(st);
		index++;
		if(index >= size())
			index = 0;
		return get(index);
	}
	
	/**
	 * Return the number of objects.
	 * @return
	 */
	public int size()
	{
		return stls.size();
	}
	
	/**
	 * Reorder the list under user control.  The user sends items from the
	 * old list one by one.  These are added to a new list in that order.  
	 * When there's only one left that is added last automatically.
	 * 
	 * Needless to say, this process must be carried through to completion.
	 * The function returns true while the process is ongoing, false when
	 * it's complete.
	 * 
	 * @param st
	 * @return
	 */
	public boolean reorderAdd(STLObject st)
	{
		if(frozen)
			Debug.d("AllSTLsToBuild.remove(): attempting to reorder a frozen list.");
		
		if(newstls == null)
			newstls = new ArrayList<STLObject>();
		
		int index = findSTL(st);
		newstls.add(get(index));
		stls.remove(index);
		
		if(stls.size() > 1)
			return true;
		
		newstls.add(get(0));
		stls = newstls;
		newstls = null;
		cache = null;  // Just in case...
		
		return false;
	}
	
	/**
	 * Freeze the list - no more editing.
	 * Also compute the XY box round everything.
	 * Also compute the individual plan boxes round each STLObject.
	 */
	private void freeze()
	{
		if(frozen)
			return;
		frozen = true;
		rectangles = new ArrayList<RrRectangle>();
		for(int i = 0; i < stls.size(); i++)
			rectangles.add(null);		
		if(cache == null)
			cache = new SliceCache();
		BoundingBox s;
		
		for(int i = 0; i < stls.size(); i++)
		{
			STLObject stl = stls.get(i);
			Transform3D trans = stl.getTransform();

			BranchGroup bg = stl.getSTL();
			java.util.Enumeration<?> enumKids = bg.getAllChildren();

			while(enumKids.hasMoreElements())
			{
				Object ob = enumKids.nextElement();

				if(ob instanceof BranchGroup)
				{
					BranchGroup bg1 = (BranchGroup)ob;
					Attributes att = (Attributes)(bg1.getUserData());
					if(XYZbox == null)
					{
						XYZbox = BBox(att.getPart(), trans);
						if(rectangles.get(i) == null)
							rectangles.set(i, new RrRectangle(XYZbox.XYbox));
						else
							rectangles.set(i, RrRectangle.union(rectangles.get(i), XYZbox.XYbox));
					} else
					{
						s = BBox(att.getPart(), trans);
						if(s != null)
						{
							XYZbox.expand(s);
							if(rectangles.get(i) == null)
								rectangles.set(i, new RrRectangle(s.XYbox));
							else
								rectangles.set(i, RrRectangle.union(rectangles.get(i), s.XYbox));
						}
					}
				}
			}
			if(rectangles.get(i) == null)
				Debug.e("AllSTLsToBuild:ObjectPlanRectangle(): object " + i + " is empty");
		}		
	}
	
	/**
	 * Run through a Shape3D and find its enclosing XYZ box
	 * @param shape
	 * @param trans
	 * @param z
	 */
	private BoundingBox BBoxPoints(Shape3D shape, Transform3D trans)
    {
		BoundingBox b = null;
        GeometryArray g = (GeometryArray)shape.getGeometry();
        Point3d p1 = new Point3d();
        Point3d q1 = new Point3d();
        
        if(g != null)
        {
            for(int i = 0; i < g.getVertexCount(); i++) 
            {
                g.getCoordinate(i, p1);
                trans.transform(p1, q1);
                if(b == null)
                	b = new BoundingBox(q1);
                else
                	b.expand(q1);
            }
        }
        return b;
    }
	
	/**
	 * Unpack the Shape3D(s) from value and find their exclosing XYZ box
	 * @param value
	 * @param trans
	 * @param z
	 */
	private BoundingBox BBox(Object value, Transform3D trans) 
    {
		BoundingBox b = null;
		BoundingBox s;
		
        if(value instanceof SceneGraphObject) 
        {
            SceneGraphObject sg = (SceneGraphObject)value;
            if(sg instanceof Group) 
            {
                Group g = (Group)sg;
                java.util.Enumeration<?> enumKids = g.getAllChildren( );
                while(enumKids.hasMoreElements())
                {
                	if(b == null)
                		b = BBox(enumKids.nextElement(), trans);
                	else
                	{
                		s = BBox(enumKids.nextElement(), trans);
                		if(s != null)
                			b.expand(s);
                	}
                }
            } else if (sg instanceof Shape3D) 
            {
                b = BBoxPoints((Shape3D)sg, trans);
            }
        }
        
        return b;
    }
	
	
	/**
	 * Return the XY box round everything
	 * @return
	 */
	public RrRectangle ObjectPlanRectangle()
	{
		freeze();
		return XYZbox.XYbox;
	}
	
	/**
	 * Find the top of the highest object.
	 * Calling this freezes the list.
	 * @return
	 */
	public double maxZ()
	{
		freeze();
		return XYZbox.Zint.high();
	}
	
	/**
	 * Stitch together the some of the edges to form a polygon.
	 * @param edges
	 * @return
	 */
	private RrPolygon getNextPolygon(ArrayList<LineSegment> edges)
	{
		if(!frozen)
		{
			Debug.e("AllSTLsToBuild:getNextPolygon() called for an unfrozen list!");
			freeze();
		}
		if(edges.size() <= 0)
			return null;
		LineSegment next = edges.get(0);
		edges.remove(0);
		RrPolygon result = new RrPolygon(next.att, true);
		result.add(next.a);
		result.add(next.b);
		Rr2Point start = next.a;
		Rr2Point end = next.b;
		
		boolean first = true;
		while(edges.size() > 0)
		{
			double d2 = Rr2Point.dSquared(start, end);
			if(first)
				d2 = Math.max(d2, 1);
			first = false;
			boolean aEnd = false;
			int index = -1;
			for(int i = 0; i < edges.size(); i++)
			{
				double dd = Rr2Point.dSquared(edges.get(i).a, end);
				if(dd < d2)
				{
					d2 = dd;
					aEnd = true;
					index = i;
				}
				dd = Rr2Point.dSquared(edges.get(i).b, end);
				if(dd < d2)
				{
					d2 = dd;
					aEnd = false;
					index = i;
				}
			}

			if(index >= 0)
			{
				next = edges.get(index);
				edges.remove(index);
				int ipt = result.size() - 1;
				if(aEnd)
				{
					result.set(ipt, Rr2Point.mul(Rr2Point.add(next.a, result.point(ipt)), 0.5));
					result.add(next.b);
					end = next.b;
				} else
				{
					result.set(ipt, Rr2Point.mul(Rr2Point.add(next.b, result.point(ipt)), 0.5));
					result.add(next.a);
					end = next.a;				
				}
			} else
				return result;
		}
		
		Debug.d("AllSTLsToBuild.getNextPolygon(): exhausted edge list!");
		
		return result;
	}
	
	/**
	 * Get all the polygons represented by the edges.
	 * @param edges
	 * @return
	 */
	private RrPolygonList simpleCull(ArrayList<LineSegment> edges)
	{
		if(!frozen)
		{
			Debug.e("AllSTLsToBuild:simpleCull() called for an unfrozen list!");
			freeze();
		}
		RrPolygonList result = new RrPolygonList();
		RrPolygon next = getNextPolygon(edges);
		while(next != null)
		{
			if(next.size() >= 3)
				result.add(next);
			next = getNextPolygon(edges);
		}
		
		return result;
	}
	
	/**
	 * Compute the support hatching polygons for this set of patterns
	 * @param stl
	 * @param layerConditions
	 * @return
	 */
	public RrPolygonList computeSupport(int stl, LayerRules layerConditions)
	{
		// No more additions or movements, please
		
		freeze();
		
		// We start by computing the union of everything in this layer because
		// that is everywhere that support _isn't_ needed.
		// We give the union the attribute of the first thing found, though
		// clearly it will - in general - represent many different substances.
		// But it's only going to be subtracted from other shapes, so what it's made
		// from doesn't matter.
		
		int layer = layerConditions.getMachineLayer();
		BooleanGridList thisLayer = slice(stl, layerConditions.getModelLayer(), layerConditions);
		
		BooleanGrid unionOfThisLayer;
		Attributes a;
		if(thisLayer.size() > 0)
		{
			unionOfThisLayer = thisLayer.get(0);
			a = unionOfThisLayer.attribute();
		}else
		{
			a = stls.get(stl).attributes(0);
			unionOfThisLayer = BooleanGrid.nullBooleanGrid();
		}
		for(int i = 1; i < thisLayer.size(); i++)
			unionOfThisLayer = BooleanGrid.union(unionOfThisLayer, thisLayer.get(i), a);
		
		// Expand the union of this layer a bit, so that any support is a little clear of 
		// this layer's boundaries.
		
		BooleanGridList allThis = new BooleanGridList();
		allThis.add(unionOfThisLayer);
		allThis = allThis.offset(layerConditions, true, -3);  // -3 is a bit of a hack...
		
		if(allThis.size() > 0)
			unionOfThisLayer = allThis.get(0);
		else
			unionOfThisLayer = BooleanGrid.nullBooleanGrid();

		// Get the layer above and union it with this layer.  That's what needs
		// support on the next layer down.
		
		BooleanGridList previousSupport = cache.getSupport(layer+1, stl);
		cache.setSupport(BooleanGridList.unions(previousSupport, thisLayer), layer, stl);
		
		// Now we subtract the union of this layer from all the stuff requiring support in the layer above.
		
		BooleanGridList support = new BooleanGridList();
	
		if(previousSupport != null)
		{
			for(int i = 0; i < previousSupport.size(); i++)
			{
				BooleanGrid above = previousSupport.get(i);
				a = above.attribute();
				if(a.getExtruder().getSupportExtruder() != null)
					support.add(BooleanGrid.difference(above, unionOfThisLayer, a));
			}
			support = support.unionDuplicates();
		}
		
		// Now force the attributes of the support pattern to be the support extruders
		// for all the materials in it.
		
		for(int i = 0; i < support.size(); i++)
		{
			Extruder e = support.attribute(i).getExtruder().getSupportExtruder();
			if(e == null)
			{
				Debug.e("AllSTLsToBuild.computeSupport(): null support extruder specified!");
				continue;
			}
			support.get(i).forceAttribute(new Attributes(e.getMaterial(), null, null, e.getAppearance()));
		}
		
		// Finally compute the support hatch.
		
		return support.hatch(layerConditions, false); //, null);
	}
	
	/**
	 * Compute the infill hatching polygons for this set of patterns
	 * @param stl
	 * @param layerConditions
	 * @param startNearHere
	 * @return
	 */
	public RrPolygonList computeInfill(int stl, LayerRules layerConditions) //, Rr2Point startNearHere)
	{
		// No more additions or movements, please
		
		freeze();
		
		// Where are we and what does the current slice look like?
		
		int layer = layerConditions.getMachineLayer();
		BooleanGridList slice = slice(stl, layerConditions.getModelLayer(), layerConditions);
		
		// If we are solid but the slices around us weren't, we need some fine infill as
		// we are (at least partly) surface
		
		BooleanGridList adjacentSlices = slice(stl, layer+1, layerConditions);
		adjacentSlices = BooleanGridList.intersections(slice(stl, layer+2, layerConditions), adjacentSlices);
		//adjacentSlices = BooleanGridList.intersections(slice(stl, layer+3, layerConditions), adjacentSlices);
		BooleanGridList justBelow = slice(stl, layer-1, layerConditions);
		adjacentSlices = BooleanGridList.intersections(justBelow, adjacentSlices);
		adjacentSlices = BooleanGridList.intersections(slice(stl, layer-2, layerConditions), adjacentSlices);
		//adjacentSlices = BooleanGridList.intersections(slice(stl, layer-3, layerConditions), adjacentSlices);
		BooleanGridList insides = null;
		
		// The insides are the bits that aren't surface.
		// The outsides are the bits that are left when the insides are subtracted from the total.
		// We grow the outsides just into the insides (nowhere else) to
		// ensure that they go a little way into the inside infill.
		
		BooleanGridList outsides = slice;
		if(adjacentSlices != null && layerConditions.getModelLayer() > 1)
		{
			insides = BooleanGridList.intersections(slice, adjacentSlices);
			outsides = BooleanGridList.differences(slice, adjacentSlices);
			outsides = outsides.offset(layerConditions, false, -2);
			outsides = BooleanGridList.intersections(outsides, slice);				
		}
			
		outsides = outsides.offset(layerConditions, false, 1);
		
		if(insides != null)
		{
			insides = insides.offset(layerConditions, false, 1);
			insides = BooleanGridList.differences(insides, outsides);
		}
		RrPolygonList hatchedPolygons = outsides.hatch(layerConditions, true);
			
		if(insides != null)
			hatchedPolygons.add(insides.hatch(layerConditions, false));
		
		return hatchedPolygons;
	}
	
	/**
	 * Compute the polygon to lay down for the machine to wipe its nose on.
	 * @param a
	 * @return
	 */
	public RrPolygon shieldPolygon(Attributes a)
	{
		RrRectangle rr = ObjectPlanRectangle();
		Rr2Point corner = Rr2Point.add(rr.sw(), new Rr2Point(-3, -3));
		RrPolygon ell = new RrPolygon(a, false);
		ell.add(corner);
		ell.add(Rr2Point.add(corner, new Rr2Point(0, 10)));
		ell.add(Rr2Point.add(corner, new Rr2Point(-2, 10)));
		ell.add(Rr2Point.add(corner, new Rr2Point(-2, -2)));
		ell.add(Rr2Point.add(corner, new Rr2Point(20, -2)));
		ell.add(Rr2Point.add(corner, new Rr2Point(20, 0)));
		ell.add(corner);
		return ell;
	}
	
	/**
	 * Compute the outline polygons for this set of patterns.
	 * @param layerConditions
	 * @param hatchedPolygons
	 * @param shield
	 * @return
	 */
	public RrPolygonList computeOutlines(int stl, LayerRules layerConditions, RrPolygonList hatchedPolygons, boolean shield)
	{
		// No more additions or movements, please
		
		freeze();	
		
		// The shapes to outline.
		
		BooleanGridList slice = slice(stl, layerConditions.getModelLayer(), layerConditions);
		
		RrPolygonList borderPolygons;
		
		// Are we building the raft under things?  If so, there is no border.
		
		if(layerConditions.getLayingSupport())
		{
			borderPolygons = null;
		} else
		{
			BooleanGridList offBorder = slice.offset(layerConditions, true, 1);
			borderPolygons = offBorder.borders();
		}


		// If we've got polygons to plot, amend them so they start in the middle 
		// of a hatch (this gives cleaner boundaries).  Also add the nose-wipe shield
		// if it's been asked for.
		
		if(borderPolygons != null && borderPolygons.size() > 0)
		{
			borderPolygons.middleStarts(hatchedPolygons, layerConditions, slice);
			try
			{
				if(shield && Preferences.loadGlobalBool("Shield"))
					borderPolygons.add(0, shieldPolygon(borderPolygons.polygon(0).getAttributes()));
			} catch (Exception ex)
			{}
		}
		
		return borderPolygons;
	}

	
	/**
	 * Generate a set of pixel-map representations, one for each extruder, for
	 * STLObject stl at height z.
	 * 
	 * @param stl
	 * @param z
	 * @param extruders
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private BooleanGridList slice(int stl, int layer, LayerRules layerRules)
	{
		if(!frozen)
		{
			Debug.e("AllSTLsToBuild.slice() called when unfrozen!");
			freeze();
		}
		
		if(layer < 0)
			return new BooleanGridList();
		
		// Is the result in the cache?  If so, just use that.
		
		BooleanGridList result = cache.getSlice(layer, stl);
		if(result != null)
			return result;
		
		// Haven't got it in the cache, so we need to compute it
		
		// Anything there?
		
		if(rectangles.get(stl) == null)
			return new BooleanGridList();
		
		// Probably...
		
		double z = layerRules.getModelZ(layer) + layerRules.getZStep()*0.5;
		Extruder[] extruders = layerRules.getPrinter().getExtruders();
		result = new BooleanGridList();
		RrCSG csgp = null;
		RrPolygonList pgl = new RrPolygonList();
		int extruderID;
		
		// Bin the edges by extruder ID.
		
		ArrayList<LineSegment>[] edges = new ArrayList[extruders.length];
		
		for(extruderID = 0; extruderID < extruders.length; extruderID++)
		{
			if(extruders[extruderID].getID() != extruderID)
				Debug.e("AllSTLsToBuild.slice(): extruder " + extruderID + "out of sequence: " + extruders[extruderID].getID());
			edges[extruderID] = new ArrayList<LineSegment>();
		}
		
		// Generate all the edges for STLObject i at this z
		
		STLObject stlObject = stls.get(stl);
		Transform3D trans = stlObject.getTransform();

		BranchGroup bg = stlObject.getSTL();
		java.util.Enumeration<?> enumKids = bg.getAllChildren();

		while(enumKids.hasMoreElements())
		{
			Object ob = enumKids.nextElement();

			if(ob instanceof BranchGroup)
			{
				BranchGroup bg1 = (BranchGroup)ob;
				Attributes attr = (Attributes)(bg1.getUserData());
				recursiveSetEdges(attr.getPart(), trans, z, attr, edges);
			}
		}

		// Turn them into lists of polygons, one for each extruder, then
		// turn those into pixelmaps.
		
		for(extruderID = 0; extruderID < edges.length; extruderID++)
		{
			pgl = simpleCull(edges[extruderID]);
			
			if(pgl.size() > 0)
			{
				// Remove wrinkles

				pgl = pgl.simplify(Preferences.gridRes()*1.5);

				// Fix small radii

				pgl = pgl.arcCompensate();

				csgp = pgl.toCSG(Preferences.tiny());

				// We use the plan rectangle of the entire stl object to store the bitmap, even though this slice may be
				// much smaller than the whole.  This allows booleans on slices to be computed much more
				// quickly as each is in the same rectangle so the bit patterns match exactly.  But it does use more memory.
				
				result.add(new BooleanGrid(csgp, rectangles.get(stl), pgl.polygon(0).getAttributes()));
			}
		}
		
		// We may need this later...
		
		cache.setSlice(result, layer, stl);
		
		return result;
	}

	
	public void destroyLayer() {}
	
	/**
	 * Add the edge where the plane z cuts the triangle (p, q, r) (if it does).
	 * Also update the triangulation of the object below the current slice used
	 * for the simulation window.
	 * @param p
	 * @param q
	 * @param r
	 * @param z
	 */
	private void addEdge(Point3d p, Point3d q, Point3d r, double z, Attributes att, ArrayList<LineSegment> edges[])
	{
		Point3d odd = null, even1 = null, even2 = null;
		int pat = 0;
		//boolean twoBelow = false;
		
		if(p.z < z)
			pat = pat | 1;
		if(q.z < z)
			pat = pat | 2;
		if(r.z < z)
			pat = pat | 4;
		
		switch(pat)
		{
		// All above
		case 0:
			return;
			
		// All below
		case 7:
			return;
			
		// q, r below, p above	
		case 6:
			//twoBelow = true;
		// p below, q, r above
		case 1:
			odd = p;
			even1 = q;
			even2 = r;
			break;
			
		// p, r below, q above	
		case 5:
			//twoBelow = true;
		// q below, p, r above	
		case 2:
			odd = q;
			even1 = r;
			even2 = p;
			break;

		// p, q below, r above	
		case 3:
			//twoBelow = true;
		// r below, p, q above	
		case 4:
			odd = r;
			even1 = p;
			even2 = q;
			break;
			
		default:
			Debug.e("addEdge(): the | function doesn't seem to work...");
		}
		
		// Work out the intersection line segment (e1 -> e2) between the z plane and the triangle
		
		even1.sub((Tuple3d)odd);
		even2.sub((Tuple3d)odd);
		double t = (z - odd.z)/even1.z;	
		Rr2Point e1 = new Rr2Point(odd.x + t*even1.x, odd.y + t*even1.y);	
		//Point3d e3_1 = new Point3d(e1.x(), e1.y(), z);
		//e1 = new Rr2Point(toGrid(e1.x()), toGrid(e1.y()));
		e1 = new Rr2Point(e1.x(), e1.y());
		t = (z - odd.z)/even2.z;
		Rr2Point e2 = new Rr2Point(odd.x + t*even2.x, odd.y + t*even2.y);
		//Point3d e3_2 = new Point3d(e2.x(), e2.y(), z);
		//e2 = new Rr2Point(toGrid(e2.x()), toGrid(e2.y()));
		e2 = new Rr2Point(e2.x(), e2.y());
		
		// Too short?
		if(!Rr2Point.same(e1, e2, Preferences.lessGridSquare()))
			edges[att.getExtruder().getID()].add(new LineSegment(e1, e2, att));
	}
	

	
	/**
	 * Run through a Shape3D and set edges from it at plane z
	 * Apply the transform first
	 * @param shape
	 * @param trans
	 * @param z
	 */
	private void addAllEdges(Shape3D shape, Transform3D trans, double z, Attributes att, ArrayList<LineSegment> edges[])
    {
        GeometryArray g = (GeometryArray)shape.getGeometry();
        Point3d p1 = new Point3d();
        Point3d p2 = new Point3d();
        Point3d p3 = new Point3d();
        Point3d q1 = new Point3d();
        Point3d q2 = new Point3d();
        Point3d q3 = new Point3d();
        
        if(g.getVertexCount()%3 != 0)
        {
        	Debug.e("addAllEdges(): shape3D with vertices not a multiple of 3!");
        }
        if(g != null)
        {
            for(int i = 0; i < g.getVertexCount(); i+=3) 
            {
                g.getCoordinate(i, p1);
                g.getCoordinate(i+1, p2);
                g.getCoordinate(i+2, p3);
                trans.transform(p1, q1);
                trans.transform(p2, q2);
                trans.transform(p3, q3);
                addEdge(q1, q2, q3, z, att, edges);
            }
        }
    }
	
	/**
	 * Unpack the Shape3D(s) from value and set edges from them
	 * @param value
	 * @param trans
	 * @param z
	 */
	private void recursiveSetEdges(Object value, Transform3D trans, double z, Attributes att, ArrayList<LineSegment> edges[]) 
    {
        if(value instanceof SceneGraphObject) 
        {
            SceneGraphObject sg = (SceneGraphObject)value;
            if(sg instanceof Group) 
            {
                Group g = (Group)sg;
                java.util.Enumeration<?> enumKids = g.getAllChildren( );
                while(enumKids.hasMoreElements())
                    recursiveSetEdges(enumKids.nextElement(), trans, z, att, edges);
            } else if (sg instanceof Shape3D) 
            {
                addAllEdges((Shape3D)sg, trans, z, att, edges);
            }
        }
    }

}
