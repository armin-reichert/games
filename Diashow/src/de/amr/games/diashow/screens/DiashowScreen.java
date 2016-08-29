package de.amr.games.diashow.screens;

import static de.amr.easy.game.Application.Assets;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Key;
import de.amr.easy.game.scene.Scene;
import de.amr.games.diashow.Diashow;

public class DiashowScreen extends Scene<Diashow> {

	private static final int KEY_NEXT = KeyEvent.VK_SPACE;

	private int current;
	private Image images[];
	private int nImages = 4;

	public DiashowScreen(Diashow game) {
		super(game);
	}

	@Override
	public void init() {
		images = new Image[nImages];
		for (int i = 0; i < nImages; i++) {
			images[i] = Assets.readImage(i + ".jpg").getScaledInstance(getApp().getWidth(), -1,
					Image.SCALE_SMOOTH);
		}
		current = 0;
	}

	@Override
	public void update() {
		if (Key.pressedOnce(KEY_NEXT)) {
			current = current < nImages - 1 ? current + 1 : 0;
			System.out.println("NEXT pressed once");
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(images[current], 0, 0, getApp().getWidth(), getApp().getHeight(), null);
	}

}
