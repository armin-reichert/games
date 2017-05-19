package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.BLINKY_HOME;
import static de.amr.games.pacman.play.PlayScene.CLYDE_HOME;
import static de.amr.games.pacman.play.PlayScene.INKY_HOME;
import static de.amr.games.pacman.play.PlayScene.PINKY_HOME;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.entities.ghost.behaviors.LoopAroundWalls;

/**
 * A scene for testing scattering and looping around a block.
 * 
 * @author Armin Reichert
 */
public class ScatteringTestScene extends Scene<ScatteringTestApp> {

	private Board board;
	private final Ghost[] ghosts = new Ghost[4];

	public ScatteringTestScene(ScatteringTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));

		ghosts[0] = new Ghost(app, board, "Blinky", BLINKY_HOME);
		ghosts[0].control.state(Scattering, new LoopAroundWalls(ghosts[0], 4, 26, S, true));

		ghosts[1] = new Ghost(app, board, "Inky", INKY_HOME);
		ghosts[1].control.state(Scattering, new LoopAroundWalls(ghosts[1], 32, 26, W, true));

		ghosts[2] = new Ghost(app, board, "Pinky", PINKY_HOME);
		ghosts[2].control.state(Scattering, new LoopAroundWalls(ghosts[2], 4, 1, S, false));

		ghosts[3] = new Ghost(app, board, "Clyde", CLYDE_HOME);
		ghosts[3].control.state(Scattering, new LoopAroundWalls(ghosts[3], 32, 1, E, false));

		Stream.of(ghosts).forEach(ghost -> {
			ghost.speed = () -> (float) Math.round(8f * TILE_SIZE / app.motor.getFrequency());
			ghost.setAnimated(true);
			ghost.control.changeTo(GhostState.Scattering);
		});
	}

	@Override
	public void update() {
		Stream.of(ghosts).forEach(Ghost::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, app.getTheme().getBoardSprite());
		Stream.of(ghosts).forEach(ghost -> ghost.draw(g));
	}
}