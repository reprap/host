/**
 * A .rfo file is a compressed archive containing multiple objects that are all to
 * be built in a RepRap machine at once.  See this web page:
 * 
 * http://reprap.org/bin/view/Main/MultipleMaterialsFiles
 * 
 * for details.
 * 
 * This is the class that handles .rfo files.
 */
package org.reprap;
// http://www.devx.com/tips/Tip/14049

import java.io.*;
import java.nio.channels.*;
import java.util.zip.*;
import java.util.Enumeration;


import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;


import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4d;
//import javax.vecmath.Matrix3d;
//import javax.vecmath.Point3d;
//import javax.vecmath.Tuple3d;
//import javax.vecmath.Vector3d;

import org.reprap.geometry.polygons.AllSTLsToBuild;
import org.reprap.utilities.Debug;
import org.reprap.gui.STLObject;

public class RFO 
{
	/**
	 * XML stack top.  If it gets 100 deep we're in trouble...
	 */
	static final int top = 100;
	
	/**
	 * Names of STL files in the compressed .rfo file
	 */
	static final String stlPrefix = "rfo-";
	static final String stlSuffix = ".stl";
	
	//**************************************************************************************
	//
	// XML file writing.  The legend file that ties everything together is XML.  This writes
	// it.
	
	class XMLOut
	{
		PrintStream XMLStream;
		String[] stack;
		int sp;
		
		/**
		 * Create an XML file called LegendFile starting with XML entry start.
		 * @param LegendFile
		 * @param start
		 */
		XMLOut(String LegendFile, String start)
		{
			FileOutputStream fileStream = null;
			try
			{
				fileStream = new FileOutputStream(LegendFile);
			} catch (Exception e)
			{
				Debug.e("XMLOut(): " + e);
			}
			XMLStream = new PrintStream(fileStream);
			stack = new String[top];
			sp = 0;
			push(start);
		}
		
		/**
		 * Start item s
		 * @param s
		 */
		void push(String s)
		{
			for(int i = 0; i < sp; i++)
				XMLStream.print(" ");
			XMLStream.println("<" + s + ">");
			int end = s.indexOf(" ");
			if(end < 0)
				stack[sp] = s;
			else
				stack[sp] = s.substring(0, end);
			sp++;
			if(sp >= top)
				Debug.e("RFO: XMLOut stack overflow on " + s);
		}
		
		/**
		 * Output a complete item s all in one go.
		 * @param s
		 */
		void write(String s)
		{
			for(int i = 0; i < sp; i++)
				XMLStream.print(" ");
			XMLStream.println("<" + s + "/>");
		}
		
		/**
		 * End the current item.
		 *
		 */
		void pop()
		{
			sp--;
			for(int i = 0; i < sp; i++)
				XMLStream.print(" ");
			if(sp < 0)
				Debug.e("RFO: XMLOut stack underflow.");
			XMLStream.println("</" + stack[sp] + ">");
		}
		
		/**
		 * Wind it up.
		 *
		 */
		void close()
		{
			while(sp > 0)
				pop();
			XMLStream.close();
		}
	}
	
	//**************************************************************************************
	//
	// XML file reading.  This reads the legend file.
	
	class XMLIn extends DefaultHandler
	{
		/**
		 * The rfo that we are reading in
		 */
		private RFO rfo;
		
		/**
		 * The STL being read
		 */
		private STLObject stl;
		
		/**
		 * The first of a list of STLs being read.
		 */
		private STLObject firstSTL;
		/**
		 * The current XML item
		 */
		private String element;
		
		/**
		 * File location for reading (eg for an input STL file).
		 */
		private String location;
		
		/**
		 * What type of file (Only STLs supported at the moment).
		 */
		private String filetype;
		
		/**
		 * The name of the material (i.e. extruder) that this item is made from.
		 */
		private String material;
		
		/**
		 * Transfom matrix to get an item in the right place.
		 */
		private double[] mElements;
		private Transform3D transform;
		
		private int rowNumber = 0;
		
		/**
		 * Open up legendFile and use it to build RFO rfo.
		 * @param legendFile
		 * @param r
		 */
		XMLIn(String legendFile, RFO r)
		{
			super();
			rfo = r;
			element = "";
			location = "";
			filetype = "";
			material = "";
			mElements = new double[16];
			setMToIdentity();			
			
			XMLReader xr = null;
			try
			{
				xr = XMLReaderFactory.createXMLReader();
			} catch (Exception e)
			{
				Debug.e("XMLIn() 1: " + e);
			}
			
			xr.setContentHandler(this);
			xr.setErrorHandler(this);
			try
			{
				xr.parse(new InputSource(legendFile));
			} catch (Exception e)
			{
				Debug.e("XMLIn() 2: " + e);
			}

		}

		/**
		 * Initialise the matrix to the identity matrix.
		 *
		 */
		private void setMToIdentity()
		{
			for(rowNumber = 0; rowNumber < 4; rowNumber++)
				for(int column = 0; column < 4; column++)
				{
					if(rowNumber == column)
						mElements[rowNumber*4 + column] = 1;
					else
						mElements[rowNumber*4 + column] = 0;
				}
			transform = new Transform3D(mElements);
			rowNumber = 0;
		}
		////////////////////////////////////////////////////////////////////
		// Event handlers.  These are callbacks for the XML parser.
		////////////////////////////////////////////////////////////////////


		/**
		 * Begin the XML document - no action needed.
		 */
		public void startDocument ()
		{
			//Debug.a("Start document");
		}


		/**
		 * End the XML document - no action needed.
		 */
		public void endDocument ()
		{
			//Debug.a("End document");
		}


		/**
		 * Start an element
		 */
		public void startElement (String uri, String name,
				String qName, org.xml.sax.Attributes atts)
		{
			if (uri.equals(""))
				element = qName;
			else
				element = name;
			//System.out.print(element);
			
			// What element is it?
			
			if(element.equalsIgnoreCase("reprap-fab-at-home-build"))
			{
				
			} else if(element.equalsIgnoreCase("object"))
			{
				stl = new STLObject();
				firstSTL = null;
			} else  if(element.equalsIgnoreCase("files"))
			{
				
			} else if(element.equalsIgnoreCase("file"))
			{
				location = atts.getValue("location");
				filetype = atts.getValue("filetype");
				material = atts.getValue("material");
				if(!filetype.equalsIgnoreCase("application/sla"))
					Debug.e("XMLIn.startElement(): unreconised object file type (should be \"application/sla\"): " + filetype);
			} else if(element.equalsIgnoreCase("transform3D"))
			{
				setMToIdentity();
			} else if(element.equalsIgnoreCase("row"))
			{
				for(int column = 0; column < 4; column++)
					mElements[rowNumber*4 + column] = Double.parseDouble(atts.getValue("m" + rowNumber + column));
			} else
			{
				Debug.e("XMLIn.startElement(): unreconised RFO element: " + element);
			}
		}

		/**
		 * End an element
		 */
		public void endElement (String uri, String name, String qName)
		{
			if (uri.equals(""))
				element = qName;
			else
				element = name;
			if(element.equalsIgnoreCase("reprap-fab-at-home-build"))
			{
				
			} else if(element.equalsIgnoreCase("object"))
			{
				stl.setTransform(transform);
				rfo.astl.add(stl);
			} else  if(element.equalsIgnoreCase("files"))
			{
				
			} else if(element.equalsIgnoreCase("file"))
			{
				org.reprap.Attributes att = stl.addSTL("file:" + rfoDir + location, null, Preferences.unselectedApp(), firstSTL);
				if(firstSTL == null)
					firstSTL = stl;
				att.setMaterial(material);
				location = "";
				filetype = "";
				material = "";

			} else if(element.equalsIgnoreCase("transform3D"))
			{
				if(rowNumber != 4)
					Debug.e("XMLIn.endElement(): incomplete Transform3D matrix - last row number is not 4: " + rowNumber);
				transform = new Transform3D(mElements);
			} else if(element.equalsIgnoreCase("row"))
			{
				rowNumber++;
			} else
			{
				Debug.e("XMLIn.endElement(): unreconised RFO element: " + element);
			}
		}

		/**
		 * Nothing to do for characters in between.
		 */
		public void characters (char ch[], int start, int length)
		{
//			for (int i = start; i < start + length; i++) 
//				System.out.print(ch[i]);
//			Debug.a();
		}

	}
	
	//**************************************************************************************
	//
	// Start of RFO handling
	
	private static final String legendName = "legend.xml";
	
	/**
	 * The name of the RFO file.
	 */
	private String fileName;
	
	/**
	 * The directory in which it is.
	 */
	private String path;
	
	/**
	 * The temporary directory
	 */
	private String tempDir;
	
	/**
	 * The location of the temporary RFO directory
	 */
	private String  rfoDir;
	
	/**
	 * The unique temporary directory name
	 */
	private String uniqueName;
	
	/**
	 * The collection of objects being written out or read in.
	 */
	private AllSTLsToBuild astl;
	
	/**
	 * The XML output for the legend file.
	 */
	private XMLOut xml;
	
	/**
	 * The constructor is the same whether we're reading or writing.  fn is where to put or get the
	 * rfo file from.  as is all the things to write; set that null when reading.
	 * @param fn
	 * @param as
	 */
	private RFO(String fn, AllSTLsToBuild as)
	{
		astl = as;
		int sepIndex = fn.lastIndexOf(File.separator);
		int fIndex = fn.indexOf("file:");
		fileName = fn.substring(sepIndex + 1, fn.length());
		if(sepIndex >= 0)
		{
			if(fIndex >= 0)
				path = fn.substring(fIndex + 5, sepIndex + 1);
			else
				path = fn.substring(0, sepIndex + 1);
		} else
			path = "";
		
		uniqueName = "rfo" + Long.toString(System.nanoTime());

		tempDir = System.getProperty("java.io.tmpdir") + File.separator + uniqueName;
		
		File rfod = new File(tempDir);
		if(!rfod.mkdir())
			throw new RuntimeException(tempDir);
		tempDir += File.separator;
		rfoDir = tempDir + "rfo";
		rfod = new File(rfoDir);
		if(!rfod.mkdir())
			throw new RuntimeException(rfoDir);
		rfoDir += File.separator;
	}
	

	public static boolean recursiveDelete(File fileOrDir)
	{
	    if(fileOrDir.isDirectory())
	    {
	        // recursively delete contents
	        for(File innerFile: fileOrDir.listFiles())
	        {
	            if(!recursiveDelete(innerFile))
	            {
	                return false;
	            }
	        }
	    }

	    return fileOrDir.delete();
	}



	//****************************************************************************
	//
	// .rfo writing
	
	/**
	 * Copy a file from one place to another
	 */
	private static void copyFile(File in, File out)
	{
		try
		{
			FileChannel inChannel = new	FileInputStream(in).getChannel();
			FileChannel outChannel = new FileOutputStream(out).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
			inChannel.close();
			outChannel.close();			
		} catch (Exception e)
		{
			Debug.e("RFO.copyFile(): " + e);
		}

	}		
	
	/**
	 * Copy a file from one place to another.
	 * @param from
	 * @param to
	 */
	private static void copyFile(String from, String to)
	{
		File inputFile;
	    File outputFile;
		int fIndex = from.indexOf("file:");
		int tIndex = to.indexOf("file:");
		if(fIndex < 0)
			inputFile = new File(from);
		else
			inputFile = new File(from.substring(fIndex + 5, from.length()));
		if(tIndex < 0)
			outputFile = new File(to);
		else
			outputFile = new File(to.substring(tIndex + 5, to.length())); 
		copyFile(inputFile, outputFile);		
	}
	
	/**
	 * Create the name of STL file number i
	 * @param i
	 * @return
	 */
	private String stlName(int i)
	{
		return stlPrefix + i + stlSuffix;
	}
	
	/**
	 * Copy each unique STL file to the temporary directory.  Files used more
	 * than once are only copied once.
	 *
	 */
	private void copySTLs()
	{
		int u = 0;
		for(int i = 0; i < astl.size(); i++)
		{
			for(int subMod1 = 0; subMod1 < astl.get(i).size(); subMod1++)
			{
				String s = astl.get(i).fileItCameFrom(subMod1);
				astl.get(i).setUnique(subMod1, u);
				for(int j = 0; j < i; j++)
				{
					for(int subMod2 = 0; subMod2 < astl.get(j).size(); subMod2++)
					{
						if(s.equals(astl.get(j).fileItCameFrom(subMod2)))
						{
							astl.get(i).setUnique(subMod1, astl.get(j).getUnique(subMod2));
							break;
						}
					}
				}
				if(astl.get(i).getUnique(subMod1) == u)
				{
					copyFile(s, rfoDir + stlName(u));
					u++;
				}
			}
		}	
	}
	
	/**
	 * Write a 4x4 homogeneous transform in XML format.
	 * @param trans
	 */
	private void writeTransform(TransformGroup trans)
	{
		Transform3D t = new Transform3D();
		Matrix4d m = new Matrix4d();
		trans.getTransform(t);
		t.get(m);
		xml.push("transform3D");
		 xml.write("row m00=\"" + m.m00 + "\" m01=\"" + m.m01 + "\" m02=\"" + m.m02 + "\" m03=\"" + m.m03 + "\"");
		 xml.write("row m10=\"" + m.m10 + "\" m11=\"" + m.m11 + "\" m12=\"" + m.m12 + "\" m13=\"" + m.m13 + "\"");
		 xml.write("row m20=\"" + m.m20 + "\" m21=\"" + m.m21 + "\" m22=\"" + m.m22 + "\" m23=\"" + m.m23 + "\"");
		 xml.write("row m30=\"" + m.m30 + "\" m31=\"" + m.m31 + "\" m32=\"" + m.m32 + "\" m33=\"" + m.m33 + "\"");
		xml.pop();
	}
	
	/**
	 * Create the legend file
	 *
	 */
	private void createLegend()
	{
		xml = new XMLOut(rfoDir + legendName, "reprap-fab-at-home-build version=\"0.1\"");
		for(int i = 0; i < astl.size(); i++)
		{
			xml.push("object name=\"object-" + i + "\"");
			 xml.push("files");
			  STLObject stlo = astl.get(i);
			  for(int subObj = 0; subObj < stlo.size(); subObj++)
			  {
				  xml.push("file location=\"" + stlName(stlo.getUnique(subObj)) + "\" filetype=\"application/sla\" material=\"" + 
						  stlo.attributes(subObj).getMaterial() + "\"");
				  xml.pop();
			  }
			 xml.pop();
			 writeTransform(stlo.trans());
			xml.pop();
		}
		xml.close();
	}
	
	/**
	 * The entire temporary directory with the legend file and ann the STLs is complete.
	 * Compress it into the required rfo file using zip.  Note we delete the temporary files as we
	 * go along, ending up by deleting the directory containing them.
	 *
	 */
	private void compress()
	{
		try
		{
			ZipOutputStream rfoFile = new ZipOutputStream(new FileOutputStream(path + fileName)); 
			File dirToZip = new File(rfoDir); 
			String[] fileList = dirToZip.list(); 
			byte[] buffer = new byte[4096]; 
			int bytesIn = 0; 

			for(int i=0; i<fileList.length; i++) 
			{ 
				File f = new File(dirToZip, fileList[i]); 
				FileInputStream fis = new FileInputStream(f); 
				String zEntry = f.getPath();
				//Debug.a("\n" + zEntry);
				int start = zEntry.indexOf(uniqueName);
				zEntry = zEntry.substring(start + uniqueName.length() + 1, zEntry.length());
				//Debug.a(tempDir);
				//Debug.a(zEntry + "\n");
				ZipEntry entry = new ZipEntry(zEntry); 
				rfoFile.putNextEntry(entry); 
				while((bytesIn = fis.read(buffer)) != -1) 
					rfoFile.write(buffer, 0, bytesIn); 
				fis.close();
			}
			rfoFile.close();
		} catch (Exception e)
		{
			Debug.e("RFO.compress(): " + e);
		}
	}
	
	/**
	 * This is what gets called to write an rfo file.  It saves all the parts of allSTL in rfo file fn.
	 * @param fn
	 * @param allSTL
	 */
	public static void save(String fn, AllSTLsToBuild allSTL)
	{
		if(!fn.endsWith(".rfo"))
			fn += ".rfo";
		RFO rfo = new RFO(fn, allSTL);
		rfo.copySTLs();
		rfo.createLegend();
		rfo.compress();
		File t = new File(rfo.tempDir);
		recursiveDelete(t);
	}
	
	//******************************************************************************************
	//
	// .rfo reading
	
	/**
	 * Arrghhh!!!!
	 */
	private String processSeparators(String is)
	{
		String result = "";
		for(int i = 0; i < is.length(); i++)
		{
			if(is.charAt(i) == '\\')
			{
				if(File.separator.charAt(0) == '/')
					result += '/';
				else
					result += '\\';
			} else if(is.charAt(i) == '/')
			{
				if(File.separator.charAt(0) == '\\')
					result += '\\';
				else
					result += '/';
			} else
				result += is.charAt(i);
		}
		
		return result;
	}
	
	/**
	 * This uncompresses the zip that is the rfo file into the temporary directory.
	 */
	private void unCompress()
	{
		try
		{
			byte[] buffer = new byte[4096];
			int bytesIn;
			ZipFile rfoFile = new ZipFile(path + fileName);
			Enumeration<? extends ZipEntry> allFiles = rfoFile.entries();
			while(allFiles.hasMoreElements())
			{
				ZipEntry ze = (ZipEntry)allFiles.nextElement();
				InputStream is = rfoFile.getInputStream(ze);
				String fName = processSeparators(ze.getName());
				File element = new File(tempDir + fName);
				org.reprap.Main.ftd.add(element);
				FileOutputStream os = new FileOutputStream(element);
				while((bytesIn = is.read(buffer)) != -1) 
					os.write(buffer, 0, bytesIn);
				os.close();
			}
		} catch (Exception e)
		{
			Debug.e("RFO.unCompress(): " + e);
		}
	}
	
	/**
	 * This reads the legend file and does what it says.
	 *
	 */
	private void interpretLegend()
	{
		@SuppressWarnings("unused")
		XMLIn xi = new XMLIn(rfoDir + legendName, this);
	}
	
	/**
	 * This is what gets called to read an rfo file from filename fn.
	 * @param fn
	 * @return
	 */
	public static AllSTLsToBuild load(String fn)
	{
		if(!fn.endsWith(".rfo"))
			fn += ".rfo";
		RFO rfo = new RFO(fn, null);
		File rfod = new File(rfo.tempDir);
		org.reprap.Main.ftd.add(rfod);
		rfod = new File(rfo.rfoDir);
		org.reprap.Main.ftd.add(rfod);
		
		rfo.astl = new AllSTLsToBuild();
		rfo.unCompress();
		try
		{
			rfo.interpretLegend();
		} catch (Exception e)
		{
			Debug.e("RFO.load(): exception - " + e.toString());
		}
		
		// Tidy up - delete the temporary files and the directory
		// containing them.
		
//		File td = new File(rfo.rfoDir);
//		String[] fileList = td.list(); 
//		for(int i=0; i<fileList.length; i++) 
//		{ 
//			File f = new File(rfo.rfoDir, fileList[i]);
//			if(!f.delete())
//				Debug.e("RFO.AllSTLsToBuild(): Can't delete file: " + fileList[i]);
//		}
//		if(!td.delete())
//			Debug.e("RFO.AllSTLsToBuild(): Can't delete file: " + rfo.rfoDir);
		return rfo.astl;
	}
}
