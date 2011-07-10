/*
 * 
 * !!!!!
// * NOTE: PLEASE ONLY EDIT THIS USING THE NETBEANS IDE 6.0.1 OR HIGHER!!!!
// * !!!!!
// * 
// * ... an .xml file is associated with this class. Cheers.
// *
// * bedPanel.java
// *
// * Created on 30 March 2008, 18:55
// */
//
//package org.reprap.gui.botConsole;
//
//import java.awt.Color;
//import java.awt.Font;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//import java.awt.font.FontRenderContext;
//import java.awt.geom.Line2D;
//import java.awt.geom.Point2D;
//
///**
// *
// * @author  reprap
// */
//public class bedPanel extends javax.swing.JPanel {
//	private static final long serialVersionUID = 1L;
//	private final int CROSS_HAIR_SIZE = 10;
//    private Line2D.Double a1, a2, b1, b2;
//    private Point2D.Double aDatum, bDatum;
//    private int x;
//    private int y;
//    private Font font;
//    private int fontSize;
//    
//    /** Creates new form bedPanel */
//    public bedPanel() {
//        
//        // First cross hair
//        aDatum = new Point2D.Double();
//        a1 = new Line2D.Double();
//        a2 = new Line2D.Double();
//        
//        // History cross hair
//        bDatum = new Point2D.Double();
//        b1 = new Line2D.Double();
//        b2 = new Line2D.Double();
//        
//        initComponents();
//    
//    }
//    
//    public void setDimensions() {
//        x = this.getWidth();
//        y = this.getHeight();
//
//        // Text imitialisation
//        fontSize = (int)(y/20);
//        font = new Font("dialog", Font.PLAIN, fontSize);
//        repaint();
//    }
//    
//    /** This method is called from within the constructor to
//     * initialize the form.
//     * WARNING: Do NOT modify this code. The content of this method is
//     * always regenerated by the Form Editor.
//     */
//    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
//    private void initComponents() {
//
//        setBackground(java.awt.Color.white);
//        setMaximumSize(new java.awt.Dimension(200, 200));
//        setMinimumSize(new java.awt.Dimension(200, 200));
//        setPreferredSize(new java.awt.Dimension(200, 200));
//
//        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
//        this.setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(0, 200, Short.MAX_VALUE)
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
//            .add(0, 200, Short.MAX_VALUE)
//        );
//    }// </editor-fold>//GEN-END:initComponents
//    
//    public void mousePressed() {
//        unClicked = false;
//    }
//    
//    public void updateCrossHair(int posX, int posY) {
//        
//        c = Color.black;
//        
//        aDatum.setLocation(posX, posY);
//        a1.setLine(aDatum.getX(), aDatum.getY()-CROSS_HAIR_SIZE, aDatum.getX(), aDatum.getY()+CROSS_HAIR_SIZE);
//        a2.setLine(aDatum.getX()-CROSS_HAIR_SIZE, aDatum.getY(), aDatum.getX()+CROSS_HAIR_SIZE, aDatum.getY());
//        
//        updateOldPosition(posX, posY);
//        
//        repaint();
//    }
//    
//    public void updateOldPosition(int posX, int posY) {
//        
//        bDatum.setLocation(posX, posY);
//        b1.setLine(bDatum.getX(), bDatum.getY()-CROSS_HAIR_SIZE/2, bDatum.getX(), bDatum.getY()+CROSS_HAIR_SIZE/2);
//        b2.setLine(bDatum.getX()-CROSS_HAIR_SIZE/2, bDatum.getY(), bDatum.getX()+CROSS_HAIR_SIZE/2, bDatum.getY());
//    }
//    
//    public void dragCrossHair(int posX, int posY) {
//        
//        c = Color.red;
//        
//        aDatum.setLocation(posX, posY);
//        a1.setLine(aDatum.getX(), aDatum.getY()-x, aDatum.getX(), aDatum.getY()+x);
//        a2.setLine(aDatum.getX()-x, aDatum.getY(), aDatum.getX()+x, aDatum.getY());
//        
//        repaint();
//    }
//    
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Graphics2D g2 = (Graphics2D)g;
//        g2.setRenderingHint(
//                        RenderingHints.KEY_ANTIALIASING,
//        RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(
//                        RenderingHints.KEY_COLOR_RENDERING,
//        RenderingHints.VALUE_COLOR_RENDER_SPEED);
//        g2.setRenderingHint(
//                        RenderingHints.KEY_RENDERING,
//        RenderingHints.VALUE_RENDER_SPEED);
//        g2.setColor(c);
//        g2.draw(a1);		
//        g2.draw(a2);
//
//        g2.setColor(Color.gray);
//        g2.draw(b1);		
//        g2.draw(b2);
//
//        if (unClicked) {
//            g2.setFont(font);
//            FontRenderContext frc = g2.getFontRenderContext();
//
//            float width;
//            float sx;
//            float sy;
//            float lineHeight = font.getSize();
//            float space = font.getSize()/4;
//            int lines = s.length;
//
//            for (int i = 0; i < lines; i++) {
//                  width = (float)font.getStringBounds(s[i], frc).getWidth();
//                  sx = (x - width)/2;
//                  sy = y/2+(lineHeight)/2 - (lines-1)*((lineHeight+space)/2) + (i*(lineHeight+space));
//                  g2.drawString(s[i], sx, sy);
//            }
//        }
//    }
//   
//    private Color c;
//    private boolean unClicked = true;
//    private String s1 = "Click to load new coordinates";
//    private String s2 = "Drag for cross-hairs";
//    private String s3 = ""; //"Home X & Y axes first";
//    private String[] s = new String[] { s3, s1, s2 };
//    
//    // Variables declaration - do not modify//GEN-BEGIN:variables
//    // End of variables declaration//GEN-END:variables
//    
// }
