package de.amr.games.muehle.play;

import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

public class RandomPlayer extends AbstractPlayer {

	private final Random rand = new Random();

	public RandomPlayer(MillApp app, Board board, StoneColor color) {
		super(app, board, color);
	}

	@Override
	public OptionalInt tryToPlaceStone() {
		OptionalInt optPos = random(model.positions().filter(model::isEmptyPosition));
		optPos.ifPresent(p -> {
			board.putStoneAt(p, color);
			stonesPlaced += 1;
		});
		return optPos;
	}

	@Override
	public OptionalInt tryToRemoveStone(StoneColor opponentColor) {
		OptionalInt optPos = random(model.positions().filter(p -> model.getStoneAt(p) == opponentColor));
		optPos.ifPresent(p -> board.removeStoneAt(p));
		return optPos;
	}

	@Override
	public OptionalInt supplyMoveStart() {
		return random(model.positions().filter(p -> model.getStoneAt(p) == color).filter(model::hasEmptyNeighbor));
	}

	@Override
	public OptionalInt supplyMoveEnd(int from) {
		return canJump() ? random(model.positions().filter(model::isEmptyPosition))
				: random(model.neighbors(from).filter(model::isEmptyPosition));
	}

	private OptionalInt random(IntStream stream) {
		int[] elements = stream.toArray();
		return elements.length == 0 ? OptionalInt.empty() : OptionalInt.of(elements[rand.nextInt(elements.length)]);
	}
}