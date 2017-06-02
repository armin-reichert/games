package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.PACMAN_HOME;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.scene.Scene;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;

/**
 * Tests Blinky's behavior.
 * 
 * @author Armin Reichert
 */
public class BlinkyTestScene extends Scene<BlinkyTestApp> {

	private Board board;
	private PacMan pacMan;
	private Ghost blinky;
	private final Random rand = new Random();

	public BlinkyTestScene(BlinkyTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));

		pacMan = new PacMan(app, board);
		pacMan.init();
		pacMan.placeAt(PACMAN_HOME);
		pacMan.speed = () -> 2f;

		pacMan.onEnemyContact = ghost -> {
			pacMan.placeAt(PACMAN_HOME);
			int dir = rand.nextBoolean() ? E : W;
			pacMan.setMoveDir(dir);
			pacMan.setNextMoveDir(dir);
			ghost.placeAt(new Tile(4, 1));
			dir = rand.nextBoolean() ? W : E;
			ghost.setMoveDir(dir); // TODO without this, ghost might get stuck
			ghost.setNextMoveDir(dir);
		};

		pacMan.state(PacManState.Walking).update = state -> {
			if (pacMan.currentTile().equals(blinky.currentTile())) {
				pacMan.onEnemyContact.accept(blinky);
			} else {
				escapeBlinky();
			}
		};

		blinky = new Ghost(app, board, "Blinky");
		blinky.init();
		blinky.state(Chasing).update = state -> blinky.follow(pacMan.currentTile());
		blinky.setColor(Color.RED);
		blinky.setAnimated(true);
		blinky.placeAt(4, 1);
		blinky.speed = () -> pacMan.speed.get() * 1.1f;

		pacMan.enemies().add(blinky);

		pacMan.beginWalking();
		blinky.beginChasing();
	};

	@Override
	public void update() {
		if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
			board.resetContent();
		}
		pacMan.update();
		blinky.update();
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, app.getTheme().getBoardSprite());
		drawGridLines(g, getWidth(), getHeight());
		pacMan.draw(g);
		blinky.draw(g);
	}

	private void escapeBlinky() {
		pacMan.move();
		Tile pacManTile = pacMan.currentTile();
		
		if (!pacMan.isExactlyOverTile(pacManTile)) {
			return;
		}
		
		int dir = pacMan.getMoveDir();
		int max = -1;
		for (int i = 0; i < 4; ++i) {
			Tile neighbor = pacManTile.neighbor(i);
			if (pacMan.canEnterTile.apply(neighbor)) {
				int dist = board.shortestRoute(blinky.currentTile(), neighbor).size();
				if (dist > max) {
					max = dist;
					dir = i;
				}
			}
		}
		if (dir != Top4.INSTANCE.inv(pacMan.getMoveDir())) {
			pacMan.setMoveDir(dir);
		} else {
			for (int i = 0; i < 4; ++i) {
				Tile neighbor = pacManTile.neighbor(i);
				if (i != dir && pacMan.canEnterTile.apply(neighbor)) {
					pacMan.setMoveDir(i);
				}
			}
		}
	}
}