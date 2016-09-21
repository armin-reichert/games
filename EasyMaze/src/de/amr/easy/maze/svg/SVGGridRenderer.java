package de.amr.easy.maze.svg;

import java.awt.Color;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import de.amr.easy.graph.api.Edge;
import de.amr.easy.graph.api.ObservableGraph;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.event.GraphListener;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.rendering.DefaultGridRenderingModel;
import de.amr.easy.grid.rendering.GridRenderer;
import de.amr.easy.grid.rendering.GridRenderingModel;

public class SVGGridRenderer<Cell, Passage extends Edge<Cell>>
		implements GraphListener<Cell, Passage> {

	private final ObservableDataGrid2D<Cell, Passage, TraversalState> grid;
	private final SVGGraphics2D g;
	private final GridRenderer<Cell, Passage> renderer;
	private final GridRenderingModel<Cell> renderingModel;

	private class SVGRenderingModel extends DefaultGridRenderingModel<Cell> {

		private final int cellSize;

		public SVGRenderingModel(int cellSize) {
			this.cellSize = cellSize;
		}

		@Override
		public int getCellSize() {
			return cellSize;
		}
	}

	public SVGGridRenderer(ObservableDataGrid2D<Cell, Passage, TraversalState> grid, int cellSize) {
		this.grid = grid;
		int width = grid.numCols() * cellSize, height = grid.numRows() * cellSize;
		g = new SVGGraphics2D(width, height);
		g.setBackground(Color.black);
		g.clearRect(0, 0, width, height);
		renderer = new GridRenderer<>();
		renderingModel = new SVGRenderingModel(cellSize);
		renderer.setRenderingModel(renderingModel);
		grid.addGraphListener(this);
	}

	public String getSVGDocument() {
		return g.getSVGDocument();
	}

	public String getSVGElement() {
		return g.getSVGElement();
	}

	@Override
	public void vertexChanged(Cell vertex, Object oldValue, Object newValue) {
	}

	@Override
	public void edgeChanged(Passage edge, Object oldValue, Object newValue) {
	}

	@Override
	public void edgeAdded(Passage edge) {
		renderer.drawPassage(g, grid, edge, true);
	}

	@Override
	public void edgeRemoved(Passage edge) {
		renderer.drawPassage(g, grid, edge, false);
	}

	@Override
	public void graphChanged(ObservableGraph<Cell, Passage> graph) {
	}

}