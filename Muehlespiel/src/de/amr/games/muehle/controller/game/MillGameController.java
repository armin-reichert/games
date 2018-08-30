package de.amr.games.muehle.controller.game;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.muehle.controller.game.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.controller.game.MillGameEvent.STONE_PLACED_IN_MILL;
import static de.amr.games.muehle.controller.game.MillGameEvent.STONE_REMOVED;
import static de.amr.games.muehle.model.board.StoneColor.BLACK;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;

import java.awt.event.KeyEvent;
import java.util.OptionalInt;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.ViewController;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.Transition;
import de.amr.games.muehle.MillGameApp;
import de.amr.games.muehle.controller.move.MoveController;
import de.amr.games.muehle.controller.move.MoveState;
import de.amr.games.muehle.controller.player.InteractivePlayer;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.controller.player.Zwick;
import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.view.Assistant;
import de.amr.games.muehle.view.MillGameScene;
import de.amr.games.muehle.view.MillGameUI;

/**
 * Controller for the mill game application.
 * 
 * @author Armin Reichert
 */
public class MillGameController extends MillGameStateMachine implements ViewController {

	public final MillGameApp app;
	public final MillGameModel model;
	public final Assistant assistant;
	private MillGameUI view;
	private Player whitePlayer;
	private Player blackPlayer;
	private Player turn;
	private Player assistedPlayer;
	private MoveController moveControl;
	private float moveTimeSeconds;
	private float placingTimeSeconds;
	private OptionalInt positionNearMouse;

	public MillGameController(MillGameApp app, MillGameModel model) {
		this.app = app;
		this.model = model;
		this.assistant = new Assistant(this);
		this.moveTimeSeconds = 0.75f;
		this.placingTimeSeconds = 1.5f;
		this.positionNearMouse = OptionalInt.empty();
	}

	public Player whitePlayer() {
		return whitePlayer;
	}

	public void setWhitePlayer(Player whitePlayer) {
		this.whitePlayer = whitePlayer;
	}

	public Player blackPlayer() {
		return blackPlayer;
	}

	public void setBlackPlayer(Player blackPlayer) {
		this.blackPlayer = blackPlayer;
	}

	public void setView(MillGameUI view) {
		this.view = view;
		assistant.setView(view);
	}

	@Override
	public View currentView() {
		return view;
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

	public OptionalInt getPositionNearMouse() {
		return positionNearMouse;
	}

	@Override
	public void init() {
		MillGameScene gameScene = new MillGameScene(app, this);
		setWhitePlayer(new InteractivePlayer(model, gameScene::findBoardPosition, WHITE));
		setBlackPlayer(new Zwick(model, BLACK));
		setView(gameScene);
		gameScene.init();
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
		if (Mouse.moved()) {
			positionNearMouse = view.findBoardPosition(Mouse.getX(), Mouse.getY());
		}
	}

	private void announceWinner(Player winner) {
		view.showMessage("wins", winner.name());
		assistant.tellWin(winner);
	}

	public Player playerInTurn() {
		return turn;
	}

	public Player playerNotInTurn() {
		return turn == whitePlayer ? blackPlayer : whitePlayer;
	}

	private void turnPlacingTo(Player player) {
		turn = player;
		view.showMessage("must_place", turn.name());
	}

	private void turnMovingTo(Player player) {
		turn = player;
		moveControl = new MoveController(turn, view, moveTimeSeconds);
		moveControl.setLogger(LOGGER);
		moveControl.init();
		view.showMessage("must_move", turn.name());
	}

	// implement methods from base class

	@Override
	protected boolean areAllStonesPlaced() {
		return model.blackStonesPlaced == 9;
	}

	@Override
	protected void switchPlacing(Transition<MillGameState, MillGameEvent> change) {
		turnPlacingTo(playerNotInTurn());
		if (!turn.isInteractive()) {
			pause(Application.PULSE.secToTicks(placingTimeSeconds));
		}
	}

	@Override
	protected void onMillClosedByPlacing(Transition<MillGameState, MillGameEvent> change) {
		if (assistedPlayer == turn) {
			assistant.tellMillClosed();
		}
	}

	@Override
	protected void switchMoving(Transition<MillGameState, MillGameEvent> change) {
		turnMovingTo(playerNotInTurn());
	}

	@Override
	protected void tryToPlaceStone(State state) {
		if (assistedPlayer == turn) {
			assistant.givePlacingHint(assistedPlayer);
		}
		turn.supplyPlacingPosition().ifPresent(placedAt -> {
			if (model.board.isEmptyPosition(placedAt)) {
				StoneColor placedColor = turn.color();
				view.putStoneAt(placedAt, placedColor);
				if (turn == whitePlayer) {
					model.whiteStonesPlaced += 1;
				} else {
					model.blackStonesPlaced += 1;
				}
				if (model.board.inMill(placedAt, placedColor)) {
					addInput(STONE_PLACED_IN_MILL);
				} else {
					addInput(STONE_PLACED);
				}
			} else {
				LOGGER.info(Messages.text("stone_at_position", placedAt));
			}
		});
	}

	@Override
	protected void tryToRemoveStone(State state) {
		turn.supplyRemovalPosition().ifPresent(p -> {
			StoneColor colorToRemove = playerNotInTurn().color();
			if (model.board.isEmptyPosition(p)) {
				LOGGER.info(Messages.text("stone_at_position_not_existing", p));
			} else if (model.board.getStoneAt(p).get() != colorToRemove) {
				LOGGER.info(Messages.text("stone_at_position_wrong_color", p));
			} else if (model.board.inMill(p, colorToRemove)
					&& !model.board.allStonesInMills(colorToRemove)) {
				LOGGER.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				view.removeStoneAt(p);
				addInput(STONE_REMOVED);
				LOGGER.info(Messages.text("removed_stone_at_position", turn.name(), p));
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
				return model.board.inMill(move.to().get(), turn.color());
			}
		}
		return false;
	}

	@Override
	protected void resetGame(State state) {
		view.clearBoard();
		model.whiteStonesPlaced = model.blackStonesPlaced = 0;
		turnPlacingTo(whitePlayer);
	}

	@Override
	protected boolean isGameOver() {
		return model.board.stoneCount(turn.color()) < 3 || (!turn.canJump() && turn.isTrapped());
	}

	@Override
	protected void onGameOver(State state) {
		announceWinner(playerNotInTurn());
		pause(Application.PULSE.secToTicks(3));
	}

	@Override
	protected boolean shallStartNewGame() {
		return !whitePlayer.isInteractive() && !blackPlayer.isInteractive()
				|| Keyboard.keyPressedOnce(KeyEvent.VK_SPACE);
	}
}