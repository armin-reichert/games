package de.amr.games.muehle;

import static de.amr.games.muehle.SteinFarbe.DUNKEL;
import static de.amr.games.muehle.SteinFarbe.HELL;

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

		brett.placeStone(HELL, 0);
		brett.placeStone(HELL, 2);
		brett.placeStone(HELL, 14);
		brett.placeStone(HELL, 2);
		brett.placeStone(HELL, 23);
		brett.placeStone(DUNKEL, 15);
		brett.placeStone(DUNKEL, 16);
		brett.placeStone(DUNKEL, 17);
		brett.placeStone(DUNKEL, 19);
		brett.placeStone(DUNKEL, 22);

		for (int p = 0; p < 24; p += 1) {
			Muehle muehle;
			muehle = brett.findMill(p, SteinFarbe.DUNKEL, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findMill(p, SteinFarbe.HELL, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler weißer Mühle: " + muehle);
			}
			muehle = brett.findMill(p, SteinFarbe.DUNKEL, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findMill(p, SteinFarbe.HELL, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler weißer Mühle: " + muehle);
			}
		}
	}
}