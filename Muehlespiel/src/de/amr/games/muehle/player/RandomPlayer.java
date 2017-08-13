package de.amr.games.muehle.player;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;

/**
 * A slightly smart random player.
 * 
 * @author Armin Reichert
 */
public class RandomPlayer extends AbstractPlayer {

	public RandomPlayer(Board board, StoneColor color) {
		super(board, color);
	}

	@Override
	public OptionalInt supplyPlacePosition() {
		return randomElement(board.positions().filter(board::isEmptyPosition));
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor otherColor) {
		return randomElement(board.positions(otherColor));
	}

	@Override
	public Move supplyMove() {
		Move move = new Move();
		randomElement(board.positions(color)).ifPresent(from -> {
			move.from = from;
			OptionalInt optTo = canJump() ? randomElement(board.positions().filter(board::isEmptyPosition))
					: randomElement(board.neighbors(from).filter(board::isEmptyPosition));
			optTo.ifPresent(to -> move.to = to);
		});
		return move;
	}

}