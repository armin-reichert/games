package de.amr.games.puzzle15;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public class PuzzleApp extends JFrame {

	public static void main(String[] args) {
		EventQueue.invokeLater(PuzzleApp::new);
	}

	private Puzzle15 puzzle;
	private PuzzleView view;

	private Action actionSolve = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Solving...");
			new SolverThread().execute();
		}
	};

	private class SolverThread extends SwingWorker<List<Node>, Void> {

		private PuzzleSolver solver = new PuzzleSolverBestFirstSearch();

		@Override
		protected List<Node> doInBackground() throws Exception {
			return solver.solve(puzzle);
		}

		@Override
		protected void done() {
			try {
				List<Node> solution = get();
				System.out.println("Max queue size " + solver.getMaxQueueSize());
				System.out.println("Found solution of length " + solution.size());
				solution.stream().filter(node -> node.getDir() != null).forEach(node -> {
					System.out.print(node.getDir() + " ");
				});
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
			// setPuzzle(Puzzle15.shuffled(20));
			setPuzzle(Puzzle15.random());
		}
	};

	public PuzzleApp() {
		puzzle = Puzzle15.ordered();
		view = new PuzzleView(this, 100);
		view.onKey('r', actionShuffle);
		view.onKey('s', actionSolve);
		setTitle("15-Puzzle");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
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