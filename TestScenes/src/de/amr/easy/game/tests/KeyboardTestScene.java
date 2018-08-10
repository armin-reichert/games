package de.amr.easy.game.tests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

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
	public void draw(Graphics2D g) {
		int fontSize = 30;
		int x = 2 * fontSize;
		g.setColor(Color.YELLOW);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
		if (Keyboard.isAltDown()) {
			g.drawString("Alt", x, 2 * fontSize);
		}
		if (Keyboard.isShiftDown()) {
			g.drawString("Shift", x, 2 * fontSize + getHeight() / 3);
		}
		if (Keyboard.isControlDown()) {
			g.drawString("Control", x, 2 * fontSize + 2 * getHeight() / 3);
		}
		g.setFont((new Font(Font.MONOSPACED, Font.BOLD, 100)));
		for (int key = 0x25; key < 512; ++key) {
			if (Keyboard.keyDown(key)) {
				g.drawString(KeyEvent.getKeyText(key), getWidth() / 2, getHeight() / 2);
			}
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

}