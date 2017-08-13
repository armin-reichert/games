package de.amr.games.muehle.player;

import java.util.OptionalInt;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;

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
	public Move supplyMove() {
		Move move = new Move();
		randomElement(model.positions(color)).ifPresent(from -> {
			move.from = from;
			OptionalInt optTo = canJump() ? randomElement(model.positions().filter(model::isEmptyPosition))
					: randomElement(model.neighbors(from).filter(model::isEmptyPosition));
			optTo.ifPresent(to -> move.to = to);
		});
		return move;
	}

}