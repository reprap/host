package org.reprap.machines;

import org.reprap.Preferences;
import org.reprap.Printer;
import org.reprap.ReprapException;

/**
 * Returns an appropriate Printer object based on the current properties
 */
public class MachineFactory {

	/**
	 * 
	 */
	private MachineFactory() {
	}
	
	/**
	 * Currently this just always assumes we're building
	 * a 3-axis cartesian printer.  It should build an
	 * appropriate type based on the local configuration.
	 * @return new machine
	 * @throws Exception
	 */
	static public Printer create() throws Exception
	{
		String machine = org.reprap.Preferences.RepRapMachine();

//		if (machine.compareToIgnoreCase("SNAPRepRap") == 0)
//		  	return new SNAPReprap();
		if (machine.compareToIgnoreCase("GCodeRepRap") == 0)
		  	return new GCodeRepRap();
		else if (machine.compareToIgnoreCase("simulator") == 0)
		    return new Simulator();		
		else
			throw new ReprapException("Invalid RepRap machine in properties file: " + machine);
	}
	
}
