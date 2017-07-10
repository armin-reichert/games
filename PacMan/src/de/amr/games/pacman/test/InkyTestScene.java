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
import java.util.Random;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManEvent;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.ChaseWithPartner;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Inky chasing Pac-Man together with Blinky.
 * 
 * @author Armin Reichert
 */
public class InkyTestScene extends Scene<InkyTestApp> {

	private final PacManTheme theme;
	private final Random rand = new Random();
	private final Board board;

	public InkyTestScene(InkyTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
		board = new Board(app.assets.text("board.txt"));
	}

	@Override
	public void init() {

		PacMan pacMan = new PacMan(app, board, () -> theme);

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

		pacMan.control.changeOnInput(PacManEvent.StartWalking, PacManState.Initialized, PacManState.Peaceful);
		pacMan.control.state(PacManState.Peaceful).update = state -> pacMan.walk();

		Ghost blinky = new Ghost(app, board, "Blinky", () -> theme);
		blinky.control.changeOnTimeout(Initialized, Waiting);
		blinky.control.changeOnTimeout(Waiting, Chasing);
		blinky.control.state(Chasing).update = state -> blinky.follow(pacMan.currentTile());

		Ghost inky = new Ghost(app, board, "Inky", () -> theme);
		inky.control.changeOnTimeout(Initialized, Waiting);
		inky.control.changeOnTimeout(Waiting, Chasing);
		inky.control.defineState(Chasing, new ChaseWithPartner(inky, blinky, pacMan));

		app.entities.add(pacMan, blinky, inky);

		pacMan.init();
		pacMan.placeAt(PACMAN_HOME);
		pacMan.speed = () -> (float) Math.round(4f * TILE_SIZE / app.pulse.getFrequency());

		blinky.init();
		blinky.setAnimated(true);
		blinky.color = Color.RED;
		blinky.speed = () -> pacMan.speed.get() * .9f;
		blinky.placeAt(GHOST_HOUSE_ENTRY);
		blinky.setMoveDir(E);
		blinky.control.state(Initialized).setDuration(app.pulse.secToTicks(1));
		blinky.control.state(Waiting).setDuration(app.pulse.secToTicks(1));

		inky.init();
		inky.setAnimated(true);
		inky.color = new Color(64, 224, 208);
		inky.speed = () -> pacMan.speed.get() * .9f;
		inky.placeAt(GHOST_HOUSE_ENTRY);
		inky.control.state(Initialized).setDuration(app.pulse.secToTicks(1));
		inky.control.state(Waiting).setDuration(app.pulse.secToTicks(1));

		pacMan.receiveEvent(PacManEvent.StartWalking);
	};

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
		app.entities.all().forEach(entity -> entity.draw(g));
	}
}