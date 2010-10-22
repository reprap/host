/**
 * 
 */
package org.reprap.utilities;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author Adrian
 *
 * I bet there's a system utility somewhere to do this...
 * 
 */
public class Timer {
	
	/**
	 * Time at the start
	 */
	private long t0 = 0;
	
	/**
	 * Time now
	 */
	private long t = 0;
	
	/**
	 * Time since last call
	 */
	private long delta;
	
	/**
	 * for 3 d.p.
	 */
	private DecimalFormat threeDec;
	
	/**
	 * Static single instance to hold all times
	 */
	static private Timer tim = null;
	
	/**
	 * Constructor just needs to create a single 
	 * instance for initialiseIfNeedBe(String e)
	 *
	 */
	private Timer() {}
	
	/**
	 * What o'clock have you?
	 *
	 */
	static private void newTime()
	{
		long last = tim.t;
		Date d = new Date();
		tim.t = d.getTime() - tim.t0;
		tim.delta = tim.t - last;
	}
	
	/**
	 * Check if we've been initialised and initialise if needed
	 * @param e
	 */
	static private void initialiseIfNeedBe()
	{
		if(tim != null) return;
		tim = new Timer();
		
		newTime();
		tim.t0 = tim.t;
		tim.threeDec = new DecimalFormat("0.000");
		tim.threeDec.setGroupingUsed(false);
	}
	
	/**
	 * Get a double as a 3 d.p. string
	 * @param v
	 * @return
	 */
	static private String d3dp(double v)
	{
		return tim.threeDec.format(v);
	}
	
	/**
	 * Generate a timestamp
	 * @return
	 */
	static public String stamp()
	{
		initialiseIfNeedBe();
		newTime();
		return " [" + d3dp(tim.t*0.001) + "s/" + tim.delta + "ms]";
	}
	
	/**
	 * Get the time from the start in seconds
	 * @return
	 */
	static public double elapsed()
	{
		initialiseIfNeedBe();
		Date d = new Date();
		long e = d.getTime() - tim.t0;
		return 0.001*(double)e;
	}
}
