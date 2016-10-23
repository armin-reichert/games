package de.amr.games.pacman.data;

import java.util.Optional;

import de.amr.easy.grid.impl.Grid;

public class Board {

	public static final int Rows = 36;
	public static final int Cols = 28;

	public static final float PacManHomeRow = 26;
	public static final float PacManHomeCol = 13.5f;

	public static final float BlinkyHomeRow = 14;
	public static final float BlinkyHomeCol = 13.5f;

	public static final float InkyHomeRow = 17.5f;
	public static final float InkyHomeCol = 11.5f;

	public static final float PinkyHomeRow = 17.5f;
	public static final float PinkyHomeCol = 13.5f;

	public static final float ClydeHomeRow = 17.5f;
	public static final float ClydeHomeCol = 15.5f;

	public static final float BonusRow = 19.5f;
	public static final float BonusCol = 13;

	public static final char Empty = ' ';
	public static final char Wall = '#';
	public static final char Door = 'D';
	public static final char GhostHouse = 'G';
	public static final char Pellet = '.';
	public static final char Energizer = 'O';
	public static final char Tunnel = 'T';
	public static final char Wormhole = 'W';

	public Grid<Character> grid;
	private final String[] rows;

	public Board(String boardData) {
		rows = boardData.split("\n");
		grid = new Grid<>(Cols, Rows, Empty, false);
		init();
	}

	public void init() {
		grid.vertexStream()
				.forEach(cell -> grid.set(cell, rows[grid.row(cell)].charAt(grid.col(cell))));
	}

	public boolean isTileValid(Tile tile) {
		int row = tile.getRow(), col = tile.getCol();
		return row >= 0 && row < Rows && col >= 0 && col < Cols;
	}

	public void setContent(Tile tile, char data) {
		Integer cell = grid.cell(tile.getCol(), tile.getRow());
		grid.set(cell, data);
	}

	public boolean has(char data, Tile tile) {
		return has(data, tile.getRow(), tile.getCol());
	}
	
	public boolean has(char data, int row, int col) {
		Integer cell = grid.cell(col, row);
		return data == grid.get(cell);
	}

	public Optional<Tile> checkContent(Tile tile, char data) {
		return has(data, tile.getRow(), tile.getCol()) ? Optional.of(tile) : Optional.empty();
	}

	public long count(char data) {
		return grid.vertexStream().filter(cell -> data == grid.get(cell)).count();
	}
}