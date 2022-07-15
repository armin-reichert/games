package de.amr.schule.gameoflife;

import java.awt.Graphics2D;
import java.util.BitSet;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.view.View;

public class GameOfLifeWorld implements Lifecycle, View {

	private BitSet grid1;
	private BitSet grid2;
	private BitSet current;
	private int gridSize;
	private int cellSize;

	public GameOfLifeWorld(int gridSize, int cellSize) {
		this.gridSize = gridSize;
		this.cellSize = cellSize;
		grid1 = new BitSet(gridSize * gridSize);
		grid2 = new BitSet(gridSize * gridSize);
		current = grid1;
	}

	public void reset() {
		grid1.clear();
		grid2.clear();
		current = grid1;
	}

	public void setGridSize(int gridSize) {
		if (this.gridSize != gridSize) {
			this.gridSize = gridSize;
			grid1 = new BitSet(gridSize * gridSize);
			grid2 = new BitSet(gridSize * gridSize);
			current = grid1;
		}
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setCellSize(int cellSize) {
		this.cellSize = cellSize;
	}

	public int getCellSize() {
		return cellSize;
	}

	public void set(int row, int col) {
		current.set(row * gridSize + col);
	}

	public void unset(int row, int col) {
		current.set(row * gridSize + col, false);
	}

	public boolean isSet(int row, int col) {
		return current.get(row * gridSize + col);
	}

	private void set(BitSet bs, int row, int col, boolean bit) {
		bs.set(row * gridSize + col, bit);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		BitSet next = current == grid1 ? grid2 : grid1;
		for (int row = 0; row < gridSize; row += 1) {
			for (int col = 0; col < gridSize; col += 1) {
				int neighbors = countNeighbors(row, col);
				set(next, row, col, isSet(row, col) && (neighbors == 2 || neighbors == 3) || neighbors == 3);
			}
		}
		current = next;
	}

	private int countNeighbors(int row, int col) {
		int rowAbove = row > 0 ? row - 1 : gridSize - 1;
		int colLeft = col > 0 ? col - 1 : gridSize - 1;
		int rowBelow = row < gridSize - 1 ? row + 1 : 0;
		int colRight = col < gridSize - 1 ? col + 1 : 0;
		int neighbors = 0;
		if (isSet(rowAbove, colLeft))
			++neighbors;
		if (isSet(rowAbove, col))
			++neighbors;
		if (isSet(rowAbove, colRight))
			++neighbors;
		if (isSet(row, colLeft))
			++neighbors;
		if (isSet(row, colRight))
			++neighbors;
		if (isSet(rowBelow, colLeft))
			++neighbors;
		if (isSet(rowBelow, col))
			++neighbors;
		if (isSet(rowBelow, colRight))
			++neighbors;
		return neighbors;
	}

	public int getWidth() {
		return 1024;
	}

	public int getHeight() {
		return 1024;
	}

	@Override
	public void draw(Graphics2D g) {
		for (int row = 0; row < gridSize; row += 1) {
			for (int col = 0; col < gridSize; col += 1) {
				if (isSet(row, col)) {
					g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
				}
			}
		}
	}
}