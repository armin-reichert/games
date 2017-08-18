package de.amr.games.muehle.player.samples;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;

/**
 * A slightly smart random player.
 * 
 * @author Armin Reichert
 */
public class RandomPlayer implements Player {

	final Board board;
	final StoneColor color;

	public RandomPlayer(Board board, StoneColor color) {
		this.board = board;
		this.color = color;
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	@Override
	public OptionalInt supplyPlacingPosition() {
		return randomElement(board.positions().filter(board::isEmptyPosition));
	}

	@Override
	public OptionalInt supplyRemovalPosition() {
		return randomElement(board.positions(color.other()));
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