package de.amr.mazes.samples.svg;

import static de.amr.easy.graph.api.TraversalState.UNVISITED;
import static de.amr.easy.grid.api.GridPosition.TOP_LEFT;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import org.jfree.graphics2d.svg.SVGUtils;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.impl.ObservableCoordDataGrid;
import de.amr.easy.grid.rendering.svg.SVGGridRenderer;
import de.amr.easy.maze.algorithms.IterativeDFS;
import de.amr.easy.maze.misc.StopWatch;

public class SVGOutputTest {

	static final int COLS = 40;
	static final int ROWS = 25;
	static final int CELLSIZE = 16;

	enum OutputFormat {
		SVG, HTML
	};

	public static void main(String[] args) throws IOException {
		SVGOutputTest app = new SVGOutputTest();
		app.createMaze();
		app.writeFile("maze.svg", OutputFormat.SVG);
		app.writeFile("maze.html", OutputFormat.HTML);
	}

	private final ObservableCoordDataGrid<TraversalState> grid;
	private final SVGGridRenderer<Integer, DefaultEdge<Integer>> svgRenderer;
	private final Consumer<Integer> mazeGenerator;

	public SVGOutputTest() {
		grid = new ObservableCoordDataGrid<>(COLS, ROWS, UNVISITED);
		svgRenderer = new SVGGridRenderer<>(grid, CELLSIZE);
		mazeGenerator = new IterativeDFS<>(grid);
	}

	private void createMaze() {
		StopWatch watch = new StopWatch();
		watch.start("Generating maze with " + grid.numCells() + " cells");
		mazeGenerator.accept(grid.cell(TOP_LEFT));
		watch.stop("Time: %.6f seconds");
	}

	private void writeFile(String path, OutputFormat fmt) throws IOException {
		File file = new File(path);
		if (fmt == OutputFormat.SVG) {
			SVGUtils.writeToSVG(file, svgRenderer.getSVGGraphics().getSVGElement());

		} else if (fmt == OutputFormat.HTML) {
			SVGUtils.writeToHTML(file, "Maze", svgRenderer.getSVGGraphics().getSVGElement());
		}
		System.out.println("Output file: " + file.getAbsolutePath());
		System.out.println("File size: " + (file.length() / 1024) + " KB");
	}
}