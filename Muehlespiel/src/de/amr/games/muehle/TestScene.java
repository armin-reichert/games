package de.amr.games.muehle;

import static de.amr.games.muehle.StoneColor.BLACK;
import static de.amr.games.muehle.StoneColor.WHITE;

import java.awt.Color;

import de.amr.easy.game.scene.Scene;

public class TestScene extends Scene<MillApp> {

	private Board brett;

	public TestScene(MillApp app) {
		super(app);
		setBgColor(Color.WHITE);
	}

	@Override
	public void init() {
		brett = new Board(600, 600);
		brett.tf.moveTo(100, 100);
		app.entities.add(brett);

		brett.placeStoneAt(0, WHITE);
		brett.placeStoneAt(2, WHITE);
		brett.placeStoneAt(14, WHITE);
		brett.placeStoneAt(2, WHITE);
		brett.placeStoneAt(23, WHITE);
		brett.placeStoneAt(15, BLACK);
		brett.placeStoneAt(16, BLACK);
		brett.placeStoneAt(17, BLACK);
		brett.placeStoneAt(19, BLACK);
		brett.placeStoneAt(22, BLACK);

		for (int p = 0; p < 24; p += 1) {
			Mill muehle;
			muehle = brett.findContainingMill(p, StoneColor.BLACK, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findContainingMill(p, StoneColor.WHITE, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler weißer Mühle: " + muehle);
			}
			muehle = brett.findContainingMill(p, StoneColor.BLACK, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findContainingMill(p, StoneColor.WHITE, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler weißer Mühle: " + muehle);
			}
		}
	}
}