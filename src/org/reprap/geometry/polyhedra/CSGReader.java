package org.reprap.geometry.polyhedra;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.vecmath.Matrix4d;

import org.reprap.CSGOp;
import org.reprap.utilities.Debug;

public class CSGReader 
{	
		private static final String group = "group()";
		private static final String difference = "difference()";
		private static final String union = "union()";
		private static final String multmatrix = "multmatrix(";
		private static final String cube = "cube(";
		private static final String cylinder = "cylinder(";
		
		private static final String[] starts = {
			"{",
			group,
			difference,
			union,
			multmatrix,
			cube,
			cylinder
		};
		
		private static final String[] cubeArgs = {
			"size=", "center="
		};
		
		private static final String[] cylinderArgs = {
			"$fn=", "$fa=", "$fs=", "h=", "r1=", "r2=", "center=" 
		};
		
		private String model;
		private String laggingModel;
		
		private static final int stackTop = 1000;
		private CSG3D stack[] = new CSG3D[stackTop];
		private int sp = 0;
		
		private CSG3D CSGModel = null;
		
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
//					if(line.startsWith("group()")) // kill group()
//					{
//						int cs = line.indexOf(")");
//						line = line.substring(cs+1);
//					}
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
			return true;
		}
		
		private void subString(int n)
		{
			if(laggingModel.length() - model.length() > 10)
				laggingModel = laggingModel.substring(n);
			model = model.substring(n);
		}
		
		private void eatOpenBracket()
		{
			if(model.startsWith("{"))
				subString(1);
		}
		
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
				Debug.e("CSGReader.parseDCI() - syntax error: " + printABit() + "...");
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
				Debug.e("CSGReader.parseDC() - syntax error: " + printABit() + "...");
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
				Debug.e("CSGReader.parseDB() - syntax error: " + printABit() + "...");
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
				Debug.e("CSGReader.parseV() - syntax error: " + printABit() + "...");
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
				Debug.e("CSGReader.parseCube() - syntax error 1: " + printABit() + "...");
			subString(cubeArgs[0].length());
			double [] s = parseV(3);
			subString(1); // get rid of ","
			if(!model.startsWith(cubeArgs[1]))
				Debug.e("CSGReader.parseCube() - syntax error 2: " + printABit() + "...");
			subString(cubeArgs[1].length());
			boolean c = parseBoolean();
			if(!model.startsWith(");"))
				Debug.e("CSGReader.parseCube() - syntax error 3: " + printABit() + "...");
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
				Debug.e("CSGReader.parseCylinder() - syntax error 1: " + printABit() + "...");
			subString(cylinderArgs[0].length());
			int fn = parseIC();
			if(!model.startsWith(cylinderArgs[1]))
				Debug.e("CSGReader.parseCylinder() - syntax error 2: " + printABit() + "...");
			subString(cylinderArgs[1].length());
			double fa = parseDC();
			if(!model.startsWith(cylinderArgs[2]))
				Debug.e("CSGReader.parseCylinder() - syntax error 3: " + printABit() + "...");
			subString(cylinderArgs[2].length());
			double fs = parseDC();
			if(!model.startsWith(cylinderArgs[3]))
				Debug.e("CSGReader.parseCylinder() - syntax error 4: " + printABit() + "...");
			subString(cylinderArgs[3].length());
			double h = parseDC();
			if(!model.startsWith(cylinderArgs[4]))
				Debug.e("CSGReader.parseCylinder() - syntax error 5: " + printABit() + "...");
			subString(cylinderArgs[4].length());
			double r1 = parseDC();
			if(!model.startsWith(cylinderArgs[5]))
				Debug.e("CSGReader.parseCylinder() - syntax error 6: " + printABit() + "...");
			subString(cylinderArgs[5].length());
			double r2 = parseDC();
			if(!model.startsWith(cylinderArgs[6]))
				Debug.e("CSGReader.parseCylinder() - syntax error 7: " + printABit() + "...");
			subString(cylinderArgs[6].length());
			boolean c = parseBoolean();
			if(!model.startsWith(");"))
				Debug.e("CSGReader.parseCylinder() - syntax error 8: " + printABit() + "...");
			subString(2);
			
			return Primitives.cylinder(fn, fa, fs, h, r1, r2, c);
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
				Debug.e("CSGReader.parseMatrix() - syntax error 1: " + printABit() + "...");
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
				Debug.e("CSGReader.parseMatrix() - syntax error 2: " + printABit() + "...");
			subString(2);
			return m;
		}
		
		private boolean startNext()
		{
			for(int i = 0; i < starts.length; i++)
				if(model.startsWith(starts[i]))
					return true;
			return false;
		}
		
		private CSG3D parseTransform()
		{
			CSG3D csga;
			Matrix4d transform;
			transform = parseMatrix();
			eatOpenBracket();
			csga = parseModel();
			return csga.transform(transform);
		}
		
		private CSG3D parseList(CSGOp operator)
		{
			eatOpenBracket();
			CSG3D csga = parseModel();
			CSG3D csgb;
			while(startNext())
			{
				eatOpenBracket();
				csgb = parseModel();
				if(operator == CSGOp.UNION)
					csga = CSG3D.union(csga, csgb);
				else
					csga = CSG3D.difference(csga, csgb);
			}
			return csga;
		}
		
		private CSG3D parseModel()
		{	
			if(model.startsWith("{"))
			{
				subString(1);
				push(parseModel());
			}else if(model.startsWith(group))
			{
				subString(group.length());
				if(!model.startsWith("{"))
					Debug.e("CSGReader.parseModel() - group() not follwed by { : " + printABit() + "...");
				else
					subString(1);
				push(parseModel()); // List(U)???
			} else if(model.startsWith("}"))
			{
				subString(1);
				return pop();
			} else if(model.startsWith(difference)) 
			{
				subString(difference.length());
				push(parseList(CSGOp.INTERSECTION)); //FIXME - OK but should be DIFFERENCE to be clearer
			} else if(model.startsWith(union))
			{
				subString(union.length());
				push(parseList(CSGOp.UNION));
			} else if(model.startsWith(multmatrix)) 
			{
				push(parseTransform());
			} else if(model.startsWith(cube))
			{
				push(parseCube());
				if(model.startsWith("}"))
					subString(1);
			} else if(model.startsWith(cylinder))
			{
				push(parseCylinder());
				if(model.startsWith("}"))
					subString(1);
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
			if(CSGModel == null)
				CSGModel = parseModel();
			return CSGModel;
		}

}
