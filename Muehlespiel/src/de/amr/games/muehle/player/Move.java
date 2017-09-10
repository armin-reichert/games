package de.amr.games.muehle.player;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;

/**
 * A move or jump.
 * 
 * @author Armin Reichert
 */
public class Move {

	private int from;
	private int to;

	public Move() {
		this.from = -1;
		this.to = -1;
	}

	public Move(int from, int to) {
		setFrom(from);
		setTo(to);
	}

	public OptionalInt getFrom() {
		return from == -1 ? OptionalInt.empty() : OptionalInt.of(from);
	}

	public void clearFrom() {
		from = -1;
	}

	public void setFrom(int from) {
		Board.checkPosition(from);
		this.from = from;
	}

	public OptionalInt getTo() {
		return to == -1 ? OptionalInt.empty() : OptionalInt.of(to);
	}

	public void clearTo() {
		to = -1;
	}

	public void setTo(int to) {
		Board.checkPosition(to);
		this.to = to;
	}

	public boolean isCompletelySpecified() {
		return from != -1 && to != -1;
	}

	@Override
	public String toString() {
		return String.format("Move(%d -> %d)", from, to);
	}

}