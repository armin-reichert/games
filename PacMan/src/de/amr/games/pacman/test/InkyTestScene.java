package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.GHOST_HOUSE_ENTRY;
import static de.amr.games.pacman.play.PlayScene.PACMAN_HOME;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManEvent;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.ChaseWithPartner;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Inky chasing Pac-Man together with Blinky.
 * 
 * @author Armin Reichert
 */
public class InkyTestScene extends Scene<InkyTestApp> {

	private final PacManTheme theme;
	private Random rand = new Random();
	private Board board;
	private PacMan pacMan;
	private Ghost blinky;
	private Ghost inky;

	public InkyTestScene(InkyTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));

		pacMan = new PacMan(app, board, () -> theme);
		pacMan.init();
		pacMan.placeAt(PACMAN_HOME);

		pacMan.speed = () -> (float) Math.round(4f * TILE_SIZE / app.motor.getFrequency());
		pacMan.onEnemyContact = ghost -> {
			ghost.placeAt(GHOST_HOUSE_ENTRY);
			int dir = rand.nextBoolean() ? W : E;
			ghost.setMoveDir(dir); // TODO without this, ghost might get stuck
			ghost.setNextMoveDir(dir);

			pacMan.placeAt(PACMAN_HOME);
			dir = rand.nextBoolean() ? E : W;
			pacMan.setMoveDir(dir);
			pacMan.setNextMoveDir(dir);
		};

		blinky = new Ghost(app, board, "Blinky", () -> theme);
		blinky.init();
		blinky.setAnimated(true);
		blinky.setColor(Color.RED);
		blinky.speed = () -> pacMan.speed.get() * .9f;
		blinky.placeAt(GHOST_HOUSE_ENTRY);
		blinky.setMoveDir(E);

		blinky.control.change(Initialized, Waiting, () -> true);
		blinky.control.change(Waiting, Chasing, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		blinky.control.state(Chasing).update = state -> blinky.follow(pacMan.currentTile());
		// blinky.control.state(Chasing).update = state -> blinky.moveRandomly();

		inky = new Ghost(app, board, "Inky", () -> theme);
		inky.init();
		inky.setAnimated(true);
		inky.setColor(new Color(64, 224, 208));
		inky.speed = () -> pacMan.speed.get() * .9f;
		inky.placeAt(GHOST_HOUSE_ENTRY);

		inky.control.change(Initialized, GhostState.Waiting, () -> true);
		inky.control.change(Waiting, Chasing, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		inky.control.state(Chasing, new ChaseWithPartner(inky, blinky, pacMan));

		app.entities.add(pacMan, blinky, inky);

		pacMan.receiveEvent(PacManEvent.StartWalking);
	};

	@Override
	public void update() {
		Stream.of(pacMan, blinky, inky).forEach(GameEntity::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoardSprite());
		range(4, board.numRows - 3).forEach(row -> range(0, board.numCols).forEach(col -> {
			if (board.contains(row, col, Pellet)) {
				drawSprite(g, row, col, theme.getPelletSprite());
			} else if (board.contains(row, col, Energizer)) {
				drawSprite(g, row, col, theme.getEnergizerSprite());
			}
		}));
		Stream.of(pacMan, blinky, inky).forEach(entity -> entity.draw(g));
	}
}