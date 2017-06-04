package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.misc.SceneHelper.drawTextCentered;
import static de.amr.games.pacman.play.PlayScene.GHOST_HOUSE_ENTRY;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

public class DeadGhostTestScene extends Scene<DeadGhostTestApp> {

	private final PacManTheme theme;
	private Board board;
	private Ghost ghost;

	public DeadGhostTestScene(DeadGhostTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));
		
		ghost = new Ghost(app, board, "Blinky", () -> theme);
		ghost.init();
		ghost.setLogger(Application.Log);
		ghost.placeAt(4, 4);
		ghost.speed = () -> 2f;
		ghost.state(Scattering).update = state -> {
			if (Keyboard.keyPressedOnce(KeyEvent.VK_K)) {
				ghost.killed();
			}
			ghost.moveRandomly();
		};
		ghost.state(Recovering).update = state -> {
			if (state.isTerminated()) {
				ghost.beginScattering();
			}
		};
		ghost.state(Dead).update = state -> {
			ghost.follow(GHOST_HOUSE_ENTRY);
			if (ghost.isExactlyOverTile(GHOST_HOUSE_ENTRY)) {
				ghost.state(Recovering).setDuration(60);
				ghost.beginRecovering();
			}
		};
		ghost.beginScattering();
	}

	@Override
	public void update() {
		ghost.update();
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoardSprite());
		drawGridLines(g, getWidth(), getHeight());
		g.setColor(Color.WHITE);
		if (ghost.state() == Scattering) {
			drawTextCentered(g, getWidth(), 17, "Press 'k' to kill ghost");
		}
		ghost.draw(g);
	}
}