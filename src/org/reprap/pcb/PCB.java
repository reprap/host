package org.reprap.pcb;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.FileReader;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.reprap.Extruder;
import org.reprap.Attributes;
import org.reprap.Preferences;
import org.reprap.geometry.polygons.*;
import org.reprap.utilities.RrGraphics;
import org.reprap.utilities.Debug;
import org.reprap.comms.GCodeReaderAndWriter;

class PCBOffsets extends JPanel {
	private static final long serialVersionUID = 1L;
	private static JDialog dialog;
	private static JTextField xo;
	private static JTextField yo;
	private static double xoff = 10;
	private static double yoff = 10;
	
	private PCBOffsets(Rectangle rec)
	{
		super(new BorderLayout());
		JPanel radioPanel;
		radioPanel = new JPanel(new GridLayout(0, 1));
		radioPanel.setSize(300,200);
		
	    JLabel jLabel2 = new JLabel();
	    radioPanel.add(jLabel2);
	    jLabel2.setText(" PCB dimensions: " + org.reprap.machines.GCodeRepRap.round(rec.ne().x() - rec.sw().x(), 1) + 
	    		"(X) x " + org.reprap.machines.GCodeRepRap.round(rec.ne().y() - rec.sw().y(), 1) + "(Y) mm");
		jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
	    JLabel jLabel3 = new JLabel();
	    radioPanel.add(jLabel3);
	    jLabel3.setText(" Offsets (X and Y) in mm:");
		jLabel3.setHorizontalAlignment(SwingConstants.CENTER);			
		xo = new JTextField("10");
		radioPanel.add(xo);
		xo.setHorizontalAlignment(SwingConstants.CENTER);
		yo = new JTextField("10");
		radioPanel.add(yo);
		yo.setHorizontalAlignment(SwingConstants.CENTER);			

		
		try
		{
			
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
			Debug.e(ex.toString());
			ex.printStackTrace();
		}	
	}
	
	public static void OKHandler()
	{
		xoff = Double.parseDouble(xo.getText().trim());
		yoff = Double.parseDouble(yo.getText().trim());
		dialog.dispose();
	}
    
    public static void pcbo(Rectangle rec) 
    {
        //Create and set up the window.
    	JFrame f = new JFrame();
    	dialog = new JDialog(f, "PCB Offsets");
        dialog.setLocation(500, 400);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new PCBOffsets(rec);
        newContentPane.setOpaque(true); //content panes must be opaque
        dialog.setContentPane(newContentPane);

        //Display the window.
        dialog.pack();
        dialog.setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
        dialog.setVisible(true);
    }
    
    
    
    public static double getXoff()
    {
    	return xoff;
    }
    
    public static double getYoff()
    {
    	return yoff;
    }
	
}

public class PCB {
	
	GerberGCode gerberGcode; 
	String[] splitline;
	Rectangle bigBox;
	BufferedReader in;
	String line;
	String formatX = "23", formatY="23";

	double scale = 1;

	double penWidth = 0.7;
	double zFeedRate = 50;
	double zDown = 0;
	static final double centreWidth = 0.9;
	static double offsetX=0;
	static double offsetY=0;
	File inputTracksAndPads;
	File inputDrill;
	File outputGCodes;
	Extruder pcbPen;
	PolygonList penPaths;
	GCodeReaderAndWriter gcode;

	/**
	 * @param args
	 */
	public void pcb(File itp, File id, File og, Extruder pp) 
	{
		inputTracksAndPads = itp;
		inputDrill = id;
		outputGCodes = og;
		pcbPen = pp;
		penWidth = pcbPen.getExtrusionSize();
		Debug.d("Gerber RS274X to GCoder Converter for RepRap\n");


		Debug.d("Input: " + inputTracksAndPads.getName());
		Debug.d("Output: " + outputGCodes.getName()+"\n");
		Debug.d("Pen Width: " + penWidth + " mm");

		createBitmap();

		penPaths = gerberGcode.getPolygons();
		
		penPaths = penPaths.nearEnds(new Point2D(0, 0), true, 1.5*penWidth);
		

		if(Preferences.simulate() && penPaths.size() > 0)
		{
			RrGraphics simulationPlot2 = new RrGraphics("PCB pen plotlines");
			//				if(currentPolygon != null)
			//					thePattern.add(new RrPolygon(currentPolygon));
			simulationPlot2.init(penPaths.getBox(), false, "0");
			simulationPlot2.add(penPaths);
		}

		
		writeGCodes();
			
		Debug.d("GCode file generated succesfully !");
	}
	
	private void raisePen()
	{
		double zf = org.reprap.machines.GCodeRepRap.round(zFeedRate, 1);
		double zu = org.reprap.machines.GCodeRepRap.round(pcbPen.getLift(), 1);
		double xyf = org.reprap.machines.GCodeRepRap.round(pcbPen.getSlowXYFeedrate(), 1);		
		try {
			gcode.queue("G1 F" + zf + "; Z feedrate");
			gcode.queue("G1 Z" + zu + "; Z clearance height");
			gcode.queue("G1 F" + xyf + "; XY feedrate");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void lowerPen()
	{
		double zf = org.reprap.machines.GCodeRepRap.round(zFeedRate, 1);
		double zd = org.reprap.machines.GCodeRepRap.round(zDown, 1);
		double xyf = org.reprap.machines.GCodeRepRap.round(pcbPen.getSlowXYFeedrate(), 1);
		try {
			gcode.queue("G1 F" + zf + "; Z feedrate");
			gcode.queue("G1 Z" + zd + "; Z drawing height");
			gcode.queue("G1 F" + xyf + "; XY feedrate");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void PCBHeader()
	{
		gcode.startRun();
		try 
		{
			gcode.queue("; PCB GCode generated by RepRap Java Host Software");
			Date myDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
			String myDateString = sdf.format(myDate);
			gcode.queue("; Created: " + myDateString);
			gcode.queue("; Gerber tracks and pads file: " + inputTracksAndPads.getName());
			gcode.queue("; Drill file: " + inputDrill.getName());
			gcode.queue(";#!RECTANGLE: " + bigBox);
			gcode.queue(";#!LAYER: 1/1");
			gcode.queue("G21 ;metric");
			gcode.queue("G90 ;absolute positioning");
			gcode.queue("M140 S0.0 ;set bed temperature and return");
			gcode.queue("T" + pcbPen.getPhysicalExtruderNumber() + "; select new extruder");
			//gcode.queue("M113; set extruder to use pot for PWM");
			gcode.queue("G28; go home");
			gcode.queue("G92 E0 ;zero the extruded length");
			raisePen();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void PCBFooter()
	{
		try 
		{
			gcode.queue("M0 ; stop RepRap");
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void polygonPlot(Polygon p)
	{
		if(p.size() <= 0)
			return;
		double x, y;
		try 
		{
			x = org.reprap.machines.GCodeRepRap.round(p.point(0).x(), 1);
			y = org.reprap.machines.GCodeRepRap.round(p.point(0).y(), 1);
		gcode.queue("G1 X" + x + " Y" + y + "; move to polygon start");
		lowerPen();
		for(int i = 1; i < p.size(); i++)
		{
			x = org.reprap.machines.GCodeRepRap.round(p.point(i).x(), 1);
			y = org.reprap.machines.GCodeRepRap.round(p.point(i).y(), 1);
			gcode.queue("G1 X" + x + " Y" + y + "; draw line");
		}
		x = org.reprap.machines.GCodeRepRap.round(p.point(0).x(), 1);
		y = org.reprap.machines.GCodeRepRap.round(p.point(0).y(), 1);
		gcode.queue("G1 X" + x + " Y" + y + "; draw back to polygon start");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		raisePen();
	}
	
	private void writeGCodes()
	{
		try {
			gcode = new GCodeReaderAndWriter(new PrintStream(outputGCodes));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		PCBHeader();
		for(int i = 0; i < penPaths.size(); i++)
			polygonPlot(penPaths.polygon(i));
		PCBFooter();
	}
	
	private void createBitmap()
	{
		gerberGcode = new GerberGCode(pcbPen, null, true); 
		
		bigBox = new Rectangle();
		Rectangle r;
		
		// processing Gerber file
		try {
			in = new BufferedReader(new FileReader(inputTracksAndPads));

			while((line = in.readLine()) != null)
			{
				r = processLine(line, false);
				if(r != null)
					bigBox = Rectangle.union(bigBox, r);
			}
		
			in.close();
			
			PCBOffsets.pcbo(bigBox);

			offsetX = PCBOffsets.getXoff() - bigBox.sw().x();
			offsetY = PCBOffsets.getYoff() - bigBox.sw().y();
			
			bigBox = bigBox.translate(new Point2D(offsetX, offsetY));
			
			
			in = new BufferedReader(new FileReader(inputTracksAndPads));
			
			BooleanGrid pattern = new BooleanGrid(CSG2D.nothing(), bigBox, new Attributes(null, null, null, pcbPen.getAppearance()));
			
			gerberGcode = new GerberGCode(pcbPen, pattern, true);

			while((line = in.readLine()) != null)
			{
				processLine(line, false);
			}

			if(inputDrill != null)
			{
				in = new BufferedReader(new FileReader(inputDrill));
				gerberGcode = new GerberGCode(pcbPen, pattern, false);
				gerberGcode.addCircleAperture(-1, centreWidth); // Just mark drill centres with a disc
				while((line = in.readLine()) != null)
				{
					processLine(line, true);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Rectangle processLine(String line, boolean drill)
	{
		Debug.d(line);
		
		boolean drillDef = false;
		
		Rectangle result = null;
		if(drill)
		{
			formatX = "24";
			formatY = "24";			
		}

		if(line.startsWith("%FSLA"))
		{

			formatX = line.substring(6, 8);
			formatY = line.substring(9, 11);
			Debug.d("Format X: " + formatX + " Format Y: " + formatY);
		}
		else
			if(line.startsWith("%ADD"))
			{
				String apertureNum, apertureType, apertureSize;


				line = line.substring(4, line.length()-2);
				apertureNum = line.substring(0, 2); 

				line = line.substring(2);
				splitline = line.split(",");
				apertureType = splitline[0];
				apertureSize = splitline[1]; 


				Debug.d("\n\nAparture: " + apertureNum);
				Debug.d("Type: " + apertureType);
				

				if(apertureType.equals("C"))
				{
					double s = scale*Double.parseDouble(apertureSize);
					gerberGcode.addCircleAperture(Integer.parseInt(apertureNum), s); 
					Debug.d("Size: " + s + " mm");
				}
				else
					if(apertureType.equals("R"))
					{

						String rectSides[] = apertureSize.split("X");
						double x = scale*Double.parseDouble(rectSides[0]);
						double y = scale*Double.parseDouble(rectSides[1]);

						gerberGcode.addRectangleAperture(Integer.parseInt(apertureNum), x, y);
						Debug.d("Size: " + x + "x" + y + "mm x mm");
					}
					else
						if(apertureType.equals("OC8"))
						{
							gerberGcode.addCircleAperture(Integer.parseInt(apertureNum), scale*Double.parseDouble(apertureSize));							
						}
						else
						{
							Debug.e(" [-] aparture type: " + apertureType + " not supported [" + line+"]\n");
							//System.exit(-1);
						}

			} else
				if(line.startsWith("T") && drill && line.length() > 3)
				{
					if(line.charAt(3) == 'C')
					{
						String apertureNum, apertureSize;


						line = line.substring(1, line.length()); 

						splitline = line.split("C");
						apertureNum = splitline[0];
						apertureSize = splitline[1]; 

						Debug.d("\n\nDrill: " + apertureNum);

						drillDef = true;

						double s = scale*Double.parseDouble(apertureSize);
						gerberGcode.addCircleAperture(Integer.parseInt(apertureNum), s);
						Debug.d("Size: " + s + " mm");
					}
				}
				else
					if(line.startsWith("G90"))
					{
						gerberGcode.enableAbsolute();
						Debug.d("Absolute coordinates");
					}
					else
						if(line.startsWith("G91"))
						{
							gerberGcode.enableRelative();
							Debug.d("Relative coordinates");
						}
						else
							if(line.startsWith("G70") || (drill && line.startsWith("M72")))
							{
								scale = 25.4;
								Debug.d("Inches");
							}
							else
								if(line.startsWith("G71")|| (drill && line.startsWith("M71")))
								{
									scale = 1;
									Debug.d("Metric");
								}
								else
									if(line.startsWith("G54"))
									{
										if(drill)
										{
											gerberGcode.selectAperture(-1);
											Debug.d("Drill centre selected.");
										} else
										{
											int aperture;

											aperture = Integer.valueOf(line.substring(4, line.length()-1).trim());
											gerberGcode.selectAperture(aperture);
											Debug.d("Apature: " + aperture + " selected.");
										}

									}
									else
										if(line.startsWith("X"))
										{
											double x, y;
											int d;
											if(drill)
											{
												d = 3;
												line = line.substring(1);
												splitline = line.split("Y");
												while(splitline[0].length() < 6)
													splitline[0] = "0" + splitline[0];
												while(splitline[1].length() < 6)
													splitline[1] = "0" + splitline[1];
											} else
											{
												splitline[0] = line.substring(1, line.indexOf("Y"));
												splitline[1] = line.substring(line.indexOf("Y")+1, line.indexOf("D"));
												d = Integer.valueOf(line.substring(line.indexOf("D")+1, line.indexOf("D")+3));
											}
							
											int divFactorX = (int)Math.pow(10.0,Integer.parseInt(formatX.substring(1)));
											int divFactorY = (int)Math.pow(10.0,Integer.parseInt(formatY.substring(1)));

											x = scale*Double.valueOf(splitline[0])/divFactorX;
											y = scale*Double.valueOf(splitline[1])/divFactorY;

											x += offsetX;
											y += offsetY;

											Debug.d(" X: "+x+" Y:"+y+" D:"+d);

											if(d==1)
											{
												result = gerberGcode.drawLine(new Point2D(x, y));
											}
											else
												if(d==2)
												{
													gerberGcode.goTo(new Point2D(x, y));
												}	
												else
													if(d==3)
													{
														result = gerberGcode.exposePoint(new Point2D(x, y));
													}
										}
										else
											if(line.startsWith("D") || (line.startsWith("T") && drill && !drillDef))
											{
												if(drill)
												{
													gerberGcode.selectAperture(-1);
													Debug.d("Drill centre selected.");
												} else
												{
													int aperture;

													aperture = Integer.valueOf(line.substring(1, 3));
													gerberGcode.selectAperture(aperture);

													Debug.d("Apature: " + aperture + " selected.");
												}
											}
		return result;
	}
}
