package de.amr.games.montagsmaler;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class Tools {

	public static void setLookAndFeel(String name) {
		try {
			for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
				if (name.equals(laf.getName())) {
					UIManager.setLookAndFeel(laf.getClassName());
					return;
				}
			}
		} catch (Exception e) {
			System.err.println("Could not set LAF '" + name + "', using system LAF instead.");
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception x) {
				x.printStackTrace(System.err);
			}
		}
	}

	public static ImageIcon loadImageIcon(String path) {
		URL url = Tools.class.getResource(path);
		if (url == null) {
			System.err.println("Could not load icon from path: " + path);
			return null;
		}
		return new ImageIcon(url);
	}

	public static Cursor createCursor(String path, Point hotspot, String name) {
		return Toolkit.getDefaultToolkit().createCustomCursor(loadImageIcon(path).getImage(), hotspot, name);
	}

	public static Color randomPenColor() {
		Random random = new Random();
		final float hue = random.nextFloat();
		final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
		final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
		return Color.getHSBColor(hue, saturation, luminance);
	}

}
