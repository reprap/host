/*
 * XYZTabPanel.java
 *
 * Created on June 30, 2008, 9:15 PM
 */

package org.reprap.gui.botConsole;

import java.io.IOException;
import org.reprap.Printer;
import org.reprap.ReprapException;
import org.reprap.utilities.Debug;
/**
 *
 * @author  ensab
 */
public class XYZTabPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 1L;

    private double XYfastSpeed;
    private double ZfastSpeed;
    private boolean firstZero = true;
    private Printer printer;
    private static double nudgeSize = 0;
    private BotConsoleFrame parentBotConsoleFrame = null;
    private Thread agitateThread = null;
    
    private void setPrefs() throws IOException {
        
        XYfastSpeed = printer.getExtruder().getFastXYFeedrate();
        ZfastSpeed = printer.getFastFeedrateZ();
        
        xySpeedField.setText(String.valueOf(XYfastSpeed));
        zSpeedField.setText(String.valueOf(ZfastSpeed));
    }
    
    /**
     * So the BotConsoleFrame can let us know who it is
     * @param b
     */
    public void setConsoleFrame(BotConsoleFrame b)
    {
        parentBotConsoleFrame = b;
        xStepperPositionJPanel.setConsoleFrame(b);
        yStepperPositionJPanel.setConsoleFrame(b);
        zStepperPositionJPanel.setConsoleFrame(b);
    }
    
    public void setMotorSpeeds() {
        
        xStepperPositionJPanel.setSpeed();
        yStepperPositionJPanel.setSpeed();
        zStepperPositionJPanel.setSpeed();
        
    }
    
    public void checkNudgeSize() {
        if (nudgeSize == 0) {
            nudgeSizeRB1.setSelected(true);
            nudgeSize = Double.parseDouble(nudgeSizeRB1.getText());
        }
    }
    
    public void setNudgeSize(Double size) {
        
        nudgeSize = size;
        
        xStepperPositionJPanel.setNudgeSize(nudgeSize);
        yStepperPositionJPanel.setNudgeSize(nudgeSize);
        zStepperPositionJPanel.setNudgeSize(nudgeSize);
    }
    
    /** Creates new form XYZTabPanel */
    public XYZTabPanel() {
        firstZero = true;
        printer = org.reprap.Main.gui.getPrinter();
        initComponents();
        try
        {
            setPrefs();
            xStepperPositionJPanel.postSetUp(1);
            yStepperPositionJPanel.postSetUp(2);
            zStepperPositionJPanel.postSetUp(3);
        } catch (Exception ex) {Debug.e(ex.toString());}
        setMotorSpeeds();
        setNudgeSize(Double.parseDouble(nudgeSizeRB1.getText()));
        agitate = false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        
        targetTempField = new javax.swing.JTextField();
        currentTempLabel = new javax.swing.JLabel();
        heatButton = new javax.swing.JToggleButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        bedTempPanel = new javax.swing.JPanel();
        heatPushed = false;
        targetTempField.setColumns(3);
        targetTempField.setFont(targetTempField.getFont().deriveFont(targetTempField.getFont().getSize()+1f));
        targetTempField.setText(String.valueOf(printer.getBedTemperatureTarget()));

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel7.setText("Target:");

        currentTempLabel.setFont(new java.awt.Font("Tahoma", 0, 12));
        currentTempLabel.setText("000");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel6.setText("Current:");
        
        heatButton.setText("Switch bed heat on");
        heatButton.setFocusCycleRoot(true);
        heatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heatButtonActionPerformed(evt);
            }
        });
        bedTempPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bed temp. (Celcius)"));
        
        
        agitateAmplitude = new javax.swing.JTextField();
        agitatePeriod = new javax.swing.JTextField();
        agitateButton = new javax.swing.JToggleButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        agitatePanel = new javax.swing.JPanel();
        agitate = false;
        agitateAmplitude.setColumns(3);
        agitateAmplitude.setFont(agitateAmplitude.getFont().deriveFont(agitateAmplitude.getFont().getSize()+1f));
        agitateAmplitude.setText("50");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel8.setText("Amplitude (mm):");
        
        agitatePeriod.setColumns(3);
        agitatePeriod.setFont(agitatePeriod.getFont().deriveFont(agitatePeriod.getFont().getSize()+1f));
        agitatePeriod.setText("3");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel9.setText("Period (s):");
        
        agitateButton.setText("Agitate Y");
        agitateButton.setFocusCycleRoot(true);
        agitateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agitateButtonActionPerformed(evt);
            }
        });        
        agitatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Agitate Y axis"));
        

        buttonGroup1 = new javax.swing.ButtonGroup();
        nudgePanel = new javax.swing.JPanel();
        nudgeSizeRB1 = new javax.swing.JRadioButton();
        nudgeSizeRB2 = new javax.swing.JRadioButton();
        nudgeSizeRB3 = new javax.swing.JRadioButton();
        motorsPanel = new javax.swing.JPanel();
        homeAllButton = new javax.swing.JButton();
        storeAllButton = new javax.swing.JButton();
        recallAllButton = new javax.swing.JButton();
        goButton = new javax.swing.JButton();
        xStepperPositionJPanel = new org.reprap.gui.botConsole.StepperPositionJPanel();
        yStepperPositionJPanel = new org.reprap.gui.botConsole.StepperPositionJPanel();
        zStepperPositionJPanel = new org.reprap.gui.botConsole.StepperPositionJPanel();
        speedsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        xySpeedField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        zSpeedField = new javax.swing.JTextField();
        plotExtruderCheck = new javax.swing.JCheckBox();
        extruderToPlotWith = new javax.swing.JTextField();

        nudgePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Nudge size (mm)"));

        buttonGroup1.add(nudgeSizeRB1);
        nudgeSizeRB1.setSelected(true);
        nudgeSizeRB1.setText("0.1");
        nudgeSizeRB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nudgeSizeRB1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(nudgeSizeRB2);
        nudgeSizeRB2.setText("1.0");
        nudgeSizeRB2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nudgeSizeRB2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(nudgeSizeRB3);
        nudgeSizeRB3.setText("10.0");
        nudgeSizeRB3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nudgeSizeRB3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout nudgePanelLayout = new org.jdesktop.layout.GroupLayout(nudgePanel);
        nudgePanel.setLayout(nudgePanelLayout);
        nudgePanelLayout.setHorizontalGroup(
            nudgePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(nudgePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(nudgeSizeRB1)
                .add(18, 18, 18)
                .add(nudgeSizeRB2)
                .add(18, 18, 18)
                .add(nudgeSizeRB3)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        nudgePanelLayout.setVerticalGroup(
            nudgePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, nudgePanelLayout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .add(nudgePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nudgeSizeRB1)
                    .add(nudgeSizeRB2)
                    .add(nudgeSizeRB3)))
        );

        motorsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Axis positions"));

        homeAllButton.setText("Home all");
        homeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeAllButtonActionPerformed(evt);
            }
        });

        storeAllButton.setText("Sto all");
        storeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeAllButtonActionPerformed(evt);
            }
        });

        recallAllButton.setText("Rcl all");
        recallAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recallAllButtonActionPerformed(evt);
            }
        });

        goButton.setText("Go");
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout motorsPanelLayout = new org.jdesktop.layout.GroupLayout(motorsPanel);
        motorsPanel.setLayout(motorsPanelLayout);
        motorsPanelLayout.setHorizontalGroup(
            motorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(motorsPanelLayout.createSequentialGroup()
                .add(motorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(motorsPanelLayout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(goButton)
                        .add(121, 121, 121)
                        .add(homeAllButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(storeAllButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(recallAllButton))
                    .add(xStepperPositionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(yStepperPositionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(zStepperPositionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                //.addContainerGap(24, Short.MAX_VALUE)
                )
        );
        motorsPanelLayout.setVerticalGroup(
            motorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, motorsPanelLayout.createSequentialGroup()
                .add(xStepperPositionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(yStepperPositionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(zStepperPositionJPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(motorsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(goButton)
                    .add(recallAllButton)
                    .add(storeAllButton)
                    .add(homeAllButton))
                .addContainerGap())
        );

        speedsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Axis speeds (mm/min)"));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel2.setText("X & Y");

        xySpeedField.setColumns(4);
        xySpeedField.setFont(new java.awt.Font("Tahoma", 0, 12));
        xySpeedField.setText("0000");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel3.setText("Z");

        zSpeedField.setColumns(4);
        zSpeedField.setFont(new java.awt.Font("Tahoma", 0, 12));
        zSpeedField.setText("0000");

        org.jdesktop.layout.GroupLayout speedsPanelLayout = new org.jdesktop.layout.GroupLayout(speedsPanel);
        speedsPanel.setLayout(speedsPanelLayout);
        speedsPanelLayout.setHorizontalGroup(
            speedsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(speedsPanelLayout.createSequentialGroup()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(xySpeedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(zSpeedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        speedsPanelLayout.setVerticalGroup(
            speedsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(speedsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel2)
                .add(jLabel3)
                .add(zSpeedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(xySpeedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        plotExtruderCheck.setText("Plot using Extruder #");

        extruderToPlotWith.setColumns(1);
        extruderToPlotWith.setText("0");
        
        
        
        
        
        
        
        
        
        org.jdesktop.layout.GroupLayout bedTempPanelLayout = new org.jdesktop.layout.GroupLayout(bedTempPanel);
        bedTempPanel.setLayout(bedTempPanelLayout);
        bedTempPanelLayout.setHorizontalGroup(
            bedTempPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, bedTempPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(bedTempPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, bedTempPanelLayout.createSequentialGroup()
                        .add(jLabel6)
                        .add(18, 18, 18)
                        .add(currentTempLabel))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, bedTempPanelLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(targetTempField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                //.add(bedTempPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                //    .add(heatButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                //.addContainerGap()
                )
                .add(heatButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE) 
        );
        bedTempPanelLayout.setVerticalGroup(
            bedTempPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, bedTempPanelLayout.createSequentialGroup()
                .add(bedTempPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    //.add(heatButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, bedTempPanelLayout.createSequentialGroup()
                        .add(bedTempPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(currentTempLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bedTempPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(targetTempField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel7)))
                    )
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(heatButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                .addContainerGap()
                )
        );
            
        
        org.jdesktop.layout.GroupLayout agitatePanelLayout = new org.jdesktop.layout.GroupLayout(agitatePanel);
        agitatePanel.setLayout(agitatePanelLayout);
        agitatePanelLayout.setHorizontalGroup(
                agitatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, agitatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(agitatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, agitatePanelLayout.createSequentialGroup()
                        .add(jLabel8)
                        .add(18, 18, 18)
                        .add(agitateAmplitude, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, agitatePanelLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel9)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(agitatePeriod, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 
                                org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                //.add(agitatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                //    .add(agitateButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                //.addContainerGap()
                )
                .add(agitateButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE) 
        );
        agitatePanelLayout.setVerticalGroup(
                agitatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, agitatePanelLayout.createSequentialGroup()
                .add(agitatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    //.add(agitateButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, agitatePanelLayout.createSequentialGroup()
                        .add(agitatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel8)
                            .add(agitateAmplitude))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(agitatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(agitatePeriod, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel9)))
                    )
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(agitateButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                .addContainerGap()
                )
        );
        
        
        

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(nudgePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(2, 2, 2)
                        .add(speedsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(plotExtruderCheck)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(extruderToPlotWith, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        //.add(235, 235, 235)
                        )
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup() 
             
                    .add(motorsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 
                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bedTempPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 
                            Short.MAX_VALUE)
                    .add(agitatePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 
                            Short.MAX_VALUE) 
                            )
                    )
                    //.add(agitatePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 
                    //      Short.MAX_VALUE)   
                    //.addContainerGap()
                )
        ));
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                   
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup() 
                   .add(bedTempPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 
                           org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                   .add(agitatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 
                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                               )
                   .add(motorsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 226, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                )
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nudgePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(speedsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(extruderToPlotWith, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(plotExtruderCheck))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


public void refreshTemperature()
{
    double t = 0;
    try {
        t = printer.getBedTemperature();
    } catch (Exception e) {
        parentBotConsoleFrame.handleException(e);
    }
    currentTempLabel.setText("" + t);
}

private void heatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heatButtonActionPerformed
        parentBotConsoleFrame.suspendPolling();
        if (heatPushed) 
        {
            try {
                printer.setBedTemperature(0);
            } catch (Exception e) {
                parentBotConsoleFrame.handleException(e);
            }
            heatButton.setText("Switch bed heat on");
            heatPushed = false;
        } else 
        {
            try {
                printer.setBedTemperature(Double.parseDouble(targetTempField.getText()));
            } catch (NumberFormatException e) {
                parentBotConsoleFrame.handleException(e);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            heatButton.setText("Switch bed heat off");
            heatPushed = true;
        }
        parentBotConsoleFrame.resumePolling();
        
}//GEN-LAST:event_heatButtonActionPerformed

//private javax.swing.JTextField agitateAmplitude;
//private javax.swing.JTextField agitatePeriod;
//private javax.swing.JLabel jLabel8;
//private javax.swing.JLabel jLabel9;
//private javax.swing.JPanel agitatePanel;
//private javax.swing.JToggleButton agitateButton;
//private boolean agitate; 

private void agitate(double a, double p)
{
    
}

private void agitateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heatButtonActionPerformed
    parentBotConsoleFrame.suspendPolling();
    if (agitate) 
    {
        agitate = false;
        agitateButton.setText("Agitate");
        parentBotConsoleFrame.resumePolling();
    } else 
    {
        agitate = true;
        agitateButton.setText("Stop Agitating");
        try {
            agitateThread = new Thread() 
            {
                public void run() 
                {
                    double amp = 0.5*Double.parseDouble(agitateAmplitude.getText());
                    double per = Double.parseDouble(agitatePeriod.getText());
                    double y0 = printer.getY() + amp;
                    double inc = 0.1/per;
                    double t = 0.75*per;
                    double y;
                    Thread.currentThread().setName("Agitate");
                    try {
                        printer.moveTo(printer.getX(), printer.getY(), printer.getZ(), 1, false, false);
                        while(agitate)
                        {
                            y = y0 + amp*Math.cos(2*Math.PI*t/per);
                            double speed = Math.abs(-amp*2*Math.PI*Math.sin(2*Math.PI*t/per)*60/per);
                            if(speed < 1)
                                speed = 1;
                            //System.out.println("" + t + "," + y + "," + speed/60);

                            printer.moveTo(printer.getX(), y, printer.getZ(), speed, false, false);

                            t += inc;
                            if(t >= per)
                                t = 0;
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };

            agitateThread.start();
        } catch (NumberFormatException e) {
            parentBotConsoleFrame.handleException(e);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }   
}//GEN-LAST:event_heatButtonActionPerformed
    
    
private void nudgeSizeRB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nudgeSizeRB1ActionPerformed
    setNudgeSize(Double.parseDouble(nudgeSizeRB1.getText()));
}//GEN-LAST:event_nudgeSizeRB1ActionPerformed

private void nudgeSizeRB2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nudgeSizeRB2ActionPerformed
    setNudgeSize(Double.parseDouble(nudgeSizeRB2.getText()));
}//GEN-LAST:event_nudgeSizeRB2ActionPerformed

private void nudgeSizeRB3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nudgeSizeRB3ActionPerformed
    setNudgeSize(Double.parseDouble(nudgeSizeRB3.getText()));
}//GEN-LAST:event_nudgeSizeRB3ActionPerformed

public void homeAll()
{
    double ze[] = new double[4];
    parentBotConsoleFrame.suspendPolling();
    try {
        printer.home();
        if(!firstZero)
            ze = printer.getZeroError();
    } catch (Exception e) {
        parentBotConsoleFrame.handleException(e);
    }
    if(!firstZero)
        Debug.d("Zero errors (steps).  X:" + ze[0] + " Y:" + ze[1] + " Z:" + ze[2]);
    xStepperPositionJPanel.zeroBox();
    yStepperPositionJPanel.zeroBox();
    zStepperPositionJPanel.zeroBox();
//    xStepperPositionJPanel.homeAxis();
//    yStepperPositionJPanel.homeAxis();
//    zStepperPositionJPanel.homeAxis();
    firstZero = false;
    parentBotConsoleFrame.resumePolling();
}

public void homeXY()
{
    parentBotConsoleFrame.suspendPolling();
    try
    {
        printer.homeToZeroX();
        printer.homeToZeroY();
    } catch (Exception e)
    {}
    xStepperPositionJPanel.zeroBox();
    yStepperPositionJPanel.zeroBox();
    parentBotConsoleFrame.resumePolling();
}

private void homeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeAllButtonActionPerformed
    homeAll();
}//GEN-LAST:event_homeAllButtonActionPerformed

public void storeAll()
{
    xStepperPositionJPanel.store();
    yStepperPositionJPanel.store();
    zStepperPositionJPanel.store(); 
}

private void storeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeAllButtonActionPerformed
    storeAll();
}//GEN-LAST:event_storeAllButtonActionPerformed

public void recallAll()
{
    xStepperPositionJPanel.recall();
    yStepperPositionJPanel.recall();
    zStepperPositionJPanel.recall();    
}

private void recallAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recallAllButtonActionPerformed
    recallAll();
}//GEN-LAST:event_recallAllButtonActionPerformed

public Printer getPrinter()
{
    return printer;
}

public void recordCurrentPosition()
{
    //parentBotConsoleFrame.suspendPolling();
    double cp[];
    try {
        cp = printer.getCoordinates();
        xStepperPositionJPanel.setTargetPositionField(cp[0]);
        yStepperPositionJPanel.setTargetPositionField(cp[1]);
        zStepperPositionJPanel.setTargetPositionField(cp[2]);
    } catch (Exception e) {
        parentBotConsoleFrame.handleException(e);
    }
    //parentBotConsoleFrame.resumePolling();
}

public void goTo(double xTo, double yTo, double zTo)
{
    parentBotConsoleFrame.suspendPolling();
    double x = printer.getX();
    double y = printer.getY();
    double z = printer.getZ();

    try
    {
        if(plotExtruderCheck.isSelected())
        {
            int eNum = Integer.parseInt(extruderToPlotWith.getText());
            GenericExtruderTabPanel etp = BotConsoleFrame.getGenericExtruderTabPanel(eNum);
            printer.selectExtruder(eNum);
            printer.getExtruder().setExtrusion(etp.getExtruderSpeed(), false);
            printer.machineWait(printer.getExtruder().getExtrusionDelayForLayer(), false);
        }
        if(z >= zTo)
        {
            //printer.setFeedrate(Double.parseDouble(xySpeedField.getText()));
            printer.singleMove(xTo, yTo, z, Double.parseDouble(xySpeedField.getText()));
            //printer.setFeedrate(Double.parseDouble(zSpeedField.getText()));
            printer.singleMove(xTo, yTo, zTo, Double.parseDouble(zSpeedField.getText()));
        } else
        {
            //printer.setFeedrate(Double.parseDouble(zSpeedField.getText()));
            printer.singleMove(x, y, zTo, Double.parseDouble(zSpeedField.getText()));
            //printer.setFeedrate(Double.parseDouble(xySpeedField.getText()));
            printer.singleMove(xTo, yTo, zTo, Double.parseDouble(xySpeedField.getText()));  
        }
        if(plotExtruderCheck.isSelected())
            printer.getExtruder().setExtrusion(0, false);
    } catch (Exception e)
    {}
    recordCurrentPosition();
    parentBotConsoleFrame.resumePolling();
}

private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
    double xTo = xStepperPositionJPanel.getTargetPositionInMM();
    double yTo = yStepperPositionJPanel.getTargetPositionInMM();
    double zTo = zStepperPositionJPanel.getTargetPositionInMM();
    goTo(xTo, yTo, zTo);
}//GEN-LAST:event_goButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField extruderToPlotWith;
    private javax.swing.JButton goButton;
    private javax.swing.JButton homeAllButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel motorsPanel;
    private javax.swing.JPanel nudgePanel;
    private javax.swing.JRadioButton nudgeSizeRB1;
    private javax.swing.JRadioButton nudgeSizeRB2;
    private javax.swing.JRadioButton nudgeSizeRB3;
    private javax.swing.JCheckBox plotExtruderCheck;
    private javax.swing.JButton recallAllButton;
    private javax.swing.JPanel speedsPanel;
    private javax.swing.JButton storeAllButton;
    private org.reprap.gui.botConsole.StepperPositionJPanel xStepperPositionJPanel;
    public static javax.swing.JTextField xySpeedField;
    private org.reprap.gui.botConsole.StepperPositionJPanel yStepperPositionJPanel;
    public static javax.swing.JTextField zSpeedField;
    private org.reprap.gui.botConsole.StepperPositionJPanel zStepperPositionJPanel;
    // End of variables declaration//GEN-END:variables
    
    private javax.swing.JLabel currentTempLabel;
    private javax.swing.JTextField targetTempField;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel bedTempPanel;
    private javax.swing.JToggleButton heatButton;
    private boolean heatPushed;
    
    private javax.swing.JTextField agitateAmplitude;
    private javax.swing.JTextField agitatePeriod;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel agitatePanel;
    private javax.swing.JToggleButton agitateButton;
    private boolean agitate;
}
