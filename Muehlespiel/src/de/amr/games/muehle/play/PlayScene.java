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
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.TextArea;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.Transition;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.player.impl.InteractivePlayer;
import de.amr.games.muehle.player.impl.Peter;
import de.amr.games.muehle.player.impl.RandomPlayer;
import de.amr.games.muehle.player.impl.Zwick;
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.Stone;

/**
 * The play scene of the mill game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	static final int NUM_STONES = 9;
	static final float MOVE_TIME_SEC = 0.75f;
	static final float PLACING_TIME_SEC = 1.5f;
	static final float REMOVAL_TIME_SEC = 1.5f;

	// Control
	private final GameControl control = new GameControl();
	private MoveControl moveControl;

	private Board board = new Board();
	private Player[] players = new Player[2];
	private int[] stonesPlaced = new int[2];
	private int turn;

	// UI
	private BoardUI boardUI;
	private AlienAssistant assistant;
	private TextArea messageArea;

	/** Finite-state-machine for game control. */
	class GameControl extends StateMachine<GamePhase, GameEvent> {

		@Override
		public void update() {
			super.update();
			assistant.update();
		}

		private int placedAt;
		private StoneColor placedColor;
		private int removedAt;

		GameControl() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTING);

			// STARTING

			state(STARTING).entry = this::reset;

			change(STARTING, PLACING);

			// PLACING

			state(PLACING).update = this::tryToPlaceStone;

			changeOnInput(STONE_PLACED, PLACING, PLACING_REMOVING, this::placingClosedMill, this::onPlacingClosedMill);

			changeOnInput(STONE_PLACED, PLACING, PLACING, this::switchPlacing);

			change(PLACING, MOVING, this::allStonesPlaced, this::switchMoving);

			// PLACING_REMOVING_STONE

			state(PLACING_REMOVING).entry = this::startRemoving;

			state(PLACING_REMOVING).update = this::tryToRemoveStone;

			change(PLACING_REMOVING, PLACING, this::stoneRemoved, this::switchPlacing);

			// MOVING

			state(MOVING).update = this::moveStone;

			change(MOVING, MOVING_REMOVING, this::moveClosedMill);

			change(MOVING, MOVING, this::isMoveFinished, this::switchMoving);

			change(MOVING, GAME_OVER, this::isGameOver);

			// MOVING_REMOVING_STONE

			state(MOVING_REMOVING).entry = this::startRemoving;

			state(MOVING_REMOVING).update = this::tryToRemoveStone;

			change(MOVING_REMOVING, MOVING, this::stoneRemoved, this::switchMoving);

			// GAME_OVER

			state(GAME_OVER).entry = s -> {
				announceWin(1 - turn);
				pause(app.pulse.secToTicks(3));
			};

			change(GAME_OVER, STARTING,
					() -> !isInteractive(0) && !isInteractive(1) || Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}

		void reset(State state) {
			boardUI.clear();
			stonesPlaced[0] = stonesPlaced[1] = 0;
			turnPlacingTo(0);
		}

		void announceWin(int i) {
			showMessage("wins", players[i].getName());
			assistant.tellWin();
		}

		boolean allStonesPlaced() {
			return stonesPlaced[1] == NUM_STONES;
		}

		boolean isInteractive(int i) {
			return players[i] instanceof InteractivePlayer;
		}

		boolean isGameOver() {
			return board.stoneCount(players[turn].getColor()) < 3 || (!canJump(turn) && isTrapped(turn));
		}

		boolean canJump(int i) {
			return players[i].canJump();
		}

		boolean isTrapped(int i) {
			return board.isTrapped(players[i].getColor());
		}

		void turnPlacingTo(int i) {
			turn = i;
			showMessage("must_place", players[turn].getName());
		}

		void switchPlacing(Transition<GamePhase, GameEvent> t) {
			turnPlacingTo(1 - turn);
			if (!isInteractive(turn)) {
				pause(app.pulse.secToTicks(PLACING_TIME_SEC));
			}
		}

		void onPlacingClosedMill(Transition<GamePhase, GameEvent> t) {
			assistant.tellMillClosed();
		}

		void turnMovingTo(int i) {
			turn = i;
			moveControl = new MoveControl(players[turn], boardUI, this::computeMoveSpeed);
			moveControl.init();
			showMessage("must_move", players[turn].getName());
		}

		float computeMoveSpeed(int from, int to) {
			return Vector2.dist(boardUI.centerPoint(from), boardUI.centerPoint(to)) / app.pulse.secToTicks(MOVE_TIME_SEC);
		}

		void switchMoving(Transition<GamePhase, GameEvent> t) {
			turnMovingTo(1 - turn);
		}

		void tryToPlaceStone(State state) {
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
					boardUI.removeStoneAt(removalPosition);
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
			showMessage("must_take", players[turn].getName(), players[1 - turn].getName());
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

	} // FSM

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

	GameControl getControl() {
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
		boardUI = new BoardUI(board, 600, 600);
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

		players[0] = whitePlayers[0];
		players[1] = blackPlayers[3];

		Font msgFont = Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36);
		messageArea = new TextArea();
		messageArea.setColor(Color.BLUE);
		messageArea.setFont(msgFont);

		assistant = new AlienAssistant(this);
		assistant.setPlayers(players[0], players[1]);
		assistant.init();

		// Layout
		boardUI.hCenter(getWidth());
		boardUI.tf.setY(50);
		messageArea.tf.moveTo(0, getHeight() - 90);
		assistant.moveHome();

		// control.setLogger(LOG);
		control.init();
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
			drawStoneCount(g, NUM_STONES - stonesPlaced[0], WHITE, boardUI.stoneRadius(), turn == 0, 40, getHeight() - 50);
			drawStoneCount(g, NUM_STONES - stonesPlaced[1], BLACK, boardUI.stoneRadius(), turn == 1, getWidth() - 100,
					getHeight() - 50);
		}
		if (control.is(PLACING_REMOVING, MOVING_REMOVING) && control.isInteractive(0) || control.isInteractive(1)) {
			boardUI.markRemovableStones(g, getOpponentPlayer().getColor());
		}
	}

	void drawStoneCount(Graphics2D g, int numStones, StoneColor color, int radius, boolean selected, int x, int y) {
		Stone stone = new Stone(color, radius);
		g.translate(x, y);
		int inset = 8;
		for (int i = numStones - 1; i >= 0; --i) {
			g.translate(i * inset, -i * inset);
			stone.draw(g);
			g.translate(-i * inset, i * inset);
		}
		if (numStones > 1) {
			g.setColor(selected ? Color.RED : Color.DARK_GRAY);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 2 * radius));
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(String.valueOf(numStones), 2 * radius, radius);
		}
		g.translate(-x, -y);
	}
}