package org.reprap.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URL;

import javax.media.j3d.Appearance;
import javax.media.j3d.AudioDevice;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Material;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.reprap.Preferences;

import com.sun.j3d.audioengines.javasound.JavaSoundMixer;

abstract public class Panel3D extends JPanel {
	private static final long serialVersionUID = 1L;
	//-------------------------------------------------------------
	
	// What follows are defaults.  These values should be overwritten from
	// the reprap.properties file.
	
	protected String wv_location = null;

	// Translate and zoom scaling factors
	
	protected double mouse_tf = 50;
	protected double mouse_zf = 50;

	protected double xwv = 300; // The RepRap machine...
	protected double ywv = 300; // ...working volume in mm.
	protected double zwv = 300;

	// Factors for front and back clipping planes and so on
	
	protected double RadiusFactor = 0.7;
	protected double BackFactor = 2.0;
	protected double FrontFactor = 0.001;
	protected double BoundFactor = 3.0;
	
	protected String worldName = "RepRap World";
	protected Vector3d wv_offset = new Vector3d(-17.3, -24.85, -2);

	// The background, and other colours

	protected Color3f bgColour = new Color3f(0.9f, 0.9f, 0.9f);
	protected Color3f selectedColour = new Color3f(0.6f, 0.2f, 0.2f);
	protected Color3f machineColour = new Color3f(0.3f, 0.4f, 0.3f);
	protected Color3f unselectedColour = new Color3f(0.3f, 0.3f, 0.3f);
//	protected Color3f shellColour = new Color3f(0.1f, 0.6f, 0.1f);
	
	// That's the end of the configuration file data
	
	//--------------------------------------------------------------
	
	protected static final Color3f black = new Color3f(0, 0, 0);	
//	protected Appearance default_app = null; // Colour for unselected parts
//	protected Appearance shell_app = null; // Colour for the lower shell during print.
	protected Appearance picked_app = null; // Colour for the selected part
	protected Appearance wv_app = null; // Colour for the working volume
//	protected Appearance extrusion_app = null; // Colour for extruded material
	protected BranchGroup wv_and_stls = new BranchGroup(); // Where in the scene

	// the
	// working volume and STLs
	// are joined on.

	protected STLObject world = null; // Everything
	protected STLObject workingVolume = null; // The RepRap machine itself.
	
	// The world in the Applet
	protected VirtualUniverse universe = null;
	protected BranchGroup sceneBranchGroup = null;
	protected Bounds applicationBounds = null;

	// Set up the RepRap working volume
	abstract protected BranchGroup createSceneBranchGroup() throws Exception;

	// Set bg light grey
	abstract protected Background createBackground();

	abstract protected BranchGroup createViewBranchGroup(
			TransformGroup[] tgArray, ViewPlatform vp);
	
	public void refreshPreferences()
	{
		// -----------------------
		
		// Set everything up from the properties file
		// All this needs to go into Preferences.java
		try
		{
		wv_location = Preferences.loadGlobalString("WorkingLocation");

		// Translate and zoom scaling factors
		
		mouse_tf = Preferences.loadGlobalDouble("MouseTranslationFactor");
		mouse_zf = Preferences.loadGlobalDouble("MouseZoomFactor");
		
		RadiusFactor = Preferences.loadGlobalDouble("RadiusFactor");
		BackFactor = Preferences.loadGlobalDouble("BackFactor");
		FrontFactor = Preferences.loadGlobalDouble("FrontFactor");
		BoundFactor = Preferences.loadGlobalDouble("BoundFactor");

		xwv = Preferences.loadGlobalDouble("WorkingX(mm)"); // The RepRap machine...
		ywv = Preferences.loadGlobalDouble("WorkingY(mm)"); // ...working volume in mm.
		zwv = Preferences.loadGlobalDouble("WorkingZ(mm)");

		// Factors for front and back clipping planes and so on
		
		worldName = Preferences.loadGlobalString("WorldName");
		wv_offset = new Vector3d(Preferences.loadGlobalDouble("WorkingOffsetX(mm)"),
				Preferences.loadGlobalDouble("WorkingOffsetY(mm)"),
				Preferences.loadGlobalDouble("WorkingOffsetZ(mm)"));

		// The background, and other colours

		bgColour = new Color3f((float)Preferences.loadGlobalDouble("BackColourR(0..1)"), 
				(float)Preferences.loadGlobalDouble("BackColourG(0..1)"), 
				(float)Preferences.loadGlobalDouble("BackColourB(0..1)"));
		
		selectedColour = new Color3f((float)Preferences.loadGlobalDouble("SelectedColourR(0..1)"), 
				(float)Preferences.loadGlobalDouble("SelectedColourG(0..1)"), 
				(float)Preferences.loadGlobalDouble("SelectedColourB(0..1)"));

		machineColour = new Color3f((float)Preferences.loadGlobalDouble("MachineColourR(0..1)"), 
				(float)Preferences.loadGlobalDouble("MachineColourG(0..1)"), 
				(float)Preferences.loadGlobalDouble("MachineColourB(0..1)"));
			
		unselectedColour = new Color3f((float)Preferences.loadGlobalDouble("UnselectedColourR(0..1)"), 
				(float)Preferences.loadGlobalDouble("UnselectedColourG(0..1)"), 
				(float)Preferences.loadGlobalDouble("UnselectedColourB(0..1)"));
		} catch (Exception ex)
		{
			System.err.println("Refresh Panel3D preferences: " + ex.toString());
		}
				
		// End of stuff from the preferences file
		
		// ----------------------
	}


	protected void initialise() throws Exception {
		
		refreshPreferences();
		
//		default_app = new Appearance();
//		default_app.setMaterial(new Material(unselectedColour, black, unselectedColour, black, 0f));

		picked_app = new Appearance();
		picked_app.setMaterial(new Material(selectedColour, black, selectedColour, black, 0f));
		
//		extrusion_app = new Appearance();
//		extrusion_app.setMaterial(new Material(unselectedColour, black, unselectedColour, black, 101f));
		
//		shell_app = new Appearance();
//		shell_app.setMaterial(new Material(shellColour, black, shellColour, black, 101f));
		
		wv_app = new Appearance();
		wv_app.setMaterial(new Material(machineColour, black, machineColour, black, 0f));

		initJava3d();

	}

	// How far away is the back?
	protected double getBackClipDistance() {
		return BackFactor * getViewPlatformActivationRadius();
	}

	// How close is the front?
	protected double getFrontClipDistance() {
		return FrontFactor * getViewPlatformActivationRadius();
	}
	
	// Set up the size of the world
	protected Bounds createApplicationBounds() {
		applicationBounds = new BoundingSphere(new Point3d(xwv * 0.5,
				ywv * 0.5, zwv * 0.5), BoundFactor
				* getViewPlatformActivationRadius());
		return applicationBounds;
	}

	// (About) how big is the world?
	protected float getViewPlatformActivationRadius() {
		return (float) (RadiusFactor * Math.sqrt(xwv * xwv + ywv * ywv + zwv * zwv));
	}

	public Color3f getObjectColour()
	{
		return unselectedColour;
	}
	// Where are we in the file system?

	public static URL getWorkingDirectory() {
		try {
			File file = new File(System.getProperty("user.dir"));
			return file.toURI().toURL();
		} catch (Exception e) {
			System.err.println("getWorkingDirectory( ): can't get user dir.");
		}

		//return getCodeBase( );
		return null;
	}

	// Return handles on big things above where we are interested

	public VirtualUniverse getVirtualUniverse() {
		return universe;
	}

	protected View createView(ViewPlatform vp) {
		View view = new View();

		PhysicalBody pb = createPhysicalBody();
		PhysicalEnvironment pe = createPhysicalEnvironment();

		AudioDevice audioDevice = createAudioDevice(pe);

		if (audioDevice != null) {
			pe.setAudioDevice(audioDevice);
			audioDevice.initialize();
		}

		view.setPhysicalEnvironment(pe);
		view.setPhysicalBody(pb);

		if (vp != null)
			view.attachViewPlatform(vp);

		view.setBackClipDistance(getBackClipDistance());
		view.setFrontClipDistance(getFrontClipDistance());

		Canvas3D c3d = createCanvas3D();
		view.addCanvas3D(c3d);
		addCanvas3D(c3d);

		return view;
	}

	protected Canvas3D createCanvas3D() {
		GraphicsConfigTemplate3D gc3D = new GraphicsConfigTemplate3D();
		gc3D.setSceneAntialiasing(GraphicsConfigTemplate.PREFERRED);
		GraphicsDevice gd[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		
		Canvas3D c3d = new Canvas3D(gd[0].getBestConfiguration(gc3D));
		//c3d.setSize(getCanvas3dWidth(c3d), getCanvas3dHeight(c3d));

		return c3d;
	}

	public javax.media.j3d.Locale getFirstLocale() {
		java.util.Enumeration<?> en = universe.getAllLocales();

		if (en.hasMoreElements() != false)
			return (javax.media.j3d.Locale) en.nextElement();

		return null;
	}

	// The size of the world

	protected Bounds getApplicationBounds() {
		if (applicationBounds == null)
			applicationBounds = createApplicationBounds();

		return applicationBounds;
	}

	// Fire up Java3D

	public void initJava3d() throws Exception {
		universe = createVirtualUniverse();

		javax.media.j3d.Locale locale = createLocale(universe);

		BranchGroup sceneBranchGroup = createSceneBranchGroup();

		ViewPlatform vp = createViewPlatform();
		BranchGroup viewBranchGroup = createViewBranchGroup(
				getViewTransformGroupArray(), vp);

		createView(vp);

		Background background = createBackground();

		if (background != null)
			sceneBranchGroup.addChild(background);

		locale.addBranchGraph(sceneBranchGroup);
		addViewBranchGroup(locale, viewBranchGroup);

	}


	protected PhysicalBody createPhysicalBody() {
		return new PhysicalBody();
	}

	protected AudioDevice createAudioDevice(PhysicalEnvironment pe) {
		JavaSoundMixer javaSoundMixer = new JavaSoundMixer(pe);

		if (javaSoundMixer == null)
			System.err.println("create of audiodevice failed");

		return javaSoundMixer;
	}

	protected PhysicalEnvironment createPhysicalEnvironment() {
		return new PhysicalEnvironment();
	}

	protected ViewPlatform createViewPlatform() {
		ViewPlatform vp = new ViewPlatform();
		vp.setViewAttachPolicy(View.RELATIVE_TO_FIELD_OF_VIEW);
		vp.setActivationRadius(getViewPlatformActivationRadius());

		return vp;
	}

	// These two are probably wrong.

	protected int getCanvas3dWidth(Canvas3D c3d) {
		return getWidth();
	}

	protected int getCanvas3dHeight(Canvas3D c3d) {
		return getHeight();
	}

	protected VirtualUniverse createVirtualUniverse() {
		return new VirtualUniverse();
	}

	protected void addViewBranchGroup(javax.media.j3d.Locale locale,
			BranchGroup bg) {
		locale.addBranchGraph(bg);
	}

	protected javax.media.j3d.Locale createLocale(VirtualUniverse u) {
		return new javax.media.j3d.Locale(u);
	}

	public TransformGroup[] getViewTransformGroupArray() {
		TransformGroup[] tgArray = new TransformGroup[1];
		tgArray[0] = new TransformGroup();

		Transform3D viewTrans = new Transform3D();
		Transform3D eyeTrans = new Transform3D();

		BoundingSphere sceneBounds = (BoundingSphere) sceneBranchGroup
				.getBounds();

		// point the view at the center of the object

		Point3d center = new Point3d();
		sceneBounds.getCenter(center);
		double radius = sceneBounds.getRadius();
		Vector3d temp = new Vector3d(center);
		viewTrans.set(temp);

		// pull the eye back far enough to see the whole object

		double eyeDist = radius / Math.tan(Math.toRadians(40) / 2.0);
		temp.x = 0.0;
		temp.y = 0.0;
		temp.z = eyeDist;
		eyeTrans.set(temp);
		viewTrans.mul(eyeTrans);

		// set the view transform

		tgArray[0].setTransform(viewTrans);

		return tgArray;
	}

	protected void addCanvas3D(Canvas3D c3d) {
		setLayout(new BorderLayout());
		add(c3d, BorderLayout.CENTER);
		doLayout();
		c3d.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
//	protected void addBlock(BranchGroup root, Appearance appearance,
//			double x1, double y1, double z1,
//			double x2, double y2, double z2,
//			float width, float height) {
//		root.addChild(addRectangularSegment(appearance, x1, y1, z1, x2, y2, z2, width, height));
//	}
//
//	protected void addBlock(TransformGroup root, Appearance appearance,
//			double x1, double y1, double z1,
//			double x2, double y2, double z2,
//			float width, float height) {
//		root.addChild(addRectangularSegment(appearance, x1, y1, z1, x2, y2, z2, width, height));
//	}
//	
//	protected TransformGroup addRectangularSegment(Appearance appearance,
//			double x1, double y1, double z1,
//			double x2, double y2, double z2,
//			float width, float height) {
//		
//		z1 += width / 2.0;
//		z2 += width / 2.0;
//		
//		Point3d p1 = new Point3d(x1, y1, z1);
//		//Point3d p2 = new Point3d(x2, y2, z2);
//
//		Vector3d unity = new Vector3d(0, 1, 0);
//		Vector3d v = new Vector3d(x2 - x1, y2 - y1, z2 - z1);
//		
//		Primitive segment = new Box(width, (float)v.length() / 2.0f, height, appearance);
//		
//		Transform3D transform = new Transform3D();
//		
//		Vector3d translate = new Vector3d(p1);
//		v.scale(0.5);
//		translate.add(v);
//		transform.setTranslation(translate);
//				
//		double angle = v.angle(unity);
//		Vector3d axis = new Vector3d();
//		axis.cross(unity, v);
//		AxisAngle4d rotationAngle = new AxisAngle4d(axis.x, axis.y, axis.z, angle);
//		transform.setRotation(rotationAngle);
//		
//		TransformGroup tg = new TransformGroup(transform);
//		tg.addChild(segment);
//		return tg;
//	}
//	
//	protected TransformGroup addCylindricalSegment(Appearance appearance,
//			double x1, double y1, double z1,
//			double x2, double y2, double z2,
//			float thickness) {
//		
//		Point3d p1 = new Point3d(x1, y1, z1);
//		//Point3d p2 = new Point3d(x2, y2, z2);
//
//		Vector3d unity = new Vector3d(0, 1, 0);
//		Vector3d v = new Vector3d(x2 - x1, y2 - y1, z2 - z1 + thickness / 2.0);
//		
//		Primitive segment = new Cylinder(thickness, (float)v.length(), appearance);
//		
//		Transform3D transform = new Transform3D();
//		
//		Vector3d translate = new Vector3d(p1);
//		v.scale(0.5);
//		translate.add(v);
//		transform.setTranslation(translate);
//				
//		double angle = v.angle(unity);
//		Vector3d axis = new Vector3d();
//		axis.cross(unity, v);
//		AxisAngle4d rotationAngle = new AxisAngle4d(axis.x, axis.y, axis.z, angle);
//		transform.setRotation(rotationAngle);
//		
//		TransformGroup tg = new TransformGroup(transform);
//		tg.addChild(segment);
//		return tg;
//	}
	
	protected double getScale() {
		return 1.0;
	}

//	protected String getStlBackground() throws Exception {
//		String path = getStlBackground("");
//		if (path != null)
//			return path;
//		path = getStlBackground("lib/");
//		if (path != null)
//			return path;
//                path = getStlBackground("../lib/");
//		if (path != null)
//			return path;
//                
//                // for eD's version of a NetBeans project - improvements welcome!
//		path = getStlBackground("../host/lib/");
//		if (path != null)
//			return path;
//		
//		throw new Exception("Cannot locate background STL file");
//	}
	
	protected String getStlBackground() throws Exception {
		
		URL u = ClassLoader.getSystemResource(wv_location);
		if(u != null)
		{
			String name = u.toString();
			//System.out.println("**- " + name);
			return name;
		}
		
		throw new Exception("Cannot locate background STL file");
	}
	
//	protected String getStlBackground(String subdir) {
//		URL codebase = null;
//		String stlURL = null;
//		String stlPath = null;
//
//		try {
////			codebase = Panel3D.getWorkingDirectory();
////			stlPath = codebase.getPath() + subdir + wv_location;
////			stlURL = codebase.toExternalForm() + subdir + wv_location;
//			URL u = ClassLoader.getSystemResource(wv_location);
//			stlPath = u.getPath();
//			stlURL = u.toExternalForm();//u.getFile();
////			System.out.println(stlPath + " : " + stlURL);
//		} catch (Exception e) {
//			System.err
//					.println("createSceneBranchGroup(): Exception finding working directory: "
//							+ codebase.toExternalForm());
//			e.printStackTrace();
//		}
//		
//		File f = new File(stlPath);
//		if (f.exists())
//			return stlURL;
//		
//		return null;
//
//	}
}