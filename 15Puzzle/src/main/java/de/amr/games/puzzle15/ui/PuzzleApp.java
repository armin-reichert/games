package de.amr.games.puzzle15.ui;

import static java.util.stream.Collectors.joining;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.games.puzzle15.model.Puzzle15;
import de.amr.games.puzzle15.solver.Heuristics;
import de.amr.games.puzzle15.solver.Node;
import de.amr.games.puzzle15.solver.Solver;
import de.amr.games.puzzle15.solver.SolverAStar;
import de.amr.games.puzzle15.solver.SolverBFS;
import de.amr.games.puzzle15.solver.SolverBestFirstSearch;

public class PuzzleApp extends JFrame {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(PuzzleApp::new);
	}

	private Puzzle15 puzzle;
	private PuzzleView view;
	private JTextArea console;

	private Action actionSolveBFS = new AbstractAction("Breadth-First Search") {

		@Override
		public void actionPerformed(ActionEvent e) {
			writeConsole("Solving...");
			new SolverThread(new SolverBFS()).execute();
		}
	};

	private Action actionSolveBestFirst = new AbstractAction("Best-First Search") {

		@Override
		public void actionPerformed(ActionEvent e) {
			writeConsole("Solving...");
			new SolverThread(
					new SolverBestFirstSearch(node -> Heuristics.manhattanDistFromOrdered(node.getPuzzle()))).execute();
		}
	};

	private Action actionSolveAStar = new AbstractAction("A* Search") {

		@Override
		public void actionPerformed(ActionEvent e) {
			writeConsole("Solving...");
			new SolverThread(new SolverAStar(node -> Heuristics.manhattanDistFromOrdered(node.getPuzzle())))
					.execute();
		}
	};

	private class SolverThread extends SwingWorker<List<Node>, Void> {

		private Solver solver;

		public SolverThread(Solver solver) {
			this.solver = solver;
		}

		@Override
		protected List<Node> doInBackground() throws Exception {
			// solver = new PuzzleSolverBestFirstSearch(node -> manhattanDistFromOrdered(node.getPuzzle()));
			// solver = new SolverAStar(node -> manhattanDistFromOrdered(node.getPuzzle()));
			return solver.solve(puzzle);
		}

		@Override
		protected void done() {
			try {
				List<Node> solution = get();
				writeConsole("Max queue size " + solver.getMaxQueueSize());
				writeConsole("Found solution of length " + (solution.size() - 1));
				writeConsole(solution.stream().map(Node::getDir).filter(Objects::nonNull).map(Object::toString)
						.collect(joining(" ")));
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
			setPuzzle(Puzzle15.shuffled());
			boolean solvable = puzzle.isSolvable();
			writeConsole(puzzle.isSolvable() ? "Solvable!" : "Not solvable!");
			actionSolveBFS.setEnabled(solvable);
			actionSolveBestFirst.setEnabled(solvable);
			actionSolveAStar.setEnabled(solvable);
		}
	};

	private Action actionRandomMoves = new AbstractAction("Make 100 Random Moves") {

		@Override
		public void actionPerformed(ActionEvent e) {
			setPuzzle(Puzzle15.randomMoves(100));
		}
	};

	private void writeConsole(String text) {
		console.append(text + "\n");
		System.out.println(text);
	}

	public PuzzleApp() {
		puzzle = Puzzle15.ordered();
		setTitle("15-Puzzle");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		view = new PuzzleView(this, 100);
		add(view);

		console = new JTextArea();
		console.setBackground(Color.BLACK);
		console.setForeground(Color.GREEN);
		console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		console.setEditable(false);
		console.setColumns(80);
		console.setLineWrap(true);
		add(new JScrollPane(console), BorderLayout.EAST);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu puzzleMenu = new JMenu("Puzzle");
		menuBar.add(puzzleMenu);
		puzzleMenu.add(actionRandomMoves);
		puzzleMenu.add(actionShuffle);

		JMenu solverMenu = new JMenu("Solver");
		solverMenu.add(actionSolveBFS);
		solverMenu.add(actionSolveBestFirst);
		solverMenu.add(actionSolveAStar);
		menuBar.add(solverMenu);

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