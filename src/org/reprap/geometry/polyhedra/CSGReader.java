package org.reprap.geometry.polyhedra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4d;

import org.reprap.CSGOp;
import org.reprap.utilities.Debug;

/**
 * This class reads in an OpenSCAD (http://openscad.org) CSG expression, parses it, and uses the result to
 * build a CSG3D model.
 * 
 * @author ensab
 *
 */
public class CSGReader 
{	
		private static final String group = "group(){";
		private static final String difference = "difference(){";
		private static final String union = "union(){";
		private static final String intersection = "intersection(){";
		private static final String multmatrix = "multmatrix(";
		private static final String cube = "cube(";
		private static final String cylinder = "cylinder(";
		private static final String sphere = "sphere(";
		
		private static final String[] starts = {
			group,
			difference,
			union,
			intersection,
			multmatrix,
			cube,
			cylinder,
			sphere
		};
		
		private static final String[] cubeArgs = {
			"size=", "center="
		};
		
		private static final String[] cylinderArgs = {
			"$fn=", "$fa=", "$fs=", "h=", "r1=", "r2=", "center=" 
		};
		
		private static final String[] sphereArgs = {
			"$fn=", "$fa=", "$fs=", "r=" 
		};
		
		private String model="";
		private String laggingModel="";
		
		private CSG3D CSGModel = null;
		
		private boolean csgAvailable = false;
		

		/**
		 * The constructor just checks the file and loads the CSG expression into a String.
		 * @param fileName
		 */
		public CSGReader(String fileName)
		{
			csgAvailable = readModel(fileName);
		}
		
		/**
		 * Check if the constructor found a model.
		 * @return
		 */
		public boolean csgAvailable()
		{
			return csgAvailable;
		}
		
		/**
		 * Return the model found by the constructor.  Do lazy 
		 * evaluation as far as parsing is concerned.
		 * @return
		 */
		public CSG3D csg()
		{
			ArrayList<CSG3D> c;
			
			if(CSGModel == null)
			{
				c = parseModel();
				if(model.length() > 0)
					Debug.d("Unparsed: " + model);
				if(c.size() != 1)
					Debug.e("CSGReader.csg() - model contains " + c.size() + " separate elements.  Did you mean to union them?");
				CSGModel = c.get(0);
			}
			return CSGModel;
		}
		
		/**
		 * For a given STL file, find if there's a CSG file for it
		 * in the same directory that we can read.  
		 * @param STLfileName
		 * @return
		 */
		public static String CSGFileExists(String STLfileName)
		{
			String fileName = new String(STLfileName);
			if(fileName.toLowerCase().endsWith(".stl"))
				fileName = fileName.substring(0, fileName.length()-4) + ".csg";
			else
				return null;

			if(fileName.startsWith("file:"))
				fileName = fileName.substring(5, fileName.length());
			try 
			{
				BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
				return fileName;
			} catch (FileNotFoundException e) 
			{
				return null;
			} 
		}
		
		/**
		 * Read a CSG model from OpenSCAD into a string.
		 * Remove the line numbers ("n12:" etc), and all white space.
		 * @param STLfileName
		 * @return
		 */
		private boolean readModel(String STLfileName)
		{
			String fileName = CSGFileExists(STLfileName);
			if(fileName == null)
				return false;
			
			model = new String();
			
			try 
			{
				BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
				String line;

				while ((line = inputStream.readLine()) != null)
				{
					line = line.replaceAll("^\\s+", ""); // kill leading white space
					if(line.startsWith("n")) // kill line number
					{
						int cs = line.indexOf(":");
						if(cs < 0)
							Debug.e("CSGReader.readModel() line number not ending in : ... " + line);
						else
							line = line.substring(cs+1);
					}
					line = line.replaceAll("^\\s+", "");  // kill more leading white space
					model += line;
				}
			} catch (FileNotFoundException e) 
			{
				return false;
			} catch (IOException e) 
			{
				return false;
			}

			model = model.replaceAll("\\s+", "");  // kill all remaining white space
			laggingModel = new String(model);
			Debug.d("CSGReader: read CSG model from: " + fileName);
			return true;
		}
		
		/**
		 * The equivalent of the String.substring() function, but
		 * it also maintains a lagging string with a few prior characters in
		 * for error messages.
		 * @param n
		 */
		private void subString(int n)
		{
			if(laggingModel.length() - model.length() > 10)
				laggingModel = laggingModel.substring(n);
			model = model.substring(n);
		}
		
		/**
		 * String for a bit of the model around where we are parsing
		 * @return
		 */
		private String printABitAbout()
		{
			return laggingModel.substring(0, Math.min(50, laggingModel.length()));
		}
		
		/**
		 * String for a bit of the model exactly where we are parsing
		 * @return
		 */
		private String printABit()
		{
			return model.substring(0, Math.min(50, model.length()));
		}	
		
		/**
		 * parse an integer terminated by a ","
		 * @return
		 */
		private int parseIC()
		{
			int c = model.indexOf(",");
			if(c <= 0)
			{
				Debug.e("CSGReader.parseDCI() - expecting , ...got: " + printABit() + "...");
				return 0;
			}
			String i = model.substring(0, c);
			subString(c+1);
			return Integer.valueOf(i);
		}
		
		/**
		 * parse a double terminated by a ","
		 * @return
		 */
		private double parseDC()
		{
			int c = model.indexOf(",");
			if(c <= 0)
			{
				Debug.e("CSGReader.parseDC() - expecting , ...got: " + printABit() + "...");
				return 0;
			}
			String d = model.substring(0, c);
			subString(c+1);
			return Double.valueOf(d);
		}
		
		/**
		 * parse a double terminated by a "]"
		 * @return
		 */
		private double parseDB()
		{
			int c = model.indexOf("]");
			if(c <= 0)
			{
				Debug.e("CSGReader.parseDB() - expecting ] ...got: " + printABit() + "...");
				return 0;
			}
			String d = model.substring(0, c);
			subString(c+1);
			return Double.valueOf(d);
		}
		
		/**
		 * parse a double terminated by a ")"
		 * @return
		 */
		private double parseDb()
		{
			int c = model.indexOf(")");
			if(c <= 0)
			{
				Debug.e("CSGReader.parseDb() - expecting ) ...got: " + printABit() + "...");
				return 0;
			}
			String d = model.substring(0, c);
			subString(c+1);
			return Double.valueOf(d);
		}
		
		/**
		 * Parse true/false
		 * @return
		 */
		private boolean parseBoolean()
		{
			boolean result = model.startsWith("true");
			if(result)
				subString(4);
			else
			{
				if(!model.startsWith("false"))
					Debug.e("CSGReader.parseBoolean() - expecting true or false ...got: " + printABit() + "...");
				else
					subString(5);
			}
			return result;
		}
		
		/**
		 * Parse a vector like "[1.2,-5.6,3.8]"
		 * @param e length of vector
		 * @return
		 */
		private double[] parseV(int e)
		{
			double[] r = new double[e];
			if(!model.startsWith("["))
				Debug.e("CSGReader.parseV() - expecting [ ...got: " + printABit() + "...");
			subString(1);
			for(int i = 0; i < e-1 ; i++)
				r[i] = parseDC();
			r[e-1] = parseDB();
			return r;
		}
		
		/**
		 * Parse a cube of the form:
		 * "cube(size=[10,20,30],center=false);"
		 * @return
		 */
		private CSG3D parseCube()
		{
			subString(cube.length());
			if(!model.startsWith(cubeArgs[0]))
				Debug.e("CSGReader.parseCuber() - expecting: " + cubeArgs[0] + " ...got: " + printABit() + "...");
			subString(cubeArgs[0].length());
			double [] s = parseV(3);
			subString(1); // get rid of ","
			if(!model.startsWith(cubeArgs[1]))
				Debug.e("CSGReader.parseCube() - expecting: " + cubeArgs[1] + " ...got: " + printABit() + "...");
			subString(cubeArgs[1].length());
			boolean c = parseBoolean();
			if(!model.startsWith(");"))
				Debug.e("CSGReader.parseCube() - expecting ); ...got: " + printABit() + "...");
			subString(2);
			
			return Primitives.cube(s[0], s[1], s[2], c);
		}
		
		/**
		 * Parse a cylinder of the form:
		 * "cylinder($fn=20,$fa=12,$fs=1,h=3,r1=2,r2=2,center=false);"
		 * 
		 * @return
		 */
		private CSG3D parseCylinder()
		{
			subString(cylinder.length());
			if(!model.startsWith(cylinderArgs[0]))
				Debug.e("CSGReader.parseCylinder() - expecting: " + cylinderArgs[0] + " ...got: " + printABit() + "...");
			subString(cylinderArgs[0].length());
			int fn = parseIC();
			if(!model.startsWith(cylinderArgs[1]))
				Debug.e("CSGReader.parseCylinder() - expecting: " + cylinderArgs[1] + " ...got: " + printABit() + "...");
			subString(cylinderArgs[1].length());
			double fa = parseDC();
			if(!model.startsWith(cylinderArgs[2]))
				Debug.e("CSGReader.parseCylinder() - expecting: " + cylinderArgs[2] + " ...got: " + printABit() + "...");
			subString(cylinderArgs[2].length());
			double fs = parseDC();
			if(!model.startsWith(cylinderArgs[3]))
				Debug.e("CSGReader.parseCylinder() - expecting: " + cylinderArgs[3] + " ...got: " + printABit() + "...");
			subString(cylinderArgs[3].length());
			double h = parseDC();
			if(!model.startsWith(cylinderArgs[4]))
				Debug.e("CSGReader.parseCylinder() - expecting: " + cylinderArgs[4] + " ...got: " + printABit() + "...");
			subString(cylinderArgs[4].length());
			double r1 = parseDC();
			if(!model.startsWith(cylinderArgs[5]))
				Debug.e("CSGReader.parseCylinder() - expecting: " + cylinderArgs[5] + " ...got: " + printABit() + "...");
			subString(cylinderArgs[5].length());
			double r2 = parseDC();
			if(!model.startsWith(cylinderArgs[6]))
				Debug.e("CSGReader.parseCylinder() - expecting: " + cylinderArgs[6] + " ...got: " + printABit() + "...");
			subString(cylinderArgs[6].length());
			boolean c = parseBoolean();
			if(!model.startsWith(");"))
				Debug.e("CSGReader.parseCylinder() - expecting ); ...got: " + printABit() + "...");
			subString(2);
			
			return Primitives.cylinder(fn, fa, fs, h, r1, r2, c);
		}
		
		
		/**
		 * Parse a sphere of the form:
		 * sphere($fn=0,$fa=12,$fs=1,r=10);
		 * 
		 */
		private CSG3D parseSphere()
		{
			subString(sphere.length());
			if(!model.startsWith(sphereArgs[0]))
				Debug.e("CSGReader.parseSphere() - expecting: " + sphereArgs[0] + " ...got: " + printABit() + "...");
			subString(sphereArgs[0].length());
			int fn = parseIC();
			if(!model.startsWith(sphereArgs[1]))
				Debug.e("CSGReader.parseSphere() - expecting: " + sphereArgs[1] + " ...got: " + printABit() + "...");
			subString(sphereArgs[1].length());
			double fa = parseDC();
			if(!model.startsWith(sphereArgs[2]))
				Debug.e("CSGReader.parseSphere() - expecting: " + sphereArgs[2] + " ...got: " + printABit() + "...");
			subString(sphereArgs[2].length());
			double fs = parseDC();
			if(!model.startsWith(sphereArgs[3]))
				Debug.e("CSGReader.parseSphere() - expecting: " + sphereArgs[3] + " ...got: " + printABit() + "...");
			subString(sphereArgs[3].length());
			double r = parseDb();
			if(!model.startsWith(";"))
				Debug.e("CSGReader.parseSphere() - expecting ; ...got: " + printABit() + "...");
			subString(1);
			
			return Primitives.sphere(fn, fa, fs, r);
		}
			
		
		/**
		 * Parse a 4x4 matrix of the form:
		 * "[[6.12303e-17,0,1,0],[0,1,0,0],[-1,0,6.12303e-17,0],[0,0,0,1]])" 
		 * @return
		 */
		private Matrix4d parseMatrix()
		{
			if(!model.startsWith("["))
				Debug.e("CSGReader.parseMatrix() - expecting [ ...got: " + printABit() + "...");
			subString(1);
			Matrix4d m = new Matrix4d();
			double[] v = parseV(4);
			subString(1); // Dump ","
			m.m00 = v[0];
			m.m01 = v[1];
			m.m02 = v[2];
			m.m03 = v[3];
			v = parseV(4);
			subString(1); // Dump ","
			m.m10 = v[0];
			m.m11 = v[1];
			m.m12 = v[2];
			m.m13 = v[3];
			v = parseV(4);
			subString(1); // Dump ","
			m.m20 = v[0];
			m.m21 = v[1];
			m.m22 = v[2];
			m.m23 = v[3];
			v = parseV(4);
			m.m30 = v[0];
			m.m31 = v[1];
			m.m32 = v[2];
			m.m33 = v[3];
			if(!model.startsWith("]"))
				Debug.e("CSGReader.parseMatrix() - expecting ] ...got: " + printABit() + "...");
			subString(1);
			return m;
		}
		
		/**
		 * Find out if the next thing is a valid starter for a sub-model
		 * @return
		 */
		private boolean startNext()
		{
			for(int i = 0; i < starts.length; i++)
			{
				if(model.startsWith(starts[i]))
					return true;
			}
			return false;
		}
		
		/**
		 * Transform a CSG object
		 * @return
		 */
		private ArrayList<CSG3D> parseTransform()
		{
			subString(multmatrix.length());
			Matrix4d transform;
			transform = parseMatrix();
			if(!model.startsWith("){"))
				Debug.e("CSGReader.parseTransform() - expecting ){ ...got: " + printABit() + "...");
			else
				subString(2);
			ArrayList<CSG3D> result = new ArrayList<CSG3D>();
			if(model.startsWith("}")) // Nothing there?
			{
				subString(1);
				return result;
			}
			ArrayList<CSG3D> r1 = parseModel();
			for(int i = 0; i < r1.size(); i++)
				result.add(r1.get(i).transform(transform));
			if(!model.startsWith("}"))
				Debug.e("CSGReader.parseTransform() - expecting } ...got: " + printABit() + "...");
			else
				subString(1);
			return result;
		}
		
		/**
		 * Union, intersection, or difference
		 * The first operand must be a single item.
		 * The second operand is a list of items of length 0 or more.
		 * @param operator
		 * @return
		 */
		private CSG3D parseCSGOperation(CSGOp operator)
		{
			CSG3D leftOperand;
			ArrayList<CSG3D> c, rightOperand;
			c = parseModel();
			if(c.size() != 1)
				Debug.e("CSGReader.parseModel() " + operator + " - first operand is not a singleton ...got: " + printABitAbout() + "...");
			leftOperand = c.get(0);
			rightOperand = parseMultipleOperands();
			switch(operator)
			{
			case UNION:
				for(int i = 0; i < rightOperand.size(); i++)
					leftOperand = CSG3D.union(leftOperand, rightOperand.get(i));
				break;
			case INTERSECTION:
				for(int i = 0; i < rightOperand.size(); i++)
					leftOperand = CSG3D.intersection(leftOperand, rightOperand.get(i));
				break;
			case DIFFERENCE:
				for(int i = 0; i < rightOperand.size(); i++)
					leftOperand = CSG3D.difference(leftOperand, rightOperand.get(i));
				break;
			default:
				Debug.e("CSGReader.parseCSGOperation() illegal operator: " + operator);
			}
			return leftOperand;
		}
		
		/**
		 * get a whole list of things (including none)
		 * @return
		 */
		private ArrayList<CSG3D> parseMultipleOperands()
		{
			ArrayList<CSG3D> result = new ArrayList<CSG3D>();
			ArrayList<CSG3D> c;
			while(startNext())
			{
				c = parseModel();
				for(int i = 0; i < c.size(); i++)
					result.add(c.get(i));
			}
			return result;
		}
		
		/**
		 * The master parsing function that does a recursive descent through 
		 * the model, parsing it all and returning the final CSG object.
		 * @return
		 */
		private ArrayList<CSG3D> parseModel()
		{	
			//System.out.println("parsing: " + model.substring(0, Math.min(50, model.length())));
			ArrayList<CSG3D> result = new ArrayList<CSG3D>();
			if(model.startsWith("{"))
			{
				Debug.d("CSGReader.parseModel(): unattached {");
				subString(1);
				result = parseMultipleOperands();
				if(!model.startsWith("}"))
					Debug.e("CSGReader.parseModel() { - expecting } ...got: " + printABit() + "...");
				else
					subString(1);
				return result;
			} else if(model.startsWith(group))
			{
				subString(group.length());
				result = parseMultipleOperands();
				if(!model.startsWith("}"))
					Debug.e("CSGReader.parseModel() group(){ - expecting } ...got: " + printABit() + "...");
				else
					subString(1);
				return result;
			} else if(model.startsWith("}"))
			{
				subString(1);
				Debug.e("CSGReader.parseModel() - unexpected } encountered: " + printABit() + "...");
				return result;
			} else if(model.startsWith(difference)) 
			{
				subString(difference.length());
				result.add(parseCSGOperation(CSGOp.DIFFERENCE)); 
				if(!model.startsWith("}"))
					Debug.e("CSGReader.parseModel() difference(){ - expecting } ...got: " + printABit() + "...");
				else
					subString(1);
				return result;
			} else if(model.startsWith(union))
			{
				subString(union.length());
				result.add(parseCSGOperation(CSGOp.UNION));
				if(!model.startsWith("}"))
					Debug.e("CSGReader.parseModel() union(){ - expecting } ...got: " + printABit() + "...");
				else
					subString(1);
				return result;
			} else if(model.startsWith(intersection))
			{
				subString(intersection.length());
				result.add(parseCSGOperation(CSGOp.INTERSECTION));
				if(!model.startsWith("}"))
					Debug.e("CSGReader.parseModel() intersection(){ - expecting } ...got: " + printABit() + "...");
				else
					subString(1);
				return result;
			} else if(model.startsWith(multmatrix)) 
			{
				return parseTransform();
			} else if(model.startsWith(cube))
			{
				result.add(parseCube());
				return result;
			} else if(model.startsWith(cylinder))
			{
				result.add(parseCylinder());
				return result;
			} else if(model.startsWith(sphere))
			{
				result.add(parseSphere());
				return result;
			} 
			Debug.e("CSGReader.parseModel() - unsupported item: " + printABit() + "...");
			return result;
		}
}
