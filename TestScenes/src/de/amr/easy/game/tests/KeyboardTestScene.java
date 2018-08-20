package de.amr.easy.game.tests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.ViewController;

public class KeyboardTestScene implements ViewController {

	@Override
	public int getWidth() {
		return 600;
	}

	@Override
	public int getHeight() {
		return 400;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		String modifiers = " ";
		for (Keyboard.Modifier modifier : Keyboard.Modifier.values()) {
			if (Keyboard.keyPressedOnce(modifier, KeyEvent.VK_A)) {
				modifiers += modifier + " ";
			}
		}
		if (modifiers.length() > 1) {
			Application.LOGGER.info(String.format("(%s) + A", modifiers));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.drawString("Press {ALT, CONTROL, SHIFT}+A", 20, getHeight() / 2);
	}
}