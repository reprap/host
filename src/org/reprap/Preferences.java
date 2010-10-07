package org.reprap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.vecmath.Color3f;
import org.reprap.utilities.Debug;

/**
 * A single centralised repository of the current preference settings.  This also
 * implements (almost) a singleton for easy global access.  If there are no current
 * preferences fallback distribution defaults are used. 
 */
public class Preferences {
	
	private static String propsFile = "reprap.properties";
	private static final String propsFolder = ".reprap";
	private static final String propsFileDist = "reprap.properties.dist";
	
	private static Preferences globalPrefs = null; 
	
	Properties fallbackPreferences;
	Properties mainPreferences;
	
	/*
	 * This section deals with internal (i.e. not RepRap machine, but this code or
	 * physics) precisions and accuracies
	 */
	
	private static final int grid = 100;             // Click outline polygons to a...
	public static int grid() { return grid; }
	
	private static final double gridRes = 1.0/grid;  // ...10 micron grid
	public static double gridRes() { return gridRes; }	
	
	private static final double lessGridSquare = gridRes*gridRes*0.01;  // Small squared size of a gridsquare
	public static double lessGridSquare() { return lessGridSquare; }	
	
	private static final double tiny = 1.0e-12;      // A small number
	public static double tiny() { return tiny; }	
	
	private static final double swell = 1.01;        // Quad tree swell factor
	public static double swell() { return swell; }	
	
	private static final double machineResolution = 0.1; // RepRap step size in mm
	public static double machineResolution() { return machineResolution; }
	
	private static final double absoluteZero = -273;
	public static double absoluteZero() { return absoluteZero; }
	
	private static final double inToMM = 25.4;
	public static double inchesToMillimetres() { return inToMM; }
	
	private static final Color3f black = new Color3f(0, 0, 0);
	
	private static boolean displaySimulation = false;
	public static boolean simulate() { return displaySimulation; }
	public static void setSimulate(boolean s) { displaySimulation = s;}
	
	private static boolean subtractive = false;
	public static boolean Subtractive() { return subtractive; }
	public static void setSubtractive(boolean s) { subtractive = s;}
	
	private static boolean gCodeUseSerial = false;
	public static boolean GCodeUseSerial() { return gCodeUseSerial; }
	public static void setGCodeUseSerial(boolean s) { gCodeUseSerial = s;}	
	
	private static String repRapMachine="GCodeRepRap";
	public static String RepRapMachine() { return repRapMachine; }
	public static void setRepRapMachine(String s) { repRapMachine = s; }

	public static Appearance unselectedApp()
	{
		Color3f unselectedColour = null;
		try
		{
			unselectedColour = new Color3f((float)Preferences.loadGlobalDouble("UnselectedColourR(0..1)"), 
				(float)Preferences.loadGlobalDouble("UnselectedColourG(0..1)"), 
				(float)Preferences.loadGlobalDouble("UnselectedColourB(0..1)"));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		Appearance unselectedApp = new Appearance();
		unselectedApp.setMaterial(new 
				Material(unselectedColour, black, unselectedColour, black, 0f));
		return unselectedApp;
	}
	
	// Main preferences constructor
	
	public Preferences() throws IOException {
		fallbackPreferences = new Properties();
		mainPreferences = new Properties();
		URL fallbackUrl = ClassLoader.getSystemResource(propsFileDist);
		//System.out.println("++++ " + fallbackUrl.toString());

		// Construct URL of user properties file
		String path = new String(System.getProperty("user.home") + File.separatorChar + 
			propsFolder + File.separatorChar + propsFile);
		File mainFile = new File(path);
		URL mainUrl = mainFile.toURI().toURL();
		
		if (fallbackUrl == null && !mainFile.exists())
			//throw new IOException("Cannot load RepRap properties file or default "+propsFileDist);
			Debug.e("Cannot load RepRap properties file or default "+propsFileDist);
		
		if (fallbackUrl != null)
			fallbackPreferences.load(fallbackUrl.openStream());
		
		if (mainFile.exists())
		{
			mainPreferences.load(mainUrl.openStream());
			if(fallbackUrl != null)
				comparePreferences();
		} else
		{
			// If we don't have a local preferences file copy the default
			// file into it.
			mainPreferences.load(fallbackUrl.openStream());
			save(true);
		}

	}
	
	/**
	 * Compare the user's preferences with the distribution one and report any
	 * different names.
	 */
	private void comparePreferences()
	{
		Enumeration<?> usersLot = mainPreferences.propertyNames();
		Enumeration<?> distLot = fallbackPreferences.propertyNames();
		
		String result = "";
		int count = 0;
		boolean noDifference = true;
		
		while(usersLot.hasMoreElements())
		{
			String next = (String)usersLot.nextElement();
			if (!fallbackPreferences.containsKey(next))
			{
				result += " " + next + "\n";
				count++;
			}
		}
		if(count > 0)
		{
			result = "Your preferences file contains:\n" + result + "which ";
			if(count > 1)
				result += "are";
			else
				result += "is";
			result += " not in the distribution preferences file.";
			Debug.d(result);
			noDifference = false;
		}
		
		result = "";
		count = 0;
		while(distLot.hasMoreElements())
		{
			String next = (String)distLot.nextElement();
			if (!mainPreferences.containsKey(next))
			{
				result += " " + next + "\n";
				count++;
			}
		}
		
		if(count > 0)
		{
			result =  "The distribution preferences file contains:\n" + result + "which ";
			if(count > 1)
				result += "are";
			else
				result += "is";
			result += " not in your preferences file.";
			Debug.d(result);
			noDifference = false;
		}
		
		if(noDifference)
			Debug.d("The distribution preferences file and yours match.  This is good.");
	}

	public void save(boolean startUp) throws FileNotFoundException, IOException {
		String savePath = new String(System.getProperty("user.home") + File.separatorChar + 
			propsFolder + File.separatorChar);
		File f = new File(savePath + File.separatorChar + propsFile);
		if (!f.exists()) {
			// No properties file exists, so we will create one and try again
			// We'll put the properties file in the .reprap folder,
			// under the user's home folder.
			File p = new File(savePath);
			if (!p.isDirectory())		// Create .reprap folder if necessary
				   p.mkdirs();
		}
		
		OutputStream output = new FileOutputStream(f);
		mainPreferences.store(output, "RepRap machine parameters. See http://objects.reprap.org/wiki/Java_Software_Preferences_File");

		if(!startUp)
			org.reprap.Main.gui.getPrinter().refreshPreferences();
	}
		
	public String loadString(String name) {
		if (mainPreferences.containsKey(name))
			return mainPreferences.getProperty(name);
		if (fallbackPreferences.containsKey(name))
			return fallbackPreferences.getProperty(name);
		System.err.println("RepRap preference: " + name + " not found in either preference file.");
		return null;
	}
	
	public int loadInt(String name) {
		String strVal = loadString(name);
		return Integer.parseInt(strVal);
	}
	
	public double loadDouble(String name) {
		String strVal = loadString(name);
		return Double.parseDouble(strVal);
	}
	
	public boolean loadBool(String name) {
		String strVal = loadString(name);
		if (strVal == null) return false;
		if (strVal.length() == 0) return false;
		if (strVal.compareToIgnoreCase("true") == 0) return true;
		return false;
	}

	public static boolean loadConfig(String configName)
	{
		propsFile = configName;
	
		try
		{
			globalPrefs = new Preferences();
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
		
	}
	synchronized private static void initIfNeeded() throws IOException {
		if (globalPrefs == null)
			globalPrefs = new Preferences();
	}

	public static String loadGlobalString(String name) throws IOException {
		initIfNeeded();
		return globalPrefs.loadString(name);
	}

	public static int loadGlobalInt(String name) throws IOException {
		initIfNeeded();
		return globalPrefs.loadInt(name);
	}
	
	public static double loadGlobalDouble(String name) throws IOException {
		initIfNeeded();
		return globalPrefs.loadDouble(name);
	}
	
	public static boolean loadGlobalBool(String name) throws IOException {
		initIfNeeded();
		return globalPrefs.loadBool(name);
	}
	
	public static void saveGlobal() throws IOException {		
		initIfNeeded();
		globalPrefs.save(false);
	}

	public static Preferences getGlobalPreferences() throws IOException {
		initIfNeeded();
		return globalPrefs;
	}
	
	public static String getProbsFolderPath()
	{
		String path;
		path = System.getProperty("user.home") + File.separatorChar + 	propsFolder + File.separatorChar;
		return path;
	}
	
	public static String getPropsFile()
	{
		return propsFile;
	}
	/**
	 * Set a new value
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	public static void setGlobalString(String name, String value) throws IOException {
		initIfNeeded();

		//System.err.println("Setting global " + name + ":" + value);
		
		globalPrefs.setString(name, value);
	}

	public static void setGlobalBool(String name, boolean value) throws IOException {
		initIfNeeded();
		globalPrefs.setString(name, value ? "true" : "false");
	}

	/**
	 * @param name
	 * @param value
	 */
	private void setString(String name, String value) {
		
		//System.err.println("Setting " + name + ":" + value);
		
		mainPreferences.setProperty(name, value);
	}
	
	/**
	 * @return an array of all the names of all the materials in extruders
	 * @throws IOException
	 */
	public static String[] allMaterials() throws IOException
	{
		int extruderCount = globalPrefs.loadInt("NumberOfExtruders");
		String[] result = new String[extruderCount];
		
		for(int i = 0; i < extruderCount; i++)
		{
			String prefix = "Extruder" + i + "_";
			result[i] = globalPrefs.loadString(prefix + "MaterialType(name)");	
		}
		
		return result;
	}
	
	public static String[] startsWith(String prefix) throws IOException 
	{
		initIfNeeded();
		Enumeration<?> allOfThem = globalPrefs.mainPreferences.propertyNames();
		List<String> r = new ArrayList<String>();
		
		while(allOfThem.hasMoreElements())
		{
			String next = (String)allOfThem.nextElement();
			if(next.startsWith(prefix))
				r.add(next);
		}
		String[] result = new String[r.size()];
		
		for(int i = 0; i < r.size(); i++)
			result[i] = (String)r.get(i);
		
		return result;		
	}
	
	public static String[] notStartsWith(String prefix) throws IOException 
	{
		initIfNeeded();
		Enumeration<?> allOfThem = globalPrefs.mainPreferences.propertyNames();
		List<String> r = new ArrayList<String>();
		
		while(allOfThem.hasMoreElements())
		{
			String next = (String)allOfThem.nextElement();
			if(!next.startsWith(prefix))
				r.add(next);
		}
		
		String[] result = new String[r.size()];
		
		for(int i = 0; i < r.size(); i++)
			result[i] = (String)r.get(i);
		
		return result;
	}
	
}
