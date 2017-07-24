package de.amr.games.muehle;

import static de.amr.games.muehle.Steinfarbe.SCHWARZ;
import static de.amr.games.muehle.Steinfarbe.WEISS;

import java.awt.Color;

import de.amr.easy.game.scene.Scene;

public class MuehleScene extends Scene<MuehleApp> {

	private MuehleBrett brett;

	public MuehleScene(MuehleApp app) {
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
	}
}