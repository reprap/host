//package org.reprap.comms.snap;
//
//import org.reprap.comms.Address;
//
///**
// *
// */
//public class SNAPAddress implements Address {	
//
//	/**
//	 * 
//	 */
//	private short address;
//
//	/**
//	 * @param address
//	 */
//	public SNAPAddress(int address) {
//		this((short)address);
//	}
//	
//	/**
//	 * @param address
//	 */
//	public SNAPAddress(short address) {
//		this.address = address;
//	}
//	
//	/**
//	 * @param address
//	 */
//	public SNAPAddress(String address) {
//		this(Short.parseShort(address));
//	}
//	
//	/**
//	 * @return the address 
//	 */
//	public short getAddress() {
//		return address;
//	}
//
//	/**
//	 * @param address
//	 */
//	public void setAddress(short address) {
//		this.address = address;
//	}
//	
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	public boolean equals(Object arg) {
//		if (arg == this)
//			return true;
//		if (arg == null)
//			return false;
//		if (!(arg instanceof SNAPAddress))
//			return false;
//		return address == ((SNAPAddress)arg).address;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.reprap.comms.Address#getBinary()
//	 */
//	public byte[] getBinary() {
//		byte [] addr = new byte[1];
//		addr[0] = (byte)address;
//		return addr;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.reprap.comms.Address#getNullAddress()
//	 */
//	public Address getNullAddress() {
//		return new SNAPAddress(255);
//	}
//	
//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	public String toString() {
//		return Integer.toString(address);
//	}
//}
