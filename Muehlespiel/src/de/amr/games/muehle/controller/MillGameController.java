package de.amr.games.muehle.controller;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.controller.fsm.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.controller.fsm.MillGameEvent.STONE_PLACED_IN_MILL;
import static de.amr.games.muehle.controller.fsm.MillGameEvent.STONE_REMOVED;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.Transition;
import de.amr.games.muehle.controller.fsm.MillGameEvent;
import de.amr.games.muehle.controller.fsm.MillGamePhase;
import de.amr.games.muehle.controller.fsm.MillGameStateMachine;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.view.Assistant;
import de.amr.games.muehle.view.MillGameUI;

/**
 * Controller for the mill game application.
 * 
 * @author Armin Reichert
 */
public class MillGameController extends MillGameStateMachine {

	private final Pulse pulse;
	public final Board board;
	private MillGameUI view;
	private Assistant assistant;
	private Player whitePlayer;
	private Player blackPlayer;
	private Player turn;
	private Player assistedPlayer;
	private MoveController moveControl;
	private int whiteStonesPlaced;
	private int blackStonesPlaced;
	private float moveTimeSeconds = 0.75f;
	private float placingTimeSeconds = 1.5f;

	public MillGameController(Pulse pulse, Board board) {
		this.pulse = pulse;
		this.board = board;
	}

	public Player whitePlayer() {
		return whitePlayer;
	}

	public void setWhitePlayer(Player whitePlayer) {
		this.whitePlayer = whitePlayer;
		if (view != null) {
			view.playerChanged(whitePlayer);
		}
	}

	public Player blackPlayer() {
		return blackPlayer;
	}

	public void setBlackPlayer(Player blackPlayer) {
		this.blackPlayer = blackPlayer;
		if (view != null) {
			view.playerChanged(blackPlayer);
		}
	}

	public void setView(MillGameUI view) {
		this.view = view;
	}

	public void setMoveTimeSeconds(float moveTimeSeconds) {
		this.moveTimeSeconds = moveTimeSeconds;
	}

	public void setPlacingTimeSeconds(float placingTimeSeconds) {
		this.placingTimeSeconds = placingTimeSeconds;
	}

	public void setAssistedPlayer(Player assistedPlayer) {
		this.assistedPlayer = assistedPlayer;
	}

	@Override
	public void init() {
		super.init();
		assistant.init();
		assistedPlayer = whitePlayer;
	}

	@Override
	public void update() {
		readUserInput();
		super.update();
		assistant.update();
	}

	private void readUserInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			assistant.toggle();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			assistant.setHelpLevel(Assistant.HelpLevel.NORMAL);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			assistant.setHelpLevel(Assistant.HelpLevel.HIGH);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_W)) {
			if (whitePlayer.isInteractive()) {
				setAssistedPlayer(whitePlayer);
			}
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			if (blackPlayer.isInteractive()) {
				setAssistedPlayer(blackPlayer);
			}
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			view.toggleBoardPositionNumbers();
		}
	}

	public void setAssistant(Assistant assistant) {
		this.assistant = assistant;
	}

	private void announceWinner(Player winner) {
		view.showMessage("wins", winner.name());
		assistant.tellWin(winner);
	}

	public int numWhiteStonesPlaced() {
		return whiteStonesPlaced;
	}

	public int numBlackStonesPlaced() {
		return blackStonesPlaced;
	}

	public Player playerInTurn() {
		return turn;
	}

	public Player playerNotInTurn() {
		return turn == whitePlayer ? blackPlayer : whitePlayer;
	}

	@Override
	public boolean isGameOver() {
		return board.stoneCount(turn.color()) < 3 || (!turn.canJump() && turn.isTrapped());
	}

	private void turnPlacingTo(Player player) {
		turn = player;
		view.showMessage("must_place", turn.name());
	}

	private void turnMovingTo(Player player) {
		turn = player;
		moveControl = new MoveController(turn, view, pulse, moveTimeSeconds);
		moveControl.setLogger(LOG);
		moveControl.init();
		view.showMessage("must_move", turn.name());
	}

	// implement methods from base class

	@Override
	protected boolean areAllStonesPlaced() {
		return blackStonesPlaced == 9;
	}

	@Override
	protected void switchPlacing(Transition<MillGamePhase, MillGameEvent> change) {
		turnPlacingTo(playerNotInTurn());
		if (!turn.isInteractive()) {
			pause(pulse.secToTicks(placingTimeSeconds));
		}
	}

	@Override
	protected void onMillClosedByPlacing(Transition<MillGamePhase, MillGameEvent> change) {
		if (assistedPlayer == turn) {
			assistant.tellMillClosed();
		}
	}

	@Override
	protected void switchMoving(Transition<MillGamePhase, MillGameEvent> change) {
		turnMovingTo(playerNotInTurn());
	}

	@Override
	protected void tryToPlaceStone(State state) {
		if (assistedPlayer == turn) {
			assistant.givePlacingHint(assistedPlayer);
		}
		turn.supplyPlacingPosition().ifPresent(placedAt -> {
			if (board.isEmptyPosition(placedAt)) {
				StoneColor placedColor = turn.color();
				view.putStoneAt(placedAt, placedColor);
				if (turn == whitePlayer) {
					whiteStonesPlaced += 1;
				} else {
					blackStonesPlaced += 1;
				}
				if (board.inMill(placedAt, placedColor)) {
					addInput(STONE_PLACED_IN_MILL);
				} else {
					addInput(STONE_PLACED);
				}
			} else {
				LOG.info(Messages.text("stone_at_position", placedAt));
			}
		});
	}

	@Override
	protected void tryToRemoveStone(State state) {
		turn.supplyRemovalPosition().ifPresent(p -> {
			StoneColor colorToRemove = playerNotInTurn().color();
			if (board.isEmptyPosition(p)) {
				LOG.info(Messages.text("stone_at_position_not_existing", p));
			} else if (board.getStoneAt(p).get() != colorToRemove) {
				LOG.info(Messages.text("stone_at_position_wrong_color", p));
			} else if (board.inMill(p, colorToRemove) && !board.allStonesInMills(colorToRemove)) {
				LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				view.removeStoneAt(p);
				addInput(STONE_REMOVED);
				LOG.info(Messages.text("removed_stone_at_position", turn.name(), p));
			}
		});
	}

	@Override
	protected void startRemoving(State state) {
		view.showMessage("must_take", turn.name(), playerNotInTurn().name());
	}

	@Override
	protected void updateMove(State state) {
		moveControl.update();
	}

	@Override
	protected boolean isMoveComplete() {
		return moveControl.is(MoveState.COMPLETE);
	}

	@Override
	protected boolean isMillClosedByMove() {
		if (isMoveComplete()) {
			Move move = moveControl.getMove().get();
			if (move.isPresent()) {
				return board.inMill(move.to().get(), turn.color());
			}
		}
		return false;
	}

	@Override
	protected void resetGame(State state) {
		view.clearBoard();
		whiteStonesPlaced = blackStonesPlaced = 0;
		turnPlacingTo(whitePlayer);
	}

	@Override
	protected void onGameOver(State state) {
		announceWinner(playerNotInTurn());
		pause(pulse.secToTicks(3));
	}

	@Override
	protected boolean shallStartNewGame() {
		return !whitePlayer.isInteractive() && !blackPlayer.isInteractive() || Keyboard.keyPressedOnce(KeyEvent.VK_SPACE);
	}
}