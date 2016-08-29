package de.amr.games.pacman.data;

import static de.amr.games.pacman.data.Board.Cols;
import static de.amr.games.pacman.data.Board.Rows;
import static de.amr.games.pacman.data.Board.Wall;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.graph.api.SingleSourcePathFinder;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.graph.traversal.BreadthFirstTraversal;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.impl.ObservableCoordGrid;

public class RouteMap {

	private ObservableCoordGrid gridGraph;

	public RouteMap(Board board) {
		gridGraph = new ObservableCoordGrid(Cols, Rows);
		gridGraph.setEventsEnabled(false);
		/*@formatter:off*/
		gridGraph.verticesStream()
			.filter(cell -> board.grid.getContent(cell) != Wall)
			.forEach(cell -> {
				Stream.of(Direction.values()).forEach(dir -> {
					Integer neighbor = gridGraph.neighbor(cell, dir);
					if (neighbor != null && board.grid.getContent(neighbor) != Wall
						&& !gridGraph.adjacent(cell, neighbor)) {
						gridGraph.addEdge(new DefaultEdge<>(cell, neighbor));
					}
				});
			});
		/*@formatter:on*/
	}

	public List<Direction> shortestRoute(Tile source, Tile target) {
		Integer sourceCell = gridGraph.cell(source.getCol(), source.getRow());
		Integer targetCell = gridGraph.cell(target.getCol(), target.getRow());
		SingleSourcePathFinder<Integer> pathFinder = new BreadthFirstTraversal<>(gridGraph, sourceCell);
		pathFinder.run();
		List<Direction> route = new ArrayList<>();
		Integer pred = null;
		for (Integer cell : pathFinder.findPath(targetCell)) {
			if (pred != null) {
				route.add(gridGraph.direction(pred, cell));
			}
			pred = cell;
		}
		return route;
	}
}
