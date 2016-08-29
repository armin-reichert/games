package de.amr.mazes.swing.rendering;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedHashSet;
import java.util.Set;

import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.graph.traversal.BreadthFirstTraversal;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableGrid2D;
import de.amr.easy.grid.rendering.DefaultGridRenderingModel;
import de.amr.easy.grid.rendering.GridCanvas;

/**
 * Animation of breadth-first-search path finding. Shows the distances as the BFS traverses the
 * graph and colors the cells according to their distance from the source.
 * 
 * @author Armin Reichert
 */
public class BFSAnimation {

	private final GridCanvas<Integer, ?> canvas;
	private final ObservableGrid2D<Integer, ?> grid;
	private final BFSRenderingModel renderingModel;
	private final Set<Integer> path;
	private BreadthFirstTraversal<Integer, ?> bfs;
	private int maxDistance;
	private Integer maxDistanceCell;
	private boolean distancesVisible;

	public BFSAnimation(GridCanvas<Integer, ?> canvas,
			ObservableGrid2D<Integer, DefaultEdge<Integer>> grid) {
		this.canvas = canvas;
		this.grid = grid;
		renderingModel = new BFSRenderingModel(canvas.currentRenderingModel().getCellSize(),
				canvas.currentRenderingModel().getPassageThickness(), Color.RED);
		path = new LinkedHashSet<Integer>();
		maxDistance = -1;
		distancesVisible = true;
	}

	public void runAnimation(Integer source) {
		// 1. run BFS silently to compute maximum distance from source:
		canvas.stopListening();
		bfs = new BreadthFirstTraversal<>(grid, source);
		bfs.run();
		maxDistance = bfs.getMaxDistance();
		maxDistanceCell = bfs.getMaxDistanceVertex();
		canvas.startListening();

		// 2. run BFS with events enabled such that coloring and distances are
		// rendered:
		canvas.pushRenderingModel(renderingModel);
		bfs.run();
		canvas.popRenderingModel();
	}

	public void showPath(Integer target) {
		path.clear();
		for (Integer cell : bfs.findPath(target)) {
			path.add(cell);
		}
		canvas.pushRenderingModel(renderingModel);
		for (Integer cell : path) {
			canvas.renderGridCell(cell);
		}
		canvas.popRenderingModel();
	}

	public Integer getMaxDistanceCell() {
		return maxDistanceCell;
	}

	public boolean isDistancesVisible() {
		return distancesVisible;
	}

	public void setDistancesVisible(boolean distancesVisible) {
		this.distancesVisible = distancesVisible;
	}

	// -- Rendering model

	private class BFSRenderingModel extends DefaultGridRenderingModel<Integer> {

		private final int cellSize;
		private final int passageThickness;
		private final Color pathColor;
		private Font textFont;

		public BFSRenderingModel(int cellSize, int passageThickness, Color pathColor) {
			this.cellSize = cellSize;
			this.passageThickness = passageThickness;
			this.pathColor = pathColor;
		}

		@Override
		public int getCellSize() {
			return cellSize;
		}

		@Override
		public String getCellText(Integer cell) {
			return distancesVisible && bfs.getDistance(cell) != -1 ? String.valueOf(bfs.getDistance(cell))
					: "";
		}

		@Override
		public Color getCellBgColor(Integer cell) {
			return path.contains(cell) ? pathColor : cellColor(cell);
		}

		@Override
		public int getPassageThickness() {
			return passageThickness;
		}

		@Override
		public Color getPassageColor(Integer cell, Direction dir) {
			if (path.contains(cell) && path.contains(grid.neighbor(cell, dir))) {
				return pathColor;
			}
			return cellColor(cell);
		}

		@Override
		public Font getCellTextFont() {
			if (textFont == null || textFont.getSize() > getPassageThickness() / 2) {
				textFont = new Font("SansSerif", Font.PLAIN, getPassageThickness() / 2);
			}
			return textFont;
		}

		private Color cellColor(Integer cell) {
			if (maxDistance == -1) {
				return renderingModel.getCellBgColor(cell);
			}
			float hue = 0.16f;
			if (maxDistance != 0) {
				hue += 0.7f * bfs.getDistance(cell) / maxDistance;
			}
			return Color.getHSBColor(hue, 0.5f, 1f);
		}
	};
}