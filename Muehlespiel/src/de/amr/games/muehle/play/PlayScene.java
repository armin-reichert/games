package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static de.amr.games.muehle.play.GamePhase.GAME_OVER;
import static de.amr.games.muehle.play.GamePhase.MOVING;
import static de.amr.games.muehle.play.GamePhase.MOVING_REMOVING;
import static de.amr.games.muehle.play.GamePhase.PLACING;
import static de.amr.games.muehle.play.GamePhase.PLACING_REMOVING;
import static de.amr.games.muehle.play.GamePhase.STARTED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.OptionalInt;
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
import de.amr.games.muehle.ui.StonePile;

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
	private final StonePile[] stoneCounter = new StonePile[2];

	private BoardUI boardUI;
	private Assistant assistant;
	private MoveControl moveControl;
	private TextArea messageArea;

	public PlayScene(MillApp app) {
		super(app);
	}

	/** A finite-state machine for controlling the game play. */
	private class FSM extends StateMachine<GamePhase, Object> {

		private OptionalInt placedAt;
		private OptionalInt removedAt;
		private int turn;

		private boolean isGameOver() {
			StoneColor color = players[turn].getColor();
			return board.stoneCount(color) < 3 || (!players[turn].canJump() && board.isTrapped(color));
		}

		private void assignPlacingTo(int player) {
			placedAt = OptionalInt.empty();
			stoneCounter[0].setSelected(player == 0);
			stoneCounter[1].setSelected(player == 1);
			showMessage(player == 0 ? "white_must_place" : "black_must_place");
			turn = player;
		}

		private void assignMovingTo(int player) {
			moveControl.setPlayer(players[player]);
			showMessage(player == 0 ? "white_must_move" : "black_must_move");
			turn = player;
		}

		private void tryToPlace() {
			OptionalInt optPlacePosition = players[turn].supplyPlacePosition();
			if (optPlacePosition.isPresent()) {
				int placePosition = optPlacePosition.getAsInt();
				if (board.hasStoneAt(placePosition)) {
					LOG.info(Messages.text("stone_at_position", placePosition));
				} else {
					boardUI.putStoneAt(placePosition, players[turn].getColor());
					players[turn].stonePlaced();
					placedAt = optPlacePosition;
				}
			}
		}

		private void tryToRemove() {
			StoneColor otherColor = players[1 - turn].getColor();
			OptionalInt optRemovalPosition = players[turn].supplyRemovalPosition(otherColor);
			if (optRemovalPosition.isPresent()) {
				int removalPosition = optRemovalPosition.getAsInt();
				if (board.isEmptyPosition(removalPosition)) {
					LOG.info(Messages.text("stone_at_position_not_existing", removalPosition));
				} else if (board.getStoneAt(removalPosition) != otherColor) {
					LOG.info(Messages.text("stone_at_position_wrong_color", removalPosition));
				} else if (board.inMill(removalPosition, otherColor) && !board.allStonesInMills(otherColor)) {
					LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
				} else {
					boardUI.removeStoneAt(removalPosition);
					LOG.info(Messages.text(turn == 0 ? "white_removed_stone_at_position" : "black_removed_stone_at_position",
							removalPosition));
					removedAt = optRemovalPosition;
				}
			}
		}

		public FSM() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> showMessage("newgame");

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			state(STARTED).exit = s -> {
				boardUI.clear();
				Stream.of(players).forEach(Player::init);
				assignPlacingTo(0);
			};

			// PLACING

			state(PLACING).update = s -> tryToPlace();

			change(PLACING, MOVING, () -> players[1].getNumStonesPlaced() == NUM_STONES, (s, t) -> assignMovingTo(1 - turn));

			change(PLACING, PLACING_REMOVING,
					() -> placedAt.isPresent() && board.inMill(placedAt.getAsInt(), players[turn].getColor()),
					(s, t) -> showMessage(turn == 0 ? "white_must_take" : "black_must_take"));

			change(PLACING, PLACING, () -> placedAt.isPresent(), (s, t) -> assignPlacingTo(1 - turn));

			// PLACING_REMOVING_STONE

			state(PLACING_REMOVING).entry = s -> removedAt = OptionalInt.empty();

			state(PLACING_REMOVING).update = s -> tryToRemove();

			change(PLACING_REMOVING, PLACING, () -> removedAt.isPresent(), (s, t) -> assignPlacingTo(1 - turn));

			// MOVING

			state(MOVING).update = s -> moveControl.update();

			change(MOVING, MOVING_REMOVING,
					() -> moveControl.is(MoveState.FINISHED)
							&& board.inMill(moveControl.getMove().get().to, players[turn].getColor()),
					(s, t) -> showMessage(turn == 0 ? "white_must_take" : "black_must_take"));

			change(MOVING, MOVING, () -> moveControl.is(MoveState.FINISHED), (s, t) -> assignMovingTo(1 - turn));

			// MOVING_REMOVING_STONE

			state(MOVING_REMOVING).entry = s -> removedAt = OptionalInt.empty();

			state(MOVING_REMOVING).update = s -> tryToRemove();

			change(MOVING_REMOVING, MOVING, () -> removedAt.isPresent(), (s, t) -> assignMovingTo(1 - turn));

			change(MOVING, GAME_OVER, this::isGameOver);

			// GAME_OVER

			state(GAME_OVER).entry = s -> showMessage(turn == 0 ? "black_wins" : "white_wins");

			change(GAME_OVER, STARTED, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
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
				boardUI.markPositionsClosingMill(g, players[control.turn].getColor(), Color.GREEN);
				boardUI.markPositionsOpeningTwoMills(g, players[control.turn].getColor(), Color.YELLOW);
				boardUI.markPositionsClosingMill(g, players[1 - control.turn].getColor(), Color.RED);
			} else if (control.is(MOVING)) {
				if (moveControl.isMoveStartPossible()) {
					Move move = moveControl.getMove().get();
					boardUI.markPosition(g, move.from, Color.ORANGE);
				} else {
					boardUI.markPossibleMoveStarts(g, players[control.turn].getColor(), players[control.turn].canJump());
					boardUI.markPositionFixingOpponent(g, players[control.turn].getColor(), players[1 - control.turn].getColor(),
							Color.RED);
				}
			}
		}
	}

	@Override
	public void init() {
		Font msgFont = Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36);

		// UI components
		boardUI = new BoardUI(board, 600, 600);
		stoneCounter[0] = new StonePile(WHITE, boardUI.getStoneRadius(),
				() -> NUM_STONES - players[0].getNumStonesPlaced());
		stoneCounter[1] = new StonePile(BLACK, boardUI.getStoneRadius(),
				() -> NUM_STONES - players[1].getNumStonesPlaced());
		messageArea = new TextArea();
		messageArea.setColor(Color.BLUE);
		messageArea.setFont(msgFont);
		assistant = new Assistant();

		// Screen layout
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);
		stoneCounter[0].tf.moveTo(40, getHeight() - 50);
		stoneCounter[1].tf.moveTo(getWidth() - 100, getHeight() - 50);
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

	@Override
	public void draw(Graphics2D g) {
		g.setColor(BoardUI.BOARD_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		boardUI.draw(g);
		assistant.draw(g);
		messageArea.hCenter(getWidth());
		messageArea.draw(g);
		if (control.is(PLACING, PLACING_REMOVING)) {
			Stream.of(stoneCounter).forEach(counter -> counter.draw(g));
		}
		if (control.is(PLACING_REMOVING, MOVING_REMOVING)) {
			boardUI.markRemovableStones(g, players[1 - control.turn].getColor());
		}
	}
}