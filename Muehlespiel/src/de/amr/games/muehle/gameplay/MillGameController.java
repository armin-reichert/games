package de.amr.games.muehle.gameplay;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.statemachine.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.statemachine.MillGameEvent.STONE_PLACED_IN_MILL;
import static de.amr.games.muehle.statemachine.MillGameEvent.STONE_REMOVED;
import static de.amr.games.muehle.statemachine.MillGamePhase.MOVING;
import static de.amr.games.muehle.statemachine.MillGamePhase.MOVING_REMOVING;
import static de.amr.games.muehle.statemachine.MillGamePhase.PLACING;
import static de.amr.games.muehle.statemachine.MillGamePhase.PLACING_REMOVING;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.Transition;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.gameplay.ui.Assistant;
import de.amr.games.muehle.gameplay.ui.MillGameUI;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.Move;
import de.amr.games.muehle.player.Player;
import de.amr.games.muehle.statemachine.MillGameEvent;
import de.amr.games.muehle.statemachine.MillGamePhase;
import de.amr.games.muehle.statemachine.MillGameStateMachine;

/**
 * Controller for the mill game application.
 * 
 * @author Armin Reichert
 */
public class MillGameController extends MillGameStateMachine {

	private final Pulse pulse;
	private final Board board;
	private MillGameUI gameUI;
	private Assistant assistant;
	private Player whitePlayer;
	private Player blackPlayer;
	private Player assistedPlayer;
	private MoveControl moveControl;
	private boolean whitesTurn;
	private int whiteStonesPlaced;
	private int blackStonesPlaced;
	private float moveTimeSeconds = 0.75f;
	private float placingTimeSeconds = 1.5f;

	public MillGameController(Pulse pulse, Board board) {
		this.pulse = pulse;
		this.board = board;
	}

	public Player getWhitePlayer() {
		return whitePlayer;
	}

	public void setWhitePlayer(Player whitePlayer) {
		this.whitePlayer = whitePlayer;
		if (gameUI != null) {
			gameUI.playerChanged(whitePlayer);
		}
	}

	public Player getBlackPlayer() {
		return blackPlayer;
	}

	public void setBlackPlayer(Player blackPlayer) {
		this.blackPlayer = blackPlayer;
		if (gameUI != null) {
			gameUI.playerChanged(blackPlayer);
		}
	}

	public void setUI(MillGameUI gameUI) {
		this.gameUI = gameUI;
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
		assistedPlayer = getWhitePlayer();
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
			if (getWhitePlayer().isInteractive()) {
				setAssistedPlayer(getWhitePlayer());
			}
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			if (getBlackPlayer().isInteractive()) {
				setAssistedPlayer(getBlackPlayer());
			}
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			gameUI.toggleBoardPositionNumbers();
		}
	}

	public void setAssistant(Assistant assistant) {
		this.assistant = assistant;
	}

	private void announceWinner(Player winner) {
		gameUI.showMessage("wins", winner.getName());
		assistant.tellWin(winner);
	}

	public Board getBoard() {
		return board;
	}

	public boolean isPlacing() {
		return is(PLACING, PLACING_REMOVING);
	}

	public int numWhiteStonesPlaced() {
		return whiteStonesPlaced;
	}

	public int numBlackStonesPlaced() {
		return blackStonesPlaced;
	}

	public boolean isMoving() {
		return is(MOVING, MOVING_REMOVING);
	}

	public boolean isRemoving() {
		return is(PLACING_REMOVING, MOVING_REMOVING);
	}

	public Player getPlayerInTurn() {
		return whitesTurn ? getWhitePlayer() : getBlackPlayer();
	}

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

	private void turnMovingToWhite(boolean whitesTurn) {
		this.whitesTurn = whitesTurn;
		moveControl = new MoveControl(getPlayerInTurn(), gameUI, pulse, moveTimeSeconds);
		moveControl.setLogger(LOG);
		moveControl.init();
		gameUI.showMessage("must_move", getPlayerInTurn().getName());
	}

	// implement methods from base class

	@Override
	protected boolean areAllStonesPlaced() {
		return blackStonesPlaced == 9;
	}

	@Override
	protected void switchPlacing(Transition<MillGamePhase, MillGameEvent> change) {
		turnPlacingToWhite(!whitesTurn);
		if (!getPlayerInTurn().isInteractive()) {
			pause(pulse.secToTicks(placingTimeSeconds));
		}
	}

	@Override
	protected void onMillClosedByPlacing(Transition<MillGamePhase, MillGameEvent> change) {
		if (assistedPlayer == getPlayerInTurn()) {
			assistant.tellMillClosed();
		}
	}

	@Override
	protected void switchMoving(Transition<MillGamePhase, MillGameEvent> change) {
		turnMovingToWhite(!whitesTurn);
	}

	@Override
	protected void tryToPlaceStone(State state) {
		if (assistedPlayer == getPlayerInTurn()) {
			assistant.givePlacingHint(assistedPlayer);
		}
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

	@Override
	protected void tryToRemoveStone(State state) {
		getPlayerInTurn().supplyRemovalPosition().ifPresent(p -> {
			StoneColor colorToRemove = getPlayerNotInTurn().getColor();
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

	@Override
	protected void startRemoving(State state) {
		gameUI.showMessage("must_take", getPlayerInTurn().getName(), getPlayerNotInTurn().getName());
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
			if (move.isCompletelySpecified()) {
				return getBoard().inMill(move.getTo().getAsInt(), getPlayerInTurn().getColor());
			}
		}
		return false;
	}

	@Override
	protected void resetGame(State state) {
		gameUI.clearBoard();
		whiteStonesPlaced = blackStonesPlaced = 0;
		turnPlacingToWhite(true);
	}

	@Override
	protected void onGameOver(State state) {
		announceWinner(getPlayerNotInTurn());
		pause(pulse.secToTicks(3));
	}

	@Override
	protected boolean newGameRequested() {
		return !getWhitePlayer().isInteractive() && !getBlackPlayer().isInteractive()
				|| Keyboard.keyPressedOnce(KeyEvent.VK_SPACE);
	}
}