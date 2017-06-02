package de.amr.games.pacman.test;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.misc.SceneHelper;
import de.amr.games.pacman.play.PlayScene;

public class DeadGhostTestScene extends Scene<DeadGhostTestApp> implements View {

	private Board board;
	private Ghost ghost;

	public DeadGhostTestScene(DeadGhostTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));
		ghost = new Ghost(app, board, "Blinky");
		ghost.init();
		ghost.setLogger(Application.Log);
		ghost.placeAt(new Tile(4, 4));
		ghost.speed = () -> 2f;
		ghost.state(GhostState.Scattering).update = state -> {
			if (Keyboard.keyPressedOnce(KeyEvent.VK_K)) {
				ghost.killed();
			}
			ghost.moveRandomly();
		};
		ghost.state(GhostState.Recovering).update = state -> {
			if (state.isTerminated()) {
				ghost.beginScattering();
			}
		};
		ghost.state(GhostState.Dead).update = state -> {
			ghost.follow(PlayScene.GHOST_HOUSE_ENTRY);
			if (ghost.isExactlyOverTile(PlayScene.GHOST_HOUSE_ENTRY)) {
				ghost.state(GhostState.Recovering).setDuration(60);
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
		SceneHelper.drawSprite(g, 3, 0, app.getTheme().getBoardSprite());
		SceneHelper.drawGridLines(g, getWidth(), getHeight());
		ghost.draw(g);
	}

}
