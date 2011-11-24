package org.reprap.pcb;

import org.reprap.geometry.polygons.*;
import org.reprap.utilities.RrGraphics;
import org.reprap.Preferences;
import org.reprap.Extruder;
import java.util.Iterator;
import java.util.LinkedList;

//import GerberGCode.Aperture;

public class GerberGCode {
	
	private class Aperture
	{
		int num;
		double width, height;
		char type;
		
		public Aperture(int num, double width, char type)
		{
			this.num = num;
			this.width = width;
			this.height = width;
			this.type = type;
		}
		
		public Aperture(int num, double width, double height, char type)
		{
			this.num = num;
			this.width = width;
			this.height = height;
			this.type = type;
		}
		
	}
	
	boolean dawingOn = false;
	LinkedList <Aperture> apertures = new LinkedList<Aperture>(); 
	Aperture curAperture = null;
	boolean absolute = true;
	Extruder pcbPen;
	BooleanGrid pcb;
	Point2D lastCoords = null;

	boolean colour = true;

	
	public GerberGCode(Extruder pp, BooleanGrid p, boolean c) 
	{
		pcbPen = pp;
		enableAbsolute();
		disableDrawing();
		pcb = p;
		colour = c;
		lastCoords = new Point2D(0, 0);
	}
	
	public Rectangle drawLine(Point2D c)
	{
		return drawFatLine(fixCoords(c));
	}	
	
	public void goTo(Point2D c)
	{
		lastCoords = new Point2D(fixCoords(c));
	}
	
	public void enableAbsolute()
	{
		absolute = true;
	}
	
	public void enableRelative()
	{
		absolute = false;
	}
	
	public void selectAperture(int aperture)
	{
		Iterator<Aperture> itr = apertures.iterator();
		Aperture cur;
		
		while(itr.hasNext())
		{
			cur = itr.next();
			if(cur.num == aperture)
			{
				curAperture = cur;
				break;
			}
		}
	}
	
	public void addCircleAperture(int apertureNum, double width)
	{
		apertures.add(new Aperture(apertureNum, width, 'C'));
	}
	
	public void addRectangleAperture(int apertureNum, double width, double height)
	{
		apertures.add(new Aperture(apertureNum, width, height, 'R'));
	}
	
	public Rectangle exposePoint(Point2D c)
	{
		if(curAperture.type == 'C')
			return createCircle(fixCoords(c));
		else
			return createRec(fixCoords(c));
		
	}
	
	public Rectangle createRec(Point2D c)
	{	
		//TODO: make this fill the rectangle
		double recWidth = curAperture.width/2.0f;
		double recHeight = curAperture.height/2.0f;
		c = fixCoords(c);
		Point2D p = new Point2D(recWidth, recHeight);
		Rectangle result = new Rectangle(Point2D.sub(c, p), Point2D.add(c, p));
		if(pcb == null)
			return result;
		pcb.homogeneous(result.sw(), result.ne(), colour);
		lastCoords = new Point2D(fixCoords(c));
		return result;
	}
	
	public Rectangle createCircle(Point2D c)
	{
		Rectangle result = circleToRectangle(c);
		if(pcb == null)
			return result;
		pcb.disc(c, curAperture.width*0.5, colour);
		lastCoords = new Point2D(fixCoords(c));
		//octagon(fixCoords(c), curAperture.width);
		return result;
	}
	

//	private void addPointToPolygons(Rr2Point c)
//	{		
//		if(currentPolygon == null && dawingOn)
//		{
//			currentPolygon = new RrPolygon(new Attributes(null, null, null, looksLike), false);
//			currentPolygon.add(new Rr2Point(c));
//		} else if(!dawingOn)
//		{
//			if(currentPolygon != null)
//				if(currentPolygon.size() > 1)
//					thePattern.add(new RrPolygon(currentPolygon));
//			currentPolygon = new RrPolygon(new Attributes(null, null, null, looksLike), false);
//			currentPolygon.add(new Rr2Point(c));
//		} else
//			currentPolygon.add(new Rr2Point(c));
//		
//		lastCoords = new Rr2Point(c);
//		enableDrawing();
//	}
	
	private Rectangle circleToRectangle(Point2D c)
	{
		Point2D p = new Point2D(0.5*curAperture.width, 0.5*curAperture.width);
		return new Rectangle(Point2D.sub(c, p), Point2D.add(c, p));
	}
	
//	private void octagon(Rr2Point p, double diameter)
//	{
//		double x, y, r;
//		double ang = 22.5*Math.PI/180;
//		r = 0.5*diameter;
//		Rr2Point q;
//		disableDrawing();
//		for(int i = 0; i <= 8; i++)
//		{
//			q = new Rr2Point(p);
//			q = Rr2Point.add(q, new Rr2Point(r*Math.cos(ang), r*Math.sin(ang)));
//			addPointToPolygons(q);
//			ang += 0.25*Math.PI;
//		}
//		disableDrawing();		
//	}
	
//	private void drawOneLine(Rr2Point c)
//	{
//		addPointToPolygons(c);
//	}
	
	private Rectangle drawFatLine(Point2D c)
	{
		Rectangle result = circleToRectangle(c);
		result = Rectangle.union(result, circleToRectangle(lastCoords));
		if(pcb == null)
			return result;
		//TODO: make this draw a fat line
		pcb.rectangle(lastCoords, c, 0.5*curAperture.width, colour);
		pcb.disc(c, curAperture.width*0.5, true);
		pcb.disc(lastCoords, curAperture.width*0.5, colour);
		lastCoords = new Point2D(c);
		return result;
	}

	private void enableDrawing()
	{
		dawingOn = true;
	}
	
	private void disableDrawing()
	{	
		dawingOn = false;
	}
	
	Point2D fixCoords(Point2D c)
	{
		if(!absolute)
		{
			c = new Point2D(c);
			c = Point2D.add(c, lastCoords);
		}
		return c;
	}
	
	public PolygonList getPolygons()
	{

		if(Preferences.simulate())
		{
			RrGraphics simulationPlot1 = new RrGraphics("PCB from gerber");
			//				if(currentPolygon != null)
			//					thePattern.add(new RrPolygon(currentPolygon));
			simulationPlot1.init(pcb.box(), false, "0");
			simulationPlot1.add(pcb);
		}

		PolygonList result = new PolygonList();
		double penWidth = pcbPen.getExtrusionSize();
		pcb = pcb.offset(-0.5*penWidth);
		PolygonList pol = pcb.allPerimiters(pcb.attribute());
		
		while(pol.size() > 0)
		{
			result.add(pol);
			pcb = pcb.offset(-penWidth);
			pol = pcb.allPerimiters(pcb.attribute());
		}

		return result;
	}
	

}
