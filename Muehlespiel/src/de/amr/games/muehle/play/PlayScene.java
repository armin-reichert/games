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
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.player.impl.InteractivePlayer;
import de.amr.games.muehle.player.impl.Peter;
import de.amr.games.muehle.player.impl.RandomPlayer;
import de.amr.games.muehle.player.impl.Zwick;
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.StoneCounter;

/**
 * The play scene of the mill game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	static final int NUM_STONES = 9;
	static final float MOVE_TIME_SEC = .5f;

	private FSM control = new FSM();
	private Board board = new Board();
	private Player[] players = new Player[2];
	private int[] stonesPlaced = new int[2];
	private StoneCounter[] stoneCounters = new StoneCounter[2];

	private int turn;

	// UI
	private BoardUI boardUI;
	private AlienAssistant assistant;
	private MoveControl moveControl;
	private TextArea messageArea;

	/** Finite-state-machine for game control. */
	class FSM extends StateMachine<GamePhase, GameEvent> {

		@Override
		public void update() {
			super.update();
			assistant.update();
		}

		private int placedAt;
		private StoneColor placedColor;
		private int removedAt;

		FSM() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTING);
			// Define the states and transitions:

			// STARTING

			state(STARTING).entry = s -> reset();

			change(STARTING, PLACING);

			// PLACING

			state(PLACING).update = s -> tryToPlaceStone();

			changeOnInput(STONE_PLACED, PLACING, PLACING_REMOVING, this::placingClosedMill, t -> assistant.tellMillClosed());

			changeOnInput(STONE_PLACED, PLACING, PLACING, t -> switchPlacing());

			change(PLACING, MOVING, () -> stonesPlaced[1] == NUM_STONES, t -> switchMoving());

			// PLACING_REMOVING_STONE

			state(PLACING_REMOVING).entry = s -> {
				removedAt = -1;
				showMessage("must_take", players[turn].getName());
			};

			state(PLACING_REMOVING).update = s -> tryToRemoveStone();

			change(PLACING_REMOVING, PLACING, () -> removedAt != -1, t -> switchPlacing());

			// MOVING

			state(MOVING).update = s -> runStoneMove();

			change(MOVING, MOVING_REMOVING, this::movingClosedMill);

			change(MOVING, MOVING, this::isMoveFinished, t -> switchMoving());

			change(MOVING, GAME_OVER, this::isGameOver);

			// MOVING_REMOVING_STONE

			state(MOVING_REMOVING).entry = s -> {
				removedAt = -1;
				showMessage("must_take", players[turn].getName());
			};

			state(MOVING_REMOVING).update = s -> tryToRemoveStone();

			change(MOVING_REMOVING, MOVING, () -> removedAt != -1, t -> switchMoving());

			// GAME_OVER

			state(GAME_OVER).entry = s -> {
				win(1 - turn);
				pause(app.pulse.secToTicks(3));
			};

			change(GAME_OVER, STARTING,
					() -> !isInteractive(players[0]) && !isInteractive(players[1]) || Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}

		void reset() {
			boardUI.clear();
			stonesPlaced[0] = stonesPlaced[1] = 0;
			turnPlacingTo(0);
		}

		void win(int winner) {
			showMessage("wins", players[winner].getName());
			assistant.tellWin();
		}

		boolean isInteractive(Player player) {
			return player instanceof InteractivePlayer;
		}

		boolean isGameOver() {
			return board.stoneCount(players[turn].getColor()) < 3 || (!canJump(turn) && isTrapped(turn));
		}

		boolean canJump(int playerNumber) {
			return players[playerNumber].canJump();
		}

		boolean isTrapped(int playerNumber) {
			return board.isTrapped(players[playerNumber].getColor());
		}

		void turnPlacingTo(int playerNumber) {
			turn = playerNumber;
			showMessage("must_place", players[turn].getName());
			pause(players[turn] instanceof InteractivePlayer ? 0 : app.pulse.secToTicks(1));
		}

		void switchPlacing() {
			turnPlacingTo(1 - turn);
		}

		void turnMovingTo(int playerNumber) {
			turn = playerNumber;
			moveControl.controlPlayer(players[turn]);
			showMessage("must_move", players[turn].getName());
		}

		void switchMoving() {
			turnMovingTo(1 - turn);
		}

		void tryToPlaceStone() {
			assistant.givePlacingHint();
			players[turn].supplyPlacingPosition().ifPresent(placePosition -> {
				if (board.hasStoneAt(placePosition)) {
					LOG.info(Messages.text("stone_at_position", placePosition));
				} else {
					StoneColor colorInTurn = players[turn].getColor();
					boardUI.putStoneAt(placePosition, colorInTurn);
					stonesPlaced[turn] += 1;
					placedAt = placePosition;
					placedColor = colorInTurn;
					addInput(STONE_PLACED);
					assistant.moveHome();
				}
			});
		}

		void tryToRemoveStone() {
			players[turn].supplyRemovalPosition().ifPresent(removalPosition -> {
				StoneColor colorToRemove = players[turn].getColor().other();
				if (board.isEmptyPosition(removalPosition)) {
					LOG.info(Messages.text("stone_at_position_not_existing", removalPosition));
				} else if (board.getStoneAt(removalPosition).get() != colorToRemove) {
					LOG.info(Messages.text("stone_at_position_wrong_color", removalPosition));
				} else if (board.inMill(removalPosition, colorToRemove) && !board.allStonesInMills(colorToRemove)) {
					LOG.info(Messages.text("stone_cannot_be_removed_from_mill"));
				} else {
					boardUI.removeStoneAt(removalPosition);
					removedAt = removalPosition;
					LOG.info(Messages.text("removed_stone_at_position", players[turn].getName(), removalPosition));
				}
			});
		}

		void runStoneMove() {
			moveControl.update();
		}

		boolean isMoveFinished() {
			return moveControl.is(MoveState.FINISHED);
		}

		boolean placingClosedMill() {
			return board.inMill(placedAt, placedColor);
		}

		boolean movingClosedMill() {
			return moveControl.is(MoveState.FINISHED)
					&& board.inMill(moveControl.getMove().get().to, players[turn].getColor());
		}
	};

	public PlayScene(MillApp app) {
		super(app);
	}

	Player getPlayerInTurn() {
		return players[turn];
	}

	Player getOpponentPlayer() {
		return players[1 - turn];
	}

	BoardUI getBoardUI() {
		return boardUI;
	}

	FSM getControl() {
		return control;
	}

	MoveControl getMoveControl() {
		return moveControl;
	}

	int getTurn() {
		return turn;
	}

	@Override
	public void init() {
		createUI();

		/*@formatter:off*/
		Player[] whitePlayers = { 
				new InteractivePlayer(board, WHITE, boardUI::findPosition),
				new RandomPlayer(board, WHITE), 
				new Peter(board, WHITE),
				new Zwick(board, WHITE)
		};

		Player[] blackPlayers = { 
				new InteractivePlayer(board, BLACK, boardUI::findPosition),
				new RandomPlayer(board, BLACK), 
				new Peter(board, BLACK),
				new Zwick(board, BLACK)
		};
		/*@formatter:on*/

		players[0] = whitePlayers[2];
		players[1] = blackPlayers[3];

		assistant.setPlayers(players[0], players[1]);

		// State machines
		moveControl = new MoveControl(boardUI, this::computeMoveSpeed);
		// moveControl.setLogger(LOG);
		// control.setLogger(LOG);

		control.init();
	}

	void createUI() {
		boardUI = new BoardUI(board, 600, 600);

		stoneCounters[0] = new StoneCounter(WHITE, boardUI.stoneRadius(), () -> NUM_STONES - stonesPlaced[0],
				() -> turn == 0);

		stoneCounters[1] = new StoneCounter(BLACK, boardUI.stoneRadius(), () -> NUM_STONES - stonesPlaced[1],
				() -> turn == 1);

		Font msgFont = Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36);
		messageArea = new TextArea();
		messageArea.setColor(Color.BLUE);
		messageArea.setFont(msgFont);

		assistant = new AlienAssistant(this);
		assistant.init();

		// Layout
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);
		stoneCounters[0].tf.moveTo(40, getHeight() - 50);
		stoneCounters[1].tf.moveTo(getWidth() - 100, getHeight() - 50);
		messageArea.tf.moveTo(0, getHeight() - 90);
		assistant.moveHome();
	}

	@Override
	public void update() {
		readInput();
		control.update();
	}

	void readInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_CONTROL, KeyEvent.VK_N)) {
			control.init();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			assistant.toggle();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			assistant.setEnabled(true);
			assistant.setAssistanceLevel(0);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			assistant.setEnabled(true);
			assistant.setAssistanceLevel(1);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			boardUI.togglePositionNumbers();
		}
	}

	float computeMoveSpeed(int from, int to) {
		return Vector2.dist(boardUI.centerPoint(from), boardUI.centerPoint(to)) / app.pulse.secToTicks(MOVE_TIME_SEC);
	}

	void showMessage(String key, Object... args) {
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
			Stream.of(stoneCounters).forEach(counter -> counter.draw(g));
		}
		if (control.is(PLACING_REMOVING, MOVING_REMOVING)) {
			boardUI.markRemovableStones(g, getOpponentPlayer().getColor());
		}
	}
}