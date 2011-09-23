package org.reprap.geometry.polyhedra;

import java.io.File;

public class CSGReader 
{
	private static final String[] tokens = {
		";",
		"=",
		"group",
		"{",
		"}",
		"(",
		")", 
		"[",
		"]",
		"difference",
		"union",
		"multmatrix",
		"cube",
		"cylinder"
		};	
	
	
		private static final String[] cubeArguments = {
			",", "=", "size", "center", "true", "false"
		};
		
		private static final String[] cylinderArguments = {
			",", "=", "$fn", "$fa", "$fs", "h", "r1", "r2", "center", "true", "false" 
		};
		
		private String model;
		private int p;
		
		private void readModel(File f)
		{
			
		}

}
