package de.amr.games.muehle.player;

import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;

import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public abstract class AbstractPlayer implements Player {

	protected final Random rand = new Random();
	protected final Board board;
	protected final StoneColor color;
	protected final StoneColor otherColor;
	protected int numStonesPlaced;

	public AbstractPlayer(Board board, StoneColor color) {
		this.board = board;
		this.color = color;
		this.otherColor = (color == WHITE ? BLACK : WHITE);
	}

	@Override
	public void init() {
		numStonesPlaced = 0;
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	@Override
	public int getNumStonesPlaced() {
		return numStonesPlaced;
	}

	@Override
	public void stonePlaced() {
		numStonesPlaced += 1;
	}

	@Override
	public boolean canJump() {
		return board.stoneCount(color) == 3;
	}

	@Override
	public void clearMove() {
	}

	/**
	 * @param stream
	 *          stream of integers
	 * @return random element from given stream
	 */
	protected OptionalInt randomElement(IntStream stream) {
		int[] elements = stream.toArray();
		return elements.length == 0 ? OptionalInt.empty() : OptionalInt.of(elements[rand.nextInt(elements.length)]);
	}

	protected String getName() {
		return getClass().getSimpleName() + "(" + (color == WHITE ? "Weiß" : "Schwarz") + ")";
	}
}
