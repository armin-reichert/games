package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;
import static de.amr.easy.graph.api.TraversalState.VISITED;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.graph.impl.DefaultWeightedEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Maze generator based on Prim's minimum spanning tree algorithm with random edge weights.
 * 
 * @author Armin Reichert
 */
public class PrimMST<Cell> implements Consumer<Cell> {

	private final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;
	private final Set<Cell> mazeCells = new HashSet<>();
	private final PriorityQueue<DefaultWeightedEdge<Cell>> cut = new PriorityQueue<>();

	public PrimMST(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
	}

	@Override
	public void accept(Cell start) {
		expandMazeAtCell(start);
		while (!cut.isEmpty()) {
			DefaultWeightedEdge<Cell> edge = cut.poll();
			Cell p = edge.either(), q = edge.other(p);
			if (!mazeCells.contains(p) || !mazeCells.contains(q)) {
				grid.addEdge(edge);
				expandMazeAtCell(mazeCells.contains(p) ? q : p);
			}
		}
	}

	private void expandMazeAtCell(Cell cell) {
		Random rnd = new Random();
		mazeCells.add(cell);
		grid.setContent(cell, COMPLETED);
		/*@formatter:off*/
		Stream.of(Direction.randomOrder())
			.map(dir -> grid.neighbor(cell, dir))
			.filter(neighbor -> neighbor != null && !mazeCells.contains(neighbor))
			.forEach(newMazeCell -> {
				cut.add(new DefaultWeightedEdge<>(cell, newMazeCell, rnd.nextDouble()));
				grid.setContent(newMazeCell, VISITED);
			});
		/*@formatter:on*/
	}
}
