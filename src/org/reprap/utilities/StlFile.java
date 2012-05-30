package org.reprap.utilities;

//package stl_loader;

import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.StringTokenizer;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;

// New from JDK 1.4 for endian related problems
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

import org.reprap.utilities.StlFileParser;      // File parser

/**
 * Title:         STL Loader
 * Description:   STL files loader (Supports ASCII and binary files) for Java3D
 *                Needs JDK 1.4 due to endian problems
 * Company:       Universidad del Pais Vasco (UPV/EHU)
 * @author:       Carlos Pedrinaci Godoy
 * @version:      1.0
 *
 * Contact : xenicp@yahoo.es
 *
 *
 * Things TO-DO:
 *    1.-We can't read binary files over the net.
 *    2.-For binary files if size is lower than expected (calculated with the number of faces)
 *    the program will block.
 *    3.-Improve the way for detecting the kind of stl file?
 *    Can give us problems if the comment of the binary file begins by "solid"
 */

public class StlFile implements Loader
{
  private static final int DEBUG = 0;     // Sets mode to Debug: outputs every action done

  // Maximum length (in chars) of basePath
  private static final int MAX_PATH_LENGTH = 1024;

  // Global variables
  private int flag;                         // Needed cause implements Loader

  private URL baseUrl = null;               // Reading files over Internet
  private String basePath = null;           // For local files

  private boolean fromUrl = false;          // Usefull for binary files
  private boolean Ascii = true;             // File type Ascii -> true o binary -> false
  private String fileName = null;

  // Arrays with coordinates and normals
  // Needed for reading ASCII files because its size is unknown until the end
  private ArrayList coordList;		// Holds Point3f
  private ArrayList normList;		// Holds Vector3f

  // GeometryInfo needs Arrays
  private Point3f[] coordArray = null;
  private Vector3f[] normArray = null;

  // Needed because TRIANGLE_STRIP_ARRAY
  // As the number of strips = the number of faces it's filled in objectToVectorArray
  private int[] stripCounts = null;

  // Default = Not available
  private String objectName=new String("Not available");

  /**
  *  Constructor
  */
  public StlFile()
  {
  }

  /**
   * Method that reads the EOL
   * Needed for verifying that the file has a correct format
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readEOL(StlFileParser parser)
  {
    try{
    parser.nextToken();
    }
    catch (IOException e)
    {
      System.err.println("IO Error on line " + parser.lineno() + ": " + e.getMessage());
    }
    if(parser.ttype != StlFileParser.TT_EOL)
    {
      System.err.println("Format Error:expecting End Of Line on line " + parser.lineno());
    }
  }

  /**
   * Method that reads the word "solid" and stores the object name.
   * It also detects what kind of file it is
   * TO-DO:
   *    1.- Better way control of exceptions?
   *    2.- Better way to decide between Ascii and Binary?
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readSolid(StlFileParser parser)
  {
	if(parser.sval == null)
	{
		// Added by AB
		this.setAscii(false);
		return;
	}
    if( !parser.sval.equals("solid"))
    {
      //System.out.println("Expecting solid on line " + parser.lineno());
      // If the first word is not "solid" then we consider the file is binary
      // Can give us problems if the comment of the binary file begins by "solid"
      this.setAscii(false);
    }
    else  // It's an ASCII file
    {
      try{
          parser.nextToken();
          }
      catch (IOException e)
      {
        System.err.println("IO Error on line " + parser.lineno() + ": " + e.getMessage());
      }
      if( parser.ttype != StlFileParser.TT_WORD)
      {
        // Is the object name always provided???
        System.err.println("Format Error:expecting the object name on line " + parser.lineno());
      }
      else
      { // Store the object Name
        this.setObjectName(new String(parser.sval));
        if(DEBUG==1)
        {
          System.out.println("Object Name:" + this.getObjectName().toString());
        }
        this.readEOL(parser);
      }
    }
  }//End of readSolid

  /**
   * Method that reads a normal
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readNormal(StlFileParser parser)
  {
    Vector3f v = new Vector3f();

    if(!(parser.ttype==StlFileParser.TT_WORD && parser.sval.equals("normal")))
    {
      System.err.println("Format Error:expecting 'normal' on line " + parser.lineno());
    }
    else
    {
      if (parser.getNumber())
      {
        v.x=(float)parser.nval;

        if(DEBUG==1)
        {
          System.out.println("Normal:");
          System.out.print("X=" + v.x + " ");
        }

        if (parser.getNumber())
        {
          v.y=(float)parser.nval;
          if(DEBUG==1)
            System.out.print("Y=" + v.y + " ");

	  if (parser.getNumber())
          {
            v.z=(float)parser.nval;
            if(DEBUG==1)
              System.out.println("Z=" + v.z);

            // We add that vector to the Normal's array
            this.normList.add(v);
            this.readEOL(parser);
	  }
          else System.err.println("Format Error:expecting coordinate on line " + parser.lineno());
        }
        else System.err.println("Format Error:expecting coordinate on line " + parser.lineno());
      }
      else System.err.println("Format Error:expecting coordinate on line " + parser.lineno());
    }
  }// End of Read Normal

  /**
   * Method that reads the coordinates of a vector
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readVertex(StlFileParser parser)
  {
    Point3f p = new Point3f();

    if(!(parser.ttype==StlFileParser.TT_WORD && parser.sval.equals("vertex")))
    {
      System.err.println("Format Error:expecting 'vertex' on line " + parser.lineno());
    }
    else
    {
      if (parser.getNumber())
      {
        p.x=(float)parser.nval;

        if(DEBUG==1)
        {
          System.out.println("Vertex:");
          System.out.print("X=" + p.x + " ");
        }

        if (parser.getNumber())
        {
          p.y=(float)parser.nval;
          if(DEBUG==1)
            System.out.print("Y=" + p.y + " ");

	  if (parser.getNumber())
          {
	    p.z=(float)parser.nval;
            if(DEBUG==1)
              System.out.println("Z=" + p.z);

            // We add that vertex to the array of vertex
            coordList.add(p);
            readEOL(parser);
	  }
          else System.err.println("Format Error: expecting coordinate on line " + parser.lineno());
        }
        else System.err.println("Format Error: expecting coordinate on line " + parser.lineno());
      }
      else System.err.println("Format Error: expecting coordinate on line " + parser.lineno());
    }
  }//End of read vertex

  /**
   * Method that reads "outer loop" and then EOL
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readLoop(StlFileParser parser)
  {
    if(!(parser.ttype==StlFileParser.TT_WORD && parser.sval.equals("outer")))
    {
      System.err.println("Format Error:expecting 'outer' on line " + parser.lineno());
    }
    else
    {
      try{
          parser.nextToken();
          }
      catch (IOException e)
      {
        System.err.println("IO error on line " + parser.lineno() + ": " + e.getMessage());
      }
      if(!(parser.ttype==StlFileParser.TT_WORD && parser.sval.equals("loop")))
      {
        System.err.println("Format Error:expecting 'loop' on line " + parser.lineno());
      }
      else readEOL(parser);
    }
  }//End of readLoop

  /**
   * Method that reads "endloop" then EOL
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readEndLoop(StlFileParser parser)
  {
    if(!(parser.ttype==StlFileParser.TT_WORD && parser.sval.equals("endloop")))
    {
      System.err.println("Format Error:expecting 'endloop' on line " + parser.lineno());
    }
    else readEOL(parser);
  }//End of readEndLoop

  /**
   * Method that reads "endfacet" then EOL
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readEndFacet(StlFileParser parser)
  {
    if(!(parser.ttype==StlFileParser.TT_WORD && parser.sval.equals("endfacet")))
    {
      System.err.println("Format Error:expecting 'endfacet' on line " + parser.lineno());
    }
    else readEOL(parser);
  }//End of readEndFacet

  /**
   * Method that reads a face of the object
   * (Cares about the format)
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readFacet(StlFileParser parser)
  {
    if(!(parser.ttype==StlFileParser.TT_WORD && parser.sval.equals("facet")))
    {
      System.err.println("Format Error:expecting 'facet' on line " + parser.lineno());
    }
    else
    {
      try{
          parser.nextToken();
          readNormal(parser);

          parser.nextToken();
          readLoop(parser);

          parser.nextToken();
          readVertex(parser);

          parser.nextToken();
          readVertex(parser);

          parser.nextToken();
          readVertex(parser);

          parser.nextToken();
          readEndLoop(parser);

          parser.nextToken();
          readEndFacet(parser);
      }
      catch (IOException e)
      {
        System.err.println("IO Error on line " + parser.lineno() + ": " + e.getMessage());
      }
    }
  }// End of readFacet

  /**
   * Method that reads a face in binary files
   * All binary versions of the methods end by 'B'
   * As in binary files we can read the number of faces, we don't need
   * to use coordArray and normArray (reading binary files should be faster)
   *
   * @param in The ByteBuffer with the data of the object.
   * @param index The facet index
   *
   * @throws IOException
   */
  private void readFacetB(ByteBuffer in, int index) throws IOException
  {
    //File structure: Normal Vertex1 Vertex2 Vertex3
    Vector3f normal = new Vector3f();
    Point3f vertex = new Point3f();

    if(DEBUG==1)
      System.out.println("Reading face number " + index);

    // Read the Normal
    normArray[index]=new Vector3f();
    normArray[index].x=in.getFloat();
    normArray[index].y=in.getFloat();
    normArray[index].z=in.getFloat();

    if(DEBUG==1)
      System.out.println("Normal: X=" + normArray[index].x + " Y=" + normArray[index].y + " Z=" + normArray[index].z);

    // Read vertex1
    coordArray[index*3] = new Point3f();
    coordArray[index*3].x=in.getFloat();
    coordArray[index*3].y=in.getFloat();
    coordArray[index*3].z=in.getFloat();

    if(DEBUG==1)
      System.out.println("Vertex 1: X=" + coordArray[index*3].x + " Y=" + coordArray[index*3].y + " Z=" + coordArray[index*3].z);

    // Read vertex2
    coordArray[index*3+1] = new Point3f();
    coordArray[index*3+1].x=in.getFloat();
    coordArray[index*3+1].y=in.getFloat();
    coordArray[index*3+1].z=in.getFloat();

    if(DEBUG==1)
      System.out.println("Vertex 2: X=" + coordArray[index*3+1].x + " Y=" + coordArray[index*3+1].y + " Z=" + coordArray[index*3+1].z);

    // Read vertex3
    coordArray[index*3+2] = new Point3f();
    coordArray[index*3+2].x=in.getFloat();
    coordArray[index*3+2].y=in.getFloat();
    coordArray[index*3+2].z=in.getFloat();

    if(DEBUG==1)
      System.out.println("Vertex 3: X=" + coordArray[index*3+2].x + " Y=" + coordArray[index*3+2].y + " Z=" + coordArray[index*3+2].z);

  }// End of readFacetB

  /**
   * Method for reading binary files
   * Execution is completly different
   * It uses ByteBuffer for reading data and ByteOrder for retrieving the machine's endian
   * (Needs JDK 1.4)
   *
   * TO-DO:
   *  1.-Be able to read files over Internet
   *  2.-If the amount of data expected is bigger than what is on the file then
   *  the program will block forever
   *
   * @param file The name of the file
   *
   * @throws IOException
   */
  private void readBinaryFile(String file) throws IOException
  {
    FileInputStream data;                 // For reading the file
    ByteBuffer dataBuffer;                // For reading in the correct endian
    byte[] Info=new byte[80];             // Header data
    byte[] Array_number= new byte[4];     // Holds the number of faces
    byte[] Temp_Info;                     // Intermediate array

    int Number_faces; // First info (after the header) on the file

    if(DEBUG==1)
      System.out.println("Machine's endian: " + ByteOrder.nativeOrder());

    // Get file's name
    if(fromUrl)
    {
      // FileInputStream can only read local files!?
      System.out.println("This version doesn't support reading binary files from internet");
    }
    else
    { // It's a local file
      data = new FileInputStream(file);

      // First 80 bytes aren't important
      if(80 != data.read(Info))
      { // File is incorrect
        //System.out.println("Format Error: 80 bytes expected");
        throw new IncorrectFormatException();
      }
      else
      { // We must first read the number of faces -> 4 bytes int
        // It depends on the endian so..

        data.read(Array_number);                      // We get the 4 bytes
        dataBuffer = ByteBuffer.wrap(Array_number);   // ByteBuffer for reading correctly the int
        dataBuffer.order(ByteOrder.nativeOrder());    // Set the right order
        Number_faces = dataBuffer.getInt();

        Temp_Info = new byte[50*Number_faces];        // Each face has 50 bytes of data

        data.read(Temp_Info);                         // We get the rest of the file

        dataBuffer = ByteBuffer.wrap(Temp_Info);      // Now we have all the data in this ByteBuffer
        dataBuffer.order(ByteOrder.nativeOrder());

        if(DEBUG==1)
          System.out.println("Number of faces= " + Number_faces);

        // We can create that array directly as we know how big it's going to be
        coordArray = new Point3f[Number_faces*3]; // Each face has 3 vertex
        normArray = new Vector3f[Number_faces];
        stripCounts = new int[Number_faces];

        for(int i=0;i<Number_faces;i++)
        {
          stripCounts[i]=3;
          try
          {
            readFacetB(dataBuffer,i);
            // After each facet there are 2 bytes without information
            // In the last iteration we dont have to skip those bytes..
            if(i != Number_faces - 1)
            {
              dataBuffer.get();
              dataBuffer.get();
            }
          }
          catch (IOException e)
          {
            // Quitar
            System.out.println("Format Error: iteration number " + i);
            throw new IncorrectFormatException();
          }
        }//End for
      }// End file reading
    }// End else
  }// End of readBinaryFile

  /**
   * Method that reads ASCII files
   * Uses StlFileParser for correct reading and format checking
   * The beggining of that method is common to binary and ASCII files
   * We try to detect what king of file it is
   *
   * TO-DO:
   *  1.- Find a best way to decide what kind of file it is
   *  2.- Is that return (first catch) the best thing to do?
   *
   * @param parser The file parser. An instance of StlFileParser.
   */
  private void readFile(StlFileParser parser)
  {
    int t;

    try{
        parser.nextToken();
        }
    catch (IOException e)
    {
      System.err.println("IO Error on line " + parser.lineno() + ": " + e.getMessage());
      System.err.println("File seems to be empty");
      return;         // ????? Throw ?????
    }

    // Here we try to detect what kind of file it is (see readSolid)
    readSolid(parser);

    if(getAscii())
    { // Ascii file
      try
      {
          parser.nextToken();
      }
      catch (IOException e)
      {
       System.err.println("IO Error on line " + parser.lineno() + ": " + e.getMessage());
      }

      // Read all the facets of the object
      while (parser.ttype != StlFileParser.TT_EOF && !parser.sval.equals("endsolid"))
      {
        readFacet(parser);
        try
        {
          parser.nextToken();
        }
        catch (IOException e)
        {
          System.err.println("IO Error on line " + parser.lineno() + ": " + e.getMessage());
        }
      }// End while

      // Why are we out of the while?: EOF or endsolid
      if(parser.ttype == StlFileParser.TT_EOF)
       System.err.println("Format Error:expecting 'endsolid', line " + parser.lineno());
      else
      {
        if(DEBUG==1)
          System.out.println("File readed");
      }
    }//End of Ascii reading

    else
    { // Binary file
      try{
        readBinaryFile(getFileName());
      }
      catch(IOException e)
      {
        System.err.println("Format Error: reading the binary file");
      }
    }// End of binary file
  }//End of readFile

  /**
   * The Stl File is loaded from the .stl file specified by
   * the filename.
   * To attach the model to your scene, call getSceneGroup() on
   * the Scene object passed back, and attach the returned
   * BranchGroup to your scene graph.  For an example, see
   * $J3D/programs/examples/ObjLoad/ObjLoad.java.
   *
   * @param filename The name of the file with the object to load
   *
   * @return Scene The scene with the object loaded.
   *
   * @throws FileNotFoundException
   * @throws IncorrectFormatException
   * @throws ParsingErrorException
   */
  public Scene load(String filename) throws FileNotFoundException,
					    IncorrectFormatException,
					    ParsingErrorException
  {
    setBasePathFromFilename(filename);
    setFileName(filename);     // For binary files

    Reader reader = new BufferedReader(new FileReader(filename));
    return load(reader);
  } // End of load(String)

   /**
   * The Stl file is loaded off of the web.
   * To attach the model to your scene, call getSceneGroup() on
   * the Scene object passed back, and attach the returned
   * BranchGroup to your scene graph.  For an example, see
   * $J3D/programs/examples/ObjLoad/ObjLoad.java.
   *
   * @param url The url to load the onject from
   *
   * @return Scene The scene with the object loaded.
   *
   * @throws FileNotFoundException
   * @throws IncorrectFormatException
   * @throws ParsingErrorException
   */
  public Scene load(URL url) throws FileNotFoundException,
				    IncorrectFormatException,
				    ParsingErrorException
  {
    BufferedReader reader;

    setBaseUrlFromUrl(url);

    try {
      reader = new BufferedReader(new InputStreamReader(url.openStream()));
    }
    catch (IOException e) {
      throw new FileNotFoundException();
    }
    fromUrl = true;
    return load(reader);
  } // End of load(URL)

  /**
   * The Stl File is loaded from the already opened file.
   * To attach the model to your scene, call getSceneGroup() on
   * the Scene object passed back, and attach the returned
   * BranchGroup to your scene graph.  For an example, see
   * $J3D/programs/examples/ObjLoad/ObjLoad.java.
   *
   * @param reader The reader to read the object from
   *
   * @return Scene The scene with the object loaded.
   *
   * @throws FileNotFoundException
   * @throws IncorrectFormatException
   * @throws ParsingErrorException
   */
  public Scene load(Reader reader) throws FileNotFoundException,
                                          IncorrectFormatException,
                                          ParsingErrorException
  {
    // That method calls the method that loads the file for real..
    // Even if the Stl format is not complicated I've decided to use
    // a parser as in the Obj's loader included in Java3D

    StlFileParser st=new StlFileParser(reader);

    // Initialize data
    coordList = new ArrayList();
    normList = new ArrayList();
    setAscii(true);      // Default ascii

    readFile(st);
    return makeScene();
  }

  /**
   * Method that takes the info from an ArrayList of Point3f
   * and returns a Point3f[].
   * Needed for ASCII files as we don't know the number of facets until the end
   *
   * @param inList The list to transform into Point3f[]
   *
   * @return Point3f[] The result.
   */
  private Point3f[] objectToPoint3Array(ArrayList inList)
  {
    Point3f outList[] = new Point3f[inList.size()];

    for (int i = 0 ; i < inList.size() ; i++) {
      outList[i] = (Point3f)inList.get(i);
    }
    return outList;
  } // End of objectToPoint3Array

  /**
   * Method that takes the info from an ArrayList of Vector3f
   * and returns a Vector3f[].
   * Needed for ASCII files as we don't know the number of facets until the end
   *
   * TO-DO:
   *  1.- Here we fill stripCounts...
   *      Find a better place to do it?
   *
   * @param inList The list to transform into Point3f[]
   *
   * @return Vector3f[] The result.
   */
  private Vector3f[] objectToVectorArray(ArrayList inList)
  {
    Vector3f outList[] = new Vector3f[inList.size()];

    if(DEBUG==1)
      System.out.println("Number of facets of the object=" + inList.size());

    // To-do
    stripCounts = new int[inList.size()];
    for (int i = 0 ; i < inList.size() ; i++) {
      outList[i] = (Vector3f)inList.get(i);
      // To-do
      stripCounts[i]=3;
    }
    return outList;
  } // End of objectToVectorArray

  /**
   * Method that creates the SceneBase with the stl file info
   *
   * @return SceneBase The scene
   */
  private SceneBase makeScene()
  {
    // Create Scene to pass back
    SceneBase scene = new SceneBase();
    BranchGroup group = new BranchGroup();
    scene.setSceneGroup(group);

    // Store the scene info on a GeometryInfo
    GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_STRIP_ARRAY);

    // Convert ArrayLists to arrays: only needed if file was not binary
    if(this.Ascii)
    {
      coordArray = objectToPoint3Array(coordList);
      normArray = objectToVectorArray(normList);
    }
    
    //for(int i = 0; i < normArray.length; i++)
    //	normArray[i].negate();

    gi.setCoordinates(coordArray);
    gi.setNormals(normArray);
    gi.setStripCounts(stripCounts);  

    // Put geometry into Shape3d
    Shape3D shape = new Shape3D();
    shape.setGeometry(gi.getGeometryArray());
    
    group.addChild(shape);
    
    
    scene.addNamedObject(objectName, shape);
    
    return scene;
  } // end of makeScene

  /////////////////////// Accessors and Modifiers ///////////////////////////

  public URL getBaseUrl()
  {
    return baseUrl;
  }

  /**
   * Modifier for baseUrl, if accessing internet.
   *
   * @param url The new url
   */
  public void setBaseUrl(URL url)
  {
    baseUrl = url;
  }

  private void setBaseUrlFromUrl(URL url)
  {
    StringTokenizer stok =
      new StringTokenizer(url.toString(), "/\\", true);
    int tocount = stok.countTokens() - 1;
    StringBuffer sb = new StringBuffer(MAX_PATH_LENGTH);
    for(int i = 0; i < tocount ; i++) {
	String a = stok.nextToken();
	sb.append(a);
// 	if((i == 0) && (!a.equals("file:"))) {
// 	    sb.append(a);
// 	    sb.append(java.io.File.separator);
// 	    sb.append(java.io.File.separator);
// 	} else {
// 	    sb.append(a);
// 	    sb.append( java.io.File.separator );
// 	}
    }
    try {
      baseUrl = new URL(sb.toString());
    }
    catch (MalformedURLException e) {
      System.err.println("Error setting base URL: " + e.getMessage());
    }
  } // End of setBaseUrlFromUrl


  public String getBasePath()
  {
    return basePath;
  }

  /**
   * Set the path where files associated with this .stl file are
   * located.
   * Only needs to be called to set it to a different directory
   * from that containing the .stl file.
   *
   * @param pathName The new Path to the file
   */
  public void setBasePath(String pathName)
  {
    basePath = pathName;
    if (basePath == null || basePath == "")
	basePath = "." + java.io.File.separator;
    basePath = basePath.replace('/', java.io.File.separatorChar);
    basePath = basePath.replace('\\', java.io.File.separatorChar);
    if (!basePath.endsWith(java.io.File.separator))
	basePath = basePath + java.io.File.separator;
  } // End of setBasePath

  /*
   * Takes a file name and sets the base path to the directory
   * containing that file.
   */
  private void setBasePathFromFilename(String fileName)
  {
    // Get ready to parse the file name
    StringTokenizer stok =
      new StringTokenizer(fileName, java.io.File.separator);

    //  Get memory in which to put the path
    StringBuffer sb = new StringBuffer(MAX_PATH_LENGTH);

    // Check for initial slash
    if (fileName!= null && fileName.startsWith(java.io.File.separator))
      sb.append(java.io.File.separator);

    // Copy everything into path except the file name
    for(int i = stok.countTokens() - 1 ; i > 0 ; i--) {
      String a = stok.nextToken();
      sb.append(a);
      sb.append(java.io.File.separator);
    }
    setBasePath(sb.toString());
  } // End of setBasePathFromFilename

  public int getFlags()
  {
    return flag;
  }

  public void setFlags(int parm)
  {
    this.flag=parm;
  }

  public boolean getAscii()
  {
    return this.Ascii;
  }

  public void setAscii(boolean tipo)
  {
    this.Ascii = tipo;
  }


  public String getFileName()
  {
    return this.fileName;
  }

  public void setFileName(String filename)
  {
    this.fileName=new String(filename);
  }


  public String getObjectName()
  {
    return this.objectName;
  }

  public void setObjectName(String name)
  {
    this.objectName = name;
  }

} // End of package stl_loader
