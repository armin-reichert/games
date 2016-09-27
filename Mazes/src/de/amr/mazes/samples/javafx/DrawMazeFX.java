package de.amr.mazes.samples.javafx;

import static de.amr.easy.graph.api.TraversalState.UNVISITED;
import static de.amr.easy.grid.api.GridPosition.BOTTOM_RIGHT;

import java.util.Timer;
import java.util.TimerTask;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.graph.traversal.BreadthFirstTraversal;
import de.amr.easy.grid.impl.ObservableDataGrid;
import de.amr.easy.maze.algorithms.KruskalMST;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DrawMazeFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private Canvas canvas;
	private Timer timer;
	private ObservableDataGrid<TraversalState> maze;
	private int cols;
	private int rows;
	private int cellSize;

	public DrawMazeFX() {
		cols = 100;
		rows = 50;
		cellSize = 16;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Pane root = new Pane();
		canvas = new Canvas((cols + 1) * cellSize, (rows + 1) * cellSize);
		root.getChildren().add(canvas);
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Maze Generation & Pathfinding");
		// primaryStage.setFullScreen(true);
		primaryStage.setOnCloseRequest(event -> timer.cancel());
		primaryStage.show();
		
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Platform.runLater(DrawMazeFX.this::nextMaze);
			}
		}, 0, 3000);
	}

	private void nextMaze() {
		maze = new ObservableDataGrid<>(cols, rows, UNVISITED);
		canvas.resize((cols + 1) * cellSize, (rows + 1) * cellSize);
		new KruskalMST(maze).accept(maze.cell(0, 0));
		drawGrid(canvas.getGraphicsContext2D());
		BreadthFirstTraversal<Integer, DefaultEdge<Integer>> bfs = new BreadthFirstTraversal<>(maze, maze.cell(0, 0));
		bfs.run();
		drawPath(bfs.findPath(maze.cell(BOTTOM_RIGHT)), canvas.getGraphicsContext2D());
	}

	private void drawGrid(GraphicsContext gc) {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		gc.translate(cellSize, cellSize);
		gc.setStroke(Color.WHITE);
		gc.setLineWidth(cellSize / 2);
		maze.edgeSequence().forEach(edge -> {
			Integer u = edge.either(), v = edge.other(u);
			gc.strokeLine(maze.col(u) * cellSize, maze.row(u) * cellSize, maze.col(v) * cellSize, maze.row(v) * cellSize);
		});
		gc.translate(-cellSize, -cellSize);
	}

	private void drawPath(Iterable<Integer> path, GraphicsContext gc) {
		gc.setStroke(Color.RED);
		gc.setLineWidth(cellSize / 4);
		gc.translate(cellSize, cellSize);
		Integer u = null;
		for (Integer v : path) {
			if (u != null) {
				gc.strokeLine(maze.col(u) * cellSize, maze.row(u) * cellSize, maze.col(v) * cellSize, maze.row(v) * cellSize);
			}
			u = v;
		}
		gc.translate(-cellSize, -cellSize);
	}
}
