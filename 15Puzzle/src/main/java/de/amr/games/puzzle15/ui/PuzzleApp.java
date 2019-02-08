package de.amr.games.puzzle15.ui;

import static de.amr.games.puzzle15.solver.Heuristics.manhattanDistFromOrdered;
import static java.util.stream.Collectors.joining;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import de.amr.games.puzzle15.model.Puzzle15;
import de.amr.games.puzzle15.solver.Node;
import de.amr.games.puzzle15.solver.Solver;
import de.amr.games.puzzle15.solver.SolverAStar;

public class PuzzleApp extends JFrame {

	public static void main(String[] args) {
		EventQueue.invokeLater(PuzzleApp::new);
	}

	private Puzzle15 puzzle;
	private PuzzleView view;

	private Action actionSolve = new AbstractAction("Solve") {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Solving...");
			new SolverThread().execute();
		}
	};

	private class SolverThread extends SwingWorker<List<Node>, Void> {

		private Solver solver;

		@Override
		protected List<Node> doInBackground() throws Exception {
			// solver = new PuzzleSolverBestFirstSearch(node -> manhattanDistFromOrdered(node.getPuzzle()));
			solver = new SolverAStar(node -> manhattanDistFromOrdered(node.getPuzzle()));
			return solver.solve(puzzle);
		}

		@Override
		protected void done() {
			try {
				List<Node> solution = get();
				System.out.println("Max queue size " + solver.getMaxQueueSize());
				System.out.println("Found solution of length " + (solution.size() - 1));
				System.out.println(solution.stream().map(Node::getDir).filter(Objects::nonNull).map(Object::toString)
						.collect(joining(" ")));
				System.out.println();
				playSolution(solution);
			} catch (Exception x) {
				x.printStackTrace();
			}
			view.repaint();
		}

		private void playSolution(List<Node> solution) {
			Timer timer = new Timer(100, null);
			timer.addActionListener(e -> {
				if (solution.isEmpty()) {
					timer.stop();
				} else {
					setPuzzle(solution.remove(0).getPuzzle());
				}
			});
			timer.start();
		}
	}

	private Action actionShuffle = new AbstractAction("Shuffle") {

		@Override
		public void actionPerformed(ActionEvent e) {
			setPuzzle(Puzzle15.random());
			boolean solvable = puzzle.isSolvable();
			System.out.println(puzzle.isSolvable() ? "Solvable!" : "Not solvable!");
			actionSolve.setEnabled(solvable);
		}
	};

	public PuzzleApp() {
		puzzle = Puzzle15.ordered();
		setTitle("15-Puzzle");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		view = new PuzzleView(this, 100);
		view.bindKeyToAction('r', actionShuffle);
		view.bindKeyToAction('s', actionSolve);
		add(view);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public Puzzle15 getPuzzle() {
		return puzzle;
	}

	public void setPuzzle(Puzzle15 puzzle) {
		this.puzzle = puzzle;
		view.repaint();
	}

}