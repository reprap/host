package org.reprap.geometry.polyhedra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.vecmath.Matrix4d;

import org.reprap.utilities.Debug;

public class CSGReader 
{	
		private static final String difference = "difference()";
		private static final String union = "union()";
		private static final String multmatrix = "multmatrix(";
		private static final String cube = "cube(";
		private static final String cylinder = "cylinder(";
		
		private static final String[] cubeArgs = {
			"size=", "center="
		};
		
		private static final String[] cylinderArgs = {
			"$fn=", "$fa=", "$fs=", "h=", "r1=", "r2=", "center=" 
		};
		
		private String model;
		
		private static final int stackTop = 1000;
		private CSG3D stack[] = new CSG3D[stackTop];
		private int sp = 0;
		
		private boolean csgAvailable;
		
		/**
		 * Stack of CSG expressions
		 * @param csg
		 */
		private void push(CSG3D csg)
		{
			stack[sp] = csg;
			sp++;
			if(sp >= stackTop)
				Debug.e("CSGReader.push() - stack overflow!");
		}
		
		/**
		 * Stack of CSG expressions
		 * @return
		 */
		private CSG3D pop()
		{
			sp--;
			if(sp < 0)
			{
				Debug.e("CSGReader.pop() - stack underflow!");
				return CSG3D.nothing();
			}
			return stack[sp];
		}
		
		/**
		 * Read a CSG model from OpenSCAD into a string.
		 * Remove the line numbers ("n12:" etc), and all white space.
		 * @param fileName
		 * @return
		 */
		private boolean readModel(String fileName)
		{
			model = new String();
			
			if(fileName.toLowerCase().endsWith(".stl"))
				fileName = fileName.substring(0, fileName.length()-4) + ".csg";
			if(fileName.startsWith("file:"))
				fileName = fileName.substring(5, fileName.length());
			
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
					if(line.startsWith("group()")) // kill group()
					{
						int cs = line.indexOf(")");
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
			return true;
		}
		
		private String printABit()
		{
			return model.substring(0, Math.min(50, model.length()));
		}
		
		/**
		 * parse an integer terminated by a ","
		 * @return
		 */
		private int parseDCI()
		{
			int c = model.indexOf(",");
			if(c <= 0)
			{
				Debug.e("CSGReader.parseDCI() - syntax error: " + printABit() + "...");
				return 0;
			}
			String i = model.substring(0, c);
			model = model.substring(c+1);
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
				Debug.e("CSGReader.parseDC() - syntax error: " + printABit() + "...");
				return 0;
			}
			String d = model.substring(0, c);
			model = model.substring(c+1);
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
				Debug.e("CSGReader.parseDB() - syntax error: " + printABit() + "...");
				return 0;
			}
			String d = model.substring(0, c);
			model = model.substring(c+1);
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
				model = model.substring(4);
			else
				model = model.substring(5);
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
				Debug.e("CSGReader.parseV() - syntax error: " + printABit() + "...");
			model = model.substring(1);
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
			model = model.substring(cube.length());
			if(!model.startsWith(cubeArgs[0]))
				Debug.e("CSGReader.parseCube() - syntax error: " + printABit() + "...");
			model = model.substring(cubeArgs[0].length());
			double [] s = parseV(3);
			model = model.substring(1); // get rid of ","
			if(!model.startsWith(cubeArgs[1]))
				Debug.e("CSGReader.parseCube() - syntax error: " + printABit() + "...");
			model = model.substring(cubeArgs[1].length());
			boolean c = parseBoolean();
			if(!model.startsWith(");"))
				Debug.e("CSGReader.parseCube() - syntax error: " + printABit() + "...");
			model = model.substring(2);
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
			return null;
		}
		
		/**
		 * Parse a matrix of the form:
		 * "multmatrix([[6.12303e-17,0,1,0],[0,1,0,0],[-1,0,6.12303e-17,0],[0,0,0,1]])" 
		 * @return
		 */
		private Matrix4d parseMatrix()
		{
			model = model.substring(multmatrix.length());
			if(!model.startsWith("["))
				Debug.e("CSGReader.parseMatrix() - syntax error: " + printABit() + "...");
			model = model.substring(1);
			Matrix4d m = new Matrix4d();
			double[] v = parseV(4);
			m.m00 = v[0];
			m.m01 = v[1];
			m.m02 = v[2];
			m.m03 = v[3];
			v = parseV(4);
			m.m10 = v[0];
			m.m11 = v[1];
			m.m12 = v[2];
			m.m13 = v[3];
			v = parseV(4);
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
				Debug.e("CSGReader.parseMatrix() - syntax error: " + printABit() + "...");
			model = model.substring(2);
			return m;
		}
		
		private CSG3D parseModel()
		{
			CSG3D csga;
			CSG3D csgb;
			Matrix4d transform;
			
			if(model.startsWith("{"))
			{
				model = model.substring(1);
				push(parseModel());
			} else if(model.startsWith("}"))
			{
				model = model.substring(1);
				return pop();
			} else if(model.startsWith(difference)) // Must deal with 0 or multiple args
			{
				model = model.substring(difference.length());
				csga = parseModel();
				csgb = parseModel();
				return CSG3D.difference(csga, csgb);
			} else if(model.startsWith(union))
			{
				model = model.substring(union.length()); // Must deal with 0 or multiple args
				csga = parseModel();
				csgb = parseModel();
				return CSG3D.union(csga, csgb);
			} else if(model.startsWith(multmatrix)) // Must deal with 0 or multiple args
			{
				transform = parseMatrix();
				csga = parseModel();
				return csga.transform(transform);
			} else if(model.startsWith(cube))
			{
				return parseCube();
			} else if(model.startsWith(cylinder))
			{
				return parseCylinder();
			} else
			{
				Debug.e("CSGReader.parseModel() - syntax error: " + printABit() + "...");
			}
			return pop();
		}
		
		public CSGReader(String fileName)
		{
			csgAvailable = readModel(fileName);
		}
		
		public boolean csgAvailable()
		{
			return csgAvailable;
		}
		
		public CSG3D csg()
		{
			return parseModel();
		}

}
