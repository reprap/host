package org.reprap.comms;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import org.reprap.utilities.Debug;
import org.reprap.utilities.RrGraphics;
import org.reprap.utilities.ExtensionFileFilter;
import org.reprap.Main;
import org.reprap.Preferences;
import org.reprap.geometry.LayerRules;

public class GCodeReaderAndWriter
{
	// Codes for responses from the machine
	// A positive number returned is a request for that line number
	// to be resent.
	
	private static final long shutDown = -3;
	//private static final long startOrNullResponse = -2;
	private static final long allSentOK = -1;
	private double eTemp;
	private double bTemp;
	private double x, y, z, e;
	private RrGraphics simulationPlot = null;
	private String lastResp;
	private boolean nonRunningWarn;
	/**
	 * Stop sending a file (if we are).
	 */
	private boolean paused = false;
	
	private boolean iAmPaused = false;
	
	/**
	 * Not quite sure why this is needed...
	 */
	private boolean alreadyReversed = false;
	
	/**
	 * The name of the port talking to the RepRap machine
	 */
	String portName;
	
	/**
	* this is if we need to talk over serial
	*/
	private SerialPort port;
	
	/**
	 * Flag to tell it we've finished
	 */
	private boolean exhaustBuffer = false;
	
	/**
	* this is for doing easy serial writes
	*/
	private PrintStream serialOutStream = null;
	
	/**
	 * this is our read handle on the serial port
	 */
	private InputStream serialInStream = null;
	
	/**
	 * The root file name for output (without ".gcode" on the end)
	 */
	private String opFileName;
	
	/**
	 * List of file names - used to reverse layer order when layers are done top-down
	 */
	private String[] opFileArray;
	
	/**
	 * Index into opFileArray
	 */
	private int opFileIndex;
	
	/**
	 * How does the first file name in a multiple set end?
	 */
	private static final String gcodeExtension = ".gcode";
	
	/**
	 * How does the first file name in a multiple set end?
	 */
	private static final String firstEnding = "_prologue";
	
	/**
	 * How does the last file name in a multiple set end?
	 */
	private static final String lastEnding = "_epilogue";
	
	/**
	 * Flag for temporary files
	 */
	private static final String tmpString = "_TeMpOrArY_";
		
	/**
	 * This is used for file input
	 */
	private BufferedReader fileInStream = null;
	
	/**
	 * How long is our G-Code input file (if any).
	 */
	private long fileInStreamLength = -1;
	
	/**
	 * This is for file output
	 */
	private PrintStream fileOutStream = null;
	
	/**
	 * The last command sent
	 */
	//private String lastCommand;
	
	/**
	 * The current linenumber
	 */
	private long lineNumber;
	
	/**
	 * The ring buffer that stores the commands for 
	 * possible resend requests.
	 */
	private int head, tail;
	private static final int buflen = 10; // No too long, or pause doesn't work well
	private String[] ringBuffer;
	private long[] ringLines;
	
	/**
	 * The transmission to the RepRap machine is handled by
	 * a separate thread.  These control that.
	 */
	//private boolean threadLock = false;
	private Thread bufferThread = null;
	private int myPriority;
	
	/**
	 * Some commands (at the moment just M105 - get temperature and M114 - get coords) generate
	 * a response.  Return that as a string.
	 */
	private int responsesExpected = 0;
//	private boolean responseAvailable = false;
//	private String response;
	
	//private boolean sendFileToMachine = false;
		
	public GCodeReaderAndWriter()
	{
		init();
	}
	
	/**
	 * constructor for when we definitely want to send GCodes to a known file
	 * @param fos
	 */
	public GCodeReaderAndWriter(PrintStream fos)
	{
		init();
		fileOutStream = fos;
	}
	
	private void init()
	{
		resetReceived();
		paused = false;
		iAmPaused = false;
		alreadyReversed = false;
		ringBuffer = new String[buflen];
		ringLines = new long[buflen];
		head = 0;
		tail = 0;
		nonRunningWarn = true;
		lineNumber = 0;
		//threadLock = false;
		exhaustBuffer = false;
		responsesExpected = 0;
//		responseAvailable = false;
//		response = "0000";
		opFileIndex = -1;
		lastResp = "";
		try
		{
			portName = Preferences.loadGlobalString("Port(name)");
		} catch (Exception ex)
		{
			Debug.e("Cannot load preference Port(name).");
			portName = "stdout";
		}

		openSerialConnection(portName);

		myPriority = Thread.currentThread().getPriority();

		bufferThread = null;
	}
	
	private void nonRunningWarning(String s)
	{
		if(nonRunningWarn)
			Debug.d("GCodeReaderAndWriter(): attempt to " + s + " a non-running output buffer.  Further attempts will not be reported.");
		nonRunningWarn = false;		
	}

	
	public boolean buildingFromFile()
	{
		return fileInStream != null;
	}
	
	public boolean savingToFile()
	{
		return fileOutStream != null;
	}

	/**
	 * Stop the printer building.
	 * This _shouldn't_ also stop it being controlled interactively.
	 */
	public void pause()
	{
		paused = true;
//		while(!bufferEmpty())
//		{
//			//Debug.e("Waiting for buffer to empty.");
//			sleep (131);
//		}
	}
	
	/**
	 * Resume building.
	 *
	 */
	public void resume()
	{
		paused = false;
	}
	
	/**
	 * Start the production run
	 * (as opposed to driving the machine interactively).
	 */
	public void startRun()
	{
		if(fileOutStream != null)
		{
			// Exhause buffer before we start.
			if(bufferThread != null)
			{
				exhaustBuffer = true;
				while(exhaustBuffer) sleep(200);
			}
			bufferThread = null;
			head = 0;
			tail = 0;
		}
	}
	
	/**
	 * Are we paused?
	 * @return
	 */
	public boolean iAmPaused()
	{
		return iAmPaused;
	}
	
	/**
	 * Send a GCode file to the machine if that's what we have to do, and
	 * return true.  Otherwise return false.
	 *
	 */
	public boolean filePlay()
	{
		if(fileInStream == null)
		{
			// Not playing a file...
			return false;
		}
		
		simulationPlot = null;

		if(Preferences.simulate())
			simulationPlot = new RrGraphics("RepRap building simulation");

//		if(bufferThread == null)
//		{
//			Debug.e("GCodeWriter: attempt to write to non-existent buffer.");
//			return true;
//		}			
		
		// Launch a thread to run through the file, so we can return control
		// to the user
		
		Thread playFile = new Thread() 
		{
			public void run() 
			{
				Thread.currentThread().setName("GCode file printer");
				String line;
				long bytes = 0;
				double fractionDone = 0;
				try 
				{
					while ((line = fileInStream.readLine()) != null) 
					{
						bufferQueue(line);
						bytes += line.length();
						fractionDone = (double)bytes/(double)fileInStreamLength;
						setFractionDone(fractionDone, -1, -1);
						while(paused)
						{
							iAmPaused = true;
							//Debug.e("Waiting for pause to end.");
							sleep(239);
						}
						iAmPaused = false;
					}
					fileInStream.close();
				} catch (Exception e) 
				{  
					Debug.e("Error printing file: " + e.toString());
					e.printStackTrace();
				}
			}
		};
		
		playFile.start();

	    return true;
	}
	
	public void setFractionDone(double fractionDone, int layer, int outOf)
	{
		org.reprap.gui.botConsole.BotConsoleFrame.getBotConsoleFrame().setFractionDone(fractionDone, layer, outOf);
	}
	
	/**
	 * Wrapper for Thread.sleep()
	 * @param millis
	 */
	public void sleep(int millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (Exception ex)
		{}		
	}
	
	/**
	 * All done.
	 *
	 */
	public void finish()
	{
		Debug.d("disposing of GCodeReaderAndWriter.");
		
//		// Wait for the ring buffer to be exhausted
//		if(fileOutStream == null && bufferThread != null)
//		{
//			exhaustBuffer = true;
//			while(exhaustBuffer) sleep(200);
//		}
		
		try
		{
			if (serialInStream != null)
				serialInStream.close();

			if (serialOutStream != null)
				serialOutStream.close();
			
			if (fileInStream != null)
				fileInStream.close();
			
			if (fileOutStream != null)
				fileOutStream.close();
			
		} catch (Exception e) {}
		
	}
	
	/**
	 * Anything in the buffer?  (NB this still works if we aren't
	 * using the buffer as then head == tail == 0 always).
	 * @return
	 */
//	public boolean bufferEmpty()
//	{
//		return head == tail;
//	}
	
	/**
	 * Between layers nothing will be queued.  Use the next two
	 * functions to slow and speed the buffer's spinning.
	 *
	 */
	public void slowBufferThread()
	{
		if(bufferThread != null)
			bufferThread.setPriority(1);
	}
	
	public void speedBufferThread()
	{
		if(bufferThread != null)		
			bufferThread.setPriority(myPriority);
	}
	
	/**
	 * Compute the checksum of a GCode string.
	 * @param cmd
	 * @return
	 */
	private String checkSum(String cmd)
	{
		int cs = 0;
		for(int i = 0; i < cmd.length(); i++)
			cs = cs ^ cmd.charAt(i);
		cs &= 0xff;
		return "*" + cs;
	}
	
	private void ringAdd(long ln, String cmd)
	{
		head++;
		if(head >= ringBuffer.length)
			head = 0;
		ringBuffer[head] = cmd;
		ringLines[head] = ln;
	}
	
	private String ringGet(long ln)
	{
		int h = head;
		do
		{
			if(ringLines[h] == ln)
				return ringBuffer[h];
			h--;
			if(h < 0)
				h = ringBuffer.length - 1;
		} while(h != head);
		Debug.e("ringGet: line " + ln + " not stored");
		return "";
	}
	
	/**
	 * Send a command to the machine.  Return true if a response is expected; 
	 * false if not.
	 * @param cmd
	 * @return
	 */
	private boolean sendLine(String cmd)
	{
		int com = cmd.indexOf(';');
		if(com > 0)
			cmd = cmd.substring(0, com);
		if(com != 0)
		{
			cmd = cmd.trim();
			if(cmd.length() > 0)
			{
				ringAdd(lineNumber, cmd);
				cmd = "N" + lineNumber + " " + cmd + " ";
				cmd += checkSum(cmd);
				serialOutStream.print(cmd + "\n");				
				serialOutStream.flush();
				Debug.c("G-code: " + cmd + " dequeued and sent");
				return true;
			}
			return false;
		}
	
		Debug.c("G-code: " + cmd + " not sent");
		if(cmd.startsWith(";#!LAYER:"))
		{
			int l = Integer.parseInt(cmd.substring(cmd.indexOf(" ") + 1, cmd.indexOf("/")));
			int o = Integer.parseInt(cmd.substring(cmd.indexOf("/") + 1));
			setFractionDone(-1, l, o+1);
		}
		return false;
	}
	
	/**
	 * Queue a command.  
	 */
	private void bufferQueue(String cmd) throws Exception
	{	
		if(simulationPlot != null)
			simulationPlot.add(cmd);
		
		if(serialOutStream == null)
		{
			nonRunningWarning("queue: \"" + cmd + "\" to");
			return;
		}
		

		if(sendLine(cmd))
		{
			long resp = waitForResponse();
			if(resp == shutDown)
			{
				throw new Exception("The RepRap machine has flagged a hard error!");
			} else if (resp == allSentOK)
			{
				lineNumber++;
				return;
			} else // Must be a re-send line number
			{
				long gotTo = lineNumber;
				lineNumber = resp;
				String rCmd = " ";
				while(lineNumber <= gotTo && !rCmd.contentEquals(""))
				{
					rCmd = ringGet(lineNumber);
					if(sendLine(rCmd))
					{
						resp = waitForResponse();
						if (resp == allSentOK)
							return;
						if(resp == shutDown)
							throw new Exception("The RepRap machine has flagged a hard error!");
					}
				}
			}
		} 
		Debug.e("bufferQueue(): failed to send " + cmd);	
	}
	
	private void resetReceived()
	{
		eTemp = Double.NEGATIVE_INFINITY;
		bTemp = Double.NEGATIVE_INFINITY;
		x = Double.NEGATIVE_INFINITY;
		y = Double.NEGATIVE_INFINITY;
		z = Double.NEGATIVE_INFINITY;
		e = Double.NEGATIVE_INFINITY;
	}
	
	/**
	 * Pick out a value from the returned string
	 * @param s
	 * @param coord
	 * @return
	 */
	private double parseReturnedValue(String s, String cue)
	{
		int i = s.indexOf(cue);
		if(i < 0)
			return Double.NEGATIVE_INFINITY;
		i += cue.length();
		String ss = s.substring(i);
		int j = ss.indexOf(" ");
		if(j < 0)
			return Double.parseDouble(ss);
		else
			return Double.parseDouble(ss.substring(0, j));
	}
	
	/**
	 * If the machine has just returned an extruder temperature, return its value
	 * @return
	 */
	public double getETemp()
	{
		if(serialOutStream == null)
		{
			nonRunningWarning("getETemp() from ");
			return 0;
		}
		if(eTemp == Double.NEGATIVE_INFINITY)
		{
			Debug.d("GCodeReaderAndWriter.getETemp() - no value stored!");
			return 0;
		}
		return eTemp;
	}
	
	/**
	 * If the machine has just returned a bed temperature, return its value
	 * @return
	 */
	public double getBTemp()
	{
		if(serialOutStream == null)
		{
			nonRunningWarning("getBTemp() from ");
			return 0;
		}
		if(bTemp == Double.NEGATIVE_INFINITY)
		{
			Debug.d("GCodeReaderAndWriter.getBTemp() - no value stored!");
			return 0;
		}
		return bTemp;
	}
	
	/**
	 * If the machine has just returned an x coordinate, return its value
	 * @return
	 */
	public double getX()
	{
		if(serialOutStream == null)
		{
			nonRunningWarning("getX() from ");
			return 0;
		}
		if(x == Double.NEGATIVE_INFINITY)
		{
			Debug.e("GCodeReaderAndWriter.getX() - no value stored!");
			return 0;
		}
		return x;
	}

	/**
	 * If the machine has just returned a y coordinate, return its value
	 * @return
	 */
	public double getY()
	{
		if(serialOutStream == null)
		{
			nonRunningWarning("getY() from ");
			return 0;
		}
		if(y == Double.NEGATIVE_INFINITY)
		{
			Debug.e("GCodeReaderAndWriter.getY() - no value stored!");
			return 0;
		}
		return y;
	}

	/**
	 * If the machine has just returned a z coordinate, return its value
	 * @return
	 */
	public double getZ()
	{
		if(serialOutStream == null)
		{
			nonRunningWarning("getZ() from ");
			return 0;
		}
		if(z == Double.NEGATIVE_INFINITY)
		{
			Debug.e("GCodeReaderAndWriter.getZ() - no value stored!");
			return 0;
		}
		return z;
	}

	/**
	 * If the machine has just returned an e coordinate, return its value
	 * @return
	 */
	public double getE()
	{
		if(serialOutStream == null)
		{
			nonRunningWarning("getE() from ");
			return 0;
		}
		if(e == Double.NEGATIVE_INFINITY)
		{
			Debug.e("GCodeReaderAndWriter.getE() - no value stored!");
			return 0;
		}
		return e;
	}
	
	public String lastResponse()
	{
		return lastResp;
	}


	/**
	 * Parse the string sent back from the RepRap machine.
	 *
	 */
	private long waitForResponse()
	{
		int i;
		String resp = "";
		long result = allSentOK;
		String lns;
		resetReceived();
		boolean goAgain;
		Date timer = new Date();
		long startWait = timer.getTime();
		long timeNow;
		long increment = 2000;
		long longWait = 10*60*1000; // 10 mins...
		
		for(;;)
		{
			timeNow = timer.getTime() - startWait;
			if(timeNow > increment)
			{
				Debug.d("GCodeReaderAndWriter().waitForResponse(): waited for " + timeNow/1000 + " seconds.");
				increment = 2*increment;
			}
			
			if(timeNow > longWait)
			{
				Debug.e("GCodeReaderAndWriter().waitForResponse(): waited for " + timeNow/1000 + " seconds.");
				try {
					queue("M0 ;shut RepRap down");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try
			{
				i = serialInStream.read();
			} catch (Exception e)
			{
				i = -1;
			}

			//anything found?
			if (i >= 0)
			{
				char c = (char)i;

				//is it at the end of the line?
				if (c == '\n' || c == '\r')
				{
					goAgain = false;
					if (resp.startsWith("start") || resp.contentEquals("")) // Startup or null string...
					{
						resp = "";
						goAgain = true;
					} else if (resp.startsWith("!!")) // Horrible hard fault?
					{
						result = shutDown;
						Debug.e("GCodeWriter.waitForResponse(): RepRap hard fault!  RepRap said: " + resp);
						
					} else if (resp.startsWith("//")) // immediate DEBUG "comment" from the firmware  ( like C++ )
					{
						Debug.d("GCodeWriter.waitForResponse(): " + resp);
						resp = "";
						goAgain = true;
					} else if (resp.endsWith("\\")) // lines ending in a single backslash are considered "continued" to the next line, like "C"
					{
					//	Debug.d("GCodeWriter.waitForResponse(): " + resp);
					//	resp = ""; don't clear the previuos response...
						goAgain = true; // but do "go again"
					} else if (resp.startsWith("rs")) // Re-send request?
					{
						lns = resp.substring(3);
						int sp = lns.indexOf(" ");
						if(sp > 0)
							lns = lns.substring(0, sp);
						result = Long.parseLong(lns);
						Debug.e("GCodeWriter.waitForResponse() - request to resend from line " + result +
								".  RepRap said: " + resp);
					} else if (!resp.startsWith("ok")) // Must be "ok" if not those - check
					{
						Debug.e("GCodeWriter.waitForResponse() - dud response from RepRap:" + resp);
						result = lineNumber; // Try to send the last line again
					}
					
					// Have we got temperatures and/or coordinates?
					
					eTemp = parseReturnedValue(resp, " T:");
					bTemp = parseReturnedValue(resp, " B:");
					if(resp.indexOf(" C:") >= 0)
					{
						x = parseReturnedValue(resp, " X:");
						y = parseReturnedValue(resp, " Y:");
						z = parseReturnedValue(resp, " Z:");
						e = parseReturnedValue(resp, " E:");
					}
					
					if(!goAgain)
					{
						Debug.c("Response: " + resp);
						lastResp = resp.substring(2);
						return result;
					}
				} else
					resp += c;
			}
		}
	}
		
	/**
	 * Send a G-code command to the machine or into a file.
	 * @param cmd
	 */
	public void queue(String cmd) throws Exception
	{
		//trim it and cleanup.
		cmd = cmd.trim();
		cmd = cmd.replaceAll("  ", " ");
		
		if(fileOutStream != null)
		{
			fileOutStream.println(cmd);
			Debug.c("G-code: " + cmd + " written to file");
		} else
			bufferQueue(cmd);
	}
	

	private void openSerialConnection(String portName)
	{
		
		int baudRate = 19200;
		serialInStream = null;
		serialOutStream = null;
		
		portName = portName.trim();
		
		//open our port.
		Debug.d("GCode opening port " + portName);
		Main.setRepRapPresent(false);
		try 
		{
			CommPortIdentifier commId = CommPortIdentifier.getPortIdentifier(portName);
			port = (SerialPort)commId.open(portName, 30000);
		} catch (NoSuchPortException e) {
			Debug.d("Can't open port: " + portName + " - no RepRap attached.");
			return;
		}
		catch (PortInUseException e){
			Debug.e("Port '" + portName + "' is already in use.");
			return;			
		}
		Main.setRepRapPresent(true);		
		//get our baudrate
		try {
			baudRate = Preferences.loadGlobalInt("BaudRate");
		}
		catch (IOException e){}
		
		// Workround for javax.comm bug.
		// See http://forum.java.sun.com/thread.jspa?threadID=673793
		// FIXME: jvandewiel: is this workaround also needed when using the RXTX library?
		try {
			port.setSerialPortParams(baudRate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		}
		catch (UnsupportedCommOperationException e) {
			Debug.d("An unsupported comms operation was encountered.\n" + e.toString());
			return;		
		}
		
		// Wait for baud rate change to take effect
		try {Thread.sleep(1000);} catch (Exception e) {}

		
		try {
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
		} catch (Exception e) {
			// Um, Linux USB ports don't do this. What can I do about it?
		}
		
		try {
			port.enableReceiveTimeout(1);
		} catch (UnsupportedCommOperationException e) {
			Debug.d("Read timeouts unsupported on this platform");
		}

		//create our steams
		try {
			OutputStream writeStream = port.getOutputStream();
			serialInStream = port.getInputStream();
			serialOutStream = new PrintStream(writeStream);
		} catch (IOException e) {
			Debug.e("GCodeWriter: Error opening serial port stream.");
			serialInStream = null;
			serialOutStream = null;
			return;		
		}

		//arduino bootloader skip.
		Debug.d("Attempting to initialize Arduino/Sanguino");
        try {Thread.sleep(1000);} catch (Exception e) {}
        for(int i = 0; i < 10; i++)
                serialOutStream.write('0');
        try {Thread.sleep(1000);} catch (Exception e) {}
        
        return;
	}
	

	
	public String loadGCodeFileForMaking()
	{
		JFileChooser chooser = new JFileChooser();
        FileFilter filter;
        filter = new ExtensionFileFilter("G Code file to be read", new String[] { "gcode" });
        chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		//chooser.setCurrentDirectory();

		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			String name = chooser.getSelectedFile().getAbsolutePath();
			try
			{
				Debug.d("opening: " + name);
				fileInStreamLength = chooser.getSelectedFile().length();
				fileInStream = new BufferedReader(new FileReader(chooser.getSelectedFile()));
				return chooser.getSelectedFile().getName();
			} catch (FileNotFoundException e) 
			{
				Debug.e("Can't read file " + name);
				fileInStream = null;
				return null;
			}
		} else
		{
			Debug.e("Can't read file.");
			fileInStream = null;
		}

		return null;
	}
	
	public String setGCodeFileForOutput(boolean topDown, String fileRoot)
	{

		File defaultFile = new File(fileRoot + ".gcode");
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(defaultFile);
		FileFilter filter;
		filter = new ExtensionFileFilter("G Code file to write to", new String[] { "gcode" });
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		opFileName = null;
		opFileArray = null;
		opFileIndex = -1;
		int result = chooser.showSaveDialog(null);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			opFileName = chooser.getSelectedFile().getAbsolutePath();
			if(opFileName.endsWith(gcodeExtension))
				opFileName = opFileName.substring(0, opFileName.length() - 6);

			try
			{
				boolean doe = false;
				String fn = opFileName;
				if(topDown)
				{
					opFileIndex = 0;
					fn += firstEnding;
					fn += tmpString;
					doe = true;
				}
				fn += gcodeExtension;
				
				Debug.d("opening: " + fn);
				File fl = new File(fn);
				if(doe) fl.deleteOnExit();
				FileOutputStream fileStream = new FileOutputStream(fl);
				fileOutStream = new PrintStream(fileStream);
				String shortName = chooser.getSelectedFile().getName();
				if(!shortName.endsWith(gcodeExtension))
					shortName += gcodeExtension;
				return shortName;
			} catch (FileNotFoundException e) 
			{
				opFileArray = null;
				opFileIndex = -1;
				Debug.e("Can't write to file '" + opFileName);
				opFileName = null;
				fileOutStream = null;
			}
		}
		else
		{
			fileOutStream = null;
		}
		return null;
	}
	
	public void startingLayer(LayerRules lc)
	{
		// If no filename or the index is not set, forget about the start layer. - Vik, 23-Feb-2009
		if((opFileIndex < 0)  || (opFileName == null))
			return;
		
		if(opFileArray == null)
		{
			if(lc.getTopDown())
			{
				opFileIndex = 0;
				opFileArray = new String[lc.getMachineLayerMax() + 3];
				opFileArray[opFileIndex] = opFileName + firstEnding + tmpString + gcodeExtension;
				finishedLayer();
			}
		}
		
		opFileArray[opFileIndex] = opFileName + lc.getMachineLayer() + tmpString + gcodeExtension;
		try
		{
			File fl = new File(opFileArray[opFileIndex]);
			fl.deleteOnExit();
			FileOutputStream fileStream = new FileOutputStream(fl);
			fileOutStream = new PrintStream(fileStream);
		} catch (Exception e)
		{
			Debug.e("Can't write to file " + opFileArray[opFileIndex]);
		}
	}
	
	public void startingEpilogue()
	{
		if(opFileArray == null)
			return;
		opFileArray[opFileIndex] = opFileName + lastEnding + tmpString + gcodeExtension;
		try
		{
			File fl = new File(opFileArray[opFileIndex]);
			fl.deleteOnExit();
			FileOutputStream fileStream = new FileOutputStream(fl);
			fileOutStream = new PrintStream(fileStream);
		} catch (Exception e)
		{
			Debug.e("Can't write to file " + opFileArray[opFileIndex]);
		}
	}
	
	public void finishedLayer()
	{
		if(opFileArray == null)
			return;
		fileOutStream.close();
		opFileIndex++;
	}
	
	private void copyFile(PrintStream ps, String ip)
	{
		File f = null;
		try 
		{
			f = new File(ip);
			FileReader fr = new FileReader(f);
			int character;
			while ((character = fr.read()) >= 0) 
				ps.print((char)character);
			
			ps.flush();
			fr.close();
		} catch (Exception e) 
		{  
			Debug.e("Error copying file: " + e.toString());
		}
	}
	
	public void reverseLayers()
	{
		if(opFileArray == null || alreadyReversed)
			return;
		
		// Stop this being called twice...
		
		alreadyReversed = true;
		
		try
		{
			FileOutputStream fileStream = new FileOutputStream(opFileName + gcodeExtension);
			fileOutStream = new PrintStream(fileStream);
		} catch (Exception e)
		{
			Debug.e("Can't write to file " + opFileName + gcodeExtension);
		}
		
		copyFile(fileOutStream, opFileArray[0]);
		for(int i = opFileIndex - 1; i > 0; i--)
			copyFile(fileOutStream, opFileArray[i]);
		copyFile(fileOutStream, opFileArray[opFileIndex]);
	}
}