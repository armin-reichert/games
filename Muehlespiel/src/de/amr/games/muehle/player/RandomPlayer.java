package de.amr.games.muehle.player;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;

/**
 * A slightly smart random player.
 * 
 * @author Armin Reichert
 */
public class RandomPlayer implements Player {

	private final Board board;
	private final StoneColor color;

	public RandomPlayer(Board board, StoneColor color) {
		this.board = board;
		this.color = color;
	}

	@Override
	public StoneColor getColor() {
		return color;
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
	public Move supplyMove(boolean canJump) {
		Move move = new Move();
		randomElement(board.positions(color)).ifPresent(from -> {
			move.from = from;
			OptionalInt optTo = canJump ? randomElement(board.positions().filter(board::isEmptyPosition))
					: randomElement(board.neighbors(from).filter(board::isEmptyPosition));
			optTo.ifPresent(to -> move.to = to);
		});
		return move;
	}

}