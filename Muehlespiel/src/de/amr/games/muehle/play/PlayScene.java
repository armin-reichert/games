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

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.BoardModel;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;
import de.amr.games.muehle.ui.Move;
import de.amr.games.muehle.ui.StonesCounter;

/**
 * The play scene of the game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	private static final int NUM_STONES = 4;

	private final PlayControl control;
	private Board board;
	private Move move;
	private Player whitePlayer;
	private Player blackPlayer;
	private Player currentPlayer;
	private Player otherPlayer;
	private Player winner;
	private StonesCounter whiteStonesToPlaceCounter;
	private StonesCounter blackStonesToPlaceCounter;
	private ScrollingText messageDisplay;
	private boolean assistantOn;

	/* A finite-state machine which controls the play scene */
	private class PlayControl extends StateMachine<GamePhase, String> {

		private boolean mustRemoveStoneOfOpponent;

		public PlayControl() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> displayMessage("newgame");

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				mustRemoveStoneOfOpponent = false;
				board.clear();
				whitePlayer.init();
				blackPlayer.init();
				setWinner(null);
				assignPlacingTo(whitePlayer);
			};

			state(PLACING).update = s -> {
				if (mustRemoveStoneOfOpponent) {
					currentPlayer.tryToRemoveStone(otherPlayer.getColor()).ifPresent(pos -> {
						mustRemoveStoneOfOpponent = false;
						assignPlacingTo(otherPlayer);
					});
				} else {
					currentPlayer.tryToPlaceStone().ifPresent(pos -> {
						if (board.getModel().isPositionInsideMill(pos, currentPlayer.getColor())) {
							mustRemoveStoneOfOpponent = true;
							displayMessage(currentPlayer.getColor() == WHITE ? "white_must_take" : "black_must_take");
						} else {
							assignPlacingTo(otherPlayer);
						}
					});
				}
			};

			change(PLACING, MOVING, () -> blackPlayer.getStonesPlaced() == NUM_STONES && !mustRemoveStoneOfOpponent);

			// MOVING

			state(MOVING).entry = s -> {
				assignMovingTo(currentPlayer);
			};

			state(MOVING).update = s -> {
				if (mustRemoveStoneOfOpponent) {
					currentPlayer.tryToRemoveStone(otherPlayer.getColor()).ifPresent(pos -> {
						mustRemoveStoneOfOpponent = false;
						assignMovingTo(otherPlayer);
					});
				} else {
					move.update();
					if (move.isComplete()) {
						if (board.getModel().isPositionInsideMill(move.getTo().getAsInt(), currentPlayer.getColor())) {
							mustRemoveStoneOfOpponent = true;
							displayMessage(currentPlayer.getColor() == WHITE ? "white_must_take" : "black_must_take");
						} else {
							assignMovingTo(otherPlayer);
						}
					}
				}
			};

			change(MOVING, GAME_OVER, PlayScene.this::isGameOver);

			// GAME_OVER

			state(GAME_OVER).entry = s -> setWinner(otherPlayer);

			change(GAME_OVER, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_ENTER));
		}
	}

	public PlayScene(MillApp app) {
		super(app);
		control = new PlayControl();
	}

	@Override
	public void init() {
		Font msgFont = Assets.storeFont("message-font", "fonts/Cookie-Regular.ttf", 40, Font.PLAIN);

		board = new Board(new BoardModel(), 600, 600);
		board.hCenter(getWidth());
		board.tf.setY(50);

		whitePlayer = new InteractivePlayer(app, board, WHITE);
		blackPlayer = new InteractivePlayer(app, board, BLACK);

		whiteStonesToPlaceCounter = new StonesCounter(WHITE, () -> NUM_STONES - whitePlayer.getStonesPlaced());
		whiteStonesToPlaceCounter.tf.moveTo(40, getHeight() - 50);
		whiteStonesToPlaceCounter.init();

		blackStonesToPlaceCounter = new StonesCounter(BLACK, () -> NUM_STONES - blackPlayer.getStonesPlaced());
		blackStonesToPlaceCounter.tf.moveTo(getWidth() - 100, getHeight() - 50);
		blackStonesToPlaceCounter.init();

		messageDisplay = new ScrollingText();
		messageDisplay.setColor(Color.BLUE);
		messageDisplay.setFont(msgFont);
		messageDisplay.tf.moveTo(0, getHeight() - 90);

		control.setLogger(LOG);
		control.init();
	}

	@Override
	public void update() {
		readInput();
		control.update();
	}

	private void readInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			control.init();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			assistantOn = !assistantOn;
			LOG.info(app.msg(assistantOn ? "assistant_on" : "assistant_off"));
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			board.togglePositionNumbers();
		}
	}

	private void displayMessage(String key, Object... args) {
		messageDisplay.setText(app.msg(key, args));
	}

	private void newMove(Player player) {
		move = new Move(board, player::supplyMoveStart, player::supplyMoveEnd, this::supplyMoveVelocity, player::canJump);
	}

	private void setCurrentPlayer(Player player) {
		currentPlayer = player;
		otherPlayer = player == whitePlayer ? blackPlayer : whitePlayer;
	}

	private void assignPlacingTo(Player player) {
		setCurrentPlayer(player);
		whiteStonesToPlaceCounter.setSelected(currentPlayer.getColor() == WHITE);
		blackStonesToPlaceCounter.setSelected(currentPlayer.getColor() == BLACK);
		displayMessage(currentPlayer.getColor() == WHITE ? "white_must_place" : "black_must_place");
	}

	private void assignMovingTo(Player player) {
		setCurrentPlayer(player);
		newMove(player);
		displayMessage(currentPlayer.getColor() == WHITE ? "white_must_move" : "black_must_move");
	}

	private void setWinner(Player player) {
		winner = player;
		if (winner != null) {
			displayMessage(winner.getColor() == WHITE ? "white_wins" : "black_wins");
		}
	}

	private boolean isGameOver() {
		StoneColor color = currentPlayer.getColor();
		return board.getModel().stoneCount(color) < 3 || (!currentPlayer.canJump() && board.getModel().isTrapped(color));
	}

	private Vector2 supplyMoveVelocity() {
		int from = move.getFrom().getAsInt(), to = move.getTo().getAsInt();
		float speed = dist(board.centerPoint(from), board.centerPoint(to))
				/ app.pulse.secToTicks(app.settings.getAsFloat("seconds-per-move"));
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
		whiteStonesToPlaceCounter.draw(g);
		blackStonesToPlaceCounter.draw(g);
		if (control.mustRemoveStoneOfOpponent) {
			board.markRemovableStones(g, otherPlayer.getColor());
		} else if (assistantOn) {
			board.markPositionsClosingMill(g, currentPlayer.getColor(), Color.GREEN);
			board.markPositionsOpeningTwoMills(g, currentPlayer.getColor(), Color.YELLOW);
			board.markPositionsClosingMill(g, otherPlayer.getColor(), Color.RED);
		}
	}

	private void drawMovingInfo(Graphics2D g) {
		if (move.getFrom().isPresent()) {
			board.markPosition(g, move.getFrom().getAsInt(), Color.ORANGE);
		} else {
			board.markPossibleMoveStarts(g, currentPlayer.getColor(), currentPlayer.canJump());
			if (assistantOn) {
				board.markPositionFixingOpponent(g, currentPlayer.getColor(), otherPlayer.getColor(), Color.RED);
			}
		}
		if (control.mustRemoveStoneOfOpponent) {
			board.markRemovableStones(g, otherPlayer.getColor());
		}
	}
}