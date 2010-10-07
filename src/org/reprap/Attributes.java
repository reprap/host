package org.reprap;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;

import org.reprap.devices.GenericExtruder;
import org.reprap.gui.STLObject;
import org.reprap.utilities.Debug;

/**
 * Small class to hold RepRap attributes that are attached to
 * Java3D shapes as user data, primarily to record the material
 * that things are made from.
 * 
 * @author adrian
 *
 */
public class Attributes {
	
	/**
	 * The name of the material
	 */
	private String material;
	
	/**
	 * The STLObject of which this is a part
	 */
	private STLObject parent;
	
	/**
	 * Where this is in the STLObject of which it is a part
	 */
	private BranchGroup part;
	
	/**
	 * The appearance (colour) in the loading and simulation windows
	 */
	private Appearance app;
	
	/**
	 * The extruder corresponsing to this material.  This is lazily evaluated
	 * (I.e. it is not set until there are some extruders around to use).
	 */
	private Extruder e;
	
	/**
	 * Constructor - it is permissible to set any argument null.  If you know
	 * what you're doing of course...
	 * @param s The name of the material
	 * @param p Parent STLObject
	 * @param b Where in p
	 * @param a what it looks like
	 */
	public Attributes(String s, STLObject p, BranchGroup b, Appearance a)
	{
		material = s;
		parent = p;
		part = b;
		app = a;
		e = null;
	}
	
	/**
	 * Just say the name of the material
	 */
	public String toString()
	{
		String result = new String();
		result += "Attributes: material is " + material;
		return result;
	}
	
	/**
	 * @return the name of the material
	 */
	public String getMaterial() { return material; }
	
	/**
	 * @return the parent object
	 */	
	public STLObject getParent() { return parent; }
	
	/**
	 * @return the bit of the STLObject that this is
	 */
	public BranchGroup getPart() { return part; }
	
	/**
	 * @return what colour am I?
	 */
	public Appearance getAppearance() { return app; }
	
	/**
	 * Find my extruder in the list (if not known) or just
	 * return it (if known).
	 * @param es The extruders currently in the printer.
	 * @return my extruder
	 */
	public Extruder getExtruder()
	{
		if(e == null)
		{
			Printer p = org.reprap.Main.gui.getPrinter();
			if(p == null)
			{
				Debug.e("Attributes.getExtruder(): null printer!");
				return null;
			}
			e = p.getExtruder(material); 
			if(e == null)
			{
				Debug.e("Attributes.getExtruder(): null extruder for " + material);
				return null;
			}
		}
		return e;
	}
	
	/**
	 * Change the material name
	 * @param s
	 */
	public void setMaterial(String s) 
	{ 
		material = s;
		e = null;
		app = GenericExtruder.getAppearanceFromMaterial(material);
		if(parent != null)
			parent.restoreAppearance();
	}
	
	/**
	 * Change the parent
	 * @param p
	 */
	public void setParent(STLObject p) { parent = p; }
	
	/**
	 * To be used in conjunction with changing the parent
	 * @param b
	 */
	public void setPart(BranchGroup b) { part = b; }
	
	/**
	 * New colour
	 * @param a
	 */
	public void setAppearance(Appearance a) { app = a; }
}
