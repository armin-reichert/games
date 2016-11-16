package de.amr.games.pacman.data;

import static de.amr.games.pacman.data.Board.Cols;
import static de.amr.games.pacman.data.Board.Rows;
import static de.amr.games.pacman.data.Board.Wall;

import java.util.ArrayList;
import java.util.List;

import de.amr.easy.graph.alg.traversal.BreadthFirstTraversal;
import de.amr.easy.graph.api.PathFinder;
import de.amr.easy.grid.impl.ObservableBareGrid;

public class RouteMap {

	private ObservableBareGrid<?> gridGraph;

	public RouteMap(Board board) {
		gridGraph = new ObservableBareGrid<>(Cols, Rows);
		gridGraph.setEventsEnabled(false);
		/*@formatter:off*/
		gridGraph.vertexStream()
			.filter(cell -> board.grid.get(cell) != Wall)
			.forEach(cell -> {
				gridGraph.getTopology().dirs().forEach(dir -> {
					gridGraph.neighbor(cell, dir).ifPresent(neighbor -> {
						if (board.grid.get(neighbor) != Wall
							&& !gridGraph.adjacent(cell, neighbor)) {
							gridGraph.addEdge(cell, neighbor);
						}
					});
				});
			});
		/*@formatter:on*/
	}

	public List<Integer> shortestRoute(Tile source, Tile target) {
		Integer sourceCell = gridGraph.cell(source.getCol(), source.getRow());
		Integer targetCell = gridGraph.cell(target.getCol(), target.getRow());
		PathFinder<Integer> pathFinder = new BreadthFirstTraversal<>(gridGraph, sourceCell);
		pathFinder.run();
		List<Integer> route = new ArrayList<>();
		Integer pred = null;
		for (Integer cell : pathFinder.findPath(targetCell)) {
			if (pred != null) {
				route.add(gridGraph.direction(pred, cell).getAsInt());
			}
			pred = cell;
		}
		return route;
	}
}
