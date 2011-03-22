/*
 * Created on May 1, 2006
 *
 * Changed by Vik to reject polts of less than 0.05mm
 */
package org.reprap.geometry;

//import java.io.IOException;

import org.reprap.Printer;
import org.reprap.Attributes;
import org.reprap.Preferences;
//import org.reprap.ReprapException;
//import org.reprap.devices.pseudo.LinePrinter;
import org.reprap.geometry.polygons.Rr2Point;
//import org.reprap.geometry.polygons.RrCSGPolygonList;
import org.reprap.geometry.polygons.RrPolygon;
import org.reprap.geometry.polygons.RrPolygonList;
import org.reprap.geometry.polygons.RrRectangle;
import org.reprap.utilities.Debug;
import org.reprap.utilities.RrGraphics;

/**
 *
 */
class segmentSpeeds
{
	/**
	 * 
	 */
	public Rr2Point p1, p2, p3;
	
	/**
	 * 
	 */
	public double ca;
	
	/**
	 * 
	 */
	public boolean plotMiddle;
	
	/**
	 * 
	 */
	public boolean abandon;
	
	/**
	 * @param before
	 * @param now
	 * @param after
	 * @param fastLength
	 */
	public segmentSpeeds(Rr2Point before, Rr2Point now, Rr2Point after, double fastLength)
	{
		Rr2Point a = Rr2Point.sub(now, before);
		double amod = a.mod();
		abandon = amod == 0;
		if(abandon)
			return;
		Rr2Point b = Rr2Point.sub(after, now);
		if(b.mod() == 0)
			ca = 0;
		else
			ca = Rr2Point.mul(a.norm(), b.norm());
		plotMiddle = true;
		if(amod <= 2*fastLength)
		{
			fastLength = amod*0.5;
			plotMiddle = false;
		}
		a = a.norm();
		p1 = Rr2Point.add(before, Rr2Point.mul(a, fastLength));
		p2 = Rr2Point.add(p1, Rr2Point.mul(a, amod - 2*fastLength));
		p3 = Rr2Point.add(p2, Rr2Point.mul(a, fastLength));
	}
	
//	int speed(int currentSpeed, double angFac)
//	{
//		double fac = (1 - 0.5*(1 + ca)*angFac);
//		return LinePrinter.speedFix(currentSpeed, fac);
//	}
}

/**
 *
 */
public class LayerProducer {
	
	private RrGraphics simulationPlot = null;
	
	/**
	 * 
	 */
	private LayerRules layerConditions = null;
	
	/**
	 * 
	 */
	private boolean paused = false;


	
	private RrPolygonList allPolygons[];
	
	/**
	 * The clue is in the name...
	 */
	private double currentFeedrate;
	
	/**
	 * Record the end of each polygon as a clue where to start next
	 */
	private Rr2Point startNearHere = null;

	
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
		
		if(startNearHere != null)
			startNearHere.destroy();
		startNearHere = null;
		allPolygons = null;
		beingDestroyed = false;
	}
	
	
	/**
	 * Set up a normal layer
	 * @param boolGrdSliceols
	 * @param ls
	 * @param lc
	 * @param simPlot
	 * @throws Exception
	 */
	public LayerProducer(RrPolygonList ap[], LayerRules lc, RrGraphics simPlot) throws Exception 
	{
		layerConditions = lc;
		startNearHere = null;
		simulationPlot = simPlot;
		
		allPolygons = ap;
		
		if(simulationPlot != null)
		{
			if(!simulationPlot.isInitialised())
			{
				RrRectangle rec = lc.getBox();
				if(Preferences.loadGlobalBool("Shield"))
					rec.expand(Rr2Point.add(rec.sw(), new Rr2Point(-7, -7))); // TODO: Yuk - this should be a parameter
				simulationPlot.init(rec, false, lc.getModelLayer());
			} else
				simulationPlot.cleanPolygons(lc.getModelLayer());
		}
	}
	

	
	/**
	 * Stop printing
	 *
	 */
	public void pause()
	{
		paused = true;
	}
	
	/**
	 * Start printing
	 *
	 */
	public void resume()
	{
		paused = false;
	}
	
	/**
	 * @return current X and Y position of the printer
	 */
	private Rr2Point posNow()
	{
		return new Rr2Point(layerConditions.getPrinter().getX(), layerConditions.getPrinter().getY());
	}
	
	/**
	 * speed up for short lines
	 * @param p
	 * @return
	 * @throws Exception 
	 */
	private boolean shortLine(Rr2Point p, boolean stopExtruder, boolean closeValve) throws Exception
	{
		Printer printer = layerConditions.getPrinter();
		double shortLen = printer.getExtruder().getShortLength();
		if(shortLen < 0)
			return false;
		Rr2Point a = Rr2Point.sub(posNow(), p);
		double amod = a.mod();
		if(amod > shortLen) {
//			Debug.d("Long segment.  Current feedrate is: " + currentFeedrate);
			return false;
		}

		//printer.setFeedrate(printer.getExtruder().getShortLineFeedrate());
// TODO: FIX THIS
//		printer.setSpeed(LinePrinter.speedFix(printer.getExtruder().getXYSpeed(), 
//				printer.getExtruder().getShortSpeed()));
		printer.printTo(p.x(), p.y(), layerConditions.getMachineZ(), printer.getExtruder().getShortLineFeedrate(), stopExtruder, closeValve);
		//printer.setFeedrate(currentFeedrate);
		return true;	
	}
	
	/**
	 * @param first First point, the end of the line segment to be plotted to from the current position.
	 * @param second Second point, the end of the next line segment; used for angle calculations
	 * @param turnOff True if the extruder should be turned off at the end of this segment.
	 * @throws Exception 
	 */
	private void plot(Rr2Point first, Rr2Point second, boolean stopExtruder, boolean closeValve) throws Exception
	{
		Printer printer = layerConditions.getPrinter();
		if (printer.isCancelled()) return;
		
		// Don't call delay; this isn't controlling the printer
		while(paused)
		{
			try
			{
				Thread.sleep(200);
			} catch (Exception ex) {}
		}
		
		if(shortLine(first, stopExtruder, closeValve))
			return;
		
		double z = layerConditions.getMachineZ();
		
		double speedUpLength = printer.getExtruder().getAngleSpeedUpLength();
		if(speedUpLength > 0)
		{
			segmentSpeeds ss = new segmentSpeeds(posNow(), first, second, 
					speedUpLength);
			if(ss.abandon)
				return;

			printer.printTo(ss.p1.x(), ss.p1.y(), z, currentFeedrate, false, false);

			if(ss.plotMiddle)
			{
//TODO: FIX THIS.
//				int straightSpeed = LinePrinter.speedFix(currentSpeed, (1 - 
//						printer.getExtruder().getAngleSpeedFactor()));
				//printer.setFeedrate(printer.getExtruder().getAngleFeedrate());
				printer.printTo(ss.p2.x(), ss.p2.y(), z, printer.getExtruder().getAngleFeedrate(), false, false);
			}

			//printer.setSpeed(ss.speed(currentSpeed, printer.getExtruder().getAngleSpeedFactor()));
			
			//printer.setFeedrate(printer.getExtruder().getAngleFeedrate());
			printer.printTo(ss.p3.x(), ss.p3.y(), z, printer.getExtruder().getAngleFeedrate(), stopExtruder, closeValve);
			//pos = ss.p3;
		// Leave speed set for the start of the next line.
		} else
			printer.printTo(first.x(), first.y(), z, currentFeedrate, stopExtruder, closeValve);
	}
	
	private void singleMove(Rr2Point p)
	{
		Printer pt = layerConditions.getPrinter();
		pt.singleMove(p.x(), p.y(), pt.getZ(), pt.getFastXYFeedrate());
	}
	
	/**
	 * @param first
	 * @param second
	 * @param startUp
	 * @param endUp
	 * @throws Exception 
	 */
	private void move(Rr2Point first, Rr2Point second, boolean startUp, boolean endUp, boolean fast) 
		throws Exception
	{
		Printer printer = layerConditions.getPrinter();
		
		if (printer.isCancelled()) return;
		
//		 Don't call delay; this isn't controlling the printer
		while(paused)
		{
			try
			{
				Thread.sleep(200);
			} catch (Exception ex) {}
		}
		
		double z = layerConditions.getMachineZ();
		
		//if(startUp)
		if(fast)
		{
			//printer.setFeedrate(printer.getFastFeedrateXY());
			printer.moveTo(first.x(), first.y(), z, printer.getExtruder().getFastXYFeedrate(), startUp, endUp);
			return;
		}
		
		double speedUpLength = printer.getExtruder().getAngleSpeedUpLength();
		if(speedUpLength > 0)
		{
			segmentSpeeds ss = new segmentSpeeds(posNow(), first, second, 
					speedUpLength);
			if(ss.abandon)
				return;

			printer.moveTo(ss.p1.x(), ss.p1.y(), z, printer.getCurrentFeedrate(), startUp, startUp);

			if(ss.plotMiddle)
			{
				//printer.setFeedrate(currentFeedrate);
				printer.moveTo(ss.p2.x(), ss.p2.y(), z, currentFeedrate, startUp, startUp);
			}

			//TODO: FIX ME!
			//printer.setSpeed(ss.speed(currentSpeed, printer.getExtruder().getAngleSpeedFactor()));
			
			//printer.setFeedrate(printer.getExtruder().getAngleFeedrate());
			printer.moveTo(ss.p3.x(), ss.p3.y(), z, printer.getExtruder().getAngleFeedrate(), startUp, endUp);
			//pos = ss.p3;
			// Leave speed set for the start of the next movement.
		} else
			printer.moveTo(first.x(), first.y(), z, currentFeedrate, startUp, endUp);
	}


	/**
	 * Plot a polygon
	 * @return
	 * @throws Exception 
	 */
	private void plot(RrPolygon p, boolean firstOneInLayer) throws Exception
	{
		Attributes att = p.getAttributes();
		Printer printer = layerConditions.getPrinter();
		double outlineFeedrate = att.getExtruder().getOutlineFeedrate();
		double infillFeedrate = att.getExtruder().getInfillFeedrate();
		
		boolean acc = att.getExtruder().getMaxAcceleration() > 0; 
	
		if(p.size() <= 1)
		{
			//startNearHere = null;
			return;
		}

		// If the length of the plot is <0.05mm, don't bother with it.
		// This will not spot an attempt to plot 10,000 points in 1mm.
		double plotDist=0;
		Rr2Point lastPoint=p.point(0);
		for (int i=1; i<p.size(); i++)
		{
			Rr2Point n=p.point(i);
			plotDist+=Rr2Point.d(lastPoint, n);
			lastPoint=n;
		}
		if (plotDist<Preferences.machineResolution()*0.5) {
			Debug.d("Rejected line with "+p.size()+" points, length: "+plotDist);
			//startNearHere = null;
			return;
		}	
		
		if(firstOneInLayer)
			printer.forceNextExtruder();
		printer.selectExtruder(att);
		
		
		if (printer.isCancelled()) return;
		
		// If getMinLiftedZ() is negative, never lift the head
		
		double liftZ = att.getExtruder().getLift();
		Boolean lift = att.getExtruder().getMinLiftedZ() >= 0 || liftZ > 0;
		
		if(acc)
			p.setSpeeds(att.getExtruder().getSlowXYFeedrate(), p.isClosed()?outlineFeedrate:infillFeedrate, 
					att.getExtruder().getMaxAcceleration());
		
		double extrudeBackLength = att.getExtruder().getExtrusionOverRun();
		double valveBackLength = att.getExtruder().getValveOverRun();
		if(extrudeBackLength > 0 && valveBackLength > 0)
			Debug.e("LayerProducer.plot(): extruder has both valve backoff and extrude backoff specified.");

		p.backStepExtrude(extrudeBackLength);
		p.backStepValve(valveBackLength);
		
		double currentZ = printer.getZ();
		if(liftZ > 0)
			printer.singleMove(printer.getX(), printer.getY(), currentZ + liftZ, printer.getFastFeedrateZ());
	
		currentFeedrate = att.getExtruder().getFastXYFeedrate();
		singleMove(p.point(0));
		
		if(liftZ > 0)
			printer.singleMove(printer.getX(), printer.getY(), currentZ, printer.getFastFeedrateZ());
		
		if(acc)
			currentFeedrate = p.speed(0);
		else
		{
			if(p.isClosed())
			{
				currentFeedrate = outlineFeedrate;			
			} else
			{
				currentFeedrate = infillFeedrate;			
			}
		}
		
		plot(p.point(0), p.point(1), false, false);
		
		// Print any lead-in.
		printer.printStartDelay(firstOneInLayer);
		
		boolean extrudeOff = false;
		boolean valveOff = false;
		boolean oldexoff;
		
		for(int i = 1; i < p.size(); i++)
		{
			Rr2Point next = p.point((i+1)%p.size());

			if (printer.isCancelled())
			{
				printer.stopMotor();
				singleMove(posNow());
				move(posNow(), posNow(), lift, lift, true);
				return;
			}

			if(acc)
				currentFeedrate = p.speed(i);

			oldexoff = extrudeOff;
			extrudeOff = (i > p.extrudeEnd() && extrudeBackLength > 0) || i == p.size()-1;
			valveOff = (i > p.valveEnd() && valveBackLength > 0) || i == p.size()-1;			
			plot(p.point(i), next, extrudeOff, valveOff);
			if(oldexoff ^ extrudeOff)
				printer.printEndReverse();
		}

		
		if(p.isClosed())
			move(p.point(0), p.point(0), false, false, true);
			
		move(posNow(), posNow(), lift, lift, true);
		
		// The last point is near where we want to start next
		if(p.isClosed())
			startNearHere = p.point(0);	
		else
			startNearHere = p.point(p.size() - 1);
		
		if(simulationPlot != null)
		{
			RrPolygonList pgl = new RrPolygonList();
			pgl.add(p);
			simulationPlot.add(pgl);
		}
		
	}
			
	/**
	 * Master plot function - draw everything.  Supress border and/or hatch by
	 * setting borderPolygons and/or hatchedPolygons null
	 * @throws Exception 
	 */
	public void plot() throws Exception
	{
		boolean firstOneInLayer = true;
		
		for(int i = 0; i < allPolygons.length; i++)
		{
			firstOneInLayer = true;
			RrPolygonList pl = allPolygons[i];
			for(int j = 0; j < pl.size(); j++)
			{
				plot(pl.polygon(j), firstOneInLayer);
				firstOneInLayer = false;
			}
		}
	}		
	
}
