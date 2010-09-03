/**
 * 
 */
package org.reprap.devices;

import java.io.IOException;
import org.reprap.Printer;

/**
 * @author Adrian
 *
 */
public class NullExtruder extends GenericExtruder
{
	/**
	 * @param extruderId
	 */
	public NullExtruder(int extruderId, Printer p)
	{
		super(extruderId, p);
	}
	
	public void setExtrusion(double speed, boolean reverse) throws IOException {}
	public void setCooler(boolean f) throws IOException {}
	public void setValve(boolean valveOpen) throws IOException {}
	public void heatOn() throws Exception {}
	public void setHeater(int heat, double maxTemp) throws IOException {}
	public void setTemperature(double temperature) throws Exception {}
	/**
	 * Purge the extruder
	 */
	public void purge(boolean homeZ) {}

	public boolean isEmpty()
	{
		return false;
	}
	
	public double getTemperature()
	{
		return getTemperatureTarget();
	}
	
}
