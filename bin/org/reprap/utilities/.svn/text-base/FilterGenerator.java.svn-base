package org.reprap.utilities;

import java.io.*;

public class FilterGenerator {
	
	static final int bit0 = 1;
	static final int bit1 = 2;
	static final int bit2 = 4;
	static final int bit3 = 8;
	static final int bit4 = 16;
	static final int bit5 = 32;
	static final int bit6 = 64;
	static final int bit7 = 128;
	
	private static void printPattern(int i)
	{
		String op = new String();
		if((i & bit6) != 0)
			op += " 1";
		else
			op += " .";
		
		if((i & bit5) != 0)
			op += " 1";
		else
			op += " .";
		
		if((i & bit4) != 0)
			op += " 1";
		else
			op += " .";
		
		op += "\n";
		
		if((i & bit7) != 0)
			op += " 1";
		else
			op += " .";
		
		op += " +";

		
		if((i & bit3) != 0)
			op += " 1";
		else
			op += " .";
		
		op += "\n";
		
		if((i & bit0) != 0)
			op += " 1";
		else
			op += " .";
		
		if((i & bit1) != 0)
			op += " 1";
		else
			op += " .";
		
		if((i & bit2) != 0)
			op += " 1";
		else
			op += " .";		
		
		System.out.println(op);
		
	}
	
	private static int r90(int i)
	{
		int r = i;
		r = r << 2;
		if((r & 512) != 0)
			r = r | 2;
		if((r & 256) != 0)
			r = r | 1;
		r = r & 255;
		return r;
	}
	
	public static void main(String[] args) 
	{
	    BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));

		boolean[] action = new boolean[256];
		String resp = null;
		for(int i = 0; i < 64; i++)
		{
			printPattern(i);
			System.out.println();
			System.out.print("Kill? ");
			try
			{
				resp = br.readLine();
			} catch (Exception e)
			{
				System.out.println(e);
			}
			action[i] = resp.startsWith("y");
			int j = i;
			for(int k = 0; k < 3; k++)
			{
				j = r90(j);
				printPattern(j);
				System.out.println();
				action[j] = action[i];
			}
			System.out.println("V");
			System.out.println("V");
		}
		
		System.out.println("--------");
		for(int i = 0; i < 256; i++)
		{
			System.out.print(action[i] + ", ");
			if((i+1)%16 == 0)
				System.out.println();
		}
	}

}
