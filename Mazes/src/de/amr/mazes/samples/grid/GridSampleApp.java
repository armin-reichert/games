package de.amr.mazes.samples.grid;

import static de.amr.easy.graph.api.TraversalState.UNVISITED;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSlider;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.impl.ObservableCoordDataGrid;
import de.amr.easy.grid.rendering.DefaultGridRenderingModel;
import de.amr.easy.grid.rendering.GridCanvas;
import de.amr.easy.grid.rendering.GridRenderingModel;
import de.amr.easy.maze.misc.Utils;

/**
 * Base class for grid sample applications.
 * 
 * @author Armin Reichert
 */
public class GridSampleApp {

	static {
		Utils.setLAF("Nimbus");
	}

	public static void launch(GridSampleApp app) {
		EventQueue.invokeLater(app::showUI);
	}

	protected ObservableCoordDataGrid<TraversalState> grid;
	protected int cellSize;
	protected String appName;
	protected JFrame window;
	protected GridCanvas<Integer, DefaultEdge<Integer>> canvas;
	protected JSlider delaySlider;
	protected Runnable work;

	protected GridSampleApp(String appName, int gridWidth, int gridHeight, int cellSize) {
		grid = new ObservableCoordDataGrid<>(gridWidth, gridHeight, UNVISITED);
		this.appName = appName;
		this.cellSize = cellSize;
		work = () -> {
		};
	}

	private void showUI() {
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle(composeTitle());
		canvas = new GridCanvas<>(grid, createRenderingModel(cellSize));
		canvas.setDelay(0);
		window.add(canvas, BorderLayout.CENTER);
		delaySlider = new JSlider(0, 50);
		delaySlider.setValue(canvas.getDelay());
		delaySlider.addChangeListener(event -> {
			if (!delaySlider.getValueIsAdjusting())
				canvas.setDelay(delaySlider.getValue());
		});
		window.add(delaySlider, BorderLayout.SOUTH);
		window.pack();
		window.setVisible(true);
		new Thread(work).start();
	}

	protected void resize(int windowWidth, int windowHeight, int cellSize) {
		grid = new ObservableCoordDataGrid<>(windowWidth / cellSize, windowHeight / cellSize, UNVISITED);
		canvas.setGrid(grid);
		canvas.setRenderingModel(createRenderingModel(cellSize));
		window.setTitle(composeTitle());
		window.pack();
	}

	protected void clear() {
		grid.removeAllEdges();
		grid.clearContent();
		canvas.resetRenderingModel();
		canvas.clear();
	}

	protected String composeTitle() {
		return String.format("%s [%d x %d, %d cells]", appName, grid.numCols(), grid.numRows(),
				grid.numRows() * grid.numCols());
	}

	protected void setDelay(int delay) {
		delaySlider.setValue(delay);
		canvas.setDelay(delay);
	}

	protected void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected GridRenderingModel<Integer> createRenderingModel(final int cellSize) {
		return new DefaultGridRenderingModel<Integer>() {

			@Override
			public int getCellSize() {
				return cellSize;
			}

			@Override
			public Color getCellBgColor(Integer cell) {
				switch (grid.getContent(cell)) {
				case VISITED:
					return Color.BLUE;
				case COMPLETED:
					return Color.WHITE;
				case UNVISITED:
					return getGridBgColor();
				default:
					return super.getCellBgColor(cell);
				}
			}
		};
	}
}