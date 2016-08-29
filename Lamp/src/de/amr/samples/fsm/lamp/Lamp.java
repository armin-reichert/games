package de.amr.samples.fsm.lamp;

import static de.amr.easy.game.Application.Assets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Lamp extends JPanel {

	private static final Icon BULB_BRIGHT, BULB_DARK;

	static {
		BufferedImage bulbs = Assets.image("bulbs.png");
		int w = bulbs.getWidth() / 2, h = bulbs.getHeight();
		BULB_DARK = new ImageIcon(bulbs.getSubimage(0, 0, w, h));
		BULB_BRIGHT = new ImageIcon(bulbs.getSubimage(w, 0, w, h));
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
		lightSwitch.setIcon(new ImageIcon(Assets.image("lightbulb_off.png")));
	}

	public void switchOff() {
		bulb.setIcon(BULB_DARK);
		lightSwitch.setText("Einschalten");
		lightSwitch.setIcon(new ImageIcon(Assets.image("lightbulb.png")));
	}
}
