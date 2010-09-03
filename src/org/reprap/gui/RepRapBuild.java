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
 
 This program loads STL files of objects, orients them, and builds them
 in the RepRap machine.
 
 It is based on one of the open-source examples in Daniel Selman's excellent
 Java3D book, and his notice is immediately below.
 
 First version 2 April 2006
 This version: 16 April 2006
 
 */

/*******************************************************************************
 * VrmlPickingTest.java Copyright (C) 2001 Daniel Selman
 * 
 * First distributed with the book "Java 3D Programming" by Daniel Selman and
 * published by Manning Publications. http://manning.com/selman
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * The license can be found on the WWW at: http://www.fsf.org/copyleft/gpl.html
 * 
 * Or by writing to: Free Software Foundation, Inc., 59 Temple Place - Suite
 * 330, Boston, MA 02111-1307, USA.
 * 
 * Authors can be contacted at: Daniel Selman: daniel@selman.org
 * 
 * If you make changes you think others would like, please contact one of the
 * authors or someone at the www.j3d.org web site.
 ******************************************************************************/

package org.reprap.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.Transform3D;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;

import org.reprap.Attributes;
import org.reprap.RFO;
import org.reprap.Preferences;
import org.reprap.geometry.polygons.AllSTLsToBuild;

/**
 * Little class to put up a radiobutton menu so you can set
 * what material something is to be made from.
 * 
 * @author adrian
 *
 */
class MaterialRadioButtons extends JPanel {
	private static final long serialVersionUID = 1L;
	private static Attributes att;
	private static JFrame frame;
	private static JTextField copies;
	private static RepRapBuild rrb;
	private static int stlIndex; 
	
	private MaterialRadioButtons()
	{
		super(new BorderLayout());
		JPanel radioPanel;
		ButtonGroup bGroup = new ButtonGroup();
		String[] names;
		radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.setSize(300,200);
		
	    JLabel jLabel2 = new JLabel();
	    radioPanel.add(jLabel2);
	    jLabel2.setText(" Number of copies of the object just loaded to print: ");
		jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		copies = new JTextField("1");
		radioPanel.add(copies);
		copies.setHorizontalAlignment(SwingConstants.CENTER);
		
		
		JLabel jLabel1 = new JLabel();
		radioPanel.add(jLabel1);
		jLabel1.setText(" Select the material for the object(s): ");
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		
		try
		{
			names = Preferences.allMaterials();
			att.setMaterial(names[0]);
			for(int i = 0; i < names.length; i++)
			{
				JRadioButton b = new JRadioButton(names[i]);
		        b.setActionCommand(names[i]);
		        b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						att.setMaterial(e.getActionCommand());
					}});
		        if(i == 0)
		        	b.setSelected(true);
		        bGroup.add(b);
		        radioPanel.add(b);
			}
			
			JButton okButton = new JButton();
			radioPanel.add(okButton);
			okButton.setText("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					OKHandler();
				}
			});
			
			add(radioPanel, BorderLayout.LINE_START);
			setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			
		} catch (Exception ex)
		{
			System.err.println(ex.toString());
			ex.printStackTrace();
		}	
	}
	
	public static void OKHandler()
	{
		//System.out.println("Copies: " + copies.getText());
		int number = Integer.parseInt(copies.getText().trim()) - 1;
		STLObject stl = rrb.getSTLs().get(stlIndex);
		rrb.moreCopies(stl, att, number);
		frame.dispose();
	}
    
    public static void createAndShowGUI(Attributes a, RepRapBuild r, int index) 
    {
    	att = a;
    	rrb = r;
    	stlIndex = index;
        //Create and set up the window.
    	frame = new JFrame("Material selector");
        frame.setLocation(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new MaterialRadioButtons();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }	   
	
}

//************************************************************************

/**
 * This is the main public class that creates a virtual world of the RepRap
 * working volume, allows you to put STL-file objects in it, move them about
 * to arrange them, and build them in the machine.
 */

public class RepRapBuild extends Panel3D implements MouseListener {
	

	
	
	private static final long serialVersionUID = 1L;
	private MouseObject mouse = null;
	private PickCanvas pickCanvas = null; // The thing picked by a mouse click
	private STLObject lastPicked = null; // The last thing picked
	//private java.util.List<STLObject> stls = new ArrayList<STLObject>(); // All the STLObjects to be built
	private AllSTLsToBuild stls;
	//private int objectIndex = 0; // Counter for STLs as they are loaded
	private boolean reordering;

	// Constructors
	public RepRapBuild() throws Exception {
		initialise();
		stls = new AllSTLsToBuild();
		reordering = false;
	}
	
	public AllSTLsToBuild getSTLs()
	{
		return stls;
	}
	
	/**
	 * Set the material to make an STL object from.
	 * @param stl 
	 */
//	private void getMaterialName(STLObject stl)
//	{
//		try {
//			MaterialRadioButtons.createAndShowGUI(stl);
//		}
//      	catch (Exception ex) {
//     		JOptionPane.showMessageDialog(null, "RepRapBuild material select exception: " + ex);
// 			ex.printStackTrace();
//     	}
//	}
	
	// Set bg light grey
	protected Background createBackground() {
		Background back = new Background(bgColour);
		back.setApplicationBounds(createApplicationBounds());
		return back;
	}

	protected BranchGroup createViewBranchGroup(TransformGroup[] tgArray,
			ViewPlatform vp) {
		BranchGroup vpBranchGroup = new BranchGroup();

		if (tgArray != null && tgArray.length > 0) {
			Group parentGroup = vpBranchGroup;
			TransformGroup curTg = null;

			for (int n = 0; n < tgArray.length; n++) {
				curTg = tgArray[n];
				parentGroup.addChild(curTg);
				parentGroup = curTg;
			}

			tgArray[tgArray.length - 1].addChild(vp);
		} else
			vpBranchGroup.addChild(vp);

		return vpBranchGroup;
	}

	// Set up the RepRap working volume

	protected BranchGroup createSceneBranchGroup() throws Exception {
		sceneBranchGroup = new BranchGroup();

		BranchGroup objRoot = sceneBranchGroup;

		Bounds lightBounds = getApplicationBounds();

		AmbientLight ambLight = new AmbientLight(true, new Color3f(1.0f, 1.0f,
				1.0f));
		ambLight.setInfluencingBounds(lightBounds);
		objRoot.addChild(ambLight);

		DirectionalLight headLight = new DirectionalLight();
		headLight.setInfluencingBounds(lightBounds);
		objRoot.addChild(headLight);

		mouse = new MouseObject(getApplicationBounds(), mouse_tf, mouse_zf);

		wv_and_stls.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		wv_and_stls.setCapability(Group.ALLOW_CHILDREN_WRITE);
		wv_and_stls.setCapability(Group.ALLOW_CHILDREN_READ);

		// Load the STL file for the working volume

		world = new STLObject(wv_and_stls, worldName);

		String stlFile = getStlBackground();

		workingVolume = new STLObject();
		workingVolume.addSTL(stlFile, wv_offset, wv_app, null);
		wv_and_stls.addChild(workingVolume.top());

		// Set the mouse to move everything

		mouse.move(world, false);
		objRoot.addChild(world.top());

		return objRoot;
	}

	// Action on mouse click

	public void mouseClicked(MouseEvent e) {
		pickCanvas.setShapeLocation(e);

		PickResult pickResult = pickCanvas.pickClosest();
		STLObject picked = null;

		if (pickResult != null) // Got anything?
		{
			Node actualNode = pickResult.getObject();

			Attributes att = (Attributes)actualNode.getUserData();
			picked = att.getParent();
			if (picked != null) // Really got something?
			{
				if (picked != workingVolume) // STL object picked?
				{
					//picked = findSTL(name);
					if (picked != null) {
						picked.setAppearance(picked_app); // Highlight it
						if (lastPicked != null  && !reordering)
							lastPicked.restoreAppearance(); // lowlight
						// the last
						// one
						if(!reordering)
							mouse.move(picked, true); // Set the mouse to move it
						lastPicked = picked; // Remember it
						reorder();
					}
				} else { // Picked the working volume - deselect all and...
					if(!reordering)
						mouseToWorld();
				}
			}
		}
	}
	
	public void mouseToWorld()
	{
		if (lastPicked != null)
			lastPicked.restoreAppearance();
		mouse.move(world, false); // ...switch the mouse to moving the world
		lastPicked = null;
	}

	// Find the stl object in the scene with the given name

//	protected STLObject findSTL(String name) {
//		STLObject stl;
//		for (int i = 0; i < stls.size(); i++) {
//			stl = stls.get(i);
//			if (stl.name == name)
//				return stl;
//		}
//		return null;
//	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	// Callback for when the user selects an STL file to load

	public void anotherSTLFile(String s) 
	{
		if (s == null)
			return;
		//objectIndex++;
		STLObject stl = new STLObject();
		Attributes att = stl.addSTL(s, null, Preferences.unselectedApp(), lastPicked);
		if(att != null)
		{
			// New separate object, or just appended to lastPicked?
			if(stl.numChildren() > 0)
			{
				wv_and_stls.addChild(stl.top());
				stls.add(stl);
			}
			MaterialRadioButtons.createAndShowGUI(att, this, stls.size() - 1);
		}
	}
	
	// Callback for when the user selects an RFO file to load

	public void addRFOFile(String s) 
	{
		if (s == null)
			return;
		//deleteAllSTLs();
		AllSTLsToBuild newStls = RFO.load(s);
		for(int i = 0; i < newStls.size(); i++)
			wv_and_stls.addChild(newStls.get(i).top());
		stls.add(newStls);
	}
	
	public void saveRFOFile(String s)
	{
		RFO.save(s, stls);
	}
	
	public void moreCopies(STLObject original, Attributes originalAttributes, int number)
	{
		if (number <= 0)
			return;
		String fileName = original.fileItCameFrom(0);
		Vector3d offset = new Vector3d();
		offset.y = 0;
		offset.z = 0;
		double increment = original.extent().x + 5;
		offset.x = increment;
		for(int i = 0; i < number; i++)
		{
			STLObject stl = new STLObject();
			Attributes newAtt = stl.addSTL(fileName, null, original.getAppearance(), null);
			newAtt.setMaterial(originalAttributes.getMaterial());
			if(newAtt != null)
			{
				Transform3D t3d1 = original.getTransform();
				Transform3D t3d2 = new Transform3D();
				t3d2.set(new Vector3d(offset));
				t3d1.mul(t3d2);
				stl.setTransform(t3d1);
				// New separate object, or just appended to lastPicked?
				if(stl.numChildren() > 0)
				{
					wv_and_stls.addChild(stl.top());
					stls.add(stl);
				}
			}
			offset.x += increment;
		}
	}

	public void start() throws Exception {
		if (pickCanvas == null)
			initialise();
	}

	protected void addCanvas3D(Canvas3D c3d) {
		setLayout(new BorderLayout());
		add(c3d, BorderLayout.CENTER);
		doLayout();

		if (sceneBranchGroup != null) {
			c3d.addMouseListener(this);

			pickCanvas = new PickCanvas(c3d, sceneBranchGroup);
			pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
			pickCanvas.setTolerance(4.0f);
		}

		c3d.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	// Callbacks for when the user rotates the selected object

	public void xRotate() {
		if (lastPicked != null)
			lastPicked.xClick();
	}

	public void yRotate() {
		if (lastPicked != null)
			lastPicked.yClick();
	}

	public void zRotate() {
		if (lastPicked != null)
			lastPicked.zClick();
	}
	
	// Callback for a request to convert units
	
	public void inToMM() {
		if (lastPicked != null)
			lastPicked.inToMM();
	}
	
	public void doReorder()
	{
		if (lastPicked != null)
		{
			lastPicked.restoreAppearance();
			mouseToWorld();
			lastPicked = null;
		}
		reordering = true;
	}
	
	/**
	 * User is reordering the list
	 */
	private void reorder()
	{
		if(!reordering)
			return;
		if(stls.reorderAdd(lastPicked))
			return;
		for(int i = 0; i < stls.size(); i++)
			stls.get(i).restoreAppearance();
		//mouseToWorld();		
		lastPicked = null;
		reordering = false;
	}
	
	// Move to the next one in the list
	
	public void nextPicked()
	{
		if (lastPicked == null)
			lastPicked = stls.get(0);
		else
		{
			lastPicked.restoreAppearance();
			lastPicked = stls.getNextOne(lastPicked);
		}
		lastPicked.setAppearance(picked_app);
		mouse.move(lastPicked, true);
	}
	
//	public void materialSTL()
//	{
//		if (lastPicked == null)
//			return;
//		getMaterialName(lastPicked);
//		mouseToWorld();
//	}
	
	// Callback to delete one of the loaded objects
	
	public void deleteSTL()
	{
		if (lastPicked == null)
			return;
		int index = -1;
		for(int i = 0; i < stls.size(); i++)
		{
			if(stls.get(i) == lastPicked)
			{
				index = i;
				break;
			}
		}
		if (index >= 0) 
		{
			stls.remove(index);
			index = wv_and_stls.indexOfChild(lastPicked.top());
			mouseToWorld();
			wv_and_stls.removeChild(index);
			lastPicked = null;
		}
	}
	
	public void deleteAllSTLs()
	{
		for(int i = 0; i < stls.size(); i++)
		{
			STLObject s = stls.get(i);
			stls.remove(i);
			int index = wv_and_stls.indexOfChild(s.top());
			wv_and_stls.removeChild(index);
		}
		mouseToWorld();
		lastPicked = null;
	}

}