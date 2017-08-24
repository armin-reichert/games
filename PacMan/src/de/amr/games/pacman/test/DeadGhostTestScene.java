package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent.Killed;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent.RecoveringStarts;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.misc.SceneHelper.drawTextCentered;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

public class DeadGhostTestScene extends Scene<DeadGhostTestApp> {

	public static final Tile GHOST_HOUSE_ENTRY = new Tile(14, 13);

	private final PacManTheme theme;
	private final Board board;
	private final Random rand;

	public DeadGhostTestScene(DeadGhostTestApp app) {
		super(app);
		theme = new ClassicTheme();
		board = new Board(Assets.text("board.txt"));
		rand = new Random();
	}

	@Override
	public void init() {

		range(0, 3).forEach(i -> {
			Ghost ghost = new Ghost(app, board, randomGhostName(), () -> theme);
			ghost.init();
			ghost.setLogger(Application.LOG);
			ghost.placeAt(randomTile());
			ghost.speed = () -> 2f;
			ghost.xOffset = () -> ghost.control.is(Recovering) ? TILE_SIZE / 2 : 0;
			defineBehavior(ghost);
			app.entities.add(ghost);
		});

		app.entities.filter(Ghost.class).forEach(ghost -> {
			int seconds = rand.nextInt(3) + 1;
			ghost.control.state().setDuration(app.pulse.secToTicks(seconds));
		});
	}

	private void defineBehavior(Ghost ghost) {

		// Initialized
		ghost.control.changeOnTimeout(Initialized, Scattering);

		// Scattering
		ghost.control.state(Scattering).update = state -> {
			if (Keyboard.keyPressedOnce(KeyEvent.VK_K)) {
				ghost.receiveEvent(Killed);
			} else {
				ghost.moveRandomly();
			}
		};
		ghost.control.changeOnInput(Killed, Scattering, Dead);

		// Dead
		ghost.control.state(Dead).update = state -> {
			ghost.follow(GHOST_HOUSE_ENTRY);
			if (ghost.isExactlyOver(GHOST_HOUSE_ENTRY)) {
				ghost.receiveEvent(RecoveringStarts);
			}
		};

		ghost.control.changeOnInput(RecoveringStarts, Dead, Recovering, t -> t.to().setDuration(120));

		// Recovering
		ghost.control.changeOnTimeout(Recovering, Scattering);
	}

	private final String[] ghostNames = { "Blinky", "Inky", "Pinky", "Clyde" };

	private String randomGhostName() {
		return ghostNames[rand.nextInt(ghostNames.length)];
	}

	private Tile randomTile() {
		Tile start = new Tile(4, 4);
		while (true) {
			Tile tile = new Tile(rand.nextInt(board.numRows), rand.nextInt(board.numCols));
			if (!board.shortestRoute(tile, start).isEmpty()) {
				return tile;
			}
		}
	}

	@Override
	public void update() {
		app.entities.all().forEach(GameEntity::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoardSprite());
		drawGridLines(g, getWidth(), getHeight());
		g.setColor(Color.WHITE);
		drawTextCentered(g, getWidth(), 1, "Press 'k' to kill ghosts");
		app.entities.filter(Ghost.class).forEach(ghost -> ghost.draw(g));
	}
}