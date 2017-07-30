package de.amr.games.muehle.test;

import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;

import java.awt.Color;

import de.amr.easy.game.scene.Scene;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Mill;
import de.amr.games.muehle.board.StoneColor;

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

		brett.putStoneAt(0, WHITE);
		brett.putStoneAt(2, WHITE);
		brett.putStoneAt(14, WHITE);
		brett.putStoneAt(2, WHITE);
		brett.putStoneAt(23, WHITE);
		brett.putStoneAt(15, BLACK);
		brett.putStoneAt(16, BLACK);
		brett.putStoneAt(17, BLACK);
		brett.putStoneAt(19, BLACK);
		brett.putStoneAt(22, BLACK);

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