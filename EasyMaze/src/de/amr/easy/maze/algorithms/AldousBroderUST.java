package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;
import static de.amr.easy.graph.api.TraversalState.UNVISITED;
import static de.amr.easy.graph.api.TraversalState.VISITED;

import java.util.function.Consumer;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Let G = (V,E) be a graph with vertices V and edge set E.
 * <p>
 * Aldous-Broder algorithm:
 * <p>
 * Input: G = (V,E)<br>
 * Output: T = (V, W), where W is a subset of E such that T is a spanning tree of G.
 * <p>
 * Let W be the empty set. Add edges to W in the following manner: starting at any vertex v in V,
 * <ol>
 * <li>If all vertices in V have been visited, halt and return T
 * <li>Choose a vertex u uniformly at random from the set of neighbors of v.
 * <li>If u has never been visited before, add the edge (u,v) to the spanning tree.
 * <li>Set v, the current vertex, to be u and return to step 1.
 * </ol>
 * 
 * References:
 * <ul>
 * <li><a href=
 * "https://www.physicsforums.com/threads/matlab-code-for-aldous-broder-algorithm-from-sp a n n i n
 * g -trees-of-a-graph.660566/ ">https://www.physicsforums.com/threads
 * /matlab-code-for-aldous-broder-algorithm-from-sp a n n i n g -trees-of-a-graph.660566/</a> <br>
 * <li><a href= "http://weblog.jamisbuck.org/2011/1/17/maze-generation-aldous-broder-algorithm" >
 * http:// weblog.jamisbuck.org/2011/1/17/maze-generation-aldous-broder-algorithm</a>
 * </ul>
 * 
 * @author Armin Reichert
 */
public class AldousBroderUST<Cell> implements Consumer<Cell> {

	private final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;
	private int numMazeCells;

	public AldousBroderUST(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
	}

	@Override
	public void accept(Cell start) {
		numMazeCells = 0;
		Cell v = start;
		addToMaze(v);
		while (numMazeCells < grid.vertexCount()) {
			Cell w = grid.neighbor(v, Direction.randomValue());
			if (w != null) {
				animate(w);
				if (grid.getContent(w) == UNVISITED) {
					addToMaze(w);
					grid.addEdge(new DefaultEdge<>(w, v));
				}
				v = w;
			}
		}
	}

	private void addToMaze(Cell cell) {
		grid.setContent(cell, COMPLETED);
		++numMazeCells;
	}

	private void animate(Cell cell) {
		TraversalState state = grid.getContent(cell);
		grid.setContent(cell, VISITED);
		grid.setContent(cell, state);
	}
}
