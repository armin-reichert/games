package de.amr.games.pacman.entities;

import static de.amr.easy.game.Application.Entities;
import static de.amr.easy.game.Application.Settings;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.BonusCol;
import static de.amr.games.pacman.data.Board.BonusRow;
import static de.amr.games.pacman.data.Board.Door;
import static de.amr.games.pacman.data.Board.Energizer;
import static de.amr.games.pacman.data.Board.Pellet;
import static de.amr.games.pacman.data.Board.Wall;
import static de.amr.games.pacman.entities.PacMan.PacManState.Dying;
import static de.amr.games.pacman.entities.PacMan.PacManState.Exploring;
import static de.amr.games.pacman.entities.PacMan.PacManState.Frightening;
import static de.amr.games.pacman.entities.PacMan.PacManState.Waiting;
import static de.amr.games.pacman.ui.PacManUI.TileSize;
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
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.behaviors.GhostAction;
import de.amr.games.pacman.fsm.State;
import de.amr.games.pacman.fsm.StateMachine;

public class PacMan extends BasePacManEntity {

	public enum PacManState {
		Waiting, Exploring, Frightening, Dying;
	};

	public final StateMachine<PacManState> control;
	public Consumer<Tile> pelletFound;
	public Consumer<Tile> energizerFound;
	public Consumer<Bonus> bonusFound;
	public Consumer<Ghost> ghostMet;

	private float speedBeforeFrightening;
	private int freezeTimer;
	private boolean couldMove;

	public PacMan(Tile home) {
		super(home);

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
			Entities.allOf(Ghost.class).forEach(ghost -> ghost.perform(GhostAction.GetFrightened));
		};

		control.state(Frightening).update = state -> {
			exploreMaze();
			if (state.isTerminated()) {
				control.changeTo(Exploring);
			}
		};

		control.state(Frightening).exit = state -> {
			speed = speedBeforeFrightening;
			Entities.allOf(Ghost.class).forEach(ghost -> ghost.perform(GhostAction.EndFrightened));
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
		top.dirs().forEach(dir -> {
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
		return Data.board.isTileValid(tile) && !Data.board.has(Wall, tile) && !Data.board.has(Door, tile);
	}

	private void exploreMaze() {
		if (freezeTimer > 0) {
			--freezeTimer;
			return;
		}
		changeMoveDir(computeMoveDir());
		couldMove = move();
		final Tile tile = currentTile();
		Data.board.checkContent(tile, Pellet).ifPresent(pelletFound);
		Data.board.checkContent(tile, Energizer).ifPresent(energizerFound);
		if (Data.bonus.isPresent() && getCol() == Math.round(BonusCol) && getRow() == Math.round(BonusRow)) {
			bonusFound.accept(Data.bonus.get());
			Data.bonus = Optional.empty();
			Data.bonusTimeRemaining = 0;
		}
		/*@formatter:off*/
		Entities.allOf(Ghost.class)
			.filter(ghost -> ghost.getCol() == getCol() && ghost.getRow() == getRow())
			.forEach(ghostMet);
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
		if (Settings.getBool("drawInternals")) {
			g.setColor(Color.WHITE);
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, TileSize * 9 / 10));
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