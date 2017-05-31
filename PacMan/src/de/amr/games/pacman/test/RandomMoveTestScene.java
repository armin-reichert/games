package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.play.PlayScene;

/**
 * 
 * @author Armin Reichert
 */
public class RandomMoveTestScene extends Scene<RandomMoveTestApp> {

	private Random rand = new Random();
	private Board board;
	private Ghost[] ghosts;

	public RandomMoveTestScene(RandomMoveTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));
		createGhosts();
	};

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_SPACE)) {
			createGhosts();
		}
		Stream.of(ghosts).forEach(Ghost::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, app.getTheme().getBoardSprite());
		Stream.of(ghosts).forEach(ghost -> ghost.draw(g));
	}

	private void createGhosts() {
		ghosts = new Ghost[5 + rand.nextInt(15)];
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i] = createRandomGhost();
		}
	}

	private Ghost createRandomGhost() {
		String names[] = { "Pinky", "Inky", "Blinky", "Clyde" };
		Ghost ghost = new Ghost(app, board, names[rand.nextInt(names.length)]);
		ghost.init();
		ghost.control.state(Scattering).update = state -> ghost.moveRandomly();
		ghost.setAnimated(true);
		ghost.speed = () -> Math.round(8f * TILE_SIZE / app.motor.getFrequency()) * (0.5f + rand.nextFloat());
		ghost.placeAt(getRandomTile());
		ghost.control.changeTo(Scattering);
		return ghost;
	}

	private Tile getRandomTile() {
		Tile home = PlayScene.GHOST_HOUSE_ENTRY;
		while (true) {
			int row = rand.nextInt(board.numRows), col = rand.nextInt(board.numCols);
			Tile tile = new Tile(row, col);
			if (!board.shortestRoute(home, tile).isEmpty()) {
				return tile;
			}
		}
	}
}