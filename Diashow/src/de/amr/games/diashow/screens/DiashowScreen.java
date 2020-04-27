package de.amr.games.diashow.screens;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;

public class DiashowScreen implements Lifecycle, View {

	private static final int KEY_NEXT = KeyEvent.VK_SPACE;

	private int width;
	private int current;
	private Image images[];
	private int nImages = 4;

	public DiashowScreen(int width, int height) {
		this.width = width;
	}

	@Override
	public void init() {
		images = new Image[nImages];
		for (int i = 0; i < nImages; i++) {
			images[i] = Assets.readImage(i + ".jpg").getScaledInstance(width, -1, Image.SCALE_SMOOTH);
		}
		current = 0;
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KEY_NEXT)) {
			current = current < nImages - 1 ? current + 1 : 0;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(images[current], 0, 0, null);
	}
}