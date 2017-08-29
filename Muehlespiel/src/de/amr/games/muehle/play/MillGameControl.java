package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.play.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.play.MillGamePhase.GAME_OVER;
import static de.amr.games.muehle.play.MillGamePhase.MOVING;
import static de.amr.games.muehle.play.MillGamePhase.MOVING_REMOVING;
import static de.amr.games.muehle.play.MillGamePhase.PLACING;
import static de.amr.games.muehle.play.MillGamePhase.PLACING_REMOVING;
import static de.amr.games.muehle.play.MillGamePhase.STARTING;

import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.Transition;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.player.impl.InteractivePlayer;

/**
 * Finite-state-machine which controls the mill game.
 */
public class MillGameControl extends StateMachine<MillGamePhase, MillGameEvent> implements MillGame {

	static final float PLACING_TIME_SEC = 1.5f;
	static final float REMOVAL_TIME_SEC = 1.5f;

	private final Board board;
	private final Player[] players;
	private final int[] stonesPlaced;
	private final MillGameUI gameUI;
	private final Pulse pulse;

	private Optional<Assistant> assistant;
	private MoveControl moveControl;
	private int turn;
	private int placedAt;
	private StoneColor placedColor;
	private int removedAt;

	public MillGameControl(Board board, Player whitePlayer, Player blackPlayer, MillGameUI gameUI, Pulse pulse) {

		super("Mühlespiel-Steuerung", MillGamePhase.class, STARTING);

		this.board = board;
		this.players = new Player[] { whitePlayer, blackPlayer };
		this.stonesPlaced = new int[2];
		this.gameUI = gameUI;
		this.pulse = pulse;

		// STARTING

		state(STARTING).entry = this::reset;

		change(STARTING, PLACING);

		// PLACING

		state(PLACING).update = this::tryToPlaceStone;

		change(PLACING, MOVING, this::allStonesPlaced, this::switchMoving);

		changeOnInput(STONE_PLACED, PLACING, PLACING_REMOVING, this::placingClosedMill, this::onPlacingClosedMill);

		changeOnInput(STONE_PLACED, PLACING, PLACING, this::switchPlacing);

		// PLACING_REMOVING

		state(PLACING_REMOVING).entry = this::startRemoving;

		state(PLACING_REMOVING).update = this::tryToRemoveStone;

		change(PLACING_REMOVING, PLACING, this::stoneRemoved, this::switchPlacing);

		// MOVING

		state(MOVING).update = this::moveStone;

		change(MOVING, MOVING_REMOVING, this::moveClosedMill);

		change(MOVING, MOVING, this::isMoveFinished, this::switchMoving);

		change(MOVING, GAME_OVER, this::isGameOver);

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
		stonesPlaced[0] = stonesPlaced[1] = 0;
		turnPlacingTo(0);
	}

	void announceWin(int i) {
		gameUI.showMessage("wins", players[i].getName());
		assistant.ifPresent(Assistant::tellWin);
	}

	boolean allStonesPlaced() {
		return stonesPlaced[1] == NUM_STONES;
	}

	boolean isInteractivePlayer(int i) {
		return players[i] instanceof InteractivePlayer;
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
		return stonesPlaced[i];
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
	public Player getPlayerInTurn() {
		return players[turn];
	}

	@Override
	public Player getPlayerNotInTurn() {
		return players[1 - turn];
	}

	@Override
	public boolean isGameOver() {
		return board.stoneCount(players[turn].getColor()) < 3 || (!canJump(turn) && isTrapped(turn));
	}

	@Override
	public boolean isMoveStartPossible() {
		return moveControl.isMoveStartPossible();
	}

	@Override
	public Optional<Move> getMove() {
		return moveControl.getMove();
	}

	boolean canJump(int i) {
		return players[i].canJump();
	}

	boolean isTrapped(int i) {
		return board.isTrapped(players[i].getColor());
	}

	void turnPlacingTo(int i) {
		turn = i;
		gameUI.showMessage("must_place", players[turn].getName());
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
		moveControl = new MoveControl(board, players[turn], gameUI, pulse);
		moveControl.init();
		gameUI.showMessage("must_move", players[turn].getName());
	}

	void switchMoving(Transition<MillGamePhase, MillGameEvent> t) {
		turnMovingTo(1 - turn);
	}

	void tryToPlaceStone(State state) {
		assistant.ifPresent(Assistant::givePlacingHint);
		players[turn].supplyPlacingPosition().ifPresent(placePosition -> {
			if (board.hasStoneAt(placePosition)) {
				LOG.info(Messages.text("stone_at_position", placePosition));
			} else {
				StoneColor colorInTurn = players[turn].getColor();
				gameUI.putStoneAt(placePosition, colorInTurn);
				stonesPlaced[turn] += 1;
				placedAt = placePosition;
				placedColor = colorInTurn;
				addInput(STONE_PLACED);
			}
		});
	}

	void tryToRemoveStone(State state) {
		players[turn].supplyRemovalPosition().ifPresent(removalPosition -> {
			StoneColor colorToRemove = players[turn].getColor().other();
			if (board.isEmptyPosition(removalPosition)) {
				LOG.info(Messages.text("stone_at_position_not_existing", removalPosition));
			} else if (board.getStoneAt(removalPosition).get() != colorToRemove) {
				LOG.info(Messages.text("stone_at_position_wrong_color", removalPosition));
			} else if (board.inMill(removalPosition, colorToRemove) && !board.allStonesInMills(colorToRemove)) {
				LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				gameUI.removeStoneAt(removalPosition);
				removedAt = removalPosition;
				LOG.info(Messages.text("removed_stone_at_position", players[turn].getName(), removalPosition));
			}
		});
	}

	boolean stoneRemoved() {
		return board.isValidPosition(removedAt);
	}

	void startRemoving(State state) {
		removedAt = -1;
		gameUI.showMessage("must_take", players[turn].getName(), players[1 - turn].getName());
	}

	void moveStone(State state) {
		moveControl.update();
	}

	boolean isMoveFinished() {
		return moveControl.is(MoveState.FINISHED);
	}

	boolean placingClosedMill() {
		return board.inMill(placedAt, placedColor);
	}

	boolean moveClosedMill() {
		if (isMoveFinished()) {
			Move move = moveControl.getMove().get();
			return board.inMill(move.to, players[turn].getColor());
		}
		return false;
	}
}