package de.amr.games.pacman.scenes;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.scenes.DrawUtil.drawSprite;

import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.entities.ghost.behaviors.LoopAroundWalls;

/**
 * A scene for testing scattering and looping around a block.
 * 
 * @author Armin Reichert
 */
public class ScatteringTestScene extends Scene<PacManGame> {

	private Board board;
	private final Ghost[] ghosts = new Ghost[4];

	public ScatteringTestScene() {
		super(Game);
	}

	@Override
	public void init() {
		Game.settings.title = "Scattering Test";
		Game.settings.set("drawInternals", true);

		board = new Board(Game.assets.text("board.txt").split("\n"));

		ghosts[0] = new Ghost(board, PlayScene.BLINKY_HOME);
		ghosts[0].setName("Blinky");
		ghosts[0].control.state(Scattering, new LoopAroundWalls(ghosts[0], 4, 26, S, true));

		ghosts[1] = new Ghost(board, PlayScene.INKY_HOME);
		ghosts[1].setName("Inky");
		ghosts[1].control.state(Scattering, new LoopAroundWalls(ghosts[1], 32, 26, W, true));

		ghosts[2] = new Ghost(board, PlayScene.PINKY_HOME);
		ghosts[2].setName("Pinky");
		ghosts[2].control.state(Scattering, new LoopAroundWalls(ghosts[2], 4, 1, S, false));

		ghosts[3] = new Ghost(board, PlayScene.CLYDE_HOME);
		ghosts[3].setName("Clyde");
		ghosts[3].control.state(Scattering, new LoopAroundWalls(ghosts[3], 32, 1, E, false));

		Stream.of(ghosts).forEach(ghost -> {
			ghost.setSpeed(Game.getGhostSpeedNormal(1));
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
		drawSprite(g, 3, 0, Game.selectedTheme().getBoardSprite());
		Stream.of(ghosts).forEach(ghost -> ghost.draw(g));
	}
}