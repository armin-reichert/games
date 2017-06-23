package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawRoute;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.BLINKY_HOME;
import static de.amr.games.pacman.play.PlayScene.CLYDE_HOME;
import static de.amr.games.pacman.play.PlayScene.INKY_HOME;
import static de.amr.games.pacman.play.PlayScene.LEFT_LOWER_CORNER;
import static de.amr.games.pacman.play.PlayScene.LEFT_UPPER_CORNER;
import static de.amr.games.pacman.play.PlayScene.PINKY_HOME;
import static de.amr.games.pacman.play.PlayScene.RIGHT_LOWER_CORNER;
import static de.amr.games.pacman.play.PlayScene.RIGHT_UPPER_CORNER;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.entities.ghost.behaviors.LoopAroundWalls;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing scattering and looping around a block.
 * 
 * @author Armin Reichert
 */
public class ScatteringTestScene extends Scene<ScatteringTestApp> {

	private final PacManTheme theme;
	private final Board board;
	private final List<Ghost> ghosts = new ArrayList<>();

	public ScatteringTestScene(ScatteringTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
		board = new Board(app.assets.text("board.txt").split("\n"));
	}

	@Override
	public void init() {
		addGhost("Blinky", Color.RED, BLINKY_HOME, RIGHT_UPPER_CORNER, S, true);
		addGhost("Inky", new Color(64, 224, 208), INKY_HOME, RIGHT_LOWER_CORNER, W, true);
		addGhost("Pinky", Color.PINK, PINKY_HOME, LEFT_UPPER_CORNER, S, false);
		addGhost("Clyde", Color.ORANGE, CLYDE_HOME, LEFT_LOWER_CORNER, E, false);
		ghosts.forEach(ghost -> {
			ghost.init();
			ghost.speed = () -> (float) Math.round(8f * TILE_SIZE / app.motor.getFrequency());
			ghost.setAnimated(true);
			ghost.receiveEvent(GhostEvent.ScatteringStarts);
		});
	}

	private void addGhost(String name, Color color, Tile home, Tile loopStart, int loopStartDir, boolean clockwise) {
		Ghost ghost = new Ghost(app, board, name, () -> theme);
		ghost.setColor(color);
		ghost.placeAt(home);
		ghost.setLogger(Application.Log);
		ghost.control.state(Scattering, new LoopAroundWalls(ghost, loopStart, loopStartDir, clockwise));
		ghost.control.changeOnInput(GhostEvent.ScatteringStarts, GhostState.Initialized, GhostState.Scattering);
		ghosts.add(ghost);
	}

	@Override
	public void update() {
		ghosts.forEach(Ghost::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoardSprite());
		ghosts.forEach(ghost -> {
			ghost.draw(g);
			LoopAroundWalls law = (LoopAroundWalls) ghost.control.state(GhostState.Scattering);
			if (!law.isLooping()) {
				g.setColor(ghost.getColor());
				drawRoute(g, board, ghost.currentTile(), ghost.getRoute());
			} else {
				List<Tile> routeTiles = law.getLoopTiles();
				Tile prev = null;
				for (Tile tile : routeTiles) {
					if (prev != null) {
						int offset = TILE_SIZE / 4;
						int x1 = prev.getCol() * TILE_SIZE + offset;
						int y1 = prev.getRow() * TILE_SIZE + offset;
						int x2 = tile.getCol() * TILE_SIZE + offset;
						int y2 = tile.getRow() * TILE_SIZE + offset;
						g.setColor(ghost.getColor());
						// g.fillOval(x1, y1, TILE_SIZE / 2, TILE_SIZE / 2);
						g.drawLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
						// g.fillOval(x2, y2, TILE_SIZE / 2, TILE_SIZE / 2);
					}
					prev = tile;
				}
			}
		});
	}
}