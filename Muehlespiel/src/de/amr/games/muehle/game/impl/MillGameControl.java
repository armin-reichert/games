package de.amr.games.muehle.game.impl;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.game.api.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.game.api.MillGameEvent.STONE_PLACED_IN_MILL;
import static de.amr.games.muehle.game.api.MillGameEvent.STONE_REMOVED;
import static de.amr.games.muehle.game.api.MillGamePhase.GAME_OVER;
import static de.amr.games.muehle.game.api.MillGamePhase.MOVING;
import static de.amr.games.muehle.game.api.MillGamePhase.MOVING_REMOVING;
import static de.amr.games.muehle.game.api.MillGamePhase.PLACING;
import static de.amr.games.muehle.game.api.MillGamePhase.PLACING_REMOVING;
import static de.amr.games.muehle.game.api.MillGamePhase.STARTING;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.Transition;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.game.api.MillGame;
import de.amr.games.muehle.game.api.MillGameEvent;
import de.amr.games.muehle.game.api.MillGamePhase;
import de.amr.games.muehle.game.api.MillGameUI;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;

/**
 * Finite-state-machine which controls the mill game.
 */
public class MillGameControl extends StateMachine<MillGamePhase, MillGameEvent> implements MillGame {

	private final MillApp app;

	private MillGameUI gameUI;
	private Optional<Assistant> assistant;
	private MoveControl moveControl;
	private boolean whitesTurn;
	private int whiteStonesPlaced;
	private int blackStonesPlaced;

	private float moveTimeSeconds = 0.75f;
	private float placingTimeSeconds = 1.5f;

	public MillGameControl(MillApp app) {

		super("MillGameControl", MillGamePhase.class, STARTING);

		this.app = app;

		// STARTING

		state(STARTING).entry = this::reset;

		change(STARTING, PLACING);

		// PLACING

		state(PLACING).update = this::tryToPlaceStone;

		changeOnInput(STONE_PLACED_IN_MILL, PLACING, PLACING_REMOVING, this::onMillClosedByPlacing);

		changeOnInput(STONE_PLACED, PLACING, MOVING, this::areAllStonesPlaced, this::switchMoving);

		changeOnInput(STONE_PLACED, PLACING, PLACING, this::switchPlacing);

		// PLACING_REMOVING

		state(PLACING_REMOVING).entry = this::startRemoving;

		state(PLACING_REMOVING).update = this::tryToRemoveStone;

		changeOnInput(STONE_REMOVED, PLACING_REMOVING, MOVING, this::areAllStonesPlaced, this::switchMoving);

		changeOnInput(STONE_REMOVED, PLACING_REMOVING, PLACING, this::switchPlacing);

		// MOVING

		state(MOVING).update = this::updateMoveAnimation;

		change(MOVING, GAME_OVER, this::isGameOver);

		change(MOVING, MOVING_REMOVING, this::isMillClosedByMove);

		change(MOVING, MOVING, this::isMoveAnimationComplete, this::switchMoving);

		// MOVING_REMOVING

		state(MOVING_REMOVING).entry = this::startRemoving;

		state(MOVING_REMOVING).update = this::tryToRemoveStone;

		changeOnInput(STONE_REMOVED, MOVING_REMOVING, MOVING, this::switchMoving);

		// GAME_OVER

		state(GAME_OVER).entry = s -> {
			announceWinner(getPlayerNotInTurn());
			pause(app.pulse.secToTicks(3));
		};

		change(GAME_OVER, STARTING, () -> !getWhitePlayer().isInteractive() && !getBlackPlayer().isInteractive()
				|| Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
	}

	public void setUI(MillGameUI gameUI) {
		this.gameUI = gameUI;
	}

	@Override
	public void init() {
		super.init();
		assistant.ifPresent(Assistant::init);
	}

	@Override
	public void update() {
		super.update();
		assistant.ifPresent(Assistant::update);
	}

	public void setAssistant(Assistant assistant) {
		this.assistant = Optional.ofNullable(assistant);
	}

	private void reset(State state) {
		gameUI.clearBoard();
		whiteStonesPlaced = blackStonesPlaced = 0;
		turnPlacingToWhite(true);
	}

	private void announceWinner(Player winner) {
		gameUI.showMessage("wins", winner.getName());
		assistant.ifPresent(Assistant::tellWin);
	}

	private boolean areAllStonesPlaced() {
		return blackStonesPlaced == NUM_STONES;
	}

	@Override
	public Board getBoard() {
		return app.getBoard();
	}

	@Override
	public boolean isPlacing() {
		return is(PLACING, PLACING_REMOVING);
	}

	@Override
	public int numWhiteStonesPlaced() {
		return whiteStonesPlaced;
	}

	@Override
	public int numBlackStonesPlaced() {
		return blackStonesPlaced;
	}

	@Override
	public boolean isMoving() {
		return is(MOVING, MOVING_REMOVING);
	}

	@Override
	public boolean isRemoving() {
		return is(PLACING_REMOVING, MOVING_REMOVING);
	}

	@Override
	public Player getWhitePlayer() {
		return app.getWhitePlayer();
	}

	@Override
	public Player getBlackPlayer() {
		return app.getBlackPlayer();
	}

	@Override
	public Player getPlayerInTurn() {
		return whitesTurn ? getWhitePlayer() : getBlackPlayer();
	}

	@Override
	public Player getPlayerNotInTurn() {
		return whitesTurn ? getBlackPlayer() : getWhitePlayer();
	}

	@Override
	public boolean isGameOver() {
		return getBoard().stoneCount(getPlayerInTurn().getColor()) < 3
				|| (!getPlayerInTurn().canJump() && getPlayerInTurn().isTrapped());
	}

	private void turnPlacingToWhite(boolean whitesTurn) {
		this.whitesTurn = whitesTurn;
		gameUI.showMessage("must_place", getPlayerInTurn().getName());
	}

	private void switchPlacing(Transition<MillGamePhase, MillGameEvent> t) {
		turnPlacingToWhite(!whitesTurn);
		if (!getPlayerInTurn().isInteractive()) {
			pause(app.pulse.secToTicks(placingTimeSeconds));
		}
	}

	private void onMillClosedByPlacing(Transition<MillGamePhase, MillGameEvent> t) {
		assistant.ifPresent(Assistant::tellMillClosed);
	}

	private void turnMovingToWhite(boolean whitesTurn) {
		this.whitesTurn = whitesTurn;
		moveControl = new MoveControl(getPlayerInTurn(), gameUI, app.pulse, moveTimeSeconds);
		moveControl.setLogger(LOG);
		moveControl.init();
		gameUI.showMessage("must_move", getPlayerInTurn().getName());
	}

	private void switchMoving(Transition<MillGamePhase, MillGameEvent> t) {
		turnMovingToWhite(!whitesTurn);
	}

	private void tryToPlaceStone(State state) {
		assistant.ifPresent(Assistant::givePlacingHint);
		getPlayerInTurn().supplyPlacingPosition().ifPresent(placedAt -> {
			if (getBoard().isEmptyPosition(placedAt)) {
				StoneColor placedColor = getPlayerInTurn().getColor();
				gameUI.putStoneAt(placedAt, placedColor);
				if (whitesTurn) {
					whiteStonesPlaced += 1;
				} else {
					blackStonesPlaced += 1;
				}
				if (getBoard().inMill(placedAt, placedColor)) {
					addInput(STONE_PLACED_IN_MILL);
				} else {
					addInput(STONE_PLACED);
				}
			} else {
				LOG.info(Messages.text("stone_at_position", placedAt));
			}
		});
	}

	private void tryToRemoveStone(State state) {
		getPlayerInTurn().supplyRemovalPosition().ifPresent(p -> {
			StoneColor colorToRemove = getPlayerInTurn().getColor().other();
			if (getBoard().isEmptyPosition(p)) {
				LOG.info(Messages.text("stone_at_position_not_existing", p));
			} else if (getBoard().getStoneAt(p).get() != colorToRemove) {
				LOG.info(Messages.text("stone_at_position_wrong_color", p));
			} else if (getBoard().inMill(p, colorToRemove) && !getBoard().allStonesInMills(colorToRemove)) {
				LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				gameUI.removeStoneAt(p);
				addInput(STONE_REMOVED);
				LOG.info(Messages.text("removed_stone_at_position", getPlayerInTurn().getName(), p));
			}
		});
	}

	private void startRemoving(State state) {
		gameUI.showMessage("must_take", getPlayerInTurn().getName(), getPlayerNotInTurn().getName());
	}

	private void updateMoveAnimation(State state) {
		moveControl.update();
	}

	private boolean isMoveAnimationComplete() {
		return moveControl.is(MoveState.COMPLETE);
	}

	private boolean isMillClosedByMove() {
		if (isMoveAnimationComplete()) {
			Move move = moveControl.getMove().get();
			return getBoard().inMill(move.to, getPlayerInTurn().getColor());
		}
		return false;
	}
}