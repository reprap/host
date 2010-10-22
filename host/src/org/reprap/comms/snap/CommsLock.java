//package org.reprap.comms.snap;
//
///**
// * A temporary class to allow locking of comms while waiting
// * for the asynchronous context delivery mechanism to be implemented
// */
//public class CommsLock {
//
//	/**
//	 * indicates if the comms is locked or not
//	 */
//	private boolean locked = false;
//	
//	/**
//	 * Lock the comms
//	 */
//	synchronized public void lock() {
//		while(locked) {
//			//System.out.println("Lock: " + Thread.currentThread().getName() + " waiting");
//			try {
//				wait();
//			} catch (InterruptedException e) {
//				// Ignore interrupts and continue
//			}
//		}
//		locked = true;
//		//System.out.println("Lock: " + Thread.currentThread().getName() + " acquired");
//	}
//	
//	/**
//	 * Unlock the comms
//	 */
//	synchronized public void unlock() {
//		if (!locked)
//			System.err.println("Warning: Calling unlock from an already unlocked state!");
//		locked = false;
//		notify();
//		//System.out.println("Unlock: " + Thread.currentThread().getName() + " released");
//	}
//	
//}
