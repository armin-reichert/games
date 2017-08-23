package de.amr.demos.maze.ui;

import java.awt.Color;

import de.amr.demos.grid.swing.core.SwingDefaultGridRenderingModel;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.api.ObservableGrid2D;

public class GridVisualization extends SwingDefaultGridRenderingModel {

	protected final ObservableGrid2D<TraversalState, Integer> grid;
	private final int gridCellSize;

	public GridVisualization(ObservableGrid2D<TraversalState, Integer> grid, int gridCellSize) {
		this.grid = grid;
		this.gridCellSize = gridCellSize;
	}

	@Override
	public int getCellSize() {
		return gridCellSize;
	}

	@Override
	public int getPassageThickness() {
		return getCellSize() * 3 / 4;
	}

	@Override
	public Color getGridBgColor() {
		return Color.BLACK;
	}

	@Override
	public Color getCellBgColor(int p) {
		switch (grid.get(p)) {
		case UNVISITED:
			return getGridBgColor();
		case VISITED:
			return Color.GREEN;
		case COMPLETED:
			return Color.WHITE;
		}
		throw new IllegalStateException();
	}
}
