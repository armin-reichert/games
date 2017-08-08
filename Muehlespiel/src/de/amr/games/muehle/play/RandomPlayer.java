package de.amr.games.muehle.play;

import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.BoardModel;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

public class RandomPlayer implements Player {

	private final Random rand = new Random();

	private final MillApp app;
	private final Board board;
	private final BoardModel model;
	private final StoneColor color;
	private int stonesPlaced;

	public RandomPlayer(MillApp app, Board board, StoneColor color) {
		this.app = app;
		this.board = board;
		this.model = board.getModel();
		this.color = color;
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
	public boolean canJump() {
		return model.stoneCount(color) == 3;
	}

	@Override
	public OptionalInt tryToPlaceStone() {
		OptionalInt optPos = random(positions().filter(model::isEmptyPosition));
		optPos.ifPresent(p -> {
			board.putStoneAt(p, color);
			stonesPlaced += 1;
		});
		return optPos;
	}

	@Override
	public OptionalInt tryToRemoveStone(StoneColor opponentColor) {
		OptionalInt optPos = random(positions().filter(p -> model.getStoneAt(p) == opponentColor));
		optPos.ifPresent(p -> board.removeStoneAt(p));
		return optPos;
	}

	@Override
	public OptionalInt supplyMoveStart() {
		return random(positions().filter(p -> model.getStoneAt(p) == color).filter(model::hasEmptyNeighbor));
	}

	@Override
	public OptionalInt supplyMoveEnd(int from) {
		return canJump() ? random(positions().filter(model::isEmptyPosition))
				: random(model.neighbors(from).filter(model::isEmptyPosition));
	}

	private OptionalInt random(IntStream stream) {
		int[] elements = stream.toArray();
		return elements.length == 0 ? OptionalInt.empty() : OptionalInt.of(elements[rand.nextInt(elements.length)]);
	}

	private IntStream positions() {
		return model.positions();
	}
}
