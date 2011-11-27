/*
 * * !!!!!
 * NOTE: PLEASE ONLY EDIT THIS USING THE NETBEANS IDE 6.0.1 OR HIGHER!!!!
 * !!!!!
 * 
 * ... an .xml file is associated with this class. Cheers. 
 *
 * GenericExtruderTabPanel.java
 *
 * Created on 27 March 2008, 18:22
 */

package org.reprap.gui.botConsole;

import org.reprap.Preferences;
import org.reprap.Printer;
import org.reprap.utilities.Timer;
import org.reprap.Extruder;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
/**
 *
 * @author  en0es
 */
public class GenericExtruderTabPanel extends javax.swing.JPanel {
	private static final long serialVersionUID = 1L;
    private int extruderID = 0;
    private boolean heatPushed = false;
    private double startTime = -1;
    private boolean ramping = false;
    private double startTemp = -1;
	private BotConsoleFrame parentBotConsoleFrame = null;
    private Extruder extruder;
    private String prefix;
 
    /** Creates new form GenericExtruderTabPanel */
    public GenericExtruderTabPanel() {
 
        UIManager.put("ProgressBar.background", Color.WHITE);
        UIManager.put("ProgressBar.foreground", Color.BLUE);

        initComponents(); 
        RampRate.setText("0.2"); // Sensible default
        
    }
    
    /**
     * So the BotConsoleFrame can let us know who it is
     * @param b
     */
    public void setConsoleFrame(BotConsoleFrame b)
    {
    	parentBotConsoleFrame = b;
    }
    
    private void deactivatePanel() {

            coolingCheck.setEnabled(false);
            currentTempLabel.setEnabled(false);
            extrudeButton.setEnabled(false);
            //feedstockQtyLabel.setEnabled(false);
            heatButton.setEnabled(false);
            homeAllButton.setEnabled(false);
            jLabel1.setEnabled(false);
            jLabel11.setEnabled(false);
            jLabel12.setEnabled(false);
            //jLabel4.setEnabled(false);
            //jLabel5.setEnabled(false);
            jLabel6.setEnabled(false);
            jLabel7.setEnabled(false);
            jPanel2.setEnabled(false);
            jPanel3.setEnabled(false);
            jPanel4.setEnabled(false);
            materialLabel.setEnabled(false);
            motorReverseCheck.setEnabled(false);
            motorSpeedField.setEnabled(false);
            moveToDumpButton.setEnabled(false);
            homeXYbutton.setEnabled(false);
            targetTempField.setEnabled(false);
            tempColor.setEnabled(false);
            tempProgress.setEnabled(false);
            valveToggleButton.setEnabled(false);
            RampButton.setEnabled(false);
            RampRate.setEnabled(false);

    }
    
    public Extruder getExtruder()
    {
    	return extruder;
    }

    public void initialiseExtruders(int id) throws Exception {
           	
        extruderID = id;
        prefix = "Extruder" + id + "_";
        

        Printer p = org.reprap.Main.gui.getPrinter();
        Extruder extruders[] = p.getExtruders();
        extruder = extruders[extruderID];
        
        if(!extruder.isAvailable()) 
        {
            deactivatePanel();
        }
            
    }
    
    public void selectExtruder()
    {
    	try {
			org.reprap.Main.gui.getPrinter().selectExtruder(extruderID, true);
		} catch (Exception e) {
			parentBotConsoleFrame.handleException(e);
		}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        materialLabel = new javax.swing.JLabel();
        //feedstockQtyLabel = new javax.swing.JLabel();
        //jLabel4 = new javax.swing.JLabel();
        //jLabel5 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        targetTempField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        currentTempLabel = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        tempProgress = new javax.swing.JProgressBar();
        tempColor = new javax.swing.JPanel();
        heatButton = new javax.swing.JToggleButton();
        coolingCheck = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        RampRate = new javax.swing.JTextField();
        RampButton = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        motorSpeedField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        extrudeButton = new javax.swing.JToggleButton();
        valveToggleButton = new javax.swing.JToggleButton();
        motorReverseCheck = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        homeXYbutton = new javax.swing.JToggleButton();
        homeAllButton = new javax.swing.JToggleButton();
        moveToDumpButton = new javax.swing.JToggleButton();

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel1.setText("Material:");

        materialLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        materialLabel.setText("materialType");

        //feedstockQtyLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        //feedstockQtyLabel.setText("00000");

        //jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12));
        //jLabel4.setText("Feedstock remaining:");

        //jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12));
        //jLabel5.setText("ml");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Temperature (degrees Celcius)"));

        targetTempField.setColumns(3);
        targetTempField.setFont(targetTempField.getFont().deriveFont(targetTempField.getFont().getSize()+1f));
        targetTempField.setText("000");

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel7.setText("Target temperature:");

        currentTempLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        currentTempLabel.setText("000");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel6.setText("Current temperature:");

        tempProgress.setOrientation(1);

        tempColor.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout tempColorLayout = new org.jdesktop.layout.GroupLayout(tempColor);
        tempColor.setLayout(tempColorLayout);
        tempColorLayout.setHorizontalGroup(
            tempColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 52, Short.MAX_VALUE)
        );
        tempColorLayout.setVerticalGroup(
            tempColorLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 54, Short.MAX_VALUE)
        );

        heatButton.setText("Switch heat on");
        heatButton.setFocusCycleRoot(true);
        heatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heatButtonActionPerformed(evt);
            }
        });

        coolingCheck.setFont(coolingCheck.getFont().deriveFont(coolingCheck.getFont().getSize()+1f));
        coolingCheck.setText("Cooling");
        coolingCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coolingCheckActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel8.setText("Ramp rate (C/sec):");

        RampRate.setColumns(3);
        RampRate.setFont(RampRate.getFont().deriveFont(RampRate.getFont().getSize()+1f));
        RampRate.setText("000");

        RampButton.setText("Ramp");
        RampButton.setFocusCycleRoot(true);
        RampButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RampButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(RampRate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .add(jLabel6)
                        .add(18, 18, 18)
                        .add(currentTempLabel))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(targetTempField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(140, 140, 140)
                .add(RampButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tempProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tempColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(coolingCheck)
                    .add(heatButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(heatButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tempColor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, RampButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(currentTempLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(targetTempField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel7)))
                    .add(tempProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(coolingCheck)
                    .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(RampRate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel8)))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Extrude"));

        motorSpeedField.setColumns(3);
        motorSpeedField.setFont(motorSpeedField.getFont().deriveFont(motorSpeedField.getFont().getSize()+1f));
        motorSpeedField.setText("000");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel12.setText("mm/min");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel11.setText("Extrude speed:");

        extrudeButton.setText("Extrude");
        extrudeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extrudeButtonActionPerformed(evt);
            }
        });

        valveToggleButton.setSelected(true);
        valveToggleButton.setText("Close valve");
        valveToggleButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        valveToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valveToggleButtonActionPerformed(evt);
            }
        });

        motorReverseCheck.setFont(motorReverseCheck.getFont().deriveFont(motorReverseCheck.getFont().getSize()+1f));
        motorReverseCheck.setText("Reverse");
        motorReverseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                motorReverseCheckActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(motorSpeedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel12)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 135, Short.MAX_VALUE)
                .add(valveToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(extrudeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(motorReverseCheck)
                .add(13, 13, 13))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel11)
                .add(motorSpeedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel12)
                .add(extrudeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(motorReverseCheck)
                .add(valveToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Maintenance"));

        homeXYbutton.setText("Home X & Y");
        homeXYbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeXYbuttonActionPerformed(evt);
            }
        });

        homeAllButton.setText("Home all");
        homeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeAllButtonAction(evt);
            }
        });

        moveToDumpButton.setText("Move to dump point");
        moveToDumpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveToDumpPointAction(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(275, Short.MAX_VALUE)
                .add(homeAllButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(homeXYbutton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(moveToDumpButton)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(homeXYbutton)
                .add(moveToDumpButton)
                .add(homeAllButton))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 
                    		Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(materialLabel))
                            .add(layout.createSequentialGroup()
                                //.add(jLabel4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                //.add(feedstockQtyLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                //.add(jLabel5)
                                ))
                        .add(202, 202, 202))
                    .add(layout.createSequentialGroup()
                        .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(materialLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    //.add(jLabel4)
                    //.add(jLabel5)
                    //.add(feedstockQtyLabel)
                    )
                .add(18, 18, 18)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void coolingCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coolingCheckActionPerformed
    	parentBotConsoleFrame.suspendPolling();
    	selectExtruder();
        try {
            extruder.setCooler(coolingCheck.isSelected(), true);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Exception setting cooler: " + ex);
            ex.printStackTrace();
        }
        parentBotConsoleFrame.resumePolling();
}//GEN-LAST:event_coolingCheckActionPerformed

private void motorReverseCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_motorReverseCheckActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_motorReverseCheckActionPerformed


    
private void heatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heatButtonActionPerformed
    	parentBotConsoleFrame.suspendPolling();
    	selectExtruder();
    	if (heatPushed) {
    		rampOff();
    		try {
    			extruder.setTemperature(0, false);
    		}
    		catch (Exception ex) {
    			JOptionPane.showMessageDialog(null, "Exception setting temperature: " + ex);
    			ex.printStackTrace();
    		}
    		heatButton.setText("Switch heater on");
    		heatPushed = false;
    	}
    	else {
    		try {
    			extruder.setTemperature(Integer.parseInt(targetTempField.getText()), false);
    		}
    		catch (Exception ex) {
    			JOptionPane.showMessageDialog(null, "Exception setting temperature: " + ex);
    			ex.printStackTrace();
    		}
    		heatButton.setText("Switch heater off");
    		heatPushed = true;
    	}
    	parentBotConsoleFrame.resumePolling();
    	
    }//GEN-LAST:event_heatButtonActionPerformed

private void setExtruderSpeed() {
    try {
            extruder.setExtrusion(extruding?Double.parseDouble(motorSpeedField.getText()):0, motorReverseCheck.isSelected());
    } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Extruder exception: " + ex);
            ex.printStackTrace();
    }
}

    private boolean extruding = false;
    
    private void extrudeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extrudeButtonActionPerformed
        if (extruding) {
                extruding = false;
                extrudeButton.setText("Extrude");

        } else {
                extruding = true;
                extrudeButton.setText("Stop extruding");

                //System.out.println("Extruding at speed: " + motorSpeedField.getText());
        }
        parentBotConsoleFrame.suspendPolling();
        selectExtruder();
        setExtruderSpeed();
        if(extruder.get5D() && extruding)
		{
			try {
				extruder.getPrinter().machineWait(5000, false, true);
			} catch (Exception e) {
				parentBotConsoleFrame.handleException(e);
			}
			extruding = false;
			setExtruderSpeed();
            extrudeButton.setText("Extrude");
		}
        parentBotConsoleFrame.resumePolling();
    }//GEN-LAST:event_extrudeButtonActionPerformed

    private void homeXYbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeXYbuttonActionPerformed
//        Reprap.finishedLayer(1);
//        Reprap.betweenLayers(layerNumber);
    	BotConsoleFrame.getXYZTabPanel().homeXY();
    }//GEN-LAST:event_homeXYbuttonActionPerformed

    private void valveToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valveToggleButtonActionPerformed
    	parentBotConsoleFrame.suspendPolling();
    	selectExtruder();
        if (valveToggleButton.isSelected()) {
        	try
        	{
        		extruder.setValve(true);
        		valveToggleButton.setText("Shut valve");
        	} catch (Exception ex) {}
        }
        else {
           	try
        	{
           		extruder.setValve(false);
           		valveToggleButton.setText("Open valve");
        	} catch (Exception ex) {}
        }
        parentBotConsoleFrame.resumePolling();
}//GEN-LAST:event_valveToggleButtonActionPerformed

private void rampOff()
{
    RampButton.setText("Ramp");
    startTime = -1;
    ramping = false;	
}

private void RampButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RampButtonActionPerformed
    if (ramping)
    	rampOff();
    else 
    {
    	heatButton.setText("Switch heat off");
    	//GregorianCalendar cal = new GregorianCalendar();
    	//Date d = cal.getTime();
		startTime = Timer.elapsed();//d.getTime() + cal.getTimeZone().getOffset(d.getTime());
		try {
			startTemp = extruder.getTemperature() - 1;
		} catch (Exception e) {
			parentBotConsoleFrame.handleException(e);
		} // Start a bit below where we are for safety
    	RampButton.setText("Ramping");
        ramping = true;
    }
}//GEN-LAST:event_RampButtonActionPerformed

private void homeAllButtonAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeAllButtonAction
	BotConsoleFrame.getXYZTabPanel().homeAll();
}//GEN-LAST:event_homeAllButtonAction

private void moveToDumpPointAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveToSwapPointAction
	double z = BotConsoleFrame.getXYZTabPanel().getPrinter().getZ();
	if(z < 0.1)
	{
		z = 1.0;
		BotConsoleFrame.getXYZTabPanel().goTo(BotConsoleFrame.getXYZTabPanel().getPrinter().getX(),
				BotConsoleFrame.getXYZTabPanel().getPrinter().getY(), z);
	}
	BotConsoleFrame.getXYZTabPanel().goTo(BotConsoleFrame.getXYZTabPanel().getPrinter().getDumpX(),
			BotConsoleFrame.getXYZTabPanel().getPrinter().getDumpY(), z);
}//GEN-LAST:event_moveToSwapPointAction


    
    public double getExtruderSpeed()
    {
    	return Double.parseDouble(motorSpeedField.getText());
    }
    
    public void setPrefs() throws Exception {
                
        setMaterialLabel(Preferences.loadGlobalString(prefix + "MaterialType(name)"));
        setMotorSpeedField(Preferences.loadGlobalInt(prefix + "ExtrusionSpeed(mm/minute)"));
        setTargetTempField(Preferences.loadGlobalInt(prefix + "ExtrusionTemp(C)"));
    } 
    
    private void setMaterialLabel(String materialType) {
        materialLabel.setText(materialType);
    }
    
    private void setMotorSpeedField(int speed) {
        motorSpeedField.setText(""+speed);
    }
    
    private void setTargetTempField(int temp) {
        targetTempField.setText(""+temp);
    }
    
    private int currentTemp;
    private final int BURNING_TEMP = 70;
    private double colorFactor = 0;
    private Color c;
    
    public void refreshTemperature() {
        try {
			currentTemp = (int)Math.round(extruder.getTemperature());
		} catch (Exception e) {
			parentBotConsoleFrame.handleException(e);
		}
        currentTempLabel.setText("" + currentTemp);
        tempProgress.setMinimum(0);
        tempProgress.setMaximum(Integer.parseInt(targetTempField.getText()));
        tempProgress.setValue(currentTemp);
        
        colorFactor = currentTemp/(BURNING_TEMP*1.0);
        if (colorFactor > 1) colorFactor = 1;
        if (colorFactor < 0) colorFactor = 0;

        int red = (int)(colorFactor * 255.0);
        int blue = 255-(int)(colorFactor * 255.0);

        c = new Color(red, 0, blue);
        
        tempColor.setBackground(c);
        
        if(!ramping)
        	return;
        if(currentTemp >= Integer.parseInt(targetTempField.getText()))
        {
        	rampOff();
        	return;
        }
        
    	//GregorianCalendar cal = new GregorianCalendar();
    	//Date d = cal.getTime();
		double elapsed = Timer.elapsed() - startTime; //d.getTime() + cal.getTimeZone().getOffset(d.getTime()) - startTime;
		double newTarget = elapsed*Double.parseDouble(RampRate.getText()) + startTemp;
        if(newTarget >= Integer.parseInt(targetTempField.getText()))
        {
        	rampOff();
        	return;
        }
        try
        {
        	//System.out.println("elapsed: " + elapsed*0.001 + " target: " + newTarget);
        	extruder.setTemperature(newTarget, false);
        } catch (Exception ex)
        {}
    }
                               
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton RampButton;
    private javax.swing.JTextField RampRate;
    private javax.swing.JCheckBox coolingCheck;
    private javax.swing.JLabel currentTempLabel;
    private javax.swing.JToggleButton extrudeButton;
    //private javax.swing.JLabel feedstockQtyLabel;
    private javax.swing.JToggleButton heatButton;
    private javax.swing.JToggleButton homeAllButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    //private javax.swing.JLabel jLabel4;
    //private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel materialLabel;
    private javax.swing.JCheckBox motorReverseCheck;
    private javax.swing.JTextField motorSpeedField;
    private javax.swing.JToggleButton moveToDumpButton;
    private javax.swing.JToggleButton homeXYbutton;
    private javax.swing.JTextField targetTempField;
    private javax.swing.JPanel tempColor;
    private javax.swing.JProgressBar tempProgress;
    private javax.swing.JToggleButton valveToggleButton;
    // End of variables declaration//GEN-END:variables
    
}
