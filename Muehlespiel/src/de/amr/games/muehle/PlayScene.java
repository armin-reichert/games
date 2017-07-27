package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.Direction.EAST;
import static de.amr.games.muehle.Direction.NORTH;
import static de.amr.games.muehle.Direction.SOUTH;
import static de.amr.games.muehle.Direction.WEST;
import static de.amr.games.muehle.GamePhase.GAME_INITIALIZED;
import static de.amr.games.muehle.GamePhase.GAME_MOVING_STONES;
import static de.amr.games.muehle.GamePhase.GAME_PLACING_STONES;
import static de.amr.games.muehle.PlayScene.MoveState.MOVE_GET_END;
import static de.amr.games.muehle.PlayScene.MoveState.MOVE_GET_START;
import static de.amr.games.muehle.PlayScene.MoveState.MOVE_IS_RUNNING;
import static de.amr.games.muehle.StoneColor.BLACK;
import static de.amr.games.muehle.StoneColor.WHITE;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.mouse.Mouse;

public class PlayScene extends Scene<MillApp> {

	private final int NUM_STONES = 6;
	private final float MOVE_SPEED = 2.5f;

	private final Mouse mouse;

	private Board board;
	private ScrollingText startText;

	private StoneColor turn;
	private int numWhiteStonesSet;
	private int numBlackStonesSet;
	private boolean mustRemoveOppositeStone;

	private Move move;

	private final PlayControl playControl = new PlayControl();

	private class PlayControl extends StateMachine<GamePhase, String> {

		public PlayControl() {
			super("Mühlespiel Steuerung", GamePhase.class, GamePhase.GAME_INITIALIZED);

			// INITIALIZED

			state(GAME_INITIALIZED).entry = s -> resetGame();

			state(GAME_INITIALIZED).exit = s -> {
				startText.visibility = () -> false;
			};

			change(GAME_INITIALIZED, GAME_PLACING_STONES, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(GAME_PLACING_STONES).entry = s -> {
				turn = WHITE;
				numWhiteStonesSet = 0;
				numBlackStonesSet = 0;
				mustRemoveOppositeStone = false;
			};

			state(GAME_PLACING_STONES).update = s -> {
				if (mustRemoveOppositeStone) {
					removeStone();
				} else {
					placeStone();
				}
			};

			change(GAME_PLACING_STONES, GAME_MOVING_STONES,
					() -> numBlackStonesSet == NUM_STONES && !mustRemoveOppositeStone);

			// MOVING

			state(GAME_MOVING_STONES).entry = s -> {
				moveControl.init();
				moveControl.setLogger(Application.LOG);
			};

			state(GAME_MOVING_STONES).update = s -> {
				moveControl.update();
			};
		}
	}

	private final MoveControl moveControl = new MoveControl();

	public enum MoveState {
		MOVE_GET_START, MOVE_GET_END, MOVE_IS_RUNNING
	};

	private class MoveControl extends StateMachine<MoveState, String> {

		public MoveControl() {
			super("Move Control", MoveState.class, MOVE_GET_START);

			state(MOVE_GET_START).entry = s -> move = new Move(board, MOVE_SPEED);

			state(MOVE_GET_START).update = s -> setMoveStartPosition();

			change(MOVE_GET_START, MOVE_GET_END, () -> move.getFrom() != -1);

			state(MOVE_GET_END).entry = s -> move.setDirection(null);

			state(MOVE_GET_END).update = s -> setMoveDirection();

			change(MOVE_GET_END, MOVE_IS_RUNNING, () -> move.getTo() != -1);

			state(MOVE_IS_RUNNING).update = s -> move.execute();

			change(MOVE_IS_RUNNING, MOVE_GET_START, () -> move.isEndPositionReached());

			state(MOVE_IS_RUNNING).exit = s -> {
				board.moveStone(move.getFrom(), move.getTo());
				nextTurn();
			};
		}
	}

	public PlayScene(MillApp app) {
		super(app);
		setBgColor(Color.WHITE);
		mouse = new Mouse();
		app.getShell().getCanvas().addMouseListener(mouse);
	}

	private int findBoardPosition(int x, int y) {
		int brettX = Math.abs(Math.round(x - board.tf.getX()));
		int brettY = Math.abs(Math.round(y - board.tf.getY()));
		return board.findNearestPosition(brettX, brettY, board.getWidth() / 18);
	}

	@Override
	public void init() {
		playControl.setLogger(Application.LOG);
		playControl.init();
	}

	@Override
	public void update() {
		mouse.poll();
		playControl.update();
	}

	private void resetGame() {
		board = new Board(600, 600);
		board.center(getWidth(), getHeight());
		app.entities.add(board);

		startText = new ScrollingText();
		startText.setColor(Color.BLACK);
		startText.setFont(new Font("Sans", Font.PLAIN, 20));
		startText.setText("Drücke SPACE zum Start");
		startText.tf.moveTo(0, getHeight() - 40);
		app.entities.add(startText);
	}

	private void nextTurn() {
		turn = turn == WHITE ? BLACK : WHITE;
	}

	// Placing

	private void placeStone() {
		int gesetzt = placeStoneInteractively();
		if (gesetzt != -1) {
			StoneColor meineFarbe = turn;
			if (board.isInsideMill(gesetzt, meineFarbe)) {
				// Mühle wurde geschlossen
				mustRemoveOppositeStone = true;
			}
			nextTurn();
		}
	}

	private int placeStoneInteractively() {
		if (!mouse.clicked())
			return -1;

		int placedAt = -1;
		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p != -1 && board.getStoneAt(p) == null) {
			// An Position p Stein setzen:
			placedAt = p;
			if (turn == WHITE) {
				board.placeStoneAt(placedAt, WHITE);
				numWhiteStonesSet += 1;
			} else {
				board.placeStoneAt(placedAt, BLACK);
				numBlackStonesSet += 1;
			}
		}
		return placedAt;
	}

	private void removeStone() {
		if (!mouse.clicked())
			return;

		StoneColor color = turn;
		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		if (board.getStoneAt(p) == null) {
			LOG.info("Kein Stein an Klickposition");
			return;
		}
		if (board.getStoneAt(p).getColor() != color) {
			LOG.info("Stein an Klickposition besitzt die falsche Farbe");
			return;
		}
		if (board.isInsideMill(p, color) && !board.allStonesOfColorInsideMills(color)) {
			LOG.info("Stein darf nicht aus Mühle entfernt werden, weil anderer Stein außerhalb Mühle existiert");
			return;
		}
		board.removeStoneAt(p);
		mustRemoveOppositeStone = false;
	}

	// Moving

	private void setMoveStartPosition() {
		if (!mouse.clicked())
			return;

		int from = findBoardPosition(mouse.getX(), mouse.getY());
		if (from == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		Stone stone = board.getStoneAt(from);
		if (stone == null) {
			LOG.info("Kein Stein an Klickposition gefunden");
			return;
		}
		if (turn != stone.getColor()) {
			LOG.info(stone.getColor() + " ist nicht am Zug");
			return;
		}
		if (!board.hasEmptyNeighbor(from)) {
			LOG.info("Stein an dieser Position kann nicht ziehen");
			return;
		}
		move.setFrom(from);
	}

	private void setMoveDirection() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_UP)) {
			move.setDirection(NORTH);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_RIGHT)) {
			move.setDirection(EAST);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
			move.setDirection(SOUTH);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_LEFT)) {
			move.setDirection(WEST);
		}
	}

	// Drawing

	@Override
	public void draw(Graphics2D g) {
		g.setColor(getBgColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		board.draw(g);
		drawStatus(g);
	}

	private void drawStatus(Graphics2D g) {
		if (playControl.is(GAME_INITIALIZED)) {
			startText.hCenter(getWidth());
			startText.draw(g);
			return;
		}
		if (playControl.is(GAME_PLACING_STONES)) {
			String text = format("Setzen: Weiß hat %d Stein(e) übrig, Schwarz hat %d Stein(e) übrig",
					NUM_STONES - numWhiteStonesSet, NUM_STONES - numBlackStonesSet);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
		if (playControl.is(GAME_MOVING_STONES)) {
			if (move.getFrom() != -1) {
				Point p = board.computeCenterPoint(move.getFrom());
				g.setColor(Color.GREEN);
				g.fillOval(Math.round(board.tf.getX() + p.x) - 5, Math.round(board.tf.getY() + p.y) - 5, 10, 10);
			}
			if (move.getTo() != -1) {
				Point p = board.computeCenterPoint(move.getTo());
				g.setColor(Color.RED);
				g.fillOval(Math.round(board.tf.getX() + p.x) - 5, Math.round(board.tf.getY() + p.y) - 5, 10, 10);
			}
			String text = (turn == WHITE ? "Weiß" : "Schwarz") + " am Zug";
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
	}
}