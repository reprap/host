//package org.reprap.gui;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.IOException;
//
//import javax.swing.ButtonGroup;
//import javax.swing.JButton;
//
//import javax.swing.JFrame;
//import javax.swing.JOptionPane;
//import javax.swing.JRadioButton;
//
//import org.reprap.devices.GenericStepperMotor;
//
///**
// *
// * Allow a user to adjust the Z axis positioning prior to production.
// *
// */
//
///**
//* This code was edited or generated using CloudGarden's Jigloo
//* SWT/Swing GUI Builder, which is free for non-commercial
//* use. If Jigloo is being used commercially (ie, by a corporation,
//* company or business for any purpose whatever) then you
//* should purchase a license for each developer using Jigloo.
//* Please visit www.cloudgarden.com for details.
//* Use of Jigloo implies acceptance of these licensing terms.
//* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
//* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
//* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
//*/
//public class CalibrateZAxis extends javax.swing.JDialog {
//	private static final long serialVersionUID = 1L;
//
//	private JRadioButton radio1step;
//	private JRadioButton radio1mm;
//	private JRadioButton radio10mm;
//	private JButton buttonZInc;
//	private JButton buttonZDec;
//	private JRadioButton radio10step;
//	private JRadioButton radio50step;
//	private ButtonGroup stepSizeGroup;
//	private JButton okButton;
//
//	private GenericStepperMotor motor;
//	private double scaleZ;
//	private int motorPosition;
//	private int motorSpeed;
//	
//	public CalibrateZAxis(JFrame frame, GenericStepperMotor motor, double scaleZ, int speed) throws IOException {
//		super(frame);
//		this.motor = motor;
//		this.scaleZ = scaleZ;
//		this.motorSpeed = speed;
//		initGUI();
//		motorPosition = motor.getPosition();
//	}
//	
//	private void initGUI() {
//		try {
//			{
//				{
//					stepSizeGroup = new ButtonGroup();
//				}
//				okButton = new JButton();
//				getContentPane().add(okButton);
//				okButton.setText("Continue...");
//				okButton.setBounds(105, 147, 105, 28);
//				okButton.setMnemonic(java.awt.event.KeyEvent.VK_ENTER);
//				okButton.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent evt) {
//						okButtonActionPerformed(evt);
//					}
//				});
//			}
//			{
//				radio1step = new JRadioButton();
//				getContentPane().add(radio1step);
//				radio1step.setText("1 step");
//				radio1step.setBounds(28, 14, 150, 28);
//				stepSizeGroup.add(radio1step);
//			}
//			{
//				radio10step = new JRadioButton();
//				getContentPane().add(radio10step);
//				radio10step.setText("10 steps");
//				radio10step.setBounds(28, 35, 147, 28);
//				stepSizeGroup.add(radio10step);
//			}
//			{
//				radio50step = new JRadioButton();
//				getContentPane().add(radio50step);
//				radio50step.setText("50 steps");
//				radio50step.setBounds(28, 56, 147, 28);
//				stepSizeGroup.add(radio50step);
//			}
//			{
//				radio1mm = new JRadioButton();
//				getContentPane().add(radio1mm);
//				radio1mm.setText("1mm");
//				radio1mm.setBounds(28, 77, 147, 28);
//				stepSizeGroup.add(radio1mm);
//			}
//			{
//				radio10mm = new JRadioButton();
//				getContentPane().add(radio10mm);
//				radio10mm.setText("10mm");
//				radio10mm.setBounds(28, 98, 147, 28);
//				stepSizeGroup.add(radio10mm);
//			}
//			{
//				buttonZInc = new JButton();
//				getContentPane().add(buttonZInc);
//				buttonZInc.setText("Increase Z");
//				buttonZInc.setBounds(182, 28, 119, 28);
//				buttonZInc.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent evt) {
//						buttonZIncActionPerformed(evt);
//					}
//				});
//			}
//			{
//				buttonZDec = new JButton();
//				getContentPane().add(buttonZDec);
//				buttonZDec.setText("Decrease Z");
//				buttonZDec.setBounds(182, 63, 119, 28);
//				buttonZDec.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent evt) {
//						buttonZDecActionPerformed(evt);
//					}
//				});
//			}
//			{
//				getContentPane().setLayout(null);
//				this.setTitle("Calibrate Z axis");
//			}
//			this.setSize(316, 214);
//			this.getRootPane().setDefaultButton(okButton);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private double getStepCount() {
//		double multiplier;
//		if (radio1step.isSelected())
//			multiplier = 1;
//		else if (radio10step.isSelected())
//			multiplier = 10;
//		else if (radio50step.isSelected())
//			multiplier = 50;
//		else if (radio1mm.isSelected())
//			multiplier = scaleZ;
//		else if (radio10mm.isSelected())
//			multiplier = scaleZ * 10.0;
//		else
//			multiplier = 1;
//		return multiplier;
//	}
//	
//	private void okButtonActionPerformed(ActionEvent evt) {
//		synchronized(this) {
//			notify();
//		}
//	}
//	
//	private void buttonZIncActionPerformed(ActionEvent evt) {
//		motorPosition += getStepCount();
//		setPosition();
//	}
//	
//	private void buttonZDecActionPerformed(ActionEvent evt) {
//		motorPosition -= getStepCount();
//		setPosition();
//	}
//	
//	private void setPosition() {
//		try {
//			motor.seek(motorSpeed, motorPosition);
//		} catch (Exception ex) {
//			JOptionPane.showMessageDialog(null, "Exception during calibration: " + ex);
//		}
//	}
//
//}
