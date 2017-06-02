package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.grid.impl.Top4;
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
		checkCollisions();
		Stream.of(ghosts).forEach(Ghost::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, app.getTheme().getBoardSprite());
		drawGridLines(g, getWidth(), getHeight());
		Stream.of(ghosts).forEach(ghost -> ghost.draw(g));
	}

	private void createGhosts() {
		ghosts = new Ghost[5 + rand.nextInt(15)];
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i] = createRandomGhost();
		}
	}

	private void checkCollisions() {
		for (int i = 0; i < ghosts.length; ++i) {
			for (int j = i; j < ghosts.length; ++j) {
				if (ghosts[i].currentTile().equals(ghosts[j].currentTile())
						&& !ghosts[i].getName().equals(ghosts[j].getName())) {
					ghosts[i].setMoveDir(Top4.INSTANCE.inv(ghosts[i].getMoveDir()));
					ghosts[j].setMoveDir(Top4.INSTANCE.inv(ghosts[j].getMoveDir()));
				}
			}
		}
	}

	private Ghost createRandomGhost() {
		String names[] = { "Pinky", "Inky", "Blinky", "Clyde" };
		Ghost ghost = new Ghost(app, board, names[rand.nextInt(names.length)]);
		ghost.init();
		ghost.state(Scattering).update = state -> ghost.moveRandomly();
		ghost.setAnimated(true);
		ghost.speed = () -> 1f + rand.nextFloat();
		ghost.placeAt(getRandomTile());
		ghost.beginScattering();
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