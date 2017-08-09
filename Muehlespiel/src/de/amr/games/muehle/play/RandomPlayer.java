package de.amr.games.muehle.play;

import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

/**
 * A slightly smart random player.
 * 
 * @author Armin Reichert
 */
public class RandomPlayer extends AbstractPlayer {

	private final Random rand = new Random();

	public RandomPlayer(MillApp app, Board board, StoneColor color) {
		super(app, board, color);
	}

	@Override
	public OptionalInt supplyPlacePosition() {
		return randomElement(model.positions().filter(model::isEmptyPosition));
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor otherColor) {
		return randomElement(model.positions().filter(p -> model.getStoneAt(p) == otherColor));
	}

	@Override
	public OptionalInt supplyMoveStartPosition() {
		return randomElement(model.positions().filter(p -> model.getStoneAt(p) == color).filter(model::hasEmptyNeighbor));
	}

	@Override
	public OptionalInt supplyMoveEndPosition(int from) {
		return canJump() ? randomElement(model.positions().filter(model::isEmptyPosition))
				: randomElement(model.neighbors(from).filter(model::isEmptyPosition));
	}

	private OptionalInt randomElement(IntStream stream) {
		int[] array = stream.toArray();
		return array.length == 0 ? OptionalInt.empty() : OptionalInt.of(array[rand.nextInt(array.length)]);
	}
}