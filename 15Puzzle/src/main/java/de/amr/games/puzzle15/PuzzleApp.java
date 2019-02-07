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

		@Override
		protected List<Node> doInBackground() throws Exception {
			return new PuzzleSolverBFS().solve(puzzle);
		}

		@Override
		protected void done() {
			try {
				get().stream().filter(node -> node.getDir() != null).forEach(node -> {
					System.out.print(node.getDir() + " ");
				});
				System.out.println();
				playSolution(get());
			} catch (Exception x) {
				x.printStackTrace();
			}
			view.repaint();
		}
	}

	private Action actionShuffle = new AbstractAction("Shuffle") {

		@Override
		public void actionPerformed(ActionEvent e) {
			puzzle = Puzzle15.shuffled(10);
			view.repaint();
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

	private void playSolution(List<Node> solution) {

		Timer timer = new Timer(1000, null);
		timer.addActionListener(e -> {
			if (solution.isEmpty()) {
				timer.stop();
			} else {
				Node node = solution.remove(0);
				puzzle = node.getPuzzle();
				if (node.getDir() != null) {
					System.out.println(node.getDir());
				}
				System.out.println(puzzle);
				view.repaint();
			}
		});
		timer.start();
	}

}