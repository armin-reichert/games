package de.amr.games.muehle.play;

import java.util.OptionalInt;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

public class SmartPlayer extends AbstractPlayer {

	public SmartPlayer(MillApp app, Board board, StoneColor color) {
		super(app, board, color);
	}

	@Override
	public OptionalInt supplyPlacePosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor otherColor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OptionalInt supplyMoveStartPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OptionalInt supplyMoveEndPosition(int from) {
		// TODO Auto-generated method stub
		return null;
	}

}
