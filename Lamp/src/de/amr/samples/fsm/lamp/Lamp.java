package de.amr.samples.fsm.lamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Lamp extends JPanel {

	private static Icon BULB_BRIGHT = null, BULB_DARK = null;

	static {
		BufferedImage bulbs;
		try {
			bulbs = ImageIO.read(Lamp.class.getResourceAsStream("assets/bulbs.png"));
			int w = bulbs.getWidth() / 2, h = bulbs.getHeight();
			BULB_DARK = new ImageIcon(bulbs.getSubimage(0, 0, w, h));
			BULB_BRIGHT = new ImageIcon(bulbs.getSubimage(w, 0, w, h));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final JLabel bulb;
	private final JButton lightSwitch;

	public Lamp() {
		bulb = new JLabel("", JLabel.CENTER);
		lightSwitch = new JButton();
		setBackground(Color.BLACK);
		setLayout(new BorderLayout());
		add(bulb, BorderLayout.CENTER);
		add(lightSwitch, BorderLayout.SOUTH);
	}

	public JButton getLightSwitch() {
		return lightSwitch;
	}

	public void switchOn() {
		bulb.setIcon(BULB_BRIGHT);
		lightSwitch.setText("Ausschalten");
		lightSwitch.setIcon(new ImageIcon("assets/lightbulb_off.png"));
	}

	public void switchOff() {
		bulb.setIcon(BULB_DARK);
		lightSwitch.setText("Einschalten");
		lightSwitch.setIcon(new ImageIcon("assets/lightbulb.png"));
	}
}
