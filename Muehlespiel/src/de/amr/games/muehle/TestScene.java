package de.amr.games.muehle;

import static de.amr.games.muehle.SteinFarbe.BLACK;
import static de.amr.games.muehle.SteinFarbe.WHITE;

import java.awt.Color;

import de.amr.easy.game.scene.Scene;

public class TestScene extends Scene<MuehleApp> {

	private Brett brett;

	public TestScene(MuehleApp app) {
		super(app);
		setBgColor(Color.WHITE);
	}

	@Override
	public void init() {
		brett = new Brett(600, 600);
		brett.tf.moveTo(100, 100);
		app.entities.add(brett);

		brett.placeStone(WHITE, 0);
		brett.placeStone(WHITE, 2);
		brett.placeStone(WHITE, 14);
		brett.placeStone(WHITE, 2);
		brett.placeStone(WHITE, 23);
		brett.placeStone(BLACK, 15);
		brett.placeStone(BLACK, 16);
		brett.placeStone(BLACK, 17);
		brett.placeStone(BLACK, 19);
		brett.placeStone(BLACK, 22);

		for (int p = 0; p < 24; p += 1) {
			Muehle muehle;
			muehle = brett.findMill(p, SteinFarbe.BLACK, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findMill(p, SteinFarbe.WHITE, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler weißer Mühle: " + muehle);
			}
			muehle = brett.findMill(p, SteinFarbe.BLACK, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findMill(p, SteinFarbe.WHITE, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler weißer Mühle: " + muehle);
			}
		}
	}
}