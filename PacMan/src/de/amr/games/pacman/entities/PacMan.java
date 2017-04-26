package de.amr.games.pacman.entities;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.data.Board.BONUS_COL;
import static de.amr.games.pacman.data.Board.BONUS_ROW;
import static de.amr.games.pacman.data.Board.TOPOLOGY;
import static de.amr.games.pacman.entities.PacMan.PacManState.Dying;
import static de.amr.games.pacman.entities.PacMan.PacManState.Exploring;
import static de.amr.games.pacman.entities.PacMan.PacManState.Frightening;
import static de.amr.games.pacman.entities.PacMan.PacManState.Waiting;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.Optional;
import java.util.function.Consumer;

import de.amr.easy.game.input.Key;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.behaviors.GhostAction;
import de.amr.games.pacman.fsm.State;
import de.amr.games.pacman.fsm.StateMachine;

public class PacMan extends PacManGameEntity {

	public enum PacManState {
		Waiting, Exploring, Frightening, Dying;
	};

	public final StateMachine<PacManState> control;
	public Consumer<Tile> onPelletFound;
	public Consumer<Tile> onEnergizerFound;
	public Consumer<Bonus> onBonusFound;
	public Consumer<Ghost> onGhostMet;

	private float speedBeforeFrightening;
	private int freezeTimer;
	private boolean couldMove;

	public PacMan(Tile home) {
		super(home);
		setName("Pac-Man");

		// default event handler

		onPelletFound = tile -> {
			Log.info("PacMan finds pellet at tile " + tile);
		};

		onEnergizerFound = tile -> {
			Log.info("PacMan finds energizer at tile " + tile);
		};

		onBonusFound = bonus -> {
			Log.info("PacMan finds bonus " + bonus);
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

		control.state(Exploring).entry = state -> {
			setAnimated(true);
		};

		control.state(Exploring).update = state -> {
			exploreMaze();
		};

		control.state(Frightening).entry = state -> {
			speedBeforeFrightening = this.speed;
			Game.entities.allOf(Ghost.class).forEach(ghost -> ghost.perform(GhostAction.GetFrightened));
		};

		control.state(Frightening).update = state -> {
			exploreMaze();
			if (state.isTerminated()) {
				control.changeTo(Exploring);
			}
		};

		control.state(Frightening).exit = state -> {
			speed = speedBeforeFrightening;
			Game.entities.allOf(Ghost.class).forEach(ghost -> ghost.perform(GhostAction.EndFrightened));
		};

		control.state(Dying).entry = state -> {
			if (getTheme().getPacManDying() != null) {
				getTheme().getPacManDying().resetAnimation();
				getTheme().getPacManDying().setAnimated(true);
			}
		};
	}

	@Override
	public void init() {
		control.changeTo(Waiting);
	}

	@Override
	public void update() {
		control.update();
	}

	@Override
	public Sprite currentSprite() {
		if (control.inState(Dying) && getTheme().getPacManDying() != null) {
			return getTheme().getPacManDying();
		}
		if (control.inState(Waiting)) {
			return getTheme().getPacManStanding(moveDir);
		}
		Sprite runningSprite = getTheme().getPacManRunning(moveDir);
		runningSprite.setAnimated(couldMove);
		return runningSprite;
	}

	@Override
	public void setAnimated(boolean animated) {
		TOPOLOGY.dirs().forEach(dir -> {
			getTheme().getPacManRunning(dir).setAnimated(animated);
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
		return Game.board.isTileValid(tile) && !Game.board.contains(tile, TileContent.Wall)
				&& !Game.board.contains(tile, TileContent.Door);
	}

	private void exploreMaze() {
		if (freezeTimer > 0) {
			--freezeTimer;
			return;
		}
		changeMoveDir(computeMoveDir());
		couldMove = move();
		final Tile tile = currentTile();
		Game.board.getContent(tile, TileContent.Pellet).ifPresent(onPelletFound);
		Game.board.getContent(tile, TileContent.Energizer).ifPresent(onEnergizerFound);
		if (Game.bonus.isPresent() && getCol() == Math.round(BONUS_COL) && getRow() == Math.round(BONUS_ROW)) {
			onBonusFound.accept(Game.bonus.get());
			Game.bonus = Optional.empty();
			Game.bonusTimeRemaining = 0;
		}
		/*@formatter:off*/
		Game.entities.allOf(Ghost.class)
			.filter(ghost -> ghost.getCol() == getCol() && ghost.getRow() == getRow())
			.forEach(onGhostMet);
		/*@formatter:on*/
	}

	private int computeMoveDir() {
		if (Key.down(VK_LEFT)) {
			return W;
		}
		if (Key.down(VK_RIGHT)) {
			return E;
		}
		if (Key.down(VK_UP)) {
			return N;
		}
		if (Key.down(VK_DOWN)) {
			return S;
		}
		return nextMoveDir;
	}

	// -- drawing

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (Game.settings.getBool("drawInternals")) {
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