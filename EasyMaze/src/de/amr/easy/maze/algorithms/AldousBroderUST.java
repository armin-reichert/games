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
public class AldousBroderUST implements Consumer<Integer> {

	private final ObservableDataGrid2D<Integer, DefaultEdge<Integer>, TraversalState> grid;
	private int numMazeCells;

	public AldousBroderUST(ObservableDataGrid2D<Integer, DefaultEdge<Integer>, TraversalState> grid) {
		this.grid = grid;
	}

	@Override
	public void accept(Integer start) {
		numMazeCells = 0;
		Integer u = start;
		addToMaze(u);
		while (numMazeCells < grid.vertexCount()) {
			Integer v = grid.neighbor(u, Direction.randomValue());
			if (v != null) {
				animate(v);
				if (grid.get(v) == UNVISITED) {
					addToMaze(v);
					grid.addEdge(new DefaultEdge<>(v, u));
				}
				u = v;
			}
		}
	}

	private void addToMaze(Integer cell) {
		grid.set(cell, COMPLETED);
		++numMazeCells;
	}

	private void animate(Integer cell) {
		TraversalState state = grid.get(cell);
		grid.set(cell, VISITED);
		grid.set(cell, state);
	}
}
