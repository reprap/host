//package org.reprap.comms.snap;
//
///**
// * 
// * Represents the checksum calculation process used in SNAP which
// * is an 8 bit CRC.
// * 
// */
//public class SNAPChecksum {
//	
//	/**
//	 * 
//	 */
//	byte crc;
//	
//	/**
//	 * 
//	 */
//	SNAPChecksum() {
//		crc = 0;
//	}
//	
//	/**
//	 * Add data to the CRC computation
//	 * @param data
//	 * @return The data passed for convenience
//	 */
//	byte addData(byte data) {
//		byte i = (byte)(data ^ crc);
//		
//		crc = 0;
//		
//		if((i & 1) != 0)
//			crc ^= 0x5e;
//		if((i & 2) != 0)
//			crc ^= 0xbc;
//		if((i & 4) != 0)
//			crc ^= 0x61;
//		if((i & 8) != 0)
//			crc ^= 0xc2;
//		if((i & 0x10) != 0)
//			crc ^= 0x9d;
//		if((i & 0x20) != 0)
//			crc ^= 0x23;
//		if((i & 0x40) != 0)
//			crc ^= 0x46;
//		if((i & 0x80) != 0)
//			crc ^= 0x8c;
//		return data;
//	}
//	
//	/**
//	 * @return
//	 */
//	byte getResult() {
//		return crc;
//	}
//	
//}
