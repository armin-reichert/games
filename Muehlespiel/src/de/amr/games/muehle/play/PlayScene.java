package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static de.amr.games.muehle.play.GameEvent.STONE_PLACED;
import static de.amr.games.muehle.play.GamePhase.GAME_OVER;
import static de.amr.games.muehle.play.GamePhase.MOVING;
import static de.amr.games.muehle.play.GamePhase.MOVING_REMOVING;
import static de.amr.games.muehle.play.GamePhase.PLACING;
import static de.amr.games.muehle.play.GamePhase.PLACING_REMOVING;
import static de.amr.games.muehle.play.GamePhase.STARTING;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.TextArea;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.InteractivePlayer;
import de.amr.games.muehle.player.Player;
import de.amr.games.muehle.player.SmartPlayer;
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.StoneCounter;

/**
 * The play scene of the mill game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	private static final int NUM_STONES = 9;

	private final FSM control = new FSM();
	private final Board board = new Board();
	private final Player[] players = new Player[2];
	private int turn;
	private final int[] stonesPlaced = new int[2];
	private final StoneCounter[] stoneCounters = new StoneCounter[2];
	private BoardUI boardUI;
	private Assistant assistant;
	private MoveControl moveControl;
	private TextArea messageArea;

	public PlayScene(MillApp app) {
		super(app);
	}

	/** A finite-state machine for controlling the game play. */
	private class FSM extends StateMachine<GamePhase, GameEvent> {

		private int placedAt;
		private StoneColor placedColor;
		private int removedAt;

		private boolean isGameOver() {
			return board.stoneCount(players[turn].getColor()) < 3 || (!canJump(turn) && isTrapped(turn));
		}

		private void assignPlacingTo(int playerNumber) {
			turn = playerNumber;
			showMessage(turn == 0 ? "white_must_place" : "black_must_place");
		}

		private void assignMovingTo(int playerNumber) {
			turn = playerNumber;
			moveControl.setPlayer(players[turn]);
			showMessage(turn == 0 ? "white_must_move" : "black_must_move");
		}

		private void tryToPlaceStone() {
			players[turn].supplyPlacePosition().ifPresent(placePosition -> {
				if (board.hasStoneAt(placePosition)) {
					LOG.info(Messages.text("stone_at_position", placePosition));
				} else {
					StoneColor colorInTurn = players[turn].getColor();
					boardUI.putStoneAt(placePosition, colorInTurn);
					stonesPlaced[turn] += 1;
					placedAt = placePosition;
					placedColor = colorInTurn;
					addInput(STONE_PLACED);
				}
			});
		}

		private void tryToRemoveStone() {
			StoneColor colorToRemove = players[1 - turn].getColor();
			players[turn].supplyRemovalPosition(colorToRemove).ifPresent(removalPosition -> {
				if (board.isEmptyPosition(removalPosition)) {
					LOG.info(Messages.text("stone_at_position_not_existing", removalPosition));
				} else if (board.getStoneAt(removalPosition) != colorToRemove) {
					LOG.info(Messages.text("stone_at_position_wrong_color", removalPosition));
				} else if (board.inMill(removalPosition, colorToRemove) && !board.allStonesInMills(colorToRemove)) {
					LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
				} else {
					boardUI.removeStoneAt(removalPosition);
					removedAt = removalPosition;
					LOG.info(Messages.text(turn == 0 ? "white_removed_stone_at_position" : "black_removed_stone_at_position",
							removalPosition));
				}
			});
		}

		private void runStoneMove() {
			moveControl.update();
		}

		private boolean placedInMill() {
			return board.inMill(placedAt, placedColor);
		}

		private boolean movedInMill() {
			return moveControl.is(MoveState.FINISHED)
					&& board.inMill(moveControl.getMove().get().to, players[turn].getColor());
		}

		/**
		 * Create the state machine.
		 */
		public FSM() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTING);

			// STARTING

			state(STARTING).entry = s -> {
				boardUI.clear();
				stonesPlaced[0] = stonesPlaced[1] = 0;
				assignPlacingTo(0);
			};

			change(STARTING, PLACING);

			// PLACING

			state(PLACING).update = s -> tryToPlaceStone();

			changeOnInput(STONE_PLACED, PLACING, PLACING_REMOVING, this::placedInMill);

			changeOnInput(STONE_PLACED, PLACING, PLACING, (e, s, t) -> assignPlacingTo(1 - turn));

			change(PLACING, MOVING, () -> stonesPlaced[1] == NUM_STONES, (s, t) -> assignMovingTo(1 - turn));

			// PLACING_REMOVING_STONE

			state(PLACING_REMOVING).entry = s -> {
				removedAt = -1;
				showMessage(turn == 0 ? "white_must_take" : "black_must_take");
			};

			state(PLACING_REMOVING).update = s -> tryToRemoveStone();

			change(PLACING_REMOVING, PLACING, () -> removedAt != -1, (s, t) -> assignPlacingTo(1 - turn));

			// MOVING

			state(MOVING).update = s -> runStoneMove();

			change(MOVING, MOVING_REMOVING, this::movedInMill);

			change(MOVING, MOVING, () -> moveControl.is(MoveState.FINISHED), (s, t) -> assignMovingTo(1 - turn));

			change(MOVING, GAME_OVER, this::isGameOver);

			// MOVING_REMOVING_STONE

			state(MOVING_REMOVING).entry = s -> {
				removedAt = -1;
				showMessage(turn == 0 ? "white_must_take" : "black_must_take");
			};

			state(MOVING_REMOVING).update = s -> tryToRemoveStone();

			change(MOVING_REMOVING, MOVING, () -> removedAt != -1, (s, t) -> assignMovingTo(1 - turn));

			// GAME_OVER

			state(GAME_OVER).entry = s -> showMessage(turn == 0 ? "black_wins" : "white_wins");

			change(GAME_OVER, STARTING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}
	}

	/**
	 * Draws hints for placing or moving on the board.
	 */
	private class Assistant extends GameEntity {

		private boolean enabled;

		public Assistant() {
			setSprites(new Sprite(Assets.image("images/alien.png")).scale(100, 100));
		}

		public void toggle() {
			setEnabled(!enabled);
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			LOG.info(Messages.text(enabled ? "assistant_on" : "assistant_off"));
		}

		@Override
		public void draw(Graphics2D g) {
			if (!enabled) {
				return;
			}
			super.draw(g);
			if (control.is(PLACING)) {
				boardUI.markPositionsClosingMill(g, players[turn].getColor(), Color.GREEN);
				boardUI.markPositionsOpeningTwoMills(g, players[turn].getColor(), Color.YELLOW);
				boardUI.markPositionsClosingMill(g, players[1 - turn].getColor(), Color.RED);
			} else if (control.is(MOVING)) {
				if (moveControl.isMoveStartPossible()) {
					Move move = moveControl.getMove().get();
					boardUI.markPosition(g, move.from, Color.ORANGE);
				} else {
					boardUI.markPossibleMoveStarts(g, players[turn].getColor(), canJump(turn));
					boardUI.markPositionFixingOpponent(g, players[turn].getColor(), players[1 - turn].getColor(), Color.RED);
				}
			}
		}
	}

	@Override
	public void init() {
		Font msgFont = Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36);

		// UI components
		boardUI = new BoardUI(board, 600, 600);
		stoneCounters[0] = new StoneCounter(WHITE, boardUI.getStoneRadius(), () -> NUM_STONES - stonesPlaced[0],
				() -> turn == 0);
		stoneCounters[1] = new StoneCounter(BLACK, boardUI.getStoneRadius(), () -> NUM_STONES - stonesPlaced[1],
				() -> turn == 1);
		messageArea = new TextArea();
		messageArea.setColor(Color.BLUE);
		messageArea.setFont(msgFont);
		assistant = new Assistant();

		// Screen layout
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);
		stoneCounters[0].tf.moveTo(40, getHeight() - 50);
		stoneCounters[1].tf.moveTo(getWidth() - 100, getHeight() - 50);
		messageArea.tf.moveTo(0, getHeight() - 90);
		assistant.hCenter(getWidth());
		assistant.tf.setY(getHeight() / 2 - assistant.getHeight());

		// Players
		players[0] = new InteractivePlayer(boardUI, WHITE);
		// players[1] = new InteractivePlayer(boardUI, BLACK);
		// players[1] = new RandomPlayer(board, BLACK);
		players[1] = new SmartPlayer(board, BLACK);

		moveControl = new MoveControl(boardUI, app.pulse);
		// moveControl.setLogger(LOG);
		control.setLogger(LOG);
		control.init();
	}

	@Override
	public void update() {
		readInput();
		control.update();
	}

	private void readInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_CONTROL, KeyEvent.VK_N)) {
			control.init();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			assistant.toggle();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			boardUI.togglePositionNumbers();
		}
	}

	private void showMessage(String key, Object... args) {
		messageArea.setText(Messages.text(key, args));
	}

	private boolean canJump(int playerNumber) {
		return board.stoneCount(players[playerNumber].getColor()) == 3;
	}

	private boolean isTrapped(int playerNumber) {
		return board.isTrapped(players[playerNumber].getColor());
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(BoardUI.BOARD_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		boardUI.draw(g);
		assistant.draw(g);
		messageArea.hCenter(getWidth());
		messageArea.draw(g);
		if (control.is(PLACING, PLACING_REMOVING)) {
			Stream.of(stoneCounters).forEach(counter -> counter.draw(g));
		}
		if (control.is(PLACING_REMOVING, MOVING_REMOVING)) {
			boardUI.markRemovableStones(g, players[1 - turn].getColor());
		}
	}
}