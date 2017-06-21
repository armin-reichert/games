package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.misc.SceneHelper.drawTextCentered;
import static de.amr.games.pacman.play.PlayScene.GHOST_HOUSE_ENTRY;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
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
		ghost.xOffset = () -> ghost.control.stateID() == Recovering ? TILE_SIZE / 2 : 0;

		ghost.control.change(Initialized, Scattering, () -> true);

		ghost.control.state(Scattering).update = state -> {
			if (Keyboard.keyPressedOnce(KeyEvent.VK_K)) {
				ghost.handleEvent(GhostEvent.Killed);
			}
			ghost.moveRandomly();
		};

		ghost.control.changeOnInput(GhostEvent.Killed, Scattering, Dead);

		ghost.control.changeOnTimeout(Recovering, Scattering);

		ghost.control.state(Dead).update = state -> {
			ghost.follow(GHOST_HOUSE_ENTRY);
			if (ghost.isExactlyOver(GHOST_HOUSE_ENTRY)) {
				ghost.handleEvent(GhostEvent.RecoveringStarts);
			}
		};

		ghost.control.changeOnInput(GhostEvent.RecoveringStarts, Dead, Recovering, (oldState, newState) -> {
			newState.setDuration(120);
		});
	}

	@Override
	public void update() {
		ghost.update();
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoardSprite());
		drawGridLines(g, getWidth(), getHeight());
		ghost.draw(g);
		g.setColor(Color.WHITE);
		if (ghost.control.stateID() == Scattering) {
			drawTextCentered(g, getWidth(), 17, "Press 'k' to kill ghost");
		}
	}
}