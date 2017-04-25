package de.amr.games.pacman.data;

import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;

import java.util.ArrayList;
import java.util.List;

import de.amr.easy.graph.alg.traversal.BreadthFirstTraversal;
import de.amr.easy.graph.api.PathFinder;
import de.amr.easy.grid.impl.ObservableBareGrid;

/**
 * A helper class for computing routes inside the maze.
 * 
 * @author Armin Reichert
 *
 */
public class RouteMap {

	private ObservableBareGrid<?> gridGraph;

	public RouteMap(Board board) {
		gridGraph = new ObservableBareGrid<>(NUM_COLS, NUM_ROWS);
		gridGraph.setEventsEnabled(false);
		/*@formatter:off*/
		gridGraph.vertexStream()
			.filter(cell -> board.grid.get(cell) != TileContent.Wall.toChar())
			.forEach(cell -> {
				gridGraph.getTopology().dirs().forEach(dir -> {
					gridGraph.neighbor(cell, dir).ifPresent(neighbor -> {
						if (board.grid.get(neighbor) != TileContent.Wall.toChar()
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
