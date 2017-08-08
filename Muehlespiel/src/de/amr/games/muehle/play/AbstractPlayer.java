package de.amr.games.muehle.play;

import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.BoardModel;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

public abstract class AbstractPlayer implements Player {

	protected final MillApp app;
	protected final Board board;
	protected final BoardModel model;
	protected final StoneColor color;
	protected int stonesPlaced;

	public AbstractPlayer(MillApp app, Board board, StoneColor color) {
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
	public void stonePlaced() {
		stonesPlaced += 1;
	}

	@Override
	public boolean canJump() {
		return model.stoneCount(color) == 3;
	}
}
