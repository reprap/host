package org.reprap.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JFrame;

import org.reprap.Preferences;

public class Utility {

	public static void centerWindowOnScreen(Window w) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		w.setLocation((screenSize.width - w.getSize().width) / 2,
				(screenSize.height - w.getSize().height) / 2);
	}

	public static Dimension getDefaultAppSize() {
		try {
			Preferences prefs = Preferences.getGlobalPreferences();
			if (prefs.loadBool("RememberWindowPosition")) {
				int width = prefs.loadInt("MainWindowWidth");
				int height = prefs.loadInt("MainWindowHeight");
				return new Dimension(width, height);
			}
		} catch (Exception ex) {
			// Ignore exception and continue with defaults
		}
		
	  	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return new Dimension(4 * screenSize.width / 5, 4 * screenSize.height / 5);
	}

	public static void positionWindowOnScreen(Window w) {
		try {
			Preferences prefs = Preferences.getGlobalPreferences();
			if (prefs.loadBool("RememberWindowPosition")) {
				int left = prefs.loadInt("MainWindowLeft");
				int top = prefs.loadInt("MainWindowTop");
				w.setLocation(left, top);
				return;
			}
		} catch (Exception ex) {
			// Ignore exception and continue with defaults
		}
		centerWindowOnScreen(w);
	}
	
	public static void centerWindowOnParent(Window w, JFrame parent) {
		Rectangle bounds = parent.getBounds();
		int cx = bounds.x + bounds.width / 2;
		int cy = bounds.y + bounds.height / 2;
		Dimension mySize = w.getSize();
		w.setLocation(cx - mySize.width / 2, cy - mySize.height / 2);
	}
	
}
