package de.amr.games.pacman.core.entities;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.entities.PacManState.Dying;
import static de.amr.games.pacman.core.entities.PacManState.Eating;
import static de.amr.games.pacman.core.entities.PacManState.Frightening;
import static de.amr.games.pacman.core.entities.PacManState.Waiting;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.function.Consumer;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.core.app.AbstractPacManApp;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostMessage;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.core.statemachine.StateMachine;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan extends PacManEntity {

	public final StateMachine<PacManState> control;

	public Consumer<Tile> onPelletFound;
	public Consumer<Tile> onEnergizerFound;
	public Consumer<Tile> onBonusFound;
	public Consumer<Ghost> onGhostMet;

	private float speedBeforeBecomingFrightening;
	private int freezeTimer;
	private boolean couldMove;

	public PacMan(AbstractPacManApp app, Board board, Tile home) {
		super(app, board, home);
		setName("Pac-Man");

		// default event handlers

		onPelletFound = tile -> {
			Log.info("PacMan eats pellet at tile " + tile);
			board.setContent(tile, TileContent.None);
		};

		onEnergizerFound = tile -> {
			Log.info("PacMan eats energizer at tile " + tile);
			board.setContent(tile, TileContent.None);
		};

		onBonusFound = tile -> {
			Log.info("PacMan eats bonus ");
			board.setContent(tile, TileContent.None);
		};

		onGhostMet = ghost -> {
			Log.info("PacMan meets ghost " + ghost);
		};

		// state machine

		control = new StateMachine<>("Pac-Man", new EnumMap<>(PacManState.class));

		control.state(Waiting).entry = state -> {
			couldMove = false;
			freezeTimer = 0;
			speed = 0;
			moveDir = W;
			nextMoveDir = W;
			placeAt(home);
		};

		control.state(Eating).entry = state -> {
			setAnimated(true);
		};

		control.state(Eating).update = state -> {
			exploreMaze();
		};

		control.state(Frightening).entry = state -> {
			speedBeforeBecomingFrightening = this.speed;
			app.entities.allOf(Ghost.class).forEach(ghost -> ghost.receive(GhostMessage.StartBeingFrightened));
		};

		control.state(Frightening).update = state -> {
			exploreMaze();
			if (state.isTerminated()) {
				control.changeTo(Eating);
			}
		};

		control.state(Frightening).exit = state -> {
			speed = speedBeforeBecomingFrightening;
			app.entities.allOf(Ghost.class).forEach(ghost -> ghost.receive(GhostMessage.EndBeingFrightened));
		};

		control.state(Dying).entry = state -> {
			if (app.getTheme().getPacManDyingSprite() != null) {
				app.getTheme().getPacManDyingSprite().resetAnimation();
				app.getTheme().getPacManDyingSprite().setAnimated(true);
			}
		};
	}

	@Override
	public String toString() {
		return String.format("Pacman[row=%d,col=%d]", getRow(), getCol());
	}

	@Override
	public void init() {
		placeAt(home);
		control.changeTo(Waiting);
	}

	@Override
	public void update() {
		if (freezeTimer > 0) {
			--freezeTimer;
			return;
		}
		control.update();
	}

	@Override
	public Sprite currentSprite() {
		if (control.inState(Dying) && app.getTheme().getPacManDyingSprite() != null) {
			return app.getTheme().getPacManDyingSprite();
		}
		if (control.inState(Waiting)) {
			return app.getTheme().getPacManStandingSprite(moveDir);
		}
		Sprite runningSprite = app.getTheme().getPacManRunningSprite(moveDir);
		runningSprite.setAnimated(couldMove);
		return runningSprite;
	}

	@Override
	public void setAnimated(boolean animated) {
		board.topology.dirs().forEach(dir -> {
			app.getTheme().getPacManRunningSprite(dir).setAnimated(animated);
		});
	}

	public void freeze(int frames) {
		this.freezeTimer = frames;
	}

	public void startAttacking(int frames, float speed) {
		this.speed = speed;
		control.state(Frightening).setDuration(frames);
		control.changeTo(Frightening);
	}

	public boolean isFrighteningEnding() {
		return control.inState(Frightening)
				&& control.state(Frightening).getTimer() < control.state(Frightening).getDuration() / 4;
	}

	@Override
	public boolean canEnter(Tile tile) {
		return board.isTileValid(tile) && !board.contains(tile, TileContent.Wall)
				&& !board.contains(tile, TileContent.Door);
	}

	private void exploreMaze() {
		changeMoveDir(computeMoveDir());
		couldMove = move();
		final Tile currentTile = currentTile();
		board.getContent(currentTile, TileContent.Pellet).ifPresent(onPelletFound);
		board.getContent(currentTile, TileContent.Energizer).ifPresent(onEnergizerFound);
		board.getContent(currentTile, TileContent.Bonus).ifPresent(onBonusFound);
		/*@formatter:off*/
		app.entities.allOf(Ghost.class)
			.filter(ghost -> ghost.getCol() == getCol() && ghost.getRow() == getRow())
			.forEach(onGhostMet);
		/*@formatter:on*/
	}

	private int computeMoveDir() {
		if (Keyboard.down(VK_LEFT)) {
			return W;
		}
		if (Keyboard.down(VK_RIGHT)) {
			return E;
		}
		if (Keyboard.down(VK_UP)) {
			return N;
		}
		if (Keyboard.down(VK_DOWN)) {
			return S;
		}
		return nextMoveDir;
	}

	// -- drawing

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (app.settings.getBool("drawInternals")) {
			g.setColor(Color.WHITE);
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, TILE_SIZE * 9 / 10));
			State state = control.state();
			StringBuilder text = new StringBuilder();
			text.append(getName()).append(" (").append(control.stateID());
			if (state.getDuration() != State.FOREVER) {
				text.append(":").append(state.getTimer()).append("|").append(state.getDuration());
			}
			text.append(")");
			g.drawString(text.toString(), tr.getX(), tr.getY() - 10);
		}
	}
}