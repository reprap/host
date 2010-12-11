//package org.reprap.comms.snap;
//
//import java.io.IOException;
//
//public class SNAPPacket {
//	
//	/**
//	 * 
//	 */
//	private final int offset_sync = 0;
//	
//	/**
//	 * 
//	 */
//	private final int offset_hdb2 = 1;
//	
//	/**
//	 * 
//	 */
//	private final int offset_hdb1 = 2;
//	
//	/**
//	 * 
//	 */
//	private final int offset_dab = 3;
//	
//	/**
//	 * 
//	 */
//	private final int offset_sab = 4;
//	
//	/**
//	 * 
//	 */
//	private final int offset_payload = 5;
//	
//	/**
//	 * 
//	 */
//	private final byte syncMarker = 0x54;
//	
//	/**
//	 * 
//	 */
//	private final int maxSize = 64;
//	
//	/**
//	 * Full raw packet contents including all headers 
//	 */
//	private byte [] buffer;
//
//	/**
//	 * 
//	 */
//	private int receiveLength = 0;
//	
//	/**
//	 * 
//	 */
//	private boolean complete = false;
//	
//	/**
//	 * 
//	 */
//	SNAPPacket() {
//		buffer = new byte[maxSize];
//	}
//	
//	/**
//	 * @param srcAddress
//	 * @param destAddress
//	 * @param payload
//	 */
//	SNAPPacket(SNAPAddress srcAddress, SNAPAddress destAddress, byte [] payload) {
//		buffer = new byte[payload.length + offset_payload + 1];
//		buffer[offset_sync] = syncMarker;
//		buffer[offset_hdb2] = 0x51;
//		buffer[offset_hdb1] = 0x30;
//		buffer[offset_dab] = (byte)destAddress.getAddress();
//		buffer[offset_sab] = (byte)srcAddress.getAddress();
//		setLength(payload.length);
//		for(int i = 0; i < payload.length; i++)
//			buffer[i + offset_payload] = payload[i];
//		generateChecksum();
//		complete = true;
//	}
//
//	/**
//	 * 
//	 */
//	private void generateChecksum() {
//		int length = getLength() + offset_payload;
//		SNAPChecksum crc = new SNAPChecksum();
//		for(int i = 1; i < length; i++)
//			crc.addData(buffer[i]);
//		buffer[length] = crc.getResult();
//	}
//	
//	/**
//	 * @return the packet type
//     */
//	public byte getPacketType() {
//		return buffer[0]; // TODO fix offset
//	}
//	
//    /**
//     * @return the payload
//     */
//    public byte [] getPayload() {
//    	int length = getLength();
//    	byte [] payload = new byte[length];
//    	for(int i = 0; i < length; i++)
//    		payload[i] = buffer[i + offset_payload];
//		return payload;
//	}
//	
//	/**
//	 * @return the raw data of the packet
//	 */
//	public byte [] getRawData() {
//		return buffer;
//	}
//	
//	/**
//	 * 
//	 * @param data
//	 * @return true is the packet is now complete, otherwise false
//	 * @throws IOException 
//	 */
//	public boolean receiveByte(byte data) throws IOException {
//		if (complete)
//			throw new IOException("Received data beyond end of packet");
//		
//		if (receiveLength >= maxSize)
//			throw new IOException("Received too much data");
//		buffer[receiveLength++] = data;
//		
//		if (receiveLength > 4) {
//			int expectedLength = getLength() + offset_payload + 1;
//			if (receiveLength >= expectedLength)
//				return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * @return true if the packet passed validation
//	 */
//	public boolean validate() {
//		if (receiveLength < offset_payload)
//			return false;
//		int expectedLength = getLength() + offset_payload + 1;
//		if (receiveLength != expectedLength)
//			return false;
//
//		SNAPChecksum crc = new SNAPChecksum();
//		for(int i = offset_hdb2; i < receiveLength - 1; i++)
//			crc.addData(buffer[i]);
//		
//		byte expectedCRC = buffer[receiveLength - 1];
//		return crc.getResult() == expectedCRC;
//	}
//	
//	/**
//	 * @return the source address of the packet
//	 */
//	public SNAPAddress getSourceAddress() {
//		return new SNAPAddress(buffer[offset_sab]);
//	}
//	
//	/**
//	 * @return the destination address for the packet
//	 */
//	public SNAPAddress getDestinationAddress() {
//		return new SNAPAddress(buffer[offset_dab]);
//	}
//
//	/**
//	 * @param length
//	 */
//	private void setLength(int length) {
//		buffer[offset_hdb1] = (byte)((buffer[offset_hdb1] & 0xf0) |
//				(length > 7 ? 8 : length));
//	}
//	
//	/**
//	 * @return the lenght of the packet
//	 */
//	public int getLength() {
//		int l = buffer[offset_hdb1] & 0x0f;
//		if ((l & 8) != 0)
//			return 8 << (l & 7);
//		return l;
//	}
//
//	/**
//	 * @return NAK packet
//	 */
//	public SNAPPacket generateNAK() {
//		SNAPPacket resp = new SNAPPacket(getDestinationAddress(), getSourceAddress(), new byte [] {});
//		resp.buffer[offset_hdb2] = (byte)((resp.buffer[offset_hdb2] & 0xfc) | 3);
//		resp.generateChecksum();
//		return resp;
//	}
//
//	/**
//	 * @return ACK packet
//	 */
//	public SNAPPacket generateACK() {
//		SNAPPacket resp = new SNAPPacket(getDestinationAddress(), getSourceAddress(), new byte [] {});
//		resp.buffer[offset_hdb2] = (byte)((resp.buffer[offset_hdb2] & 0xfc) | 2);
//		resp.generateChecksum();
//		return resp;
//	}
//	
//	/**
//	 * @return true if the packet represents an ACK
//	 */
//	public boolean isAck() {
//		return ((buffer[offset_hdb2] & 3) == 2);
//	}
//
//	/**
//	 * @return true if the packet represents a NAK
//	 */
//	public boolean isNak() {
//		return ((buffer[offset_hdb2] & 3) == 3);
//	}
//}
