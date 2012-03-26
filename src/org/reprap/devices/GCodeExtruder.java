package org.reprap.devices;

import org.reprap.comms.GCodeReaderAndWriter;
import org.reprap.utilities.Debug;
import org.reprap.Printer;
import org.reprap.Preferences;

public class GCodeExtruder extends GenericExtruder
{
	GCodeReaderAndWriter gcode;
	
	/**
	 * @param prefs
	 * @param extruderId
	 */
	public GCodeExtruder(GCodeReaderAndWriter writer, int extruderId, Printer p)
	{
		super(extruderId, p);
		es.setSpeed(0);
		gcode = writer;
	}
	
	/**
	 * Zero the extruded length
	 *
	 */
	public void zeroExtrudedLength(boolean really) throws Exception
	{
		//if(es.length() > 0)
		//{
			super.zeroExtrudedLength(really);
			if(really)
			{
				String s = "G92 E0";
				if(Debug.d())
					s += " ; zero the extruded length";
				gcode.queue(s);
			}
		//}
	}
	

	
	public void setTemperature(double temperature, boolean wait) throws Exception
	{
		String s;
		if(wait)
		{
			s = "M109 S" + temperature;
			if(Debug.d())
				s += " ; set temperature and wait";
		} else
		{
			s = "M104 S" + temperature;
			if(Debug.d())
				s += " ; set temperature and return";
		}
		gcode.queue(s);
		super.setTemperature(temperature, wait);
	}
	
	public void setHeater(int heat, double maxTemp) {}
	
	public double getTemperature() throws Exception
	{
		String s = "M105";
		if(Debug.d())
			s += " ; get temperature";
		gcode.queue(s);
		es.setCurrentTemperature(gcode.getETemp());
		return es.currentTemperature();
	}
	
	public void setExtrusion(double speed, boolean reverse) throws Exception
	{
		if(getExtruderSpeed() < 0)
			return;
		String s;
		if (speed < Preferences.tiny())
		{
			if(!fiveD)
			{
				s = "M103";
				if(Debug.d())
					s += " ; extruder off";
				gcode.queue(s);
			}
		} else
		{
			if(!fiveD)
			{
				if (speed != es.speed())
				{
					s = "M108 S" + speed;
					if(Debug.d())
						s += " ; extruder speed in RPM";
					gcode.queue(s);
				}

				if (es.reverse())
				{
					s = "M102";
					if(Debug.d())
						s += " ; extruder on, reverse";
					gcode.queue(s);
				} else
				{
					s = "M101";
					if(Debug.d())
						s += " ; extruder on, forward";
					gcode.queue(s);
				}
			}
		}
		super.setExtrusion(speed, reverse);
	}
	
	//TODO: make these real G codes.
	public void setCooler(boolean coolerOn, boolean really) throws Exception
	{
		if(really)
		{	
			String s;
			if (coolerOn)
			{
				s = "M106";
				if(Debug.d())
					s += " ; cooler on";
				gcode.queue(s);
			} else
			{
				s = "M107";
				if(Debug.d())
					s += " ; cooler off";
				gcode.queue(s);
			}
		}
	}
	

	public void setValve(boolean valveOpen) throws Exception
	{
		if(valvePulseTime <= 0)
			return;
		String s;
		if (valveOpen)
		{
			s = "M126 P" + valvePulseTime;
			if(Debug.d())
				s += " ; valve open";
			gcode.queue(s);
		} else
		{
			s = "M127 P" + valvePulseTime;
			if(Debug.d())
				s += " ; valve closed";
			gcode.queue(s);
		}
	}
	
	public boolean isEmpty()
	{
		return false;
	}
	

}