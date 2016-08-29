package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.maze.datastructures.Partition;
import de.amr.easy.maze.datastructures.Partition.EquivClass;

/**
 * Maze generator using Eller's algorithm.
 * 
 * More information
 * <a href= "http://weblog.jamisbuck.org/2010/12/29/maze-generation-eller-s-algorithm"> here</a>.
 * 
 * @author Armin Reichert
 */
public class Eller<Cell> implements Consumer<Cell> {

	private final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;
	private final Random rnd;
	private final Partition<Cell> partition;

	public Eller(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
		rnd = new Random();
		partition = new Partition<Cell>();
	}

	@Override
	public void accept(Cell start) {
		for (int y = 0; y < grid.numRows() - 1; ++y) {
			connectCellsHorizontally(y, false);
			connectCellsVertically(y);
		}
		connectCellsHorizontally(grid.numRows() - 1, true);
	}

	private void connectCells(Cell v, Cell w) {
		grid.addEdge(new DefaultEdge<Cell>(v, w));
		grid.setContent(v, COMPLETED);
		grid.setContent(w, COMPLETED);
		partition.union(partition.find(v), partition.find(w));
	}

	private void connectCellsHorizontally(int y, boolean all) {
		for (int x = 0; x < grid.numCols() - 1; ++x) {
			if (all || rnd.nextBoolean()) {
				Cell left = grid.cell(x, y);
				Cell right = grid.cell(x + 1, y);
				if (partition.find(left) != partition.find(right)) {
					connectCells(left, right);
				}
			}
		}
	}

	private void connectCellsVertically(int y) {
		Set<EquivClass> connected = new HashSet<>();
		for (int x = 0; x < grid.numCols(); ++x) {
			if (rnd.nextBoolean()) {
				Cell cell = grid.cell(x, y);
				Cell below = grid.cell(x, y + 1);
				connectCells(cell, below);
				connected.add(partition.find(cell));
			}
		}
		// collect cells of still unconnected components
		List<Cell> unconnected = new ArrayList<>();
		for (int x = 0; x < grid.numCols(); ++x) {
			Cell cell = grid.cell(x, y);
			EquivClass component = partition.find(cell);
			if (!connected.contains(component)) {
				unconnected.add(cell);
			}
		}
		// shuffle cells to avoid biased maze
		Collections.shuffle(unconnected);
		// connect cells and mark component as connected
		for (Cell cell : unconnected) {
			EquivClass component = partition.find(cell);
			if (!connected.contains(component)) {
				Cell below = grid.cell(grid.col(cell), y + 1);
				connectCells(cell, below);
				connected.add(component);
			}
		}
	}
}
