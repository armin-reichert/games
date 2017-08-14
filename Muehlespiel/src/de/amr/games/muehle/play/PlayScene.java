package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static de.amr.games.muehle.play.GamePhase.GAME_OVER;
import static de.amr.games.muehle.play.GamePhase.MOVING;
import static de.amr.games.muehle.play.GamePhase.PLACING;
import static de.amr.games.muehle.play.GamePhase.STARTED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.TextArea;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
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
	private final StoneCounter[] stoneCounter = new StoneCounter[2];

	private BoardUI boardUI;
	private MoveControl moveControl;
	private TextArea messageArea;

	private boolean assistant;
	private int turn;

	public PlayScene(MillApp app) {
		super(app);
	}

	/* A finite-state machine which controls the play scene */
	private class FSM extends StateMachine<GamePhase, Object> {

		private boolean remove;

		private boolean isGameOver() {
			StoneColor color = players[turn].getColor();
			return board.stoneCount(color) < 3 || (!players[turn].canJump() && board.isTrapped(color));
		}

		public FSM() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> showMessage("newgame");

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				remove = false;
				boardUI.clear();
				Stream.of(players).forEach(Player::init);
				assignPlacingTo(0);
			};

			state(PLACING).update = s -> {
				if (remove) {
					tryToRemove().ifPresent(pos -> {
						remove = false;
						assignPlacingTo(1 - turn);
					});
				} else {
					tryToPlace().ifPresent(p -> {
						if (board.inMill(p, players[turn].getColor())) {
							remove = true;
							showMessage(turn == 0 ? "white_must_take" : "black_must_take");
						} else {
							assignPlacingTo(1 - turn);
						}
					});
				}
			};

			change(PLACING, MOVING, () -> players[1].getNumStonesPlaced() == NUM_STONES && !remove);

			// MOVING

			state(MOVING).entry = s -> assignMovingTo(turn);

			state(MOVING).update = s -> {
				if (remove) {
					tryToRemove().ifPresent(p -> {
						remove = false;
						assignMovingTo(1 - turn);
					});
				} else {
					moveControl.update();
					if (moveControl.is(MoveState.FINISHED)) {
						Move move = moveControl.getMove().get();
						if (board.inMill(move.to, players[turn].getColor())) {
							remove = true;
							showMessage(turn == 0 ? "white_must_take" : "black_must_take");
						} else {
							assignMovingTo(1 - turn);
						}
					}
				}
			};

			change(MOVING, GAME_OVER, this::isGameOver);

			// GAME_OVER

			state(GAME_OVER).entry = s -> showMessage(turn == 0 ? "black_wins" : "white_wins");

			change(GAME_OVER, STARTED, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}
	}

	@Override
	public void init() {
		Font msgFont = Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36);

		// UI components
		boardUI = new BoardUI(board, 600, 600);
		stoneCounter[0] = new StoneCounter(WHITE, () -> NUM_STONES - players[0].getNumStonesPlaced());
		stoneCounter[1] = new StoneCounter(BLACK, () -> NUM_STONES - players[1].getNumStonesPlaced());
		messageArea = new TextArea();
		messageArea.setColor(Color.BLUE);
		messageArea.setFont(msgFont);

		// Screen layout
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);
		stoneCounter[0].tf.moveTo(40, getHeight() - 50);
		stoneCounter[1].tf.moveTo(getWidth() - 100, getHeight() - 50);
		messageArea.tf.moveTo(0, getHeight() - 90);

		// Players
		players[0] = new InteractivePlayer(boardUI, WHITE);
		// players[1] = new InteractivePlayer(boardUI, BLACK);
		// players[1] = new RandomPlayer(board, BLACK);
		players[1] = new SmartPlayer(board, BLACK);

		// control.setLogger(LOG);
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
			assistant = !assistant;
			LOG.info(Messages.text(assistant ? "assistant_on" : "assistant_off"));
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			boardUI.togglePositionNumbers();
		}
	}

	private void showMessage(String key, Object... args) {
		messageArea.setText(Messages.text(key, args));
	}

	private void assignPlacingTo(int player) {
		stoneCounter[0].setSelected(player == 0);
		stoneCounter[1].setSelected(player == 1);
		showMessage(player == 0 ? "white_must_place" : "black_must_place");
		turn = player;
	}

	private void assignMovingTo(int player) {
		moveControl = new MoveControl(boardUI, players[player], app.pulse);
		showMessage(player == 0 ? "white_must_move" : "black_must_move");
		turn = player;
	}

	private OptionalInt tryToPlace() {
		OptionalInt optPlacePosition = players[turn].supplyPlacePosition();
		if (optPlacePosition.isPresent()) {
			int placePosition = optPlacePosition.getAsInt();
			if (board.hasStoneAt(placePosition)) {
				LOG.info(Messages.text("stone_at_position", placePosition));
			} else {
				boardUI.putStoneAt(placePosition, players[turn].getColor());
				players[turn].stonePlaced();
				return optPlacePosition;
			}
		}
		return OptionalInt.empty();
	}

	private OptionalInt tryToRemove() {
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
				return optRemovalPosition;
			}
		}
		return OptionalInt.empty();
	}

	// Drawing

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		boardUI.draw(g);
		messageArea.hCenter(getWidth());
		messageArea.draw(g);
		drawStateSpecificInfo(g);
	}

	private void drawStateSpecificInfo(Graphics2D g) {
		switch (control.stateID()) {
		case PLACING:
			drawPlacingInfo(g);
			break;
		case MOVING:
			drawMovingInfo(g);
			break;
		default:
			break;
		}
	}

	private void drawPlacingInfo(Graphics2D g) {
		if (assistant) {
			boardUI.markPositionsClosingMill(g, players[turn].getColor(), Color.GREEN);
			boardUI.markPositionsOpeningTwoMills(g, players[turn].getColor(), Color.YELLOW);
			boardUI.markPositionsClosingMill(g, players[1 - turn].getColor(), Color.RED);
		}
		if (control.remove) {
			boardUI.markRemovableStones(g, players[1 - turn].getColor());
		}
		Stream.of(stoneCounter).forEach(counter -> counter.draw(g));
	}

	private void drawMovingInfo(Graphics2D g) {
		if (assistant) {
			if (moveControl.isMoveStartPossible()) {
				Move move = moveControl.getMove().get();
				boardUI.markPosition(g, move.from, Color.ORANGE);
			} else {
				boardUI.markPossibleMoveStarts(g, players[turn].getColor(), players[turn].canJump());
				boardUI.markPositionFixingOpponent(g, players[turn].getColor(), players[1 - turn].getColor(), Color.RED);
			}
		}
		if (control.remove) {
			boardUI.markRemovableStones(g, players[1 - turn].getColor());
		}
	}
}