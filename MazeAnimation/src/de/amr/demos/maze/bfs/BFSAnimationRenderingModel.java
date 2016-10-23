package de.amr.demos.maze.bfs;

import java.awt.Color;

import de.amr.demos.maze.ui.GridVisualization;
import de.amr.easy.graph.alg.traversal.BreadthFirstTraversal;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.api.WeightedEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableGrid2D;

public class BFSAnimationRenderingModel extends GridVisualization {

	private BreadthFirstTraversal<Integer, WeightedEdge<Integer>> bfs;
	private int maxDistance;

	public BFSAnimationRenderingModel(ObservableGrid2D<TraversalState> grid, int gridCellSize,
			BreadthFirstTraversal<Integer, WeightedEdge<Integer>> bfs, int maxDistance) {
		super(grid, gridCellSize);
		this.bfs = bfs;
		this.maxDistance = maxDistance;
	}

	@Override
	public Color getCellBgColor(Integer p) {
		return getDistanceColor(p);
	}

	@Override
	public Color getPassageColor(Integer p, Direction dir) {
		return getDistanceColor(p);
	}

	private Color getDistanceColor(Integer p) {
		if (maxDistance == -1) {
			return super.getCellBgColor(p);
		}
		float hue = 0.16f;
		if (maxDistance != 0) {
			hue += 0.7f * bfs.getDistance(p) / maxDistance;
		}
		return Color.getHSBColor(hue, 0.5f, 1f);
	}

}
