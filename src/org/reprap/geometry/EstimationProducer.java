package org.reprap.geometry;

import org.reprap.geometry.Producer;
import org.reprap.gui.RepRapBuild;
import org.reprap.machines.Simulator;

/**
 * A specialisation of Producer that doesn't preview anything
 * and always uses the virtual printer.  This is useful to
 * determine in advance the resource requirements (time and materials)
 * to produce an assembly.
 */
public class EstimationProducer extends Producer {

	/**
	 * @param builder
	 * @throws Exception
	 */
	public EstimationProducer(RepRapBuild builder) throws Exception {
		super(null, builder);
		
		layerRules.setPrinter(new Simulator()); 
		//reprap = new NullCartesianMachine(); 

	}

}
