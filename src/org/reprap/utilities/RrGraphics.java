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
 
 
 RrGraphics: Simple 2D graphics
 
 First version 20 May 2005
 This version: 1 May 2006 (Now in CVS - no more comments here)
 
 */

package org.reprap.utilities;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import org.reprap.geometry.polygons.BooleanGrid;
import org.reprap.geometry.polygons.Point2D;
import org.reprap.geometry.polygons.Rectangle;
import org.reprap.geometry.polygons.HalfPlane;
import org.reprap.geometry.polygons.Interval;
import org.reprap.geometry.polygons.Polygon;
import org.reprap.geometry.polygons.PolygonList;
import org.reprap.gui.StatusMessage;
import org.reprap.Attributes;
import javax.media.j3d.Appearance;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.media.j3d.Material;

/**
 * Class to plot images of geometrical structures for debugging.
 * 
 * @author ensab
 *
 */
public class RrGraphics 
{
	static final Color background = Color.white;
	static final Color boxes = Color.blue;
	static final Color polygon1 = Color.red;
	static final Color polygon0 = Color.black;	
	static final Color infill = Color.pink;
	static final Color hatch1 = Color.magenta;
	static final Color hatch0 = Color.orange;
	
	/**
	 * Pixels 
	 */
	private final int frame = 600;
	
	/**
	 * 
	 */
	private int frameWidth;
	
	/**
	 * 
	 */
	private int frameHeight;
	
	/**
	 * 
	 */
	private PolygonList p_list = null;
	
	/**
	 * The layer being built
	 */
	private String layerNumber;
	
	/**
	 * 
	 */
	//private RrCSGPolygon csg_p = null;
	
	/**
	 * 
	 */
	private BooleanGrid bg = null;
	
	/**
	 * 
	 */
	private boolean csgSolid = true;
	
	/**
	 * 
	 */
	private List<HalfPlane> hp = null;
	
	/**
	 * 
	 */
	private double scale;
	
	/**
	 * 
	 */
	private Point2D p_0;
	
	/**
	 * 
	 */
	private Point2D pos;
	
	private Rectangle scaledBox, originalBox;
	
	/**
	 * 
	 */
	private static Graphics2D g2d;
	private static JFrame jframe;
	/**
	 * 
	 */
	private boolean plot_box = false;
	
	private String title = "RepRap diagnostics";
	
	private boolean initialised = false;
	
	/**
	 * Constructor for just a box - add stuff later
	 * @param b
	 * @param pb
	 */
	public RrGraphics(Rectangle b, String t) 
	{
		p_list = null;
		hp = null;
		title = t;
		init(b, false, "0");
	}
	
	/**
	 * Constructor for nothing - add stuff later
	 * @param b
	 * @param pb
	 */
	public RrGraphics(String t) 
	{
		p_list = null;
		hp = null;
		title = t;
		initialised = false;
		layerNumber = "0";
	}
	
	public void cleanPolygons(String ln)
	{
		p_list = null;
		hp = null;
		layerNumber = ln;
	}
	
	private void setScales(Rectangle b)
	{
		scaledBox = b.scale(1.2);
		
		double width = scaledBox.x().length();
		double height = scaledBox.y().length();
		if(width > height)
		{
			frameWidth = frame;
			frameHeight = (int)(0.5 + (frameWidth*height)/width);
		} else
		{
			frameHeight = frame;
			frameWidth = (int)(0.5 + (frameHeight*width)/height);
		}
		double xs = (double)frameWidth/width;
		double ys = (double)frameHeight/height;
		
		if (xs < ys)
			scale = xs;
		else
			scale = ys;	
		
		p_0 = new Point2D((frameWidth - (width + 2*scaledBox.x().low())*scale)*0.5,
				(frameHeight - (height + 2*scaledBox.y().low())*scale)*0.5);
		
		pos = new Point2D(width*0.5, height*0.5);
	}
	
	private void plotBar()
	{
		g2d.setColor(boxes);
		Point2D p = new Point2D(scaledBox.ne().x() - 12, scaledBox.sw().y() + 5);
		move(p);
		p = new Point2D(scaledBox.ne().x() - 2, scaledBox.sw().y() + 5);
		plot(p);	
	}
	
	/**
	 * @param b
	 */
	public void init(Rectangle b, boolean waitTillDone, String ln)
	{
		originalBox = b;
		setScales(b);
		
		jframe = new JFrame();
		jframe.setSize(frameWidth, frameHeight);
		jframe.getContentPane().add(new MyComponent());
		jframe.setTitle(title);
		jframe.setVisible(true);
		jframe.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		jframe.addMouseListener(new myMouse());
		jframe.addKeyListener(new myKB());
		jframe.setIgnoreRepaint(false);
		
		initialised = true;
		
		layerNumber = ln;
		
		if(waitTillDone)
		{
			StatusMessage statusWindow = new StatusMessage(new JFrame());
			//statusWindow.setButton("Continue");
			statusWindow.setMessage("Left mouse - magnify\n" +
					"Middle mouse - evaluate\n" +
					"Right mouse - full image\n" +
					"b - toggle boxes\n" + 
					"s - toggle solid shading\n\n" 
			);
			statusWindow.setLocation(new Point(frameWidth + 20, 0));
			statusWindow.setVisible(true);


			boolean loop = true;
			while(loop)
			{
				try {
					Thread.sleep(100);
					loop = !statusWindow.isCancelled();
				} catch (InterruptedException e) 
				{

				}
			}
			jframe.dispose();
		}
	}
	
	
	public boolean isInitialised()
	{
		return initialised;
	}
	
	/**
	 * Plot a G code
	 * @param gCode
	 */
	public void add(String gCode)
	{
		if(p_list == null)
			p_list = new PolygonList();
		Rectangle box = new Rectangle(new Interval(0, 200), new Interval(0, 200)); // Default is entire plot area
		int com = gCode.indexOf(';');
		if(com > 0)
			gCode = gCode.substring(0, com);
		if(com != 0)
		{
			gCode = gCode.trim();
			if(gCode.length() > 0)
			{
				if(!isInitialised())
				{
					Debug.d("RrGraphics.add(G Codes) - plot area not initialized.");
					init(box, false, "0");
				}
			}
			return;
		}
		if(gCode.startsWith(";#!LAYER:"))
		{
			int l = Integer.parseInt(gCode.substring(gCode.indexOf(" ") + 1, gCode.indexOf("/")));
			cleanPolygons("" + l);
		}
		if(gCode.startsWith(";#!RECTANGLE:"))
		{
			String xs = gCode.substring(gCode.indexOf("x:") + 1, gCode.indexOf("y"));
			String ys = gCode.substring(gCode.indexOf("y:") + 1, gCode.indexOf(">"));
			double x0 = Double.parseDouble(xs.substring(xs.indexOf("l:") + 1, xs.indexOf(",")));
			double x1 = Double.parseDouble(xs.substring(xs.indexOf("h:") + 1, xs.indexOf("]")));
			double y0 = Double.parseDouble(ys.substring(ys.indexOf("l:") + 1, ys.indexOf(",")));
			double y1 = Double.parseDouble(ys.substring(ys.indexOf("h:") + 1, ys.indexOf("]")));
			box = new Rectangle(new Interval(x0, x1), new Interval(y0, y1));
			init(box, false, "0");
		}
	}
	
	/**
	 * @param pl
	 */
	public void add(PolygonList pl)
	{
		if(pl == null)
			return;
		if(pl.size() <= 0)
			return;
		if(p_list == null)
			p_list = new PolygonList(pl);
		else
			p_list.add(pl);
		jframe.repaint();
	}
	
	public void add(BooleanGrid b)
	{
		bg = b;
		jframe.repaint();
	}
	
	/**
	 * Real-world coordinates to pixels
	 * @param p
	 * @return
	 */
	private Point2D transform(Point2D p)
	{
		return new Point2D(p_0.x() + scale*p.x(), (double)frameHeight - 
				(p_0.y() + scale*p.y()));
	}
	
	/**
	 * Pixels to real-world coordinates
	 * @param p
	 * @return
	 */
	private Point2D iTransform(int x, int y)
	{
		return new Point2D(((double)x - p_0.x())/scale, ((double)(frameHeight - y)
				- p_0.y())/scale);
	}
	
	/**
	 * Move invisibly to a point
	 * @param p
	 */
	private void move(Point2D p)
	{
		pos = transform(p);
	}
		
	/**
	 * Draw a straight line to a point
	 * @param p
	 */
	private void plot(Point2D p)
	{
		Point2D a = transform(p);
		g2d.drawLine((int)Math.round(pos.x()), (int)Math.round(pos.y()), 
				(int)Math.round(a.x()), (int)Math.round(a.y()));
		pos = a;
	}
	
	
	/**
	 * Plot a box
	 * @param b
	 */
	private void plot(Rectangle b)
	{
		if(Rectangle.intersection(b, scaledBox).empty())
			return;
		
		g2d.setColor(boxes);
		move(b.sw());
		plot(b.nw());
		plot(b.ne());
		plot(b.se());
		plot(b.sw());
	}
	
	/**
	 * Set the colour from a RepRap attribute
	 * @param at
	 */
	private void setColour(Attributes at)
	{
		Appearance ap = at.getAppearance();
		Material mt = ap.getMaterial();
		Color3f col = new Color3f();
		mt.getDiffuseColor(col);
		g2d.setColor(col.get());		
	}
	
	/**
	 * Plot a polygon
	 * @param p
	 */
	private void plot(Polygon p)
	{
		if(p.size() <= 0)
			return;
		if(Rectangle.intersection(p.getBox(), scaledBox).empty())
			return;
	    if(p.getAttributes().getAppearance() == null)
	    {
	    	Debug.e("RrGraphics: polygon with size > 0 has null appearance.");
	    	return;
	    }
	    
	    setColour(p.getAttributes());
		move(p.point(0));
		for(int i = 1; i < p.size(); i++)	
				plot(p.point(i));
		if(p.isClosed())
		{
			g2d.setColor(Color.RED);
			plot(p.point(0));
		}
	}	

	
	/**
	 * Fill a Boolean Grid where it's solid.
	 * @param q
	 */
	private void fillBG(BooleanGrid b)
	{
	    if(b.attribute().getAppearance() == null)
	    {
	    	Debug.e("RrGraphics: booleanGrid has null appearance.");
	    	return;
	    }
	    
	    setColour(b.attribute());
		for(int x = 0; x < frameWidth; x++)
			for(int y = 0; y < frameHeight; y++)
			{
				boolean v = b.get(iTransform(x, y));
				if(v)
					g2d.drawRect(x, y, 1, 1);  // Is this really the most efficient way?
			}
	}
	

	
	/**
	 * Master plot function - draw everything
	 */
	private void plot()
	{
		plotBar();
		if(bg != null)
		{
			fillBG(bg);
		}
		
		if(p_list != null)
		{
			int leng = p_list.size();
			for(int i = 0; i < leng; i++)
				plot(p_list.polygon(i));
			if(plot_box)
			{
				for(int i = 0; i < leng; i++)
					plot(p_list.polygon(i).getBox());
			} 
		}
		jframe.setTitle(title + ", layer: " + layerNumber);
	}
	
	class myKB implements KeyListener
	{
		public void keyTyped(KeyEvent k)
		{
			switch(k.getKeyChar())
			{
			case 'b':
			case 'B':
				plot_box = !plot_box;
				break;
				
			case 's':
			case 'S':
				csgSolid = !csgSolid;
				
			default:
			}
			jframe.repaint();
		}
		
		public void keyPressed(KeyEvent k)
		{	
		}
		
		public void keyReleased(KeyEvent k)
		{	
		}
	}
	
	/**
	 * Clicking the mouse magnifies
	 * @author ensab
	 *
	 */
	class myMouse implements MouseListener
	{
		private Rectangle magBox(Rectangle b, int ix, int iy)
		{
			Point2D cen = iTransform(ix, iy);
			//System.out.println("Mouse: " + cen.toString() + "; box: " +  scaledBox.toString());
			Point2D off = new Point2D(b.x().length()*0.05, b.y().length()*0.05);
			return new Rectangle(Point2D.sub(cen, off), Point2D.add(cen, off));
		}
		
		public void mousePressed(MouseEvent e) {
		}
	    public void mouseReleased(MouseEvent e) {
	    }
	    public void mouseEntered(MouseEvent e) {
	    }
	    public void mouseExited(MouseEvent e) {
	    }
	    
	    public void mouseClicked(MouseEvent e) 
	    {
			int ix = e.getX() - 5;  // Why needed??
			int iy = e.getY() - 25; //  "     "
			
			switch(e.getButton())
			{
			case MouseEvent.BUTTON1:
				setScales(magBox(scaledBox, ix, iy));
				break;

			case MouseEvent.BUTTON2:

				break;
				
			case MouseEvent.BUTTON3:

			default:
				setScales(originalBox);
			}
			jframe.repaint();
	    } 
	}
	
	/**
	 * Canvas to paint on 
	 */
	class MyComponent extends JComponent 
	{
		private static final long serialVersionUID = 1L;
		public MyComponent()
		{
			super();
		}
		// This method is called whenever the contents needs to be painted
		public void paint(Graphics g) 
		{
			// Retrieve the graphics context; this object is used to paint shapes
			g2d = (Graphics2D)g;
			// Draw everything
			plot();
		}
	}
}
