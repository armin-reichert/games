package de.amr.games.muehle;

import static de.amr.games.muehle.Steinfarbe.SCHWARZ;
import static de.amr.games.muehle.Steinfarbe.WEISS;

import java.awt.Color;

import de.amr.easy.game.scene.Scene;

public class MuehleTestScene extends Scene<MuehleApp> {

	private MuehleBrett brett;

	public MuehleTestScene(MuehleApp app) {
		super(app);
		setBgColor(Color.WHITE);
	}

	@Override
	public void init() {
		brett = new MuehleBrett(600, 600);
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
			muehle = brett.findeHorizontaleMühle(p, Steinfarbe.SCHWARZ);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findeHorizontaleMühle(p, Steinfarbe.WEISS);
			if (muehle != null) {
				System.out.println(p + " liegt in horizontaler weißer Mühle: " + muehle);
			}
			muehle = brett.findeVertikaleMühle(p, Steinfarbe.SCHWARZ);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler schwarzer Mühle: " + muehle);
			}
			muehle = brett.findeVertikaleMühle(p, Steinfarbe.WEISS);
			if (muehle != null) {
				System.out.println(p + " liegt in vertikaler weißer Mühle: " + muehle);
			}
		}

	}
}