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
//import org.reprap.comms.Communicator;
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

    //private static Communicator communicator = null;
    
    private static boolean repRapAttached = false;
    
    private Producer producer = null;
    
    private Printer printer = null;
    
    // Window to walk the file tree
    
    private JFileChooser chooser;
    private JFrame mainFrame;
    private RepRapBuild builder;

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
        


        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menubar.add(viewMenu);



        
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


        segmentPause = new JCheckBoxMenuItem("Pause before segment");


        layerPause = new JCheckBoxMenuItem("Pause before layer");


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

		
		System.exit(0);
	}
	
	public static void main(String[] args) {
            
            Thread.currentThread().setName("Main");
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {


                
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
        

        
        public static void setRepRapPresent(boolean a)
        {
        	repRapAttached = a;
        }
        
        public static boolean repRapPresent()
        {
        	return repRapAttached;
        }

        
//        public static Communicator getCommunicator() {
//            return communicator;
//        }

        private static final int localNodeNumber = 0;
    }
