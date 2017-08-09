package de.amr.games.muehle.play;

import java.util.OptionalInt;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

/**
 * A slightly smart random player.
 * 
 * @author Armin Reichert
 */
public class RandomPlayer extends AbstractPlayer {

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

}