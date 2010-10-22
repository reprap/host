package org.reprap.utilities;

import java.io.*;
import java.util.ArrayList;
//import java.util.List;

/**
 * Gets round the fact that Java DeleteOnExit() doesn't do it in the
 * right order.
 */
public class RrDeleteOnExit 
{	
	private ArrayList<File> toDelete = null;
	
	public RrDeleteOnExit()
	{
		toDelete = new ArrayList<File>();
	}
	
	public void add(File f)
	{
		toDelete.add(f);
	}
	
	public void killThem()
	{
		if(toDelete == null)
			return;
		
		for(int i = toDelete.size() - 1; i >= 0; i--)
			if(!toDelete.get(i).delete())
				Debug.e("RrDeleteOnExit.killThem(): Unable to delete: " + toDelete.get(i).getAbsolutePath());
		toDelete = null;
	}
	
	protected void finalize() throws Throwable
	{
		Debug.d("RrDeleteOnExit.finalise()");
		killThem();
	}
}
