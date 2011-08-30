package org.reprap.gui;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridLayout;


public class PreferencesValue {
	
	private JTextField textfieldValue = null;
	private BooleanChoice boolchoiceValue = null;
	
	
	public class BooleanChoice extends JPanel {
		private boolean userchoice;
		private JRadioButton trueButton, falseButton;
		private ButtonGroup bgroup;
		private JTextField valueTextField;
		
		public BooleanChoice(Boolean boolvalue)
		{
			
			
			if(boolvalue == true)
				trueButton   = new JRadioButton("True"  , true);
			else
				trueButton   = new JRadioButton("True"  , false);
			
			if(boolvalue == false)
				falseButton  = new JRadioButton("False"   , true);
			else
				falseButton  = new JRadioButton("False"   , false);

			bgroup = new ButtonGroup();
			bgroup.add(trueButton);
			bgroup.add(falseButton);
			

			this.setLayout(new GridLayout(1, 2));
			this.add(trueButton);
			this.add(falseButton);
			
			this.userchoice = boolvalue;

		}
		
		public String getText()
		{
			
			if(bgroup.isSelected( (DefaultButtonModel)trueButton.getModel() ))
				this.userchoice = true;
			else
				this.userchoice = false;
			
			if(this.userchoice)
				return "true";
			else
				return "false";
		}
		
		public void setValue(boolean boolvalue)
		{
			if(boolvalue == true)
				trueButton.setSelected(true); 
			else
				trueButton.setSelected(false); 
			
			if(boolvalue == false)
				falseButton.setSelected(true);
			else
				falseButton.setSelected(false);
		}
	}

	
	
	public PreferencesValue(JTextField l)
	{
		textfieldValue = l;
	}
	
	public PreferencesValue(BooleanChoice b)
	{
		boolchoiceValue = b;
	}
	
	public String getText()
	{
		if(textfieldValue != null)
			return textfieldValue.getText();
		if(boolchoiceValue != null)
			return boolchoiceValue.getText();
		
		return null;
	}
	
	public void setText(String str)
	{
		if(textfieldValue != null)
			textfieldValue.setText(str);
		if(boolchoiceValue != null)
			boolchoiceValue.setValue(getBoolFromString(str));
	}
	
	public Component getObject()
	{
		if(textfieldValue != null)
			return textfieldValue;
		else return boolchoiceValue;
	}
	

	private boolean getBoolFromString(String strVal)
	{

		if (strVal.compareToIgnoreCase("true") == 0) return true;
		
		return false;
	}
	
	public void makeBoolean()
	{
		boolean boolvalue = getBoolFromString(this.getText());
		textfieldValue = null;
		boolchoiceValue = new BooleanChoice(boolvalue);
	}
}
