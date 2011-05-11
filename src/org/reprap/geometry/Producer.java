package org.reprap.geometry;

import javax.swing.JCheckBoxMenuItem;
import org.reprap.Preferences;
import org.reprap.Printer;
import org.reprap.Extruder;
import org.reprap.Attributes;
import org.reprap.geometry.polygons.Rr2Point;
import org.reprap.geometry.polygons.RrRectangle;
import org.reprap.geometry.polygons.AllSTLsToBuild;
import org.reprap.geometry.polygons.RrPolygonList;
import org.reprap.geometry.polygons.RrPolygon;
import org.reprap.geometry.polygons.RrCSG;
import org.reprap.geometry.polygons.BooleanGrid;
import org.reprap.geometry.polygons.BooleanGridList;
import org.reprap.gui.RepRapBuild;
import org.reprap.utilities.Debug;
import org.reprap.utilities.RrGraphics;

public class Producer {
	
	private boolean paused = false;
	
	//private LayerProducer layer = null;
	
	protected LayerRules layerRules = null;
	
	private RrGraphics simulationPlot = null;
	
	
	/**
	 * The list of objects to be built
	 */
	protected RepRapBuild bld;
	
//	protected boolean interLayerCooling;

	//protected STLSlice stlc;
	protected AllSTLsToBuild allSTLs;
	
	/**
	 * @param preview
	 * @param builder
	 * @throws Exception
	 */
	public Producer(Printer pr, RepRapBuild builder) throws Exception 
	{
		bld = builder;
		
		allSTLs = bld.getSTLs();
		
		RrRectangle gp = allSTLs.ObjectPlanRectangle();
		gp = new RrRectangle(new Rr2Point(gp.x().low() - 6, gp.y().low() - 6), 
		new Rr2Point(gp.x().high() + 6, gp.y().high() + 6));
		if(Preferences.simulate())
		{
			simulationPlot = new RrGraphics("RepRap building simulation");
		} else
			simulationPlot = null;
		
		double modZMax = allSTLs.maxZ();
		double stepZ = pr.getExtruders()[0].getExtrusionHeight();
		int foundationLayers = Math.max(0, pr.getFoundationLayers());
		
		int modLMax = (int)(modZMax/stepZ);
		
		layerRules = new LayerRules(pr, modZMax, modZMax + foundationLayers*stepZ,
				modLMax, modLMax + foundationLayers, true, gp);
	}
	
	/**
	 * Set the source checkbox used to determine if there should
	 * be a pause between segments.
	 * 
	 * @param segmentPause The source checkbox used to determine
	 * if there should be a pause.  This is a checkbox rather than
	 * a boolean so it can be changed on the fly. 
	 */
	public void setSegmentPause(JCheckBoxMenuItem segmentPause) {
		layerRules.getPrinter().setSegmentPause(segmentPause);
	}

	/**
	 * Set the source checkbox used to determine if there should
	 * be a pause between layers.
	 * 
	 * @param layerPause The source checkbox used to determine
	 * if there should be a pause.  This is a checkbox rather than
	 * a boolean so it can be changed on the fly.
	 */
	public void setLayerPause(JCheckBoxMenuItem layerPause) {
		layerRules.getPrinter().setLayerPause(layerPause);
	}
	
	public void setCancelled(boolean c)
	{
		layerRules.getPrinter().setCancelled(c);
	}
	
	public void pause()
	{
		paused = true;
//		if(layer != null)
//			layer.pause();
	}
	
	public void resume()
	{
		paused = false;
//		if(layer != null)
//			layer.resume();
	}
	
	/**
	 * NB - this does not call wait - this is a purely interactive function and
	 * does not control the machine
	 *
	 */
	private void waitWhilePaused()
	{
		while(paused)
		{
			try
			{
				Thread.sleep(200);
			} catch (Exception ex) {}
		}
	}
	
	public int getLayers()
	{
		return layerRules.getMachineLayerMax();
	}
	
	public int getLayer()
	{
		return layerRules.getMachineLayer();
	}	
	
	public void produce() throws Exception
	{
//		RrRectangle gp = layerRules.getBox();
		
//		gp = new RrRectangle(new Rr2Point(gp.x().low() - 6, gp.y().low() - 6), 
//				new Rr2Point(gp.x().high() + 6, gp.y().high() + 6));
		
		
		layerRules.getPrinter().startRun(layerRules);
		
		if(Preferences.Subtractive())
			produceSubtractive();
		else
		{
			if(layerRules.getTopDown())
				produceAdditiveTopDown();
			else
				Debug.e("Producer.produce(): bottom-up builds no longer supported.");
				//produceAdditiveGroundUp(gp);
		}
	}

	private void fillFoundationRectangle(Printer reprap, RrRectangle gp) throws Exception
	{
		RrPolygonList shield = new RrPolygonList();
		Extruder e = reprap.getExtruder();
		Attributes fa = new Attributes(e.getMaterial(), null, null, e.getAppearance());
//		if(Preferences.loadGlobalBool("Shield")) // Should the foundation have a shield, or not?
//			shield.add(allSTLs.shieldPolygon(fa));
		RrCSG rect = RrCSG.RrCSGFromBox(gp);
		BooleanGrid bg = new BooleanGrid(rect, gp.scale(1.1), fa);
		RrPolygonList h[] = {shield, bg.hatch(layerRules.getHatchDirection(e), layerRules.getHatchWidth(e), bg.attribute())};
		LayerProducer lp = new LayerProducer(h, layerRules, simulationPlot);
		lp.plot();
		reprap.getExtruder().stopExtruding();
		//reprap.setFeedrate(reprap.getFastFeedrateXY());
	}
	
//	private void layFoundationGroundUp(RrRectangle gp) throws Exception
//	{
//		if(layerRules.getFoundationLayers() <= 0)
//			return;
//		
//		Printer reprap = layerRules.getPrinter();
//
//		while(layerRules.getMachineLayer() < layerRules.getFoundationLayers()) 
//		{
//			
//			if (reprap.isCancelled())
//				break;
//			waitWhilePaused();
//			
//			Debug.d("Commencing foundation layer at " + layerRules.getMachineZ());
//
//			reprap.startingLayer(layerRules);
//			// Change Z height
//			//reprap.singleMove(reprap.getX(), reprap.getY(), layerRules.getMachineZ(), reprap.getFastFeedrateZ());
//			fillFoundationRectangle(reprap, gp);
//			reprap.finishedLayer(layerRules);
//			reprap.betweenLayers(layerRules);
//			layerRules.stepMachine(reprap.getExtruder());
//		}
//		layerRules.setLayingSupport(false);
//	}
	
	private void layFoundationTopDown(RrRectangle gp) throws Exception
	{
		if(layerRules.getFoundationLayers() <= 0)
			return;
		
		layerRules.setLayingSupport(true);
		layerRules.getPrinter().setSeparating(false);
		
		Printer reprap = layerRules.getPrinter();
		
		while(layerRules.getMachineLayer() >= 0) 
		{
			
			if (reprap.isCancelled())
				break;
			waitWhilePaused();
			
			Debug.d("Commencing foundation layer at " + layerRules.getMachineZ());

			reprap.startingLayer(layerRules);
			// Change Z height
			//reprap.singleMove(reprap.getX(), reprap.getY(), layerRules.getMachineZ(), reprap.getFastFeedrateZ());
			fillFoundationRectangle(reprap, gp);		
			reprap.finishedLayer(layerRules);
			reprap.betweenLayers(layerRules);
			layerRules.stepMachine(reprap.getExtruder());
		}
	}
	

	
	/**
	 * @throws Exception
	 */
	private void produceAdditiveTopDown() throws Exception 
	{		
		bld.mouseToWorld();
		
		Printer reprap = layerRules.getPrinter();
		
		layerRules.setLayingSupport(false);
		
		//BooleanGridList slice, previousSlice;
		
		int lastExtruder = -1;
		int totalPhysicalExtruders = 0;
		for(int extruder = 0; extruder < reprap.getExtruders().length; extruder++)
		{
			int thisExtruder = reprap.getExtruders()[extruder].getPhysicalExtruderNumber();
			if(thisExtruder > lastExtruder)
			{
				totalPhysicalExtruders++;
				if(thisExtruder - lastExtruder != 1)
				{
					Debug.e("Producer.produceAdditiveTopDown(): Physical extruders out of sequence: " + 
							lastExtruder + " then " + thisExtruder);
					Debug.e("(Physical extruder addresses should be sequential (or equal) starting at 0.)");
				}
				lastExtruder = thisExtruder;				
			}
		}
		
		RrPolygonList allPolygons[] = new RrPolygonList[totalPhysicalExtruders];
		RrPolygonList tempPolygons[] = new RrPolygonList[totalPhysicalExtruders];
		
		boolean firstTimeRound = true;
		
		while(layerRules.getModelLayer() >= 0 ) 
		{
			if(layerRules.getModelLayer() == 0)
				reprap.setSeparating(true);
			else
				reprap.setSeparating(false);
			
			if (reprap.isCancelled())
				break;
			
			waitWhilePaused();
			
			Debug.d("Commencing model layer " + layerRules.getModelLayer() + " at " + layerRules.getMachineZ());
			
			reprap.startingLayer(layerRules);
			
			reprap.waitWhileBufferNotEmpty();
			reprap.slowBuffer();
			

			for(int physicalExtruder = 0; physicalExtruder < allPolygons.length; physicalExtruder++)
				allPolygons[physicalExtruder] = new RrPolygonList();
			
			boolean shield = true;
			Rr2Point startNearHere = new Rr2Point(0, 0);
			for(int stl = 0; stl < allSTLs.size(); stl++)
			{
					RrPolygonList fills = allSTLs.computeInfill(stl, layerRules);
					RrPolygonList borders = allSTLs.computeOutlines(stl, layerRules, fills, shield);
					fills = fills.cullShorts();
					shield = false;
					RrPolygonList support = allSTLs.computeSupport(stl, layerRules);
					/*
				    borders = borders.nearEnds(startNearHere, false, -1);
					
					if(borders.size() > 0)
					{
						RrPolygon last = borders.polygon(borders.size() - 1);
						startNearHere = last.point(last.size() - 1);
					}
					fills = fills.nearEnds(startNearHere, false, -1);
					if(fills.size() > 0)
					{
						RrPolygon last = fills.polygon(fills.size() - 1);
						startNearHere = last.point(last.size() - 1);
					}
					support = support.nearEnds(startNearHere, false, -1);
					if(support.size() > 0)
					{
						RrPolygon last = support.polygon(support.size() - 1);
						startNearHere = last.point(last.size() - 1);
					}
					*/
					for(int physicalExtruder = 0; physicalExtruder < allPolygons.length; physicalExtruder++)
						tempPolygons[physicalExtruder] = new RrPolygonList();
					for(int pol = 0; pol < borders.size(); pol++)
					{
						//shield = false;
						RrPolygon p = borders.polygon(pol);
						tempPolygons[p.getAttributes().getExtruder().getPhysicalExtruderNumber()].add(p);
					}
					for(int pol = 0; pol < fills.size(); pol++)
					{
						//shield = false;
						RrPolygon p = fills.polygon(pol);
						tempPolygons[p.getAttributes().getExtruder().getPhysicalExtruderNumber()].add(p);
					}
					for(int pol = 0; pol < support.size(); pol++)
					{
						//shield = false;
						RrPolygon p = support.polygon(pol);
						tempPolygons[p.getAttributes().getExtruder().getPhysicalExtruderNumber()].add(p);
					}
					for(int physicalExtruder = 0; physicalExtruder < allPolygons.length; physicalExtruder++)
					{
						if(tempPolygons[physicalExtruder].size() > 0)
						{
							double linkUp = tempPolygons[physicalExtruder].polygon(0).getAttributes().getExtruder().getExtrusionSize();
							linkUp = (4*linkUp*linkUp);
							tempPolygons[physicalExtruder].radicalReOrder(linkUp);
							tempPolygons[physicalExtruder] = tempPolygons[physicalExtruder].nearEnds(startNearHere, false, -1);
							if(tempPolygons[physicalExtruder].size() > 0)
							{
								RrPolygon last = tempPolygons[physicalExtruder].polygon(tempPolygons[physicalExtruder].size() - 1);
								startNearHere = last.point(last.size() - 1);
							}
							allPolygons[physicalExtruder].add(tempPolygons[physicalExtruder]);
						}
					}
			}
			
			
			LayerProducer lp = new LayerProducer(allPolygons, layerRules, simulationPlot);
			layerRules.setFirstAndLast(allPolygons);
			lp.plot();

			reprap.finishedLayer(layerRules);
			reprap.betweenLayers(layerRules);
			
			if(firstTimeRound)
			{
				reprap.setTop(reprap.getX(), reprap.getY(), reprap.getZ());
				firstTimeRound = false;
			}

			allSTLs.destroyLayer();

			layerRules.step(reprap.getExtruder());
			
		}
		
		layFoundationTopDown(layerRules.getBox());
		
		reprap.terminate();
	}
	

	private void produceSubtractive() throws Exception 
	{
		Debug.e("Need to implement the Producer.produceSubtractive() function... :-)");
	}

	/**
	 * The total distance moved is the total distance extruded plus 
	 * plus additional movements of the extruder when no materials 
	 * was deposited
	 * 
	 * @return total distance the extruder has moved 
	 */
	public double getTotalDistanceMoved() {
		return layerRules.getPrinter().getTotalDistanceMoved();
	}
	
	/**
	 * @return total distance that has been extruded in millimeters
	 */
	public double getTotalDistanceExtruded() {
		return layerRules.getPrinter().getTotalDistanceExtruded();
	}
	
	/**
	 * TODO: This figure needs to get added up as we go along to allow for different extruders
	 * @return total volume that has been extruded
	 */
	public double getTotalVolumeExtruded() {
		return layerRules.getPrinter().getTotalDistanceExtruded() * layerRules.getPrinter().getExtruder().getExtrusionHeight() * 
		layerRules.getPrinter().getExtruder().getExtrusionSize();
	}
	
	/**
	 * 
	 */
	public void dispose() {
		layerRules.getPrinter().dispose();
	}

	/**
	 * @return total elapsed time in seconds between start and end of building the 3D object
	 */
	public double getTotalElapsedTime() {
		return layerRules.getPrinter().getTotalElapsedTime();
	}
	
}
