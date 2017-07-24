package de.amr.games.muehle;

import java.awt.Color;

import de.amr.easy.game.scene.Scene;
import de.amr.games.muehle.MuehleBrett.Steinfarbe;

public class MuehleScene extends Scene<MuehleApp> {

	MuehleBrett brett;

	public MuehleScene(MuehleApp app) {
		super(app);
		setBgColor(Color.WHITE);
	}

	@Override
	public void init() {
		brett = new MuehleBrett(600, 600);
		brett.tf.moveTo(100, 100);
		app.entities.add(brett);

		brett.setzeStein(Steinfarbe.WEISS, 0);
		// brett.setzeStein(Steinfarbe.WEISS, 1);
		brett.setzeStein(Steinfarbe.WEISS, 2);
		brett.setzeStein(Steinfarbe.WEISS, 14);
		brett.setzeStein(Steinfarbe.WEISS, 2);
		brett.setzeStein(Steinfarbe.WEISS, 23);
		brett.setzeStein(Steinfarbe.SCHWARZ, 15);
		brett.setzeStein(Steinfarbe.SCHWARZ, 16);
		brett.setzeStein(Steinfarbe.SCHWARZ, 17);
		brett.setzeStein(Steinfarbe.SCHWARZ, 19);
		brett.setzeStein(Steinfarbe.SCHWARZ, 22);
	}

}
