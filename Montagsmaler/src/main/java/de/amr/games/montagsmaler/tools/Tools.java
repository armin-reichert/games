package de.amr.games.montagsmaler.tools;

import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Random;

public class Tools {

	private static final Random RND = new Random();

	private Tools() {
	}

	public static void setLookAndFeel(String lafName) {
		try {
			for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
				if (lafName.equals(laf.getName())) {
					UIManager.setLookAndFeel(laf.getClassName());
					return;
				}
			}
		} catch (Exception e) {
			Logger.error("Could not set LAF %s, using system LAF instead.", lafName);
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception x) {
				Logger.error(x);
			}
		}
	}

	public static ImageIcon loadImageIcon(String path) {
		URL url = Tools.class.getResource("/de/amr/games/montagsmaler/" + path);
		if (url == null) {
			Logger.error("Could not load icon from path '%s'", path);
			throw new MissingResourceException("Could not load icon from path: \" + path", "", "");
		}
		Logger.trace("Icon from URL '%s' loaded", url.toExternalForm());
		return new ImageIcon(url);
	}

	public static Cursor createCursor(String path, Point hotspot, String name) {
		return Toolkit.getDefaultToolkit().createCustomCursor(loadImageIcon(path).getImage(), hotspot, name);
	}

	public static Color randomPenColor() {

		final float hue = RND.nextFloat();
		final float saturation = 0.9f;// 1.0 for brilliant, 0.0 for dull
		final float luminance = 1.0f; // 1.0 for brighter, 0.0 for black
		return Color.getHSBColor(hue, saturation, luminance);
	}
}