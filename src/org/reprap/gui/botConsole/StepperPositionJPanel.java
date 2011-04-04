/*
 * StepperPositionJPanel.java
 *
 * Created on June 30, 2008, 4:59 PM
 */

package org.reprap.gui.botConsole;

import java.io.IOException;
import javax.swing.JOptionPane;
import org.reprap.Preferences;
import org.reprap.Printer;
import org.reprap.utilities.Debug;

//import org.reprap.AxisMotor;
//import org.reprap.comms.Communicator;
//import org.reprap.comms.snap.SNAPAddress;
//import org.reprap.devices.GenericStepperMotor;

/**
 *
 * @author  ensab
 */
public class StepperPositionJPanel extends javax.swing.JPanel {
	private static final long serialVersionUID = 1L;
    //private Communicator communicator;
    private int motorID;
    private String axis;
    //private GenericStepperMotor motor;
    private Printer printer;

    private double currentSpeed;
    @SuppressWarnings("unused")
	//private double motorStepsPerMM;
    private double axisLength;
    private double nudgeSize;
    private double newTargetAfterNudge;
    private BotConsoleFrame parentBotConsoleFrame = null;
    /**
     * Really set it up under control...
     * @param m
     * @throws java.io.IOException
     */
    public void postSetUp(int m) throws IOException {
        
        //communicator = org.reprap.Main.getCommunicator();
        motorID = m;
        printer = org.reprap.Main.gui.getPrinter();

        switch(motorID)
        {
        case 1:
                axis = "X";
                //motor = printer.getXMotor();
                axisLength = Preferences.loadGlobalDouble("WorkingX(mm)"); // TODO: Replace with Prefs when Java3D parameters work for small wv's.
                break;
        case 2:
                axis = "Y";
                //motor = printer.getYMotor();
                axisLength = Preferences.loadGlobalDouble("WorkingY(mm)"); // TODO: Replace with Prefs when Java3D parameters work for small wv's.
                break;
        case 3:
                axis = "Z";
                //motor = printer.getZMotor();
                axisLength = Preferences.loadGlobalDouble("WorkingZ(mm)"); // TODO: Replace with Prefs when Java3D parameters work for small wv's.
                break;
        default:
                axis = "X";
        		//motor = printer.getXMotor();
                Debug.e("StepperPanel - dud axis id:" + motorID);
        }
        
//        if(!motor.isAvailable())
//        {
//            deactivateMotorPanel();  
//            return;
//        }
        
        targetPosition.setEnabled(true);
        stepDownButton.setEnabled(true);
        stepUpButton.setEnabled(true);
        axisLabel.setText(axis);
        targetPosition.setText("0");
        storedPosition.setText("0");
        
        //int address = Preferences.loadGlobalInt(axis + "Axis" + "Address");
        
        //motor = new GenericStepperMotor(communicator, new SNAPAddress(address), Preferences.getGlobalPreferences(), motorID);

        //motorStepsPerMM = Preferences.loadGlobalDouble(axis + "AxisScale(steps/mm)");
	
//      TODO: Activate this code when the Java3D parameters allow a small enough working volume. Currently I get a black screen.
//      axisLength = Preferences.loadGlobalDouble("Working" + axis + "(mm)");

    }
    
    public void setConsoleFrame(BotConsoleFrame b)
    {
    	parentBotConsoleFrame = b;
    }
    
//    private void deactivateMotorPanel() {
//            //motor.dispose();
//            //motor = null;
//            axisLabel.setEnabled(false);
//            targetPosition.setEnabled(false);
//            //endButton.setEnabled(false);
//            homeButton.setEnabled(false);
//            storeButton.setEnabled(false);
//            recallButton.setEnabled(false);
//            stepDownButton.setEnabled(false);
//            stepUpButton.setEnabled(false);
//    }
    
//    private int getSpeedFromFeed(double f)
//    {
//    	if (axis.equals("Z"))
//    		return printer.convertFeedrateToSpeedZ(f);
//    	else
//    		return printer.convertFeedrateToSpeedXY(f);
//    }
    
    public void zeroBox()
    {
    	targetPosition.setText("0");
    }
    
    public void homeAxis() {
    	parentBotConsoleFrame.suspendPolling();
        try {
            setSpeed();
			switch(motorID)
			{
			case 1:
				printer.homeToZeroX();
				break;
			case 2:
				printer.homeToZeroY();
				break;
			case 3:
				printer.homeToZeroZ();
				break;
			default:
				Debug.e("StepperPositionPanel - homeReset.  Dud motor id: " + motorID);
			}
            //motor.homeReset(getSpeedFromFeed(currentSpeed));
            zeroBox();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Could not home motor: " + ex);
        }
        parentBotConsoleFrame.resumePolling();
    }
    
    public void setSpeed() {
        if (motorID == 3)
        {
        	currentSpeed = Double.parseDouble(org.reprap.gui.botConsole.XYZTabPanel.zSpeedField.getText());
        	//printer.setFeedrate(currentSpeed);
        } else
        {
       		currentSpeed = Double.parseDouble(org.reprap.gui.botConsole.XYZTabPanel.xySpeedField.getText());
       		//printer.setFeedrate(currentSpeed);
        }
    }
    
    public double getTargetPositionInMM() {
        double targetMM = Double.parseDouble(targetPosition.getText());
        if (targetMM > axisLength) {
            targetMM = axisLength;
            targetPosition.setText("" + round(targetMM, 2));
        }
        if (targetMM < 0) {
            targetMM = 0;
            targetPosition.setText("" + round(targetMM, 2));
        }
        return targetMM;
    }
    
//    private int getTargetPositionInSteps() {
//        double targetMM = getTargetPositionInMM();
//        return (int)Math.round(targetMM * motorStepsPerMM);
//    }
//    
//    private double stepsToMM(int steps)
//    {
//        return (double)steps/motorStepsPerMM;
//    }
    
    public void moveToTarget() {
    	parentBotConsoleFrame.suspendPolling();
    	    setSpeed();
    		double x, y, z, p;
    		x = printer.getX();
    		y = printer.getY();
    		z = printer.getZ();
    		p = getTargetPositionInMM();
    		try
    		{
    			switch(motorID)
    			{
    			case 1:
    				x = p;
    				break;
    			case 2:
    				y = p;
    				break;
    			case 3:
    				z = p;
    				break;
    			default:
    				Debug.e("moveToTarget()  Dud motor id: " + motorID);
    			}
    			printer.singleMove(x, y, z, currentSpeed);
    			//printer.moveTo(x, y, z, currentSpeed, false, false);
    		} catch (Exception ex)
    		{}
//	    	try {
//	        	motor.seek(getSpeedFromFeed(currentSpeed), getTargetPositionInSteps());
//	        } 
//	        catch (Exception ex) {
//	            JOptionPane.showMessageDialog(null, axis + " motor could not seek: " + ex);
//	        }
    		parentBotConsoleFrame.resumePolling();
    }
    
//    public void moveToTargetBlocking() {
//    	    setSpeed();
//    		try {
//                motor.seekBlocking(getSpeedFromFeed(currentSpeed), getTargetPositionInSteps());
//            } 
//            catch (Exception ex) {
//                JOptionPane.showMessageDialog(null, axis + " motor could not block: " + ex);
//            }
//    }
    
    public double round(double Rval, int r2dp) {
        double p = (Double)Math.pow(10,r2dp);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (double)tmp/p;
    }
    
//    public void updateCurrentPositionLabels() {
//        currentPositionLabel.setText("(" + getTargetPositionInMM() + ")");
//         
//    }
    
    public void setNudgeSize(double size) {
        nudgeSize = size;
    }
    
    public void setTargetPositionField(double coord) {
    	targetPosition.setText("" + coord);
    }

    
    /** Creates new form StepperPositionJPanel */
    public StepperPositionJPanel() {

      
        initComponents();
        

    }

    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stepUpButton = new javax.swing.JButton();
        stepDownButton = new javax.swing.JButton();
        homeButton = new javax.swing.JButton();
        mmLabel1 = new javax.swing.JLabel();
        targetPosition = new javax.swing.JTextField();
        axisLabel = new javax.swing.JLabel();
        storeButton = new javax.swing.JButton();
        recallButton = new javax.swing.JButton();
        storedPosition = new javax.swing.JLabel();
        mmLabel2 = new javax.swing.JLabel();

        stepUpButton.setText(">");
        stepUpButton.setEnabled(false);
        stepUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepUpButtonActionPerformed(evt);
            }
        });

        stepDownButton.setText("<");
        stepDownButton.setEnabled(false);
        stepDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepDownButtonActionPerformed(evt);
            }
        });

        homeButton.setText("Home");
        homeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeButtonActionPerformed(evt);
            }
        });

        mmLabel1.setFont(new java.awt.Font("Tahoma", 0, 12));
        mmLabel1.setText("mm");

        targetPosition.setColumns(4);
        targetPosition.setFont(targetPosition.getFont().deriveFont((float)12));
        targetPosition.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        targetPosition.setText("0");
        targetPosition.setEnabled(false);

        axisLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        axisLabel.setText("X");

        storeButton.setText("Sto");
        storeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeButtonActionPerformed(evt);
            }
        });

        recallButton.setText("Rcl");
        recallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recallButtonActionPerformed(evt);
            }
        });

        storedPosition.setFont(new java.awt.Font("Tahoma", 0, 12));
        storedPosition.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        storedPosition.setText("(?)");
        storedPosition.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        storedPosition.setMaximumSize(new java.awt.Dimension(50, 15));
        storedPosition.setPreferredSize(new java.awt.Dimension(50, 15));

        mmLabel2.setFont(new java.awt.Font("Tahoma", 0, 12));
        mmLabel2.setText("mm");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(axisLabel)
                .add(4, 4, 4)
                .add(targetPosition, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(3, 3, 3)
                .add(mmLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stepDownButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stepUpButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(homeButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(storeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(recallButton)
                .add(3, 3, 3)
                .add(storedPosition, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mmLabel2)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(axisLabel)
                    .add(targetPosition, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mmLabel1)
                    .add(stepDownButton)
                    .add(stepUpButton)
                    .add(homeButton)
                    .add(storeButton)
                    .add(recallButton)
                    .add(storedPosition, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mmLabel2))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void stepUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepUpButtonActionPerformed
        setSpeed();
        newTargetAfterNudge = getTargetPositionInMM() + nudgeSize;
        targetPosition.setText("" + round(newTargetAfterNudge, 2));
        moveToTarget();
}//GEN-LAST:event_stepUpButtonActionPerformed

private void stepDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stepDownButtonActionPerformed
        setSpeed();
        //System.out.println(nudgeSize);
        double newTargetAfterNudge = getTargetPositionInMM() - nudgeSize;
        targetPosition.setText("" + round(newTargetAfterNudge, 2));
        moveToTarget();
}//GEN-LAST:event_stepDownButtonActionPerformed

private void homeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeButtonActionPerformed
    homeAxis();
}//GEN-LAST:event_homeButtonActionPerformed

public void store()
{
//    double pos = 0;
//		switch(motorID)
//		{
//		case 1:
//			pos = printer.getX();
//			break;
//		case 2:
//			pos = printer.getY();
//			break;
//		case 3:
//			pos = printer.getZ();
//			break;
//		default:
//			Debug.e("store()  Dud motor id: " + motorID);
//		}
//    try
//    {   
//        moPos = motor.getPosition();
//    } catch (Exception ex) {}
//    double pos = stepsToMM(moPos);
    storedPosition.setText(targetPosition.getText());    
}

private void storeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeButtonActionPerformed
    store();
}//GEN-LAST:event_storeButtonActionPerformed

public void recall()
{
    targetPosition.setText(storedPosition.getText());
}

private void recallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recallButtonActionPerformed
    recall();
    
}//GEN-LAST:event_recallButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel axisLabel;
    private javax.swing.JButton homeButton;
    private javax.swing.JLabel mmLabel1;
    private javax.swing.JLabel mmLabel2;
    private javax.swing.JButton recallButton;
    private javax.swing.JButton stepDownButton;
    private javax.swing.JButton stepUpButton;
    private javax.swing.JButton storeButton;
    private javax.swing.JLabel storedPosition;
    private javax.swing.JTextField targetPosition;
    // End of variables declaration//GEN-END:variables

}
