/*
 * Created on Mar 29, 2006
 *
 */
package org.reprap;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.reprap.geometry.Producer;
import org.reprap.machines.MachineFactory;
import org.reprap.gui.RepRapBuild;
import org.reprap.gui.Utility;
import org.reprap.gui.botConsole.BotConsoleFrame;
import org.reprap.comms.Communicator;
import org.reprap.utilities.ExtensionFileFilter;
import org.reprap.utilities.RrDeleteOnExit;
import org.reprap.utilities.Debug;

/**
 *
 * mainpage RepRap Host Controller Software
 * 
 * section overview Overview
 * 
 * Please see http://reprap.org/ for more details.
 *  
 */


public class Main {
	
	public static RrDeleteOnExit ftd = null;

    private static Communicator communicator = null;
    
    private static boolean repRapAttached = false;
    
    private Producer producer = null;
    
    private Printer printer = null;
    
    // Window to walk the file tree
    
    private JFileChooser chooser;
    private JFrame mainFrame;
    private RepRapBuild builder;
//    private PreviewPanel preview = null;
//    private JCheckBoxMenuItem viewBuilder;
//    private JCheckBoxMenuItem viewPreview;
    private JCheckBoxMenuItem segmentPause;
    private JCheckBoxMenuItem layerPause;
    
    private JMenuItem cancelMenuItem;
    private JMenuItem produceProduceB;

    public void setSegmentPause(boolean state) {
        segmentPause.setState(state);
    }
    
    public void setLayerPause(boolean state) {
        layerPause.setState(state);
    }
    
    public void clickCancel() {
        cancelMenuItem.doClick();
    }
    
    private JSplitPane panel;
	
	public Main() {
		ftd = new RrDeleteOnExit();
        chooser = new JFileChooser();
 
        // Do we want just to list .stl files, or all?
        // If all, comment out the next two lines
        
        FileFilter filter = new ExtensionFileFilter("STL", new String[] { "STL" });
        chooser.setFileFilter(filter);
        
        try
        {
        	printer = MachineFactory.create();
        } catch (Exception ex)
        {
        	System.err.println("MachineFactory.create() failed.\n");
        	ex.printStackTrace();
        }
	}

	private void createAndShowGUI() throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(false);
        mainFrame = new JFrame("RepRap             grid: 20 mm");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Required so menus float over Java3D
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        // Create menus
        JMenuBar menubar = new JMenuBar();
        
        //JMenu fileMenu = new JMenu("File");
        //fileMenu.setMnemonic(KeyEvent.VK_F);
        //menubar.add(fileMenu);
        
        //JMenuItem fileOpen = new JMenuItem("Open...", KeyEvent.VK_O);
        //fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        //fileOpen.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		onOpen();
		//	}});
        //fileMenu.add(fileOpen);
        
        //fileMenu.addSeparator();

        //JMenuItem filePrefs = new JMenuItem("Preferences...", KeyEvent.VK_R);
        //filePrefs.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		Preferences prefs = new Preferences();
		//		prefs.setVisible(true);	// prefs.show();
		//	}});
        //fileMenu.add(filePrefs);

        //fileMenu.addSeparator();

        //JMenuItem fileExit = new JMenuItem("Exit", KeyEvent.VK_X);
        //fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        //fileExit.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		dispose();
		//	}});
        //fileMenu.add(fileExit);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menubar.add(viewMenu);

//        JMenuItem viewToggle = new JMenuItem("Toggle view", KeyEvent.VK_V);
//        viewToggle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
//        viewToggle.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				onViewToggle();
//			}});
//        viewMenu.add(viewToggle);
        
//        viewBuilder = new JCheckBoxMenuItem("Setup build");
//        viewBuilder.setSelected(true);
//        viewBuilder.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				onViewBuilder();
//			}});
//        viewMenu.add(viewBuilder);
        
//        viewPreview = new JCheckBoxMenuItem("Progress");
//        viewPreview.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				onViewPreview();
//			}});
//        viewMenu.add(viewPreview);

        
        JMenu manipMenu = new JMenu("Manipulate");
        manipMenu.setMnemonic(KeyEvent.VK_M);
        menubar.add(manipMenu);

        JMenuItem manipX = new JMenuItem("Rotate X", KeyEvent.VK_X);
        manipX.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        manipX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRotateX();
			}});
        manipMenu.add(manipX);

        JMenuItem manipY = new JMenuItem("Rotate Y", KeyEvent.VK_Y);
        manipY.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        manipY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRotateY();
			}});
        manipMenu.add(manipY);

        JMenuItem manipZ = new JMenuItem("Rotate Z", KeyEvent.VK_Z);
        manipZ.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        manipZ.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onRotateZ();
			}});
        manipMenu.add(manipZ);
        
        JMenuItem inToMM = new JMenuItem("Scale by 25.4 (in -> mm)", KeyEvent.VK_I);
        inToMM.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        inToMM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				oninToMM();
			}});
        manipMenu.add(inToMM);
        
        JMenuItem nextP = new JMenuItem("Select next object that will be built", KeyEvent.VK_N);
        nextP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        nextP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onNextPicked();
			}});
        manipMenu.add(nextP);
        
        JMenuItem reorder = new JMenuItem("Reorder the building sequence", KeyEvent.VK_R);
        reorder.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        reorder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onReorder();
			}});
        manipMenu.add(reorder);
        
        
        JMenuItem deleteSTLW = new JMenuItem("Delete selected object", KeyEvent.VK_W);
        deleteSTLW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        deleteSTLW.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onDelete();
			}});
        manipMenu.add(deleteSTLW);
        
        JMenuItem deleteSTL = new JMenuItem("Delete selected object", KeyEvent.VK_DELETE);
        deleteSTL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteSTL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onDelete();
			}});
        manipMenu.add(deleteSTL);
        
        

        //JMenu produceMenu = new JMenu("Build");
        //produceMenu.setMnemonic(KeyEvent.VK_P);
        //menubar.add(produceMenu);

        
        produceProduceB = new JMenuItem("Start build...", KeyEvent.VK_B);
        produceProduceB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        produceProduceB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onProduceB();
			}});
        //produceMenu.add(produceProduceB);

        cancelMenuItem = new JMenuItem("Cancel", KeyEvent.VK_P);
        cancelMenuItem.setEnabled(false);
        cancelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(producer != null)
					producer.setCancelled(true);
			}});
        //produceMenu.add(cancelMenuItem);
        
        
        
        //produceMenu.addSeparator();

        segmentPause = new JCheckBoxMenuItem("Pause before segment");
        //produceMenu.add(segmentPause);

        layerPause = new JCheckBoxMenuItem("Pause before layer");
        //produceMenu.add(layerPause);

        //produceMenu.addSeparator();
        
        //JMenuItem estimateMenuItemB = new JMenuItem("Estimate build resources...", KeyEvent.VK_E);
        //estimateMenuItemB.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		estimateResourcesB();
		//	}});
        //produceMenu.add(estimateMenuItemB);
                
        //JMenu toolsMenu = new JMenu("Tools");
        //toolsMenu.setMnemonic(KeyEvent.VK_T);
        //menubar.add(toolsMenu);
        
        //JMenuItem toolsWorkingVolume = new JMenuItem("Working volume probe", KeyEvent.VK_W);
        //toolsWorkingVolume.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		try {
		//			new org.reprap.gui.steppertest.WorkingVolumeFrame();
		//		}
        //     	catch (Exception ex) {
        //     		JOptionPane.showMessageDialog(null, "Working volume probe exception: " + ex);
        // 			ex.printStackTrace();
        //     	}
		//	}});
        //toolsMenu.add(toolsWorkingVolume);
        
      //JMenuItem toolsMaintenancePositions = new JMenuItem("Maintenance positions", KeyEvent.VK_M);
      //toolsMaintenancePositions.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		try {
		//			new org.reprap.gui.steppertest.MaintenancePositionsFrame();
		//		}
        //    	catch (Exception ex) {
        //   		JOptionPane.showMessageDialog(null, "Maintenance positions exception: " + ex);
       	//		ex.printStackTrace();
        //   	}
		//	}});
      //toolsMenu.add(toolsMaintenancePositions);
        
        //JMenuItem toolsExerciser = new JMenuItem("Stepper exerciser", KeyEvent.VK_S);
        //toolsExerciser.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		try {
		//			org.reprap.gui.steppertest.Main.main(null);
		//		}
        //      	catch (Exception ex) {
        //    		JOptionPane.showMessageDialog(null, "Stepper exerciser exception: " + ex);
        // 			ex.printStackTrace();
        //     	}
		//	}});
        //toolsMenu.add(toolsExerciser);

        //JMenuItem toolsExtruderExerciser = new JMenuItem("Extruder exerciser", KeyEvent.VK_E);
        //toolsExtruderExerciser.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		try {
		//			org.reprap.gui.extrudertest.Main.main(null);
		//		}
        //      	catch (Exception ex) {
        //     		JOptionPane.showMessageDialog(null, "Extruder exerciser exception: " + ex);
        // 			ex.printStackTrace();
        //     	}
		//	}});
        //toolsMenu.add(toolsExtruderExerciser);

        //JMenuItem toolsExtruderProfiler = new JMenuItem("Extruder heat profiler", KeyEvent.VK_H);
        //toolsExtruderProfiler.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		try {
		//			org.reprap.gui.extruderprofile.Main.main(null);
		//		}
        //      	catch (Exception ex) {
        //     		JOptionPane.showMessageDialog(null, "Extruder profiler exception: " + ex);
        // 			ex.printStackTrace();
        //     	}
		//	}});
        //toolsMenu.add(toolsExtruderProfiler);

        

        //JMenu diagnosticsMenu = new JMenu("Diagnostics");
        //toolsMenu.add(diagnosticsMenu);
        //JMenuItem diagnosticsCommsTest = new JMenuItem("Basic comms test");
        //diagnosticsMenu.add(diagnosticsCommsTest);

        // Create the main window area
        // This is a horizontal box layout that includes
        // both the builder and preview screens, one of
        // which may be invisible.

        Box builderFrame = new Box(BoxLayout.Y_AXIS);
        builderFrame.add(new JLabel("Setup build"));
        builder = new RepRapBuild();
        builderFrame.setMinimumSize(new Dimension(0,0));
        builderFrame.add(builder);
        
        panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); 
        panel.setPreferredSize(Utility.getDefaultAppSize());
        panel.setMinimumSize(new Dimension(0, 0));
        panel.setResizeWeight(0.5);
        panel.setOneTouchExpandable(true);
        panel.setContinuousLayout(true);
        panel.setLeftComponent(builderFrame);
//        if(org.reprap.Preferences.loadGlobalBool("DisplaySimulation"))
//        	panel.setRightComponent(createPreviewPanel());
//        else
//        	preview = null;
        panel.setDividerLocation(panel.getPreferredSize().width);
        
        mainFrame.getContentPane().add(panel);
                
        mainFrame.setJMenuBar(menubar);
        
        mainFrame.pack();
        Utility.positionWindowOnScreen(mainFrame);
        mainFrame.setVisible(true);
	}
	
	protected void finalize() throws Throwable
	{
		Debug.d("Main finalise()");
		ftd.killThem();
	}

//	private Box createPreviewPanel() throws Exception {
//		
//        Box pane = new Box(BoxLayout.Y_AXIS);
//        pane.add(new JLabel("Build progress"));
//		preview = new PreviewPanel();
//		pane.setMinimumSize(new Dimension(0,0));
//		if(preview != null)
//			pane.add(preview);
//		
//		return pane;
//	}
	
//	private void onProduceT() {
//        cancelMenuItem.setEnabled(true);
//        produceProduceT.setEnabled(false);
//		Thread t = new Thread() {
//			public void run() {
//				Thread.currentThread().setName("Producer");
//				try {
//					// TODO Some kind of progress indicator would be good
//					
//					if (!viewPreview.isSelected()) {
//						viewPreview.setSelected(true);
//						updateView();
//					}
//
////					if(preview != null)
////					{
////						preview.setSegmentPause(segmentPause);
////						preview.setLayerPause(layerPause);
////					}
//					
//					producer = new Producer(printer, preview, builder);
//					producer.setSegmentPause(segmentPause);
//					producer.setLayerPause(layerPause);
//					producer.produce();
//					String usage = getResourceMessage(producer);
//					producer.dispose();
//					producer = null;
//			        cancelMenuItem.setEnabled(false);
//			        produceProduceT.setEnabled(true);
//					JOptionPane.showMessageDialog(mainFrame, "Production complete.  " +
//							usage);
//				}
//				catch (Exception ex) {
//					JOptionPane.showMessageDialog(mainFrame, "Production exception: " + ex);
//					ex.printStackTrace();
//				}
//			}
//		};
//		t.start();
//	}
	
	/**
	 * Return the printer being used
	 */
	public Printer getPrinter()
	{
		return printer;
	}
	
	/**
	 * Stop production
	 *
	 */
	public void pause()
	{
		if(producer != null)
			producer.pause();
		try
		{
			printer.stopMotor();
			printer.stopValve();
			printer.pause();
		} catch (Exception ex) {}
	}
	
	/**
	 * Resume production
	 * NB: does not re-start the extruder
	 *
	 */
	public void resume()
	{
		printer.resume();
		if(producer != null)
			producer.resume();
	}
	
	public int getLayers()
	{
		if(producer == null)
			return 0;
		return producer.getLayers();
	}
	
	public int getLayer()
	{
		if(producer == null)
			return 0;
		return producer.getLayer();
	}
	
	public void onProduceB() {
        cancelMenuItem.setEnabled(true);
        produceProduceB.setEnabled(false);
		Thread t = new Thread() {
			public void run() {
				Thread.currentThread().setName("Producer");
				try {
					
//					if (!viewPreview.isSelected()) {
//						viewPreview.setSelected(true);
//						updateView();
//					}
					
//					if(preview != null)
//					{
//						preview.setSegmentPause(segmentPause);
//						preview.setLayerPause(layerPause);
//					}
					
					if(printer == null)
						System.err.println("Production attempted with null printer.");
					producer = new Producer(printer, builder);
					producer.setSegmentPause(segmentPause);
					producer.setLayerPause(layerPause);
					producer.produce();
					String usage = getResourceMessage(producer);
					producer.dispose();
					producer = null;
			        cancelMenuItem.setEnabled(false);
			        produceProduceB.setEnabled(true);
					JOptionPane.showMessageDialog(mainFrame, "Production complete.");// +	usage);
				}
				catch (Exception ex) {
					JOptionPane.showMessageDialog(mainFrame, "Production exception: " + ex);
					ex.printStackTrace();
				}
			}
		};
		t.start();
	}
	
    public File onOpen(String description, String[] extensions, String defaultRoot) 
    {
        String result = null;
        File f;
        FileFilter filter = new ExtensionFileFilter(description, extensions);

        chooser.setFileFilter(filter);
        if(!defaultRoot.contentEquals("") && extensions.length == 1)
        {
        	File defaultFile = new File(defaultRoot + "." + extensions[0]);
        	chooser.setSelectedFile(defaultFile);
        }
        
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            f = chooser.getSelectedFile();
            result = "file:" + f.getAbsolutePath();
            if(extensions[0].toUpperCase().contentEquals("RFO"))
            	builder.addRFOFile(result);
            if(extensions[0].toUpperCase().contentEquals("STL"))
            	builder.anotherSTLFile(result);

            return f;
        }
        return null;
    }
    
    public String saveRFO(String fileRoot)
    {
        String result = null;
        File f;
        FileFilter filter;
        
        
		File defaultFile = new File(fileRoot + ".rfo");
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(defaultFile);
		filter = new ExtensionFileFilter("RFO file to write to", new String[] { "rfo" });
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
        chooser.setFileFilter(filter);

        int returnVal = chooser.showSaveDialog(null);// chooser.showOpenDialog(mainFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION) 
        {
            f = chooser.getSelectedFile();
            result = "file:" + f.getAbsolutePath();

            	builder.saveRFOFile(result);
            return f.getName();
        }
        return "";   	
    }
    
    public void deleteAllSTLs()
    {
    	builder.deleteAllSTLs();
    }
    
    private void onRotateX() {
    	  builder.xRotate();
    }

    private void onRotateY() {
  	  builder.yRotate();
    }

    private void onRotateZ() {
  	  builder.zRotate();
    }
    
    private void oninToMM() {
    	  builder.inToMM();
      } 
    
    private void onNextPicked()
    {
    	builder.nextPicked();
    }
    
    private void onReorder()
    {
    	builder.doReorder();
    }
    
//    private void onMaterial() {
//  	  builder.materialSTL();
//    }
    
    private void onDelete() {
    	  builder.deleteSTL();
      }
    
    public void mouseToWorld()
    {
    	builder.mouseToWorld();
    }

//	private void onViewBuilder() {
////    		if (!viewBuilder.isSelected() && !viewPreview.isSelected())
////    			viewPreview.setSelected(true);
//        	updateView();
//    }

//    private void onViewPreview() {
//		if (!viewPreview.isSelected() && !viewBuilder.isSelected())
//			viewBuilder.setSelected(true);
//		updateView();
//    }
    
//    private void onViewToggle() {
//    		if (viewBuilder.isSelected()) {
//    			viewPreview.setSelected(true);
//    			viewBuilder.setSelected(false);
//    		} else {
//    			viewPreview.setSelected(false);
//    			viewBuilder.setSelected(true);
//    		}
//        	updateView();
//    }
    
//    private void updateView() {
////    	    if (viewBuilder.isSelected() && viewPreview.isSelected())
////    	    	  panel.setDividerLocation(0.5);
////    	    else if (viewBuilder.isSelected())
//  	    	  panel.setDividerLocation(1.0);
////    	    else
////    	    	  panel.setDividerLocation(0.0);
//    }
    
//    private void estimateResourcesT() {
//	    	EstimationProducer eProducer = null;
//	    	try {
//	    		eProducer = new EstimationProducer(builder);
//	    		eProducer.produce();
//	    		JOptionPane.showMessageDialog(mainFrame,
//	    				"Expected " + getResourceMessage(eProducer));
//	    		
//	    	} catch (Exception ex) {
//	    		JOptionPane.showMessageDialog(null, "Exception during estimation: " + ex);    
//	    	} finally {
//	    		if (eProducer != null)
//	    			eProducer.dispose();
//	    	}
//    }
    
//    private void estimateResourcesB() {
//    	EstimationProducer eProducer = null;
//    	try {
//    		eProducer = new EstimationProducer(builder);
//    		eProducer.produce();
//    		JOptionPane.showMessageDialog(mainFrame,
//    				"Expected " + getResourceMessage(eProducer));
//    		
//    	} catch (Exception ex) {
//    		JOptionPane.showMessageDialog(null, "Exception during estimation: " + ex);    
//    	} finally {
//    		if (eProducer != null)
//    			eProducer.dispose();
//    	}
//}
    
	private String getResourceMessage(Producer rProducer) {
		double moved = Math.round(rProducer.getTotalDistanceMoved() * 10.0) / 10.0;
		double extruded = Math.round(rProducer.getTotalDistanceExtruded() * 10.0) / 10.0;
		double extrudedVolume = Math.round(rProducer.getTotalVolumeExtruded() * 10.0) / 10.0;
		double time = Math.round(rProducer.getTotalElapsedTime() * 10.0) / 10.0;
		return "Total distance travelled=" + moved +
			"mm.  Total distance extruded=" + extruded +
			"mm.  Total volume extruded=" + extrudedVolume +
			"mm^3.  Elapsed time=" + time + "s";
	}
	
	public void dispose() 
	{
		Debug.d("Main dispose()");
		ftd.killThem();
		/// TODO This class should be fixed so it gets the dispose on window close
//		try {
//			// Attempt to save screen position if requested
//			org.reprap.Preferences prefs = org.reprap.Preferences.getGlobalPreferences();
//			if (prefs.loadBool("RememberWindowPosition")) {
//				//prefs.setGlobalBool("MainWindowTop", xxx);
//			}
//		} catch (Exception ex) {
//			
//		}
		
		System.exit(0);
	}
	
	public static void main(String[] args) {
            
            Thread.currentThread().setName("Main");
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
//                try {
//                     initComms();
//                }
//                catch (Exception ex) {
//                        JOptionPane.showMessageDialog(null, "Error initialising comms: " + ex);
//                                ex.printStackTrace();
//                }
                

                
                try {
                        Thread.currentThread().setName("RepRap");
                                gui = new Main();
                                gui.createAndShowGUI();
                }
                catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error in the main GUI: " + ex);
                                ex.printStackTrace();
                }
                
                gui.mainFrame.setFocusable(true);
                gui.mainFrame.requestFocus();
                BotConsoleFrame.main(null);

            }
        });

	}
        
        
        public static Main gui;
        
//        private static void initComms() throws Exception {
//
//            SNAPAddress myAddress = new SNAPAddress(localNodeNumber);
//
//            String port = "";
//            String err = "";
//            String machine = "simulator";
//			@SuppressWarnings("unused")
//			Boolean use_serial = false;
//
//			repRapAttached = false;
//			try
//			{
//	            port = org.reprap.Preferences.loadGlobalString("Port(name)");
//	            machine = org.reprap.Preferences.loadGlobalString("RepRap_Machine");
//				use_serial = org.reprap.Preferences.loadGlobalBool("GCodeUseSerial");
//			}
//			catch (Exception e)	{}
//
//			if (machine.equals("SNAPRepRap"))
//			{
//				try
//				{
//					communicator = new SNAPCommunicator(port, myAddress);
//				}
//				catch (gnu.io.NoSuchPortException e)
//				{
//					err = "\nCould not connect at " + port + ".\n\n";
//					err += "Check to make sure that is the right path.\n";
//					err += "Check that you have your serial connector plugged in.\n\n";
//					err += "The program will continue but your geometry preference has been set to 'nullcartesian' for this session.";
//
//					org.reprap.Preferences.setGlobalString("RepRap_Machine", "simulator");
//					
//					communicator = null;
//
//					throw new Exception(err);
//				}
//				catch (gnu.io.PortInUseException e)
//				{
//					err = "\nThe " + port + " port is already in use by another program, or your bot isn't plugged in.\n";
//					err += "The program will continue but your geometry preference has been set to 'nullcartesian' for this session.";
//
//					org.reprap.Preferences.setGlobalString("RepRap_Machine", "simulator");
//
//					throw new Exception(err);
//				}
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}
//				repRapAttached = true;
//			}
//		}
        
        public static void setRepRapPresent(boolean a)
        {
        	repRapAttached = a;
        }
        
        public static boolean repRapPresent()
        {
        	return repRapAttached;
        }

        
        public static Communicator getCommunicator() {
            return communicator;
        }

        private static final int localNodeNumber = 0;
    }
