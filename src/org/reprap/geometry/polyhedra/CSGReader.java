package org.reprap.geometry.polyhedra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
		private static final String group = "group()";
		private static final String difference = "difference()";
		private static final String union = "union()";
		private static final String intersection = "intersection()";
		private static final String multmatrix = "multmatrix(";
		private static final String cube = "cube(";
		private static final String cylinder = "cylinder(";
		private static final String sphere = "sphere(";
		
		private static final String[] starts = {
			"{",
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
		
		private static final int stackTop = 1000;
		private CSG3D stack[] = new CSG3D[stackTop];
		private int sp = 0;
		
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
			if(CSGModel == null)
			{
				CSGModel = parseModel();
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
			BufferedReader inputStream;
			try 
			{
				inputStream = new BufferedReader(new FileReader(fileName));
				return fileName;
			} catch (FileNotFoundException e) 
			{
				return null;
			} catch (IOException e) 
			{
				return null;
			}
		}
		
//		/**
//		 * Stack of CSG expressions
//		 * @param csg
//		 */
//		private void push(CSG3D csg)
//		{
//			stack[sp] = csg;
//			sp++;
//			if(sp >= stackTop)
//				Debug.e("CSGReader.push() - stack overflow!");
//		}
//		
//		/**
//		 * Stack of CSG expressions
//		 * @return
//		 */
//		private CSG3D pop()
//		{
//			sp--;
//			if(sp < 0)
//			{
//				Debug.e("CSGReader.pop() - stack underflow!");
//				return CSG3D.nothing();
//			}
//			return stack[sp];
//		}
		
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
		
//		/**
//		 * Does what it says on the tin for "{"
//		 */
//		private void eatOpenBracket()
//		{
//			if(model.startsWith("{"))
//				subString(1);
//		}
//		
//		private void eatAllClosedBracket()
//		{
//			while(model.startsWith("}"))
//				subString(1);
//		}
		
		/**
		 * String for a bit of the model around where we are parsing
		 * @return
		 */
		private String printABit()
		{
			return laggingModel.substring(0, Math.min(50, laggingModel.length()));
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
				subString(5);
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
		 * Parse a matrix of the form:
		 * "multmatrix([[6.12303e-17,0,1,0],[0,1,0,0],[-1,0,6.12303e-17,0],[0,0,0,1]])" 
		 * @return
		 */
		private Matrix4d parseMatrix()
		{
			subString(multmatrix.length());
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
			if(!model.startsWith("])"))
				Debug.e("CSGReader.parseMatrix() - expecting ]) ...got: " + printABit() + "...");
			subString(2);
			return m;
		}
		
		/**
		 * Find out if the next thing is a valid starter for a sub-model
		 * @return
		 */
		private boolean startNext()
		{
			for(int i = 0; i < starts.length; i++)
				if(model.startsWith(starts[i]))
					return true;
			return false;
		}
		
		/**
		 * Transform a CSG object
		 * @return
		 */
		private CSG3D parseTransform()
		{
			Matrix4d transform;
			transform = parseMatrix();
			return parseModel().transform(transform);
		}
		
		/**
		 * OpenSCAD allows boolean operators to have arbitrarily many second operands.
		 * This deals with them unioned.
		 * @param  leftOperand
		 * @return
		 */
		private CSG3D parseListUnioned(CSG3D leftOperand)
		{
			while(startNext())
			{
				CSG3D csgb = parseModel();
				leftOperand = CSG3D.union(leftOperand, csgb);
			}
			return leftOperand;
		}
		
		/**
		 * OpenSCAD allows boolean operators to have arbitrarily many second operands.
		 * This deals with them intersected.
		 * @param operator
		 * @return
		 */
		private CSG3D parseListIntersected(CSG3D leftOperand)
		{
			while(startNext())
			{
				CSG3D csgb = parseModel();
				leftOperand = CSG3D.intersection(leftOperand, csgb);
			}
			return leftOperand;
		}
		
		/**
		 * OpenSCAD allows boolean operators to have arbitrarily many second operands.
		 * This deals with them differenced.
		 * @param operator
		 * @return
		 */
		private CSG3D parseListDifferenced(CSG3D leftOperand)
		{
			while(startNext())
			{
				CSG3D csgb = parseModel();
				leftOperand = CSG3D.difference(leftOperand, csgb);
			}
			return leftOperand;
		}
		
		/**
		 * The master parsing function that does a recursive descent through 
		 * the model, parsing it all and returning the final CSG object.
		 * @return
		 */
		private CSG3D parseModel()
		{	
			if(model.startsWith("{"))
			{
				subString(1);
				CSG3D c1 = parseModel();
				if(!model.startsWith("}"))
					Debug.e("CSGReader.parseModel() - { block not follwed by } ...got: " + printABit() + "...");
				else
					subString(1);
				return c1;
			} else if(model.startsWith(group))
			{
				subString(group.length());
				return parseModel();
			} else if(model.startsWith("}"))
			{
				subString(1);
				Debug.e("CSGReader.parseModel() - unexpected } encountered: " + printABit() + "...");
				return CSG3D.nothing();
			} else if(model.startsWith(difference)) 
			{
				subString(difference.length());
				return parseListDifferenced(parseModel());
			} else if(model.startsWith(union))
			{
				subString(union.length());
				return parseListUnioned(parseModel());
			} else if(model.startsWith(intersection))
			{
				subString(intersection.length());
				return parseListIntersected(parseModel());
			} else if(model.startsWith(multmatrix)) 
			{
				return parseTransform();
			} else if(model.startsWith(cube))
			{
				return parseCube();
			} else if(model.startsWith(cylinder))
			{
				return parseCylinder();
			} else if(model.startsWith(sphere))
			{
				return parseSphere();
			} 
			Debug.e("CSGReader.parseModel() - unsupported item: " + printABit() + "...");
			return CSG3D.nothing();
		}
}
