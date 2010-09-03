/*
 
RepRap
------
 
The Replicating Rapid Prototyper Project
 
 
Copyright (C) 2006
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
 
Wrapper class for STL objects that allows them easily to be moved about
by the mouse.  The STL object itself is a Shape3D loaded by the STL loader.

First version 14 April 2006
This version: 14 April 2006
 
 */

package org.reprap.gui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.picking.PickTool;

import org.j3d.renderer.java3d.loaders.STLLoader;
import org.reprap.Attributes;
import org.reprap.Preferences;

/**
 * Class for holding a group (maybe just 1) of 3D objects for RepRap to make.
 * They can be moved around on the build platform en mass, but not moved
 * relative to each other, so they can represent an assembly made from several
 * different materials.
 * 
 * @author adrian
 * 
 */

public class STLObject
{
	
	/**
	 * Little class to hold offsets of loaded STL objects
	 */
	class Offsets
	{
		private Vector3d centreToOrigin;
		private Vector3d bottomLeftShift;
	}
	
	/**
	 * Little class to hold tripples of the parts of this STLObject loaded.
	 *
	 */
	class Contents
	{
	    private String sourceFile = null;   // The STL file I was loaded from
	    private BranchGroup stl = null;     // The actual STL geometry
	    private Attributes att = null;		// The attributes associated with it
	    private int unique = 0;
	    
	    Contents(String s, BranchGroup st, Attributes a)
	    {
	    	sourceFile = s;
	    	stl = st;
	    	att = a;
	    }
	    
	    void setUnique(int i)
	    {
	    	unique = i;
	    }
	    
	    int getUnique()
	    {
	    	return unique;
	    }
	}
	
    private MouseObject mouse = null;   // The mouse, if it is controlling us
    private BranchGroup top = null;     // The thing that links us to the world
    private BranchGroup handle = null;  // Internal handle for the mouse to grab
    private TransformGroup trans = null;// Static transform for when the mouse is away
    private BranchGroup stl = null;     // The actual STL geometry
    private Vector3d extent = null;       // X, Y and Z extent
    private BoundingBox bbox = null;    // Temporary storage for the bounding box while loading
    private Vector3d rootOffset = null; // Offset of the first-loaded STL under stl
    private List<Contents> contents = null;
    

    public STLObject()
    {
    	stl = new BranchGroup();
    	
    	contents = new ArrayList<Contents>();
        
        // No mouse yet
        
        mouse = null;
        
        // Set up our bit of the scene graph
        
        top = new BranchGroup();
        handle = new BranchGroup();
        trans = new TransformGroup();
        
        top.setCapability(BranchGroup.ALLOW_DETACH);
        top.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        top.setCapability(Group.ALLOW_CHILDREN_WRITE);
        top.setCapability(Group.ALLOW_CHILDREN_READ);
        top.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
        top.setCapability(Node.ALLOW_BOUNDS_READ);
        
        handle.setCapability(BranchGroup.ALLOW_DETACH);
        handle.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        handle.setCapability(Group.ALLOW_CHILDREN_WRITE);
        handle.setCapability(Group.ALLOW_CHILDREN_READ);
        
        trans.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        trans.setCapability(Group.ALLOW_CHILDREN_WRITE);
        trans.setCapability(Group.ALLOW_CHILDREN_READ);
        trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        trans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        stl.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        stl.setCapability(Group.ALLOW_CHILDREN_WRITE);
        stl.setCapability(Group.ALLOW_CHILDREN_READ);
        stl.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        stl.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        trans.addChild(stl);
        handle.addChild(trans);
        top.addChild(handle);
        
        Attributes nullAtt = new Attributes(null, this, null, null);
        top.setUserData(nullAtt);
        handle.setUserData(nullAtt);
        trans.setUserData(nullAtt);
        stl.setUserData(nullAtt);
        
        bbox = null;
    }
    
    /**
     * Load an STL object from a file with a known offset (set that null to put
     * the object bottom-left-at-origin) and set its appearance
     * 
     * @param location
     * @param offset
     * @param app
     */
    public Attributes addSTL(String location, Vector3d offset, Appearance app, STLObject lastPicked) 
    {
    	Attributes att = new Attributes(null, this, null, app);
    	BranchGroup child = loadSingleSTL(location, att, offset, lastPicked);
    	if(child == null)
    		return null;
    	if(lastPicked == null)
    		contents.add(new Contents(location, child, att));
    	else
    		lastPicked.contents.add(new Contents(location, child, att));
    	return att;
    }

    /**
     * Actually load the stl file and set its attributes.  Offset decides where to put it relative to 
     * the origin.  If lastPicked is null, the file is loaded as a new independent STLObject; if not
     * it is added to lastPicked and subsequently is subjected to all the same transforms, so they retain
     * their relative positions.  This is how multi-material objects are loaded.
     * 
     * @param location
     * @param att
     * @param offset
     * @param lastPicked
     * @return
     */
    private BranchGroup loadSingleSTL(String location, Attributes att, Vector3d offset, STLObject lastPicked)
    {
    	BranchGroup result = null;
        STLLoader loader = new STLLoader();
        Scene scene;
        try 
        {
            scene = loader.load(location);
            if (scene != null) 
            {
                result = scene.getSceneGroup();
                result.setCapability(Node.ALLOW_BOUNDS_READ);
                result.setCapability(Group.ALLOW_CHILDREN_READ);
                result.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                

                
                // Recursively add its attribute
                
                Hashtable<?,?> namedObjects = scene.getNamedObjects( );
                java.util.Enumeration<?> enumValues = namedObjects.elements( );
                
                if( enumValues != null ) 
                {
                    while(enumValues.hasMoreElements( )) 
                    {
                    	Shape3D value = (Shape3D)enumValues.nextElement();
                        bbox = (BoundingBox)value.getBounds();
                        
                        value.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE );
                        GeometryArray g = (GeometryArray)value.getGeometry();
                        g.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
                        
                        recursiveSetUserData(value, att);
                    }
                }
                
                att.setPart(result);
                result.setUserData(att);
                Offsets off;
                if(lastPicked != null)
                {
                	// Add this object to lastPicked
                	setOffset(result, lastPicked.rootOffset);
                	lastPicked.stl.addChild(result);
                	lastPicked.setAppearance(lastPicked.getAppearance());
                	lastPicked.updateBox(bbox);
                } else
                {
                	// New independent object.
                	stl.addChild(result);
                	off = getOffsets(result, offset);
                	rootOffset = off.centreToOrigin;
                	setOffset(stl, rootOffset);
                	Transform3D temp_t = new Transform3D();
                    temp_t.set(off.bottomLeftShift);
                	trans.setTransform(temp_t);
                	restoreAppearance();
                }
            } 

        } catch ( Exception e ) 
        {
            System.err.println("loadSingelSTL(): Exception loading STL file from: " 
                    + location);
            e.printStackTrace();
        }
        return result;
    }
    
    private void updateBox(BoundingBox bb)
    {
        javax.vecmath.Point3d pNew = new javax.vecmath.Point3d();
        javax.vecmath.Point3d pOld = new javax.vecmath.Point3d();
        bb.getLower(pNew);
        bbox.getLower(pOld);
        if(pNew.x < pOld.x)
        	pOld.x = pNew.x;
        if(pNew.y < pOld.y)
        	pOld.y = pNew.y;
        if(pNew.z < pOld.z)
        	pOld.z = pNew.z;
        bbox.setLower(pOld);
        extent = new Vector3d(pOld.x, pOld.y, pOld.z);

        bb.getUpper(pNew);
        bbox.getUpper(pOld);
        if(pNew.x > pOld.x)
        	pOld.x = pNew.x;
        if(pNew.y > pOld.y)
        	pOld.y = pNew.y;
        if(pNew.z > pOld.z)
        	pOld.z = pNew.z;
        bbox.setUpper(pOld);
        
        extent.x = pOld.x - extent.x;
        extent.y = pOld.y - extent.y;
        extent.z = pOld.z - extent.z;
        
    }
    
    public BranchGroup top()
    {
    	return top;
    }
    
    public TransformGroup trans()
    {
    	return trans;
    }
    
    public BranchGroup handle()
    {
    	return handle;
    }
    
    public Vector3d extent()
    {
    	return extent;
    }
    
    public String fileItCameFrom(int i)
    {
    	return contents.get(i).sourceFile;
    }
    
    public Attributes attributes(int i)
    {
    	return contents.get(i).att;
    }
    
    public BranchGroup branchGroup(int i)
    {
    	return contents.get(i).stl;
    } 
    
    public int size()
    {
    	return contents.size();
    }
    
    public void setUnique(int i, int v)
    {
    	contents.get(i).setUnique(v);
    }
    
    public int getUnique(int i)
    {
    	return contents.get(i).getUnique();
    }
    
    
    
    /**
     * Find how to move the object by actually changing all its coordinates (i.e. don't just add a
     * transform).  Also record its size.
     * @param child
     * @param offset
     */
    private Offsets getOffsets(BranchGroup child, Vector3d userOffset) 
    {
    	Offsets result = new Offsets();
    	Vector3d offset = null;
    	if(userOffset != null)
    		offset = new Vector3d(userOffset);
    	
    	if(child != null && bbox != null)
    	{
            javax.vecmath.Point3d p0 = new javax.vecmath.Point3d();
            javax.vecmath.Point3d p1 = new javax.vecmath.Point3d();
            bbox.getLower(p0);
            bbox.getUpper(p1);
            
            // If no offset requested, set it to bottom-left-at-origin
            
            if(offset == null) 
            {
                offset = new Vector3d();
                offset.x = -p0.x;  // Generally offset to put bottom left at the origin
                offset.y = -p0.y;
                offset.z = -p0.z;
            } //else
            	//offset.z = -p0.z;  // Tie it down whatever the user has said...
            
            // How big?
            
            extent = new Vector3d(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
            
            // Position us centre at origin:
            
            offset = add(offset, neg(scale(extent, 0.5)));
            
            // Recursively apply that.  N.B. we do not apply a transform to the
            // loaded object; we actually shift all its points to put it in this
            // standard place.
            
            //setOffset(offset);
            
            result.centreToOrigin = offset;
            
            //System.out.println("centreToOrigin = " + offset.toString());

            // Now shift us to have bottom left at origin using our transform.
            
            //Transform3D temp_t = new Transform3D();
            //temp_t.set(scale(size, 0.5));
            result.bottomLeftShift = scale(extent, 0.5);
            //System.out.println("half-size = " + result.bottomLeftShift.toString());
            //trans.setTransform(temp_t);
            
            //restoreAppearance();
            
        } else
            System.err.println("applyOffset(): no bounding box or child.");
    	
    	return result;
    }
    
    
    /**
     * Make an STL object from an existing BranchGroup
     */
    public STLObject(BranchGroup s, String n) 
    {
    	this();
  
        stl.addChild(s);
        extent = new Vector3d(1, 1, 1);  // Should never be needed.
        
        Transform3D temp_t = new Transform3D();
        trans.setTransform(temp_t); 
    }


    /**
     * method to recursively set the user data for objects in the scenegraph tree
     * we also set the capabilites on Shape3D objects required by the PickTool
     * @param value
     * @param me
     */
    private void recursiveSetUserData(Object value, Object me) 
    {
        if( value instanceof SceneGraphObject  ) 
        {
            // set the user data for the item
            SceneGraphObject sg = (SceneGraphObject) value;
            sg.setUserData( me );
            
            // recursively process group
            if( sg instanceof Group ) 
            {
                Group g = (Group) sg;
                
                // recurse on child nodes
                java.util.Enumeration<?> enumKids = g.getAllChildren( );
                
                while(enumKids.hasMoreElements( ))
                    recursiveSetUserData( enumKids.nextElement( ), me );
            } else if ( sg instanceof Shape3D ) 
            {
                ((Shape3D)sg).setUserData(me);
                PickTool.setCapabilities( (Node) sg, PickTool.INTERSECT_FULL );
            }
        }
    }
    
    // Move the object by p permanently (i.e. don't just apply a transform).
    
    private void recursiveSetOffset(Object value, Vector3d p) 
    {
        if( value instanceof SceneGraphObject != false ) 
        {
            // set the user data for the item
            SceneGraphObject sg = (SceneGraphObject) value;
            
            // recursively process group
            if( sg instanceof Group ) 
            {
                Group g = (Group) sg;
                
                // recurse on child nodes
                java.util.Enumeration<?> enumKids = g.getAllChildren( );
                
                while(enumKids.hasMoreElements( ))
                    recursiveSetOffset( enumKids.nextElement( ), p );
            } else if (sg instanceof Shape3D) 
            {
                    s3dOffset((Shape3D)sg, p);
            }
        }
    }
    
    private void setOffset(BranchGroup bg, Vector3d p)
    {
    	recursiveSetOffset(bg, p);
    }
    
    // Shift a Shape3D permanently by p
    
    private void s3dOffset(Shape3D shape, Tuple3d p)
    {
        GeometryArray g = (GeometryArray)shape.getGeometry();
        Point3d p3d = new Point3d();
        if(g != null)
        {
            for(int i = 0; i < g.getVertexCount(); i++) 
            {
                g.getCoordinate(i, p3d);
                p3d.add(p);
                g.setCoordinate(i, p3d);
            }
        }
    }
    
    // Scale the object by s permanently (i.e. don't just apply a transform).
    
    private void recursiveSetScale( Object value, double s) 
    {
        if( value instanceof SceneGraphObject != false ) 
        {
            // set the user data for the item
            SceneGraphObject sg = (SceneGraphObject) value;
            
            // recursively process group
            if( sg instanceof Group ) 
            {
                Group g = (Group) sg;
                
                // recurse on child nodes
                java.util.Enumeration<?> enumKids = g.getAllChildren( );
                
                while(enumKids.hasMoreElements( ))
                    recursiveSetScale( enumKids.nextElement( ), s );
            } else if ( sg instanceof Shape3D ) 
            {
                    s3dScale((Shape3D)sg, s);
            }
        }
    }
    
   // Scale a Shape3D permanently by s
    
    private void s3dScale(Shape3D shape, double s)
    {
        GeometryArray g = (GeometryArray)shape.getGeometry();
        Point3d p3d = new Point3d();
        if(g != null)
        {
            for(int i = 0; i < g.getVertexCount(); i++) 
            {
                g.getCoordinate(i, p3d);
                p3d.scale(s);
                g.setCoordinate(i, p3d);
            }
        }
    }
    


    // Set my transform
    
    public void setTransform(Transform3D t3d)
    {
        trans.setTransform(t3d);
    }
    
    // Get my transform
    
    public Transform3D getTransform()
    {
    	Transform3D result = new Transform3D();
        trans.getTransform(result);
        return result;
    }
    
    // Get one of the the actual objects
    
//    public BranchGroup getSTL(int i)
//    {
//    	return (BranchGroup)(stl.getChild(i));
//    }
    public BranchGroup getSTL()
    {
    	return stl;
    }
    // Get the number of objects
    
    public int numChildren()
    {
    	return stl.numChildren();
    }
    
    // The mouse calls this to tell us it is controlling us
    
    public void setMouse(MouseObject m)
    {
        mouse = m;
    }
    
    // Change colour etc. - recursive private call to walk the tree
    
    private static void setAppearance_r(Object gp, Appearance a) 
    {
        if( gp instanceof Group ) 
        {
            Group g = (Group) gp;
            
            // recurse on child nodes
            java.util.Enumeration<?> enumKids = g.getAllChildren( );
            
            while(enumKids.hasMoreElements( )) 
            {
                Object child = enumKids.nextElement( );
                if(child instanceof Shape3D) 
                {
                    Shape3D lf = (Shape3D) child;
                    lf.setAppearance(a);
                } else
                    setAppearance_r(child, a);
            }
        }
    }
    
    // Change colour etc. - call the internal fn to do the work.
    
    public void setAppearance(Appearance a)
    {
        setAppearance_r(stl, a);     
    }
    
    /**
     * dig down to find our appearance
     * @param gp
     * @return
     */
    private static Appearance getAppearance_r(Object gp) 
    {
        if( gp instanceof Group ) 
        {
            Group g = (Group) gp;
            
            // recurse on child nodes
            java.util.Enumeration<?> enumKids = g.getAllChildren( );
            
            while(enumKids.hasMoreElements( )) 
            {
                Object child = enumKids.nextElement( );
                if(child instanceof Shape3D) 
                {
                    Shape3D lf = (Shape3D) child;
                    return lf.getAppearance();
                } else
                    return getAppearance_r(child);
            }
        }
        return new Appearance();
    }
    
    public Appearance getAppearance()
    {
    	return getAppearance_r(stl);
    }
    
    /**
     * Restore the appearances to the correct colour.
     */
    public void restoreAppearance()
    {
    	java.util.Enumeration<?> enumKids = stl.getAllChildren( );
        
        while(enumKids.hasMoreElements( ))
        {
        	Object b = enumKids.nextElement();
        	if(b instanceof BranchGroup)
        	{
        		Attributes att = (Attributes)((BranchGroup)b).getUserData();
        		if(att != null)
        			setAppearance_r(b, att.getAppearance());
        		else
        			System.err.println("restoreAppearance(): no Attributes!");
        	}
        }
    }
    
    // Why the !*$! aren't these in Vector3d???
    
    public static Vector3d add(Vector3d a, Vector3d b)
    {
        Vector3d result = new Vector3d();
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.z = a.z + b.z;
        return result;
    }
    
    public static Vector3d neg(Vector3d a)
    {
        Vector3d result = new Vector3d(a);
        result.negate();
        return result;
    }
    
    public static Vector3d scale(Vector3d a, double s)
    {
        Vector3d result = new Vector3d(a);
        result.scale(s);
        return result;
    }
    
    // Put a vector in the positive octant (sort of abs for vectors)
    
    private Vector3d posOct(Vector3d v)
    {
        Vector3d result = new Vector3d();
        result.x = Math.abs(v.x);
        result.y = Math.abs(v.y);
        result.z = Math.abs(v.z);
        return result;
    }
    
    // Apply a 90 degree click transform about one of the coordinate axes,
    // which should be set in t.  This can only be done if we're being controlled
    // by the mouse, making us the active object.
    
    private void rClick(Transform3D t)
    {
        if(mouse == null)
            return;
        
        // Get the mouse transform and split it into a rotation and a translation
        
        Transform3D mtrans = new Transform3D();
        mouse.getTransform(mtrans);
        Vector3d mouseTranslation = new Vector3d();
        Matrix3d mouseRotation = new Matrix3d();
        mtrans.get(mouseRotation, mouseTranslation);
        
        // Subtract the part of the translation that puts the bottom left corner
        // at the origin.
        
        Vector3d zero = scale(extent, 0.5);
        mouseTranslation = add(mouseTranslation, neg(zero));       
        
        // Click the size record round by t
        
        t.transform(extent);
        extent = posOct(extent); 
        
        // Apply the new rotation to the existing one
        
        Transform3D spin = new Transform3D();
        spin.setRotation(mouseRotation);
        t.mul(spin);
        
        // Add a new translation to put the bottom left corner
        // back at the origin.
        
        zero = scale(extent, 0.5);
        mouseTranslation = add(mouseTranslation, zero);
        
        // Then slide us back where we were
        
        Transform3D fromZeroT = new Transform3D();
        fromZeroT.setTranslation(mouseTranslation);

        fromZeroT.mul(t);
        
        // Apply the whole new transformation
        
        mouse.setTransform(fromZeroT);       
    }
    
   // Rescale the STL object (for inch -> mm conversion)
    
    private void rScale(double s)
    {
        if(mouse == null)
            return;
        
        // Get the mouse transform and split it into a rotation and a translation
        
        Transform3D mtrans = new Transform3D();
        mouse.getTransform(mtrans);
        Vector3d mouseTranslation = new Vector3d();
        Matrix3d mouseRotation = new Matrix3d();
        mtrans.get(mouseRotation, mouseTranslation);
        
        // Subtract the part of the translation that puts the bottom left corner
        // at the origin.
        
        Vector3d zero = scale(extent, 0.5);
        mouseTranslation = add(mouseTranslation, neg(zero));       
        
        // Rescale the box
        
       	extent.scale(s);
        
        // Add a new translation to put the bottom left corner
        // back at the origin.
        
        zero = scale(extent, 0.5);
        mouseTranslation = add(mouseTranslation, zero);
        
        // Then slide us back where we were
        
        Transform3D fromZeroT = new Transform3D();
        fromZeroT.setTranslation(mouseTranslation);
        
        // Apply the whole new transformation
        
        mouse.setTransform(fromZeroT);
        
        // Rescale the object
 
        Enumeration<?> things;

        things = stl.getAllChildren();
        while(things.hasMoreElements()) 
        {
        	Object value = things.nextElement();
        	recursiveSetScale(value, s);
        }


    }
    
    // Apply X, Y or Z 90 degree clicks to us if we're the active (i.e. mouse
    // controlled) object.
    
    public void xClick()
    {
        if(mouse == null)
            return;
        
        Transform3D x90 = new Transform3D();
        x90.set(new AxisAngle4d(1, 0, 0, 0.5*Math.PI));
        
        rClick(x90);
    }
    
    public void yClick()
    {
        if(mouse == null)
            return;
        
        Transform3D x90 = new Transform3D();
        x90.set(new AxisAngle4d(0, 1, 0, 0.5*Math.PI));
        
        rClick(x90);
    }
    
    // Do Zs by 45 deg
    
    public void zClick()
    {
        if(mouse == null)
            return;
        
        Transform3D x45 = new Transform3D();
        x45.set(new AxisAngle4d(0, 0, 1, 0.25*Math.PI));
        
        rClick(x45);
    } 
    
    // This is called when the user wants to convert the object from
    // inches to mm.
    
    public void inToMM()
    {
        if(mouse == null)
            return;
        
        rScale(Preferences.inchesToMillimetres());
    } 
}

//********************************************************************************
