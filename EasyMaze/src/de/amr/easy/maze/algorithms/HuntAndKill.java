package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;
import static de.amr.easy.graph.api.TraversalState.UNVISITED;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Generates a maze in the spirit of the "hunt-and-kill" algorithm.
 *
 * <p>
 * A difference to the algorithm described in
 * <a href="http://weblog.jamisbuck.org/2011/1/24/maze-generation-hunt-and-kill-algorithm.html">
 * Jamis Buck's blog</a> is that this algorithm does not "hunt" row-wise through the unvisited maze
 * cells in the neighborhood of the already completed maze, but it stores exactly these cells in a
 * set and picks a random candidate from this set in the "hunt"-stage. That means the "hunt" is like
 * getting the poor animal placed directly before the gun ;-)
 * 
 * @author Armin Reichert
 * 
 * @param <Cell>
 *          grid cell type
 */
public class HuntAndKill<Cell> implements Consumer<Cell> {

	private final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;
	private final Set<Cell> fairGame; // cells that are "hunted" for
	private final Random rnd;

	public HuntAndKill(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
		fairGame = new HashSet<>();
		rnd = new Random();
	}

	@Override
	public void accept(Cell start) {
		Cell current = start;
		addCellToMaze(current);
		while (current != null) {
			Cell unvisitedNeighbor = findRandomNeighbor(current, UNVISITED);
			if (unvisitedNeighbor != null) {
				connect(current, unvisitedNeighbor);
				current = unvisitedNeighbor;
			} else {
				current = huntForCell();
			}
		}
	}

	private Cell huntForCell() {
		Iterator<Cell> fairGameIterator = fairGame.iterator();
		if (fairGameIterator.hasNext()) {
			// pick a random set element
			int i = rnd.nextInt(fairGame.size());
			while (i-- > 0) {
				fairGameIterator.next();
			}
			Cell cell = fairGameIterator.next();
			Cell mazeCell = findRandomNeighbor(cell, COMPLETED); // always exists for a cell from the set!
			connect(mazeCell, cell);
			return cell;
		}
		return null;
	}

	private Cell findRandomNeighbor(Cell cell, TraversalState state) {
		for (Direction dir : Direction.randomOrder()) {
			Cell neighbor = grid.neighbor(cell, dir);
			if (neighbor != null && grid.getContent(neighbor) == state) {
				return neighbor;
			}
		}
		return null;
	}

	private void addCellToMaze(Cell cell) {
		grid.setContent(cell, COMPLETED);
		fairGame.remove(cell);
		for (Direction dir : Direction.values()) {
			Cell neighbor = grid.neighbor(cell, dir);
			if (neighbor == null)
				continue;
			TraversalState state = grid.getContent(neighbor);
			if (state == TraversalState.UNVISITED) {
				fairGame.add(neighbor);
			}
		}
	}

	private void connect(Cell mazeCell, Cell newCell) {
		addCellToMaze(newCell);
		grid.addEdge(new DefaultEdge<Cell>(mazeCell, newCell));
	}
}
