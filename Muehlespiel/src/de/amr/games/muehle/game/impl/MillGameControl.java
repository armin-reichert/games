package de.amr.games.muehle.game.impl;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.game.api.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.game.api.MillGamePhase.GAME_OVER;
import static de.amr.games.muehle.game.api.MillGamePhase.MOVING;
import static de.amr.games.muehle.game.api.MillGamePhase.MOVING_REMOVING;
import static de.amr.games.muehle.game.api.MillGamePhase.PLACING;
import static de.amr.games.muehle.game.api.MillGamePhase.PLACING_REMOVING;
import static de.amr.games.muehle.game.api.MillGamePhase.STARTING;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.Transition;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.game.api.MillGame;
import de.amr.games.muehle.game.api.MillGameEvent;
import de.amr.games.muehle.game.api.MillGamePhase;
import de.amr.games.muehle.game.api.MillGameUI;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.player.impl.InteractivePlayer;

/**
 * Finite-state-machine which controls the mill game.
 */
public class MillGameControl extends StateMachine<MillGamePhase, MillGameEvent> implements MillGame {

	static final float MOVE_TIME_SEC = 0.75f;
	static final float PLACING_TIME_SEC = 1.5f;
	static final float REMOVAL_TIME_SEC = 1.5f;

	private final Board board;
	private final Player whitePlayer;
	private final Player blackPlayer;
	private final Pulse pulse;

	private MillGameUI gameUI;
	private Optional<Assistant> assistant;
	private MoveControl moveControl;
	private int turn;
	private int whiteStonesPlaced;
	private int blackStonesPlaced;
	private int placedAt;
	private StoneColor placedColor;
	private int removedAt;

	public MillGameControl(Board board, Player whitePlayer, Player blackPlayer, Pulse pulse) {

		super("Mühlespiel-Steuerung", MillGamePhase.class, STARTING);

		this.board = board;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.pulse = pulse;

		// STARTING

		state(STARTING).entry = this::reset;

		change(STARTING, PLACING);

		// PLACING

		state(PLACING).update = this::tryToPlaceStone;

		changeOnInput(STONE_PLACED, PLACING, PLACING_REMOVING, this::isPlacingClosingMill, this::onPlacingClosedMill);

		changeOnInput(STONE_PLACED, PLACING, MOVING, this::allStonesPlaced, this::switchMoving);

		changeOnInput(STONE_PLACED, PLACING, PLACING, () -> !allStonesPlaced(), this::switchPlacing);

		// PLACING_REMOVING

		state(PLACING_REMOVING).entry = this::startRemoving;

		state(PLACING_REMOVING).update = this::tryToRemoveStone;

		change(PLACING_REMOVING, MOVING, () -> stoneRemoved() && allStonesPlaced(), this::switchMoving);

		change(PLACING_REMOVING, PLACING, this::stoneRemoved, this::switchPlacing);

		// MOVING

		state(MOVING).update = this::moveStone;

		change(MOVING, GAME_OVER, this::isGameOver);

		change(MOVING, MOVING_REMOVING, this::isMoveClosingMill);

		change(MOVING, MOVING, this::isMoveFinished, this::switchMoving);

		// MOVING_REMOVING

		state(MOVING_REMOVING).entry = this::startRemoving;

		state(MOVING_REMOVING).update = this::tryToRemoveStone;

		change(MOVING_REMOVING, MOVING, this::stoneRemoved, this::switchMoving);

		// GAME_OVER

		state(GAME_OVER).entry = s -> {
			announceWin(1 - turn);
			pause(pulse.secToTicks(3));
		};

		change(GAME_OVER, STARTING,
				() -> !isInteractivePlayer(0) && !isInteractivePlayer(1) || Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
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
		this.assistant = Optional.of(assistant);
	}

	void reset(State state) {
		gameUI.clearBoard();
		whiteStonesPlaced = blackStonesPlaced = 0;
		turnPlacingTo(0);
	}

	void announceWin(int i) {
		gameUI.showMessage("wins", getPlayer(i).getName());
		assistant.ifPresent(Assistant::tellWin);
	}

	boolean allStonesPlaced() {
		return blackStonesPlaced == NUM_STONES;
	}

	boolean isInteractivePlayer(int i) {
		return getPlayer(i) instanceof InteractivePlayer;
	}

	@Override
	public Board getBoard() {
		return board;
	}

	@Override
	public boolean isPlacing() {
		return is(PLACING, PLACING_REMOVING);
	}

	@Override
	public int getNumStonesPlaced(int i) {
		return i == 0 ? whiteStonesPlaced : blackStonesPlaced;
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
	public int getTurn() {
		return turn;
	}

	@Override
	public Player getPlayer(int i) {
		return i == 0 ? whitePlayer : blackPlayer;
	}

	@Override
	public Player getWhitePlayer() {
		return whitePlayer;
	}

	@Override
	public Player getBlackPlayer() {
		return blackPlayer;
	}

	@Override
	public Player getPlayerInTurn() {
		return getPlayer(turn);
	}

	@Override
	public Player getPlayerNotInTurn() {
		return getPlayer(1 - turn);
	}

	@Override
	public boolean isGameOver() {
		return board.stoneCount(getPlayer(turn).getColor()) < 3 || (!canJump(turn) && isTrapped(turn));
	}

	@Override
	public Optional<Move> getMove() {
		return moveControl.getMove();
	}

	boolean canJump(int i) {
		return getPlayer(i).canJump();
	}

	boolean isTrapped(int i) {
		return board.isTrapped(getPlayer(i).getColor());
	}

	void turnPlacingTo(int i) {
		turn = i;
		gameUI.showMessage("must_place", getPlayer(turn).getName());
	}

	void switchPlacing(Transition<MillGamePhase, MillGameEvent> t) {
		turnPlacingTo(1 - turn);
		if (!isInteractivePlayer(turn)) {
			pause(pulse.secToTicks(PLACING_TIME_SEC));
		}
	}

	void onPlacingClosedMill(Transition<MillGamePhase, MillGameEvent> t) {
		assistant.ifPresent(Assistant::tellMillClosed);
	}

	void turnMovingTo(int i) {
		turn = i;
		moveControl = new MoveControl(getPlayer(turn), gameUI, pulse, MOVE_TIME_SEC);
		moveControl.setLogger(LOG);
		moveControl.init();
		gameUI.showMessage("must_move", getPlayer(turn).getName());
	}

	void switchMoving(Transition<MillGamePhase, MillGameEvent> t) {
		turnMovingTo(1 - turn);
	}

	void tryToPlaceStone(State state) {
		assistant.ifPresent(Assistant::givePlacingHint);
		getPlayer(turn).supplyPlacingPosition().ifPresent(p -> {
			if (board.isEmptyPosition(p)) {
				placedAt = p;
				placedColor = getPlayer(turn).getColor();
				gameUI.putStoneAt(placedAt, placedColor);
				if (turn == 0) {
					whiteStonesPlaced += 1;
				} else {
					blackStonesPlaced += 1;
				}
				addInput(STONE_PLACED);
			} else {
				LOG.info(Messages.text("stone_at_position", p));
			}
		});
	}

	void tryToRemoveStone(State state) {
		getPlayer(turn).supplyRemovalPosition().ifPresent(p -> {
			StoneColor colorToRemove = getPlayer(turn).getColor().other();
			if (board.isEmptyPosition(p)) {
				LOG.info(Messages.text("stone_at_position_not_existing", p));
			} else if (board.getStoneAt(p).get() != colorToRemove) {
				LOG.info(Messages.text("stone_at_position_wrong_color", p));
			} else if (board.inMill(p, colorToRemove) && !board.allStonesInMills(colorToRemove)) {
				LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				gameUI.removeStoneAt(p);
				removedAt = p;
				LOG.info(Messages.text("removed_stone_at_position", getPlayer(turn).getName(), p));
			}
		});
	}

	boolean stoneRemoved() {
		return board.isValidPosition(removedAt);
	}

	void startRemoving(State state) {
		removedAt = -1;
		gameUI.showMessage("must_take", getPlayer(turn).getName(), getPlayer(1 - turn).getName());
	}

	void moveStone(State state) {
		moveControl.update();
	}

	boolean isMoveFinished() {
		return moveControl.is(MoveState.COMPLETE);
	}

	boolean isPlacingClosingMill() {
		return board.inMill(placedAt, placedColor);
	}

	boolean isMoveClosingMill() {
		if (isMoveFinished()) {
			Move move = moveControl.getMove().get();
			return board.inMill(move.to, getPlayer(turn).getColor());
		}
		return false;
	}
}