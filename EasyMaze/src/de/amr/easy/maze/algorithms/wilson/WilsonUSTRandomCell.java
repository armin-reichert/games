package de.amr.easy.maze.algorithms.wilson;

import java.util.LinkedList;
import java.util.List;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Wilson's algorithm with random vertex selection.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTRandomCell<Cell> extends WilsonUST<Cell> {

	private final List<Cell> cellsOutsideTree;

	public WilsonUSTRandomCell(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
		cellsOutsideTree = new LinkedList<Cell>();
		for (Cell cell : grid.vertices()) {
			cellsOutsideTree.add(cell);
		}
	}

	@Override
	public void accept(Cell start) {
		addCellToTree(start);
		while (!cellsOutsideTree.isEmpty()) {
			loopErasedRandomWalk(pickRandomCellOutsideTree());
		}
	}

	@Override
	protected void addCellToTree(Cell v) {
		super.addCellToTree(v);
		cellsOutsideTree.remove(v);
	}

	protected void addEdge(Cell v, Cell w) {
		grid.addEdge(new DefaultEdge<Cell>(v, w));
		cellsOutsideTree.remove(v);
		cellsOutsideTree.remove(w);
	}

	private Cell pickRandomCellOutsideTree() {
		return cellsOutsideTree.get(rnd.nextInt(cellsOutsideTree.size()));
	}
}
