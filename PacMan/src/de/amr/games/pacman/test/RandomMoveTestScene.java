package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.play.PlayScene;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Random ghost movement test app.
 * 
 * @author Armin Reichert
 */
public class RandomMoveTestScene extends Scene<RandomMoveTestApp> {

	private final PacManTheme theme;
	private final Random rand = new Random();
	private Board board;
	private Ghost[] ghosts;

	public RandomMoveTestScene(RandomMoveTestApp app) {
		super(app);
		theme = new ClassicTheme();
	}

	@Override
	public void init() {
		board = new Board(Assets.text("board.txt"));
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
		drawSprite(g, 3, 0, theme.getBoardSprite());
		drawGridLines(g, getWidth(), getHeight());
		Stream.of(ghosts).forEach(ghost -> ghost.draw(g));
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

	private void createGhosts() {
		ghosts = new Ghost[5 + rand.nextInt(15)];
		for (int i = 0; i < ghosts.length; ++i) {
			ghosts[i] = createRandomGhost();
		}
	}

	private Ghost createRandomGhost() {
		String names[] = { "Pinky", "Inky", "Blinky", "Clyde" };
		Ghost ghost = new Ghost(app, board, names[rand.nextInt(names.length)], () -> theme);
		ghost.init();
		ghost.setAnimated(true);
		ghost.speed = () -> 1f + rand.nextFloat();
		ghost.placeAt(getRandomTile());

		ghost.control.change(Initialized, Scattering, () -> true);
		ghost.control.state(Scattering).update = state -> ghost.moveRandomly();

		return ghost;
	}

	private void checkCollisions() {
		for (int i = 0; i < ghosts.length; ++i) {
			for (int j = i; j < ghosts.length; ++j) {
				if (ghosts[i].currentTile().equals(ghosts[j].currentTile())
						&& !ghosts[i].getName().equals(ghosts[j].getName())) {
					ghosts[i].setMoveDir(Top4.inv(ghosts[i].getMoveDir()));
					ghosts[j].setMoveDir(Top4.inv(ghosts[j].getMoveDir()));
				}
			}
		}
	}
}