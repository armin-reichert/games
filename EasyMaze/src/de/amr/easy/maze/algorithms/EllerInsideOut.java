package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.impl.RawGrid;
import de.amr.easy.grid.iterators.shapes.Rectangle;
import de.amr.easy.grid.iterators.shapes.Square;
import de.amr.easy.maze.datastructures.Partition;
import de.amr.easy.maze.datastructures.Partition.EquivClass;

/**
 * Maze generator similar to Eller's algorithm but growing the maze inside-out.
 * 
 * @author Armin Reichert
 */
public class EllerInsideOut implements Consumer<Integer> {

	private final ObservableDataGrid2D<Integer, DefaultEdge<Integer>, TraversalState> grid;
	private final RawGrid squareGrid;
	private final Random rnd;
	private final Partition<Integer> mazeParts;
	private Square<Integer> square;
	private Iterable<Integer> layer;
	private Map<Integer, Integer> cellIndex;
	private final int offsetX;
	private final int offsetY;

	public EllerInsideOut(ObservableDataGrid2D<Integer, DefaultEdge<Integer>, TraversalState> grid) {
		this.grid = grid;
		int n = Math.max(grid.numCols(), grid.numRows());
		offsetX = (n - grid.numCols()) / 2;
		offsetY = (n - grid.numRows()) / 2;
		squareGrid = new RawGrid(n, n);
		rnd = new Random();
		mazeParts = new Partition<>();
	}

	@Override
	public void accept(Integer start) {
		while (nextLayer() <= squareGrid.numCols()) {
			connectCellsInsideLayer(false);
			connectCellsWithNextLayer();
		}
		layer = new Rectangle<>(grid, grid.cell(GridPosition.TOP_LEFT), grid.numCols(), grid.numRows());
		connectCellsInsideLayer(true);

		if (grid.edgeCount() != grid.vertexCount() - 1) {
			throw new IllegalStateException("#edges: " + grid.edgeCount() + ", #cells: " + grid.vertexCount());
		}
	}

	private int nextLayer() {
		int x, y, size;
		if (square == null) {
			Integer center = grid.cell(GridPosition.CENTER);
			x = grid.col(center) + offsetX;
			y = grid.row(center) + offsetY;
			size = 1;
		} else {
			x = squareGrid.col(square.getTopLeft()) - 1;
			y = squareGrid.row(square.getTopLeft()) - 1;
			size = square.getSize() + 2;
		}
		if (size <= squareGrid.numCols()) {
			square = new Square<>(squareGrid, squareGrid.cell(x, y), size);
			layer = croppedLayer();
		}
		return size;
	}

	private List<Integer> croppedLayer() {
		List<Integer> result = new ArrayList<>();
		cellIndex = new HashMap<>();
		int index = 0;
		for (Integer cell : square) {
			int x = squareGrid.col(cell) - offsetX;
			int y = squareGrid.row(cell) - offsetY;
			if (grid.isValidCol(x) && grid.isValidRow(y)) {
				Integer gridCell = grid.cell(x, y);
				result.add(gridCell);
				cellIndex.put(gridCell, index);
			}
			++index;
		}
		return result;
	}

	private void connectCells(Integer v, Integer w) {
		if (grid.adjacent(v, w)) {
			return;
		}
		// System.out.println(coord(v) + "->" + coord(w));
		grid.addEdge(new DefaultEdge<>(v, w));
		grid.set(v, COMPLETED);
		grid.set(w, COMPLETED);
		mazeParts.union(mazeParts.find(v), mazeParts.find(w));
	}

	private void connectCellsInsideLayer(boolean all) {
		Integer prevCell = null, firstCell = null;
		for (Integer cell : layer) {
			if (firstCell == null) {
				firstCell = cell;
			}
			if (prevCell != null && areNeighbors(prevCell, cell)) {
				if (all || rnd.nextBoolean()) {
					if (mazeParts.find(prevCell) != mazeParts.find(cell)) {
						connectCells(prevCell, cell);
					}
				}
			}
			prevCell = cell;
		}
		if (prevCell != null && firstCell != null && prevCell != firstCell && areNeighbors(prevCell, firstCell)
				&& !grid.adjacent(prevCell, firstCell)) {
			if (all || rnd.nextBoolean()) {
				if (mazeParts.find(prevCell) != mazeParts.find(firstCell)) {
					connectCells(prevCell, firstCell);
				}
			}
		}
	}

	private void connectCellsWithNextLayer() {
		Set<EquivClass> connected = new HashSet<>();
		// randomly select cells and connect with next layer unless another cell from the same
		// equivalence class is already
		// connected to the next layer
		for (Integer cell : layer) {
			if (rnd.nextBoolean() && !connected.contains(mazeParts.find(cell))) {
				List<Integer> candidates = getNeighborsInNextLayer(cell);
				if (!candidates.isEmpty()) {
					Integer neighbor = candidates.get(rnd.nextInt(candidates.size()));
					connectCells(cell, neighbor);
					connected.add(mazeParts.find(cell));
				}
			}
		}
		// collect cells of still unconnected maze parts and shuffle them to avoid biased maze
		List<Integer> unconnectedCells = new ArrayList<>();
		for (Integer cell : layer) {
			if (!connected.contains(mazeParts.find(cell))) {
				unconnectedCells.add(cell);
			}
		}
		Collections.shuffle(unconnectedCells);
		// connect remaining cells and mark maze parts as connected
		for (Integer cell : unconnectedCells) {
			if (!connected.contains(mazeParts.find(cell))) {
				List<Integer> candidates = getNeighborsInNextLayer(cell);
				if (!candidates.isEmpty()) {
					Integer neighbor = candidates.get(rnd.nextInt(candidates.size()));
					connectCells(cell, neighbor);
					connected.add(mazeParts.find(cell));
				}
			}
		}
	}

	private List<Integer> getNeighborsInNextLayer(Integer cell) {
		List<Integer> result = new ArrayList<>(4);
		int squareSize = square.getSize();
		if (squareSize == 1) {
			addNeighborIfAny(cell, Direction.N, result);
			addNeighborIfAny(cell, Direction.E, result);
			addNeighborIfAny(cell, Direction.S, result);
			addNeighborIfAny(cell, Direction.W, result);
			return result;
		}
		int index = cellIndex.get(cell);
		if (index == 0) {
			addNeighborIfAny(cell, Direction.W, result);
			addNeighborIfAny(cell, Direction.N, result);
		} else if (index < squareSize - 1) {
			addNeighborIfAny(cell, Direction.N, result);
		} else if (index == squareSize - 1) {
			addNeighborIfAny(cell, Direction.N, result);
			addNeighborIfAny(cell, Direction.E, result);
		} else if (index < 2 * (squareSize - 1)) {
			addNeighborIfAny(cell, Direction.E, result);
		} else if (index == 2 * (squareSize - 1)) {
			addNeighborIfAny(cell, Direction.E, result);
			addNeighborIfAny(cell, Direction.S, result);
		} else if (index < 3 * (squareSize - 1)) {
			addNeighborIfAny(cell, Direction.S, result);
		} else if (index == 3 * (squareSize - 1)) {
			addNeighborIfAny(cell, Direction.S, result);
			addNeighborIfAny(cell, Direction.W, result);
		} else {
			addNeighborIfAny(cell, Direction.W, result);
		}
		return result;
	}

	private void addNeighborIfAny(Integer cell, Direction dir, List<Integer> list) {
		Integer neighbour = grid.neighbor(cell, dir);
		if (neighbour != null) {
			list.add(neighbour);
		}
	}

	private boolean areNeighbors(Integer c, Integer d) {
		return d.equals(grid.neighbor(c, Direction.N)) || d.equals(grid.neighbor(c, Direction.E))
				|| d.equals(grid.neighbor(c, Direction.S)) || d.equals(grid.neighbor(c, Direction.W));
	}
}
