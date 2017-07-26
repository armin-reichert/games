package de.amr.games.muehle;

import static de.amr.games.muehle.Farbe.SCHWARZ;
import static de.amr.games.muehle.Farbe.WEISS;

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

		brett.setzeStein(WEISS, 0);
		brett.setzeStein(WEISS, 2);
		brett.setzeStein(WEISS, 14);
		brett.setzeStein(WEISS, 2);
		brett.setzeStein(WEISS, 23);
		brett.setzeStein(SCHWARZ, 15);
		brett.setzeStein(SCHWARZ, 16);
		brett.setzeStein(SCHWARZ, 17);
		brett.setzeStein(SCHWARZ, 19);
		brett.setzeStein(SCHWARZ, 22);

		for (int p = 0; p < 24; p += 1) {
			Muehle muehle;
			muehle = brett.findeMühle(p, Farbe.SCHWARZ, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findeMühle(p, Farbe.WEISS, true);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler weißer Mühle: " + muehle);
			}
			muehle = brett.findeMühle(p, Farbe.SCHWARZ, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findeMühle(p, Farbe.WEISS, false);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler weißer Mühle: " + muehle);
			}
		}
	}
}