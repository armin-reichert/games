package de.amr.easy.maze.svg;

import static de.amr.easy.graph.api.TraversalState.UNVISITED;
import static de.amr.easy.grid.api.GridPosition.TOP_LEFT;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.jfree.graphics2d.svg.SVGUtils;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.impl.ObservableCoordDataGrid;
import de.amr.easy.maze.algorithms.IterativeDFS;

public class SVGOutputTest {

	static final int COLS = 60;
	static final int ROWS = 45;
	static final int CELLSIZE = 16;

	public static void main(String[] args) throws IOException {
		new SVGOutputTest(new File("maze.svg"));
	}

	private final ObservableCoordDataGrid<TraversalState> grid;
	private final SVGGridRenderer<Integer, DefaultEdge<Integer>> svgRenderer;
	private final Consumer<Integer> mazeGenerator;

	public SVGOutputTest(File out) throws IOException {
		grid = new ObservableCoordDataGrid<>(COLS, ROWS, UNVISITED);
		svgRenderer = new SVGGridRenderer<>(grid, CELLSIZE);
		mazeGenerator = new IterativeDFS<>(grid);
		mazeGenerator.accept(grid.cell(TOP_LEFT));
		SVGUtils.writeToSVG(out, svgRenderer.getSVGGraphics().getSVGElement());
	}
}