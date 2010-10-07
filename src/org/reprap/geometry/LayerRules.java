
package org.reprap.geometry;

import org.reprap.Printer;
import org.reprap.Extruder;
import org.reprap.geometry.polygons.RrHalfPlane;
import org.reprap.geometry.polygons.RrRectangle;
import org.reprap.geometry.polygons.Rr2Point;
import org.reprap.Preferences;
import org.reprap.utilities.Debug;

/**
 * This stores a set of facts about the layer currently being made, and the
 * rules for such things as infill patterns, support patterns etc.
 */
public class LayerRules 
{	
	/**
	 * The machine
	 */
	private Printer printer;
	
	/**
	 * How far up the model we are in mm
	 */
	private double modelZ;
	
	/**
	 * How far we are up from machine Z=0
	 */
	private double machineZ;
	
	/**
	 * The count of layers up the model
	 */
	private int modelLayer;
	
	/**
	 * The number of layers the machine has done
	 */
	private int machineLayer;
	
	/**
	 * The top of the model in model coordinates
	 */
	private double modelZMax;
	
	/**
	 * The highest the machine should go this build
	 */
	private double machineZMax;
	
	/**
	 * The number of the last model layer (first = 0)
	 */
	private int modelLayerMax;
	
	/**
	 * The number of the last machine layer (first = 0)
	 */
	private int machineLayerMax;
	
	/**
	 * Putting down foundations?
	 */
	private boolean layingSupport;
	
	/**
	 * The step height of all the extruders
	 */
	private double zStep;
	
	/**
	 * If we take a short step, remember it and add it on next time
	 */
	private double addToStep = 0;
	
	/**
	 * Are we going top to bottom or ground up?
	 */
	private boolean topDown = false;
	
	/**
	 * This is true until it is first read, when it becomes false
	 */
	private boolean notStartedYet;
	
	/**
	 * layers above and below where we are for infill and support calculations
	 */
	//private RrCSGPolygonList [] layerRecord;
	
	/**
	 * The machineLayer value for each entry in layerRecord
	 */
	private int [] recordNumber;
	
	/**
	 * index into layerRecord
	 */
	private int layerPointer;
	
	/**
	 * The XY rectangle that bounds the build
	 */
	private RrRectangle bBox;
	
	/**
	 * 
	 * @param p
	 * @param modZMax
	 * @param macZMax
	 * @param modLMax
	 * @param macLMax
	 * @param found
	 */
	public LayerRules(Printer p, double modZMax, double macZMax,
			int modLMax, int macLMax, boolean found, RrRectangle bb)
	{
		printer = p;
		
		bBox = bb;
		
		notStartedYet = true;

		topDown = printer.getTopDown();

		modelZMax = modZMax;
		machineZMax = macZMax;
		modelLayerMax = modLMax;
		machineLayerMax = macLMax;
		if(topDown)
		{
			modelZ = modelZMax;
			machineZ = machineZMax;
			modelLayer = modelLayerMax;
			machineLayer = machineLayerMax;			
		} else
		{
			modelZ = 0;
			machineZ = 0;
			modelLayer = -1;
			machineLayer = 0;			
		}
		addToStep = 0;

		layingSupport = found;
		Extruder[] es = printer.getExtruders();
		zStep = es[0].getExtrusionHeight();
		int fineLayers = es[0].getLowerFineLayers();
		if(es.length > 1)
		{
			for(int i = 1; i < es.length; i++)
			{
				if(es[i].getLowerFineLayers() > fineLayers)
					fineLayers = es[i].getLowerFineLayers();
				if(Math.abs(es[i].getExtrusionHeight() - zStep) > Preferences.tiny())
					Debug.e("Not all extruders extrude the same height of filament: " + 
							zStep + " and " + es[i].getExtrusionHeight());
			}
		}
		
		recordNumber = new int[fineLayers+1];
		layerPointer = 0;
	}
	
	public RrRectangle getBox()
	{
		return bBox;
	}
	
	public boolean getTopDown() { return topDown; }
	
	public void setPrinter(Printer p) { printer = p; }
	public Printer getPrinter() { return printer; }
	
	public double getModelZ() { return modelZ; }
	
	public double getModelZ(int layer) 
	{
		return zStep*layer; 
	}
	
	public double getMachineZ() { return machineZ; }
	
	public int getModelLayer() { return modelLayer; }
	
	public int getModelLayerMax() { return modelLayerMax; }
	
	public int getMachineLayerMax() { return machineLayerMax; }
	
	public int getMachineLayer() { return machineLayer; }
	
	public int getFoundationLayers() { return machineLayerMax - modelLayerMax; }
	
	public double getModelZMAx() { return modelZMax; }
	
	public double getMachineZMAx() { return machineZMax; }
	
	public double getZStep() { return zStep; }
	
	public boolean notStartedYet()
	{
		if(notStartedYet)
		{
			notStartedYet = false;
			return true;
		}
		return false;
	}
	
	
	public void setLayingSupport(boolean lf) { layingSupport = lf; }
	public boolean getLayingSupport() { return layingSupport; }
	
	/**
	 * Does the layer about to be produced need to be recomputed?
	 * @return
	 */
	public boolean recomputeLayer()
	{
		return getFoundationLayers() - getMachineLayer() <= 2;
	}
	
	/**
	 * The hatch pattern is:
	 * 
	 *  Foundation:
	 *   X and Y rectangle
	 *   
	 *  Model:
	 *   Alternate even then odd (which can be set to the same angle if wanted).
	 *   
	 * @return
	 */
	public RrHalfPlane getHatchDirection(Extruder e) 
	{
		double angle;
		
		if(getMachineLayer() < getFoundationLayers())
		{
			if(getMachineLayer() == getFoundationLayers() - 2)
				angle = e.getEvenHatchDirection();
			else
				angle = e.getOddHatchDirection();
		} else
		{
			if(getModelLayer()%2 == 0)
				angle = e.getEvenHatchDirection();
			else
				angle = e.getOddHatchDirection();
		}
		angle = angle*Math.PI/180;
		return new RrHalfPlane(new Rr2Point(0.0, 0.0), new Rr2Point(Math.sin(angle), Math.cos(angle)));
	}
	
	/**
	 * The gap in the layer zig-zag is:
	 * 
	 *  Foundation:
	 *   The foundation width for all but...
	 *   ...the penultimate foundation layer, which is half that and..
	 *   ...the last foundation layer, which is the model fill width
	 *   
	 *  Model:
	 *   The model fill width
	 *   
	 * @param e
	 * @return
	 */
	public double getHatchWidth(Extruder e)
	{
		if(getMachineLayer() < getFoundationLayers())
			return e.getExtrusionFoundationWidth();
		
		return e.getExtrusionInfillWidth();
	}
	
	/**
	 * Move the machine up/down, but leave the model's layer where it is.
	 *
	 * @param e
	 */
	public void stepMachine(Extruder e)
	{
		double sZ = e.getExtrusionHeight();
		int ld;
		
		if(topDown)
		{
			machineZ -= (sZ + addToStep);
			machineLayer--;
			ld = getFoundationLayers() - getMachineLayer();
			if(ld == 2)
				addToStep = sZ*(1 - e.getSeparationFraction());
			else if(ld == 1)
				addToStep = -sZ*(1 - e.getSeparationFraction());
			else
				addToStep = 0;
		} else
		{
			machineZ += (sZ + addToStep);
			machineLayer++;
			ld = getFoundationLayers() - getMachineLayer();
			if(ld == 2)
				addToStep = -sZ*(1 - e.getSeparationFraction());
			else if(ld == 1)
				addToStep = sZ*(1 - e.getSeparationFraction());
			else
				addToStep = 0;
		}
	}
	
	public void moveZAtStartOfLayer()
	{
		double z = getMachineZ();

		if(topDown)
		{
			printer.setZ(z - (zStep + addToStep));
		}
		printer.singleMove(printer.getX(), printer.getY(), z, printer.getFastFeedrateZ());
	}
	
	/**
	 * Move both the model and the machine up/down a layer
	 * @param e
	 */
	public void step(Extruder e)
	{		
		double sZ = e.getExtrusionHeight();
		if(topDown)
		{
			modelZ -= (sZ + addToStep);
			modelLayer--;			
		} else
		{
			modelZ += (sZ + addToStep);
			modelLayer++;
		}
		addToStep = 0;
		stepMachine(e);
	}
	
	public void setFractionDone()
	{
		// Set -ve to force the system to query the layer rules
		
		org.reprap.gui.botConsole.BotConsoleFrame.getBotConsoleFrame().setFractionDone(-1, -1, -1);
	}
	
}
