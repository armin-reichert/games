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
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.StonesCounter;

/**
 * The play scene of the mill game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	private static final int NUM_STONES = 9;

	private final PlayControl control;
	private BoardUI boardUI;
	private Board board;
	private MoveControl moveControl;
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
			return board.stoneCount(color) < 3 || (!current.canJump() && board.isTrapped(color));
		}

		public PlayControl() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> showMessage("newgame");

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				mustRemoveStoneOfOpponent = false;
				boardUI.clear();
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
						if (board.inMill(pos, current.getColor())) {
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
					moveControl.update();
					if (moveControl.isAnimationComplete()) {
						Move move = moveControl.getMove();
						if (board.inMill(move.to, current.getColor())) {
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

		board = new Board();
		boardUI = new BoardUI(board, 600, 600);
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);

		white = new InteractivePlayer(app, boardUI, board, WHITE);
		black = new InteractivePlayer(app, boardUI, board, BLACK);
		// black = new RandomPlayer(app, board, BLACK);
		// black = new StrackPlayer(app, board, BLACK);

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
			boardUI.togglePositionNumbers();
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
		moveControl = new MoveControl(boardUI, player, app.pulse);
		showMessage(player.getColor() == WHITE ? "white_must_move" : "black_must_move");
		switchTo(player);
	}

	private OptionalInt tryToPlaceStone(Player player) {
		OptionalInt optPlacePosition = player.supplyPlacePosition();
		if (optPlacePosition.isPresent()) {
			int placePosition = optPlacePosition.getAsInt();
			if (board.hasStoneAt(placePosition)) {
				LOG.info(Messages.text("stone_at_position", placePosition));
			} else {
				boardUI.putStoneAt(placePosition, player.getColor());
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
			if (board.isEmptyPosition(removalPosition)) {
				LOG.info(Messages.text("stone_at_position_not_existing", removalPosition));
			} else if (board.getStoneAt(removalPosition) != otherColor) {
				LOG.info(Messages.text("stone_at_position_wrong_color", removalPosition));
			} else if (board.inMill(removalPosition, otherColor) && !board.allStonesInMills(otherColor)) {
				LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				boardUI.removeStoneAt(removalPosition);
				LOG.info(Messages.text(player.getColor() == WHITE ? "white_took_stone" : "black_took_stone", removalPosition));
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
			boardUI.markRemovableStones(g, other.getColor());
		} else if (assistantOn) {
			boardUI.markPositionsClosingMill(g, current.getColor(), Color.GREEN);
			boardUI.markPositionsOpeningTwoMills(g, current.getColor(), Color.YELLOW);
			boardUI.markPositionsClosingMill(g, other.getColor(), Color.RED);
		}
	}

	private void drawMovingInfo(Graphics2D g) {
		if (moveControl.isMoveStartPossible()) {
			Move move = moveControl.getMove();
			boardUI.markPosition(g, move.from, Color.ORANGE);
		} else {
			boardUI.markPossibleMoveStarts(g, current.getColor(), current.canJump());
			if (assistantOn) {
				boardUI.markPositionFixingOpponent(g, current.getColor(), other.getColor(), Color.RED);
			}
		}
		if (control.mustRemoveStoneOfOpponent) {
			boardUI.markRemovableStones(g, other.getColor());
		}
	}
}