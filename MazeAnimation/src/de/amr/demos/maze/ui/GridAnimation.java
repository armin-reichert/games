package de.amr.demos.maze.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.easy.graph.api.ObservableGraph;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.api.WeightedEdge;
import de.amr.easy.graph.api.event.EdgeAddedEvent;
import de.amr.easy.graph.api.event.EdgeChangeEvent;
import de.amr.easy.graph.api.event.EdgeRemovedEvent;
import de.amr.easy.graph.api.event.GraphObserver;
import de.amr.easy.graph.api.event.GraphTraversalListener;
import de.amr.easy.graph.api.event.VertexChangeEvent;
import de.amr.easy.grid.api.ObservableGrid2D;
import de.amr.easy.grid.rendering.swing.SwingGridRenderer;

public class GridAnimation
		implements GraphTraversalListener<Integer>, GraphObserver<Integer, WeightedEdge<Integer, Integer>> {

	private final ObservableGrid2D<TraversalState, Integer> grid;
	private final BufferedImage canvas;
	private final SwingGridRenderer renderer;
	private int delay;

	public GridAnimation(ObservableGrid2D<TraversalState, Integer> grid, int gridCellSize, int width, int height) {
		this.grid = grid;
		grid.addGraphObserver(this);
		canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		renderer = new SwingGridRenderer();
		setRenderingModel(new GridVisualization(grid, gridCellSize));
	}

	public int getDelay() {
		return delay;
	}

	public void faster(int amount) {
		setDelay(getDelay() - amount);
	}

	public void slower(int amount) {
		setDelay(getDelay() + amount);
	}

	public void setDelay(int delay) {
		this.delay = Math.max(0, delay);
	}

	public void setRenderingModel(GridVisualization renderingModel) {
		renderer.setRenderingModel(renderingModel);
	}

	public void clearCanvas() {
		Graphics2D g = getDrawGraphics();
		g.setColor(renderer.getRenderingModel().getGridBgColor());
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public void render(Graphics2D g) {
		g.drawImage(canvas, 0, 0, null);
	}

	private Graphics2D getDrawGraphics() {
		return (Graphics2D) canvas.getGraphics();
	}

	private void delay() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void edgeTouched(Integer source, Integer target) {
		delay();
		if (grid.edge(source, target).isPresent()) {
			renderer.drawPassage(getDrawGraphics(), grid, grid.edge(source, target).get(), true);
		} else {
			renderer.drawPassage(getDrawGraphics(), grid, null, false);
		}
	}

	@Override
	public void vertexTouched(Integer vertex, TraversalState oldState, TraversalState newState) {
		delay();
		renderer.drawCell(getDrawGraphics(), grid, vertex);
	}

	@Override
	public void vertexChanged(VertexChangeEvent<Integer, WeightedEdge<Integer, Integer>> event) {
		delay();
		renderer.drawCell(getDrawGraphics(), grid, event.getVertex());
	}

	@Override
	public void edgeChanged(EdgeChangeEvent<Integer, WeightedEdge<Integer, Integer>> event) {
		delay();
		renderer.drawPassage(getDrawGraphics(), grid, event.getEdge(), true);
	}

	@Override
	public void edgeAdded(EdgeAddedEvent<Integer, WeightedEdge<Integer, Integer>> event) {
		delay();
		renderer.drawPassage(getDrawGraphics(), grid, event.getEdge(), true);
	}

	@Override
	public void edgeRemoved(EdgeRemovedEvent<Integer, WeightedEdge<Integer, Integer>> event) {
		delay();
		renderer.drawPassage(getDrawGraphics(), grid, event.getEdge(), false);
	}

	@Override
	public void graphChanged(ObservableGraph<Integer, WeightedEdge<Integer, Integer>> graph) {
		renderer.drawGrid(getDrawGraphics(), grid);
	}
}