package de.amr.games.muehle.player;

import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;

import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public abstract class AbstractPlayer implements Player {

	protected final Random rand = new Random();
	protected final MillApp app;
	protected final Board model;
	protected final StoneColor color;
	protected final StoneColor otherColor;
	protected int stonesPlaced;

	public AbstractPlayer(MillApp app, Board board, StoneColor color) {
		this.app = app;
		this.model = board;
		this.color = color;
		this.otherColor = (color == WHITE ? BLACK : WHITE);
	}

	@Override
	public void init() {
		stonesPlaced = 0;
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	@Override
	public int getStonesPlaced() {
		return stonesPlaced;
	}

	@Override
	public void stonePlaced() {
		stonesPlaced += 1;
	}

	@Override
	public boolean canJump() {
		return model.stoneCount(color) == 3;
	}

	@Override
	public void clearMove() {
	}

	/**
	 * 
	 * @param stream
	 *          stream of ints
	 * @return random element from given stream
	 */
	protected OptionalInt randomElement(IntStream stream) {
		int[] array = stream.toArray();
		return array.length == 0 ? OptionalInt.empty() : OptionalInt.of(array[rand.nextInt(array.length)]);
	}

	protected String getName() {
		return color == WHITE ? "Weiß" : "Schwarz";
	}
}
