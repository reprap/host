package org.reprap.geometry.polygons;

import java.util.ArrayList;
import java.util.List;
import org.reprap.Attributes;
import org.reprap.Extruder;
import org.reprap.geometry.LayerRules;
import org.reprap.utilities.Debug;

/**
 * Class to hold a list of BooleanGrids with associated atributes for each
 * @author ensab
 *
 */

public class BooleanGridList 
{

		private List<BooleanGrid> shapes = null;
		
//		protected void finalize() throws Throwable
//		{
//			shapes = null;
//			super.finalize();
//		}
		
		public BooleanGridList()
		{
			shapes = new ArrayList<BooleanGrid>();
		}
		
		/**
		 * Deep copy
		 * @param a
		 */
		public BooleanGridList(BooleanGridList a)
		{
			shapes = new ArrayList<BooleanGrid>();
			for(int i = 0; i < a.size(); i++)
				shapes.add(new BooleanGrid(a.get(i)));
		}
		
		/**
		 * Return the ith shape
		 * @param i
		 * @return
		 */
		public BooleanGrid get(int i)
		{
			return shapes.get(i);
		}
		
		/**
		 * Is a point in any of the shapes?
		 * @param p
		 * @return
		 */
		public boolean membership(Rr2Point p)
		{
			for(int i = 0; i < size(); i++)
				if(get(i).get(p))
					return true;
			return false;
		}
		
		/**
		 * Return the ith attribute
		 * @param i
		 * @return
		 */
		public Attributes attribute(int i)
		{
			return shapes.get(i).attribute();
		}
		
		/**
		 * How many shapes are there in the list?
		 * @return
		 */
		public int size()
		{
			return shapes.size();
		}
		
		/**
		 * Remove an entry and close the gap
		 * @param i
		 */
		public void remove(int i)
		{
			shapes.remove(i);
		}

		
		/**
		 * Add a shape on the end
		 * @param p
		 */
		public void add(BooleanGrid b)
		{
			if(b == null)
				Debug.e("BooleanGridList.add(): attempt to add null BooleanGrid.");
			if(b != BooleanGrid.nullBooleanGrid())
				shapes.add(b);
		}
		
		/**
		 * Add another list of shapes on the end
		 * @param a
		 */
		public void add(BooleanGridList aa)
		{
			for(int i = 0; i < aa.size(); i++)
					add(aa.get(i));
		}
		
		/**
		 * Offset all the shapes in the list for this layer
		 * @param lc
		 * @param outline
		 * @param multiplier
		 * @return
		 */
		public BooleanGridList offset(LayerRules lc, boolean outline, double multiplier)
		{
			boolean foundation = lc.getLayingSupport();
			if(outline && foundation)
				Debug.e("Offsetting a foundation outline!");
			
			BooleanGridList result = new BooleanGridList();
			for(int i = 0; i < size(); i++)
			{
				Attributes att = attribute(i);
				if(att == null)
					Debug.e("BooleanGridList.offset(): null attribute!");
				else
				{
					Extruder [] es = lc.getPrinter().getExtruders();
					Extruder e;
					int shells;
					if(foundation)
					{
						e = es[0];  // By convention extruder 0 builds the foundation
						shells = 1;
					} else
					{
						e = att.getExtruder();
						shells = e.getShells();					
					}
					if(outline)
					{
						int shell = 0;
						boolean carryOn = true;
						while(carryOn && shell < shells)
						{
							BooleanGrid thisOne = get(i).offset(-multiplier*((double)shell + 0.5)*e.getExtrusionSize());
							if(thisOne.isEmpty())
								carryOn = false;
							else
								result.add(thisOne);
							shell++;
						}
					} else
					{
						// Must be a hatch.  Only do it if the gap is +ve or we're building the foundation
						double offSize;
						int ei = e.getInfillExtruderNumber();
						Extruder ife = e;
						if(ei >= 0)
							ife = es[ei];
						if(foundation)
							offSize = 3;
						else
							offSize = -multiplier*((double)shells + 0.5)*e.getExtrusionSize() + ife.getInfillOverlap();
						if (e.getExtrusionInfillWidth() > 0 || foundation)  // Z value doesn't matter here
								result.add(get(i).offset(offSize));
					}
				}
			}
			return result;			
		}
		
		/**
		 * Work out all the polygons forming a set of borders
		 * @return
		 */
		public RrPolygonList borders()
		{
			RrPolygonList result = new RrPolygonList();
			for(int i = 0; i < size(); i++)
				result.add(get(i).allPerimiters(attribute(i))); 
			return result;
		}
		
		/**
		 * Work out all the open polygond forming a set of infill hatches.  If surface
		 * is true, these polygone are on the outside (top or bottom).  If it's false
		 * they are in the interior.
		 * @param layerConditions
		 * @param surface
		 * @return
		 */
		public RrPolygonList hatch(LayerRules layerConditions, boolean surface) //, Rr2Point startNearHere)
		{
			RrPolygonList result = new RrPolygonList();
			boolean foundation = layerConditions.getLayingSupport();
			Extruder [] es = layerConditions.getPrinter().getExtruders();
			for(int i = 0; i < size(); i++)
			{
				Extruder e;
				Attributes att = attribute(i);
				if(foundation)
					e = es[0]; // Extruder 0 is used for foundations
				else
					e = att.getExtruder();
				Extruder ei;
				if(!surface)
				{
					ei = e.getInfillExtruder();
					if(ei != null)
						att = new Attributes(ei.getMaterial(), null, null, ei.getAppearance());
				} else
					ei = e;
				if(ei != null)
					result.add(get(i).hatch(layerConditions.getHatchDirection(ei), layerConditions.getHatchWidth(ei), att)); //, startNearHere)); 
			}	
			return result;
		}
		
		/**
		 * Run through the list, unioning entries in it that share the same material so that
		 * the result has just one entry per material.
		 */
		public BooleanGridList unionDuplicates()
		{
			BooleanGridList result = new BooleanGridList();

			if(size() <= 0)
				return result;
			
			if(size() == 1)
				return this;

			boolean[] usedUp = new boolean[size()];
			for(int i = 0; i < usedUp.length; i++)
				usedUp[i] = false;

			for(int i = 0; i < size() - 1; i++)
			{
				if(!usedUp[i])
				{
					BooleanGrid union = get(i);
					int iExId = union.attribute().getExtruder().getID();
					for(int j = i+1; j < size(); j++)
					{
						if(!usedUp[j])
						{
							BooleanGrid jg = get(j);
							if(iExId == jg.attribute().getExtruder().getID())
							{
								union = BooleanGrid.union(union, jg);
								usedUp[j] = true;
							}
						}
					}
					result.add(union);
				}
			}
			
			if(!usedUp[size() - 1])
				result.add(get(size() - 1));

			return result;
		}
		
		
		/**
		 * Return a list of unions between the entries in a and b.
		 * Only pairs with the same extruder are unioned.  If an element
		 * of a has no corresponding element in b, or vice versa, then 
		 * those elements are returned unmodified in the result.
		 * @param a
		 * @param b
		 * @return
		 */
		public static BooleanGridList unions(BooleanGridList a, BooleanGridList b)
		{
			BooleanGridList result = new BooleanGridList();
			
			if(a == b)
				return a;
			if(a == null)
				return b;
			if(a.size() <= 0)
				return b;
			if(b == null)
				return a;
			if(b.size() <= 0)
				return a;
			
			boolean[] bMatched = new boolean[b.size()];
			for(int i = 0; i < bMatched.length; i++)
				bMatched[i] = false;
			
			for(int i = 0; i < a.size(); i++)
			{
				BooleanGrid abg = a.get(i);
				boolean aMatched = false;
				for(int j = 0; j < b.size(); j++)
				{
					if(abg.attribute().getExtruder().getID() == b.attribute(j).getExtruder().getID())
					{
						result.add(BooleanGrid.union(abg, b.get(j)));
						bMatched[j] = true;
						aMatched = true;
						break;
					}
				}
				if(!aMatched)
					result.add(abg);
			}
			
			for(int i = 0; i < bMatched.length; i++)
				if(!bMatched[i])
					result.add(b.get(i));
			
			return result.unionDuplicates();
		}
		
		/**
		 * Return a list of intersections between the entries in a and b.
		 * Only pairs with the same extruder are intersected.  If an element
		 * of a has no corresponding element in b, or vice versa, then no entry is returned
		 * for them.
		 * @param a
		 * @param b
		 * @return
		 */
		public static BooleanGridList intersections(BooleanGridList a, BooleanGridList b)
		{
			BooleanGridList result = new BooleanGridList();
			if(a == b)
				return a;
			if(a == null || b == null)
				return result;
			if(a.size() <= 0  || b.size() <= 0)
				return result;
			
			for(int i = 0; i < a.size(); i++)
			{
				BooleanGrid abg = a.get(i);
				for(int j = 0; j < b.size(); j++)
				{
					if(abg.attribute().getExtruder().getID() == b.attribute(j).getExtruder().getID())
					{
						result.add(BooleanGrid.intersection(abg, b.get(j)));	
						break;
					}
				}
			}
			return result.unionDuplicates();
		}
		
		
		/**
		 * Return a list of differences between the entries in a and b.
		 * Only pairs with the same attribute are intersected.  If an element
		 * of a has no corresponding element in b, then an entry equal to a is returned
		 * for that.
		 * @param a
		 * @param b
		 * @return
		 */
		public static BooleanGridList differences(BooleanGridList a, BooleanGridList b)
		{
			BooleanGridList result = new BooleanGridList();
			
			if(a == b)
				return result;
			if(a == null)
				return result;
			if(a.size() <= 0)
				return result;
			if(b == null)
				return a;
			if(b.size() <= 0)
				return a;
			
			for(int i = 0; i < a.size(); i++)
			{
				BooleanGrid abg = a.get(i);
				boolean aMatched = false;
				for(int j = 0; j < b.size(); j++)
				{
					if(abg.attribute().getExtruder().getID() == b.attribute(j).getExtruder().getID())
					{
						result.add(BooleanGrid.difference(abg, b.get(j)));
						aMatched = true;
						break;
					}
				}
				if(!aMatched)
					result.add(abg);
					
			}
			return result.unionDuplicates();
		}
		

}
