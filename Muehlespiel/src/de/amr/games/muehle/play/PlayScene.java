package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2.dist;
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

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.TextArea;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.BoardModel;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.ui.Board;
import de.amr.games.muehle.ui.StonesCounter;

/**
 * The play scene of the mill game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	private static final int NUM_STONES = 9;
	private static final float MOVE_SECONDS = .75f;

	private final PlayControl control;
	private Board board;
	private Move move;
	private Player white;
	private Player black;
	private Player current;
	private Player other;
	private StonesCounter whiteStillToPlaceCounter;
	private StonesCounter blackStillToPlaceCounter;
	private TextArea messageDisplay;
	private boolean assistantOn;

	/* A finite-state machine which controls the play scene */
	private class PlayControl extends StateMachine<GamePhase, Object> {

		private boolean mustRemoveStoneOfOpponent;

		private boolean isGameOver() {
			StoneColor color = current.getColor();
			return board.getModel().stoneCount(color) < 3 || (!current.canJump() && board.getModel().isTrapped(color));
		}

		public PlayControl() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> showMessage("newgame");

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				mustRemoveStoneOfOpponent = false;
				board.clear();
				white.init();
				black.init();
				assignPlacingTo(white);
			};

			state(PLACING).update = s -> {
				if (mustRemoveStoneOfOpponent) {
					tryToRemoveStone(current, other.getColor()).ifPresent(pos -> {
						mustRemoveStoneOfOpponent = false;
						assignPlacingTo(other);
					});
				} else {
					tryToPlaceStone(current).ifPresent(pos -> {
						if (board.getModel().inMill(pos, current.getColor())) {
							mustRemoveStoneOfOpponent = true;
							showMessage(current.getColor() == WHITE ? "white_must_take" : "black_must_take");
						} else {
							assignPlacingTo(other);
						}
					});
				}
			};

			change(PLACING, MOVING, () -> black.getStonesPlaced() == NUM_STONES && !mustRemoveStoneOfOpponent);

			// MOVING

			state(MOVING).entry = s -> assignMovingTo(current);

			state(MOVING).update = s -> {
				if (mustRemoveStoneOfOpponent) {
					tryToRemoveStone(current, other.getColor()).ifPresent(pos -> {
						mustRemoveStoneOfOpponent = false;
						assignMovingTo(other);
					});
				} else {
					move.update();
					if (move.isComplete()) {
						if (board.getModel().inMill(move.getTo().getAsInt(), current.getColor())) {
							mustRemoveStoneOfOpponent = true;
							showMessage(current.getColor() == WHITE ? "white_must_take" : "black_must_take");
						} else {
							assignMovingTo(other);
						}
					}
				}
			};

			change(MOVING, GAME_OVER, this::isGameOver);

			// GAME_OVER

			state(GAME_OVER).entry = s -> showMessage(other.getColor() == WHITE ? "white_wins" : "black_wins");

			change(GAME_OVER, STARTED, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}
	}

	public PlayScene(MillApp app) {
		super(app);
		control = new PlayControl();
	}

	@Override
	public void init() {
		Font msgFont = Assets.storeFont("message-font", "fonts/Cookie-Regular.ttf", 36, Font.PLAIN);

		board = new Board(new BoardModel(), 600, 600);
		board.hCenter(getWidth());
		board.tf.setY(50);

		white = new InteractivePlayer(app, board, WHITE);
		// black = new InteractivePlayer(app, board, BLACK);
		// black = new RandomPlayer(app, board, BLACK);
		black = new StrackPlayer(app, board, BLACK);

		whiteStillToPlaceCounter = new StonesCounter(WHITE, () -> NUM_STONES - white.getStonesPlaced());
		whiteStillToPlaceCounter.tf.moveTo(40, getHeight() - 50);
		whiteStillToPlaceCounter.init();

		blackStillToPlaceCounter = new StonesCounter(BLACK, () -> NUM_STONES - black.getStonesPlaced());
		blackStillToPlaceCounter.tf.moveTo(getWidth() - 100, getHeight() - 50);
		blackStillToPlaceCounter.init();

		messageDisplay = new TextArea();
		messageDisplay.setColor(Color.BLUE);
		messageDisplay.setFont(msgFont);
		messageDisplay.tf.moveTo(0, getHeight() - 90);

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
			assistantOn = !assistantOn;
			LOG.info(Messages.text(assistantOn ? "assistant_on" : "assistant_off"));
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			board.togglePositionNumbers();
		}
	}

	private void showMessage(String key, Object... args) {
		messageDisplay.setText(Messages.text(key, args));
	}

	private void switchTo(Player player) {
		current = player;
		other = player == white ? black : white;
	}

	private void assignPlacingTo(Player player) {
		whiteStillToPlaceCounter.setSelected(player.getColor() == WHITE);
		blackStillToPlaceCounter.setSelected(player.getColor() == BLACK);
		showMessage(player.getColor() == WHITE ? "white_must_place" : "black_must_place");
		switchTo(player);
	}

	private void assignMovingTo(Player player) {
		move = new Move(board, player, this::supplyMoveVelocity);
		showMessage(player.getColor() == WHITE ? "white_must_move" : "black_must_move");
		switchTo(player);
	}

	private OptionalInt tryToPlaceStone(Player player) {
		OptionalInt optPlacePosition = player.supplyPlacePosition();
		if (optPlacePosition.isPresent()) {
			int placePosition = optPlacePosition.getAsInt();
			if (board.getModel().hasStoneAt(placePosition)) {
				LOG.info(Messages.text("stone_at_position", placePosition));
			} else {
				board.putStoneAt(placePosition, player.getColor());
				player.stonePlaced();
				return optPlacePosition;
			}
		}
		return OptionalInt.empty();
	}

	private OptionalInt tryToRemoveStone(Player player, StoneColor otherColor) {
		OptionalInt optRemovalPosition = player.supplyRemovalPosition(otherColor);
		if (optRemovalPosition.isPresent()) {
			int removalPosition = optRemovalPosition.getAsInt();
			if (board.getModel().isEmptyPosition(removalPosition)) {
				LOG.info(Messages.text("stone_at_position_not_existing", removalPosition));
			} else if (board.getModel().getStoneAt(removalPosition) != otherColor) {
				LOG.info(Messages.text("stone_at_position_wrong_color", removalPosition));
			} else if (board.getModel().inMill(removalPosition, otherColor)
					&& !board.getModel().allStonesInMills(otherColor)) {
				LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				board.removeStoneAt(removalPosition);
				LOG.info(
						Messages.text(player.getColor() == WHITE ? "white_took_stone" : "black_took_stone", removalPosition));
				return optRemovalPosition;
			}
		}
		return OptionalInt.empty();
	}

	private Vector2 supplyMoveVelocity() {
		int from = move.getFrom().getAsInt(), to = move.getTo().getAsInt();
		float speed = dist(board.centerPoint(from), board.centerPoint(to)) / app.pulse.secToTicks(MOVE_SECONDS);
		Direction dir = board.getModel().getDirection(from, to).get();
		switch (dir) {
		case NORTH:
			return new Vector2(0, -speed);
		case EAST:
			return new Vector2(speed, 0);
		case SOUTH:
			return new Vector2(0, speed);
		case WEST:
			return new Vector2(-speed, 0);
		}
		return Vector2.nullVector();
	}

	// Drawing

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		board.draw(g);
		messageDisplay.hCenter(getWidth());
		messageDisplay.draw(g);
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
		whiteStillToPlaceCounter.draw(g);
		blackStillToPlaceCounter.draw(g);
		if (control.mustRemoveStoneOfOpponent) {
			board.markRemovableStones(g, other.getColor());
		} else if (assistantOn) {
			board.markPositionsClosingMill(g, current.getColor(), Color.GREEN);
			board.markPositionsOpeningTwoMills(g, current.getColor(), Color.YELLOW);
			board.markPositionsClosingMill(g, other.getColor(), Color.RED);
		}
	}

	private void drawMovingInfo(Graphics2D g) {
		if (move.getFrom().isPresent()) {
			board.markPosition(g, move.getFrom().getAsInt(), Color.ORANGE);
		} else {
			board.markPossibleMoveStarts(g, current.getColor(), current.canJump());
			if (assistantOn) {
				board.markPositionFixingOpponent(g, current.getColor(), other.getColor(), Color.RED);
			}
		}
		if (control.mustRemoveStoneOfOpponent) {
			board.markRemovableStones(g, other.getColor());
		}
	}
}