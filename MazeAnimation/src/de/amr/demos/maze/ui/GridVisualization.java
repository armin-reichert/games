package de.amr.demos.maze.ui;

import java.awt.Color;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.api.ObservableGrid2D;
import de.amr.easy.grid.rendering.swing.SwingDefaultGridRenderingModel;

public class GridVisualization extends SwingDefaultGridRenderingModel {

	protected final ObservableGrid2D<TraversalState> grid;
	private final int gridCellSize;

	public GridVisualization(ObservableGrid2D<TraversalState> grid, int gridCellSize) {
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
	public Color getCellBgColor(Integer p) {
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
