package org.reprap.gui;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class StatusMessage extends javax.swing.JDialog {
	private static final long serialVersionUID = 1L;
	private boolean cancelRequested = false;
	private JButton cancelButton;
	private JTextPane message;

	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		StatusMessage inst = new StatusMessage(frame);
		inst.setVisible(true);
	}
	
	public StatusMessage(JFrame frame) {
		super(frame);
		initGUI();
	}
	
	private void initGUI() {
		try {
			{
				message = new JTextPane();
				getContentPane().add(message);
				message.setBounds(0, 0, 280, 77);
				message.setEditable(false);
				message.setBackground(Color.blue);
				message.setForeground(Color.yellow);
				message.setEnabled(false);
				SimpleAttributeSet set = new SimpleAttributeSet();
				StyleConstants.setAlignment(set, StyleConstants.ALIGN_CENTER);
				message.setParagraphAttributes(set, true);			}
			{
				cancelButton = new JButton();
				getContentPane().add(cancelButton);
				cancelButton.setText("Cancel");
				cancelButton.setBounds(105, 84, 91, 28);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						cancelButtonActionPerformed(evt);
					}
				});
			}
			
			{
					getContentPane().setLayout(null);
			}
			{
				this.setTitle("Progress");
			}
			this.setSize(288, 137);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setMessage(String text) {
		message.setText(text);
	}

	public boolean isCancelled() {
		return cancelRequested;
	}

	public void setCancelled(boolean b) {
		cancelRequested = b;
	}
	
	private void cancelButtonActionPerformed(ActionEvent evt) {
		cancelRequested = true;
		setVisible(false);
	}

}
