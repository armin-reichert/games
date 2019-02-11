package de.amr.games.puzzle15.ui;

import static de.amr.games.puzzle15.solver.Heuristics.manhattanDistFromOrdered;
import static java.util.stream.Collectors.joining;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.games.puzzle15.model.Puzzle15;
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

	private static Predicate<Solver> queueSizeOver(int size) {
		return solver -> solver.getMaxQueueSize() > size;
	}

	private static Predicate<Solver> runtimeOver(int millis) {
		return solver -> solver.runningTimeMillis() > millis;
	}

	private Puzzle15 puzzle, savedPuzzle;
	private PuzzleView view;
	private JTextArea console;
	private Solver selectedSolver;
	private List<Node> solution;

	private Action actionSolveBFS = new AbstractAction("Breadth-First Search") {

		@Override
		public void actionPerformed(ActionEvent e) {
			selectedSolver = new SolverBFS(queueSizeOver(1_000_000));
		}
	};

	private Action actionSolveBestFirst = new AbstractAction("Best-First Search") {

		@Override
		public void actionPerformed(ActionEvent e) {
			selectedSolver = new SolverBestFirstSearch(node -> manhattanDistFromOrdered(node.getPuzzle()),
					queueSizeOver(1_000_000));
		}
	};

	private Action actionSolveAStar = new AbstractAction("A* Search") {

		@Override
		public void actionPerformed(ActionEvent e) {
			selectedSolver = new SolverAStar(node -> manhattanDistFromOrdered(node.getPuzzle()),
					queueSizeOver(1_000_000).or(runtimeOver(10_000)));
		}
	};

	private Action actionRunSolver = new AbstractAction("Solve") {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedSolver != null) {
				writeConsole(String.format("\nSolving puzzle using %s...", selectedSolverName()));
				new SolverThread().execute();
			}
		}
	};

	private String selectedSolverName() {
		if (selectedSolver.getClass() == SolverBFS.class) {
			return "Breadth-First Search";
		}
		if (selectedSolver.getClass() == SolverBestFirstSearch.class) {
			return "Best-First Search";
		}
		if (selectedSolver.getClass() == SolverAStar.class) {
			return "A* Search";
		}
		return "";
	}

	private class SolverThread extends SwingWorker<List<Node>, Void> {

		@Override
		protected List<Node> doInBackground() throws Exception {
			return selectedSolver.solve(puzzle);
		}

		@Override
		protected void done() {
			try {
				setSolution(get());
				writeConsole("Max queue size " + selectedSolver.getMaxQueueSize());
				writeConsole("Found solution of length " + (solution.size() - 1));
				writeConsole(solution.stream().map(Node::getDir).filter(Objects::nonNull).map(Object::toString)
						.collect(joining(" ")));
			} catch (ExecutionException x) {
				writeConsole("Solving aborted: " + x.getMessage());
			} catch (InterruptedException x) {
				writeConsole("Solving interrupted: " + x.getMessage());
			}
			view.repaint();
		}
	}

	private Action actionPlaySolution = new AbstractAction("Play Solution") {

		@Override
		public void actionPerformed(ActionEvent event) {
			if (solution == null) {
				return;
			}
			savedPuzzle = puzzle;
			Timer playTimer = new Timer(100, null);
			Timer resetTimer = new Timer(2000, null);
			resetTimer.addActionListener(e -> {
				setPuzzle(savedPuzzle);
				resetTimer.stop();
			});
			playTimer.addActionListener(e -> {
				if (solution.isEmpty()) {
					playTimer.stop();
					resetTimer.start();
				} else {
					setPuzzle(solution.remove(0).getPuzzle());
				}
			});
			playTimer.start();
		}
	};

	private Action actionShuffle = new AbstractAction("Shuffle") {

		@Override
		public void actionPerformed(ActionEvent e) {
			setPuzzle(Puzzle15.shuffled());
			savedPuzzle = puzzle;
			setSolution(null);
			boolean solvable = puzzle.isSolvable();
			writeConsole(puzzle.isSolvable() ? "Solvable!" : "Not solvable!");
			actionRunSolver.setEnabled(solvable);
		}
	};

	private Action actionRandomMoves = new AbstractAction("Make 100 Random Moves") {

		@Override
		public void actionPerformed(ActionEvent e) {
			setPuzzle(Puzzle15.randomMoves(100));
			savedPuzzle = puzzle;
			setSolution(null);
		}
	};

	private Action actionResetPuzzle = new AbstractAction("Reset Puzzle") {

		@Override
		public void actionPerformed(ActionEvent e) {
			setPuzzle(Puzzle15.ordered());
			savedPuzzle = puzzle;
			setSolution(null);
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
		puzzleMenu.add(actionResetPuzzle);

		JMenu solverMenu = new JMenu("Solver");
		solverMenu.add(actionRunSolver);
		solverMenu.add(actionPlaySolution);
		solverMenu.addSeparator();
		ButtonGroup bg = new ButtonGroup();
		bg.add(solverMenu.add(new JRadioButtonMenuItem(actionSolveBestFirst)));
		bg.add(solverMenu.add(new JRadioButtonMenuItem(actionSolveAStar)));
		bg.add(solverMenu.add(new JRadioButtonMenuItem(actionSolveBFS)));
		menuBar.add(solverMenu);
		bg.getElements().nextElement().setSelected(true);
		selectedSolver = new SolverBestFirstSearch(node -> manhattanDistFromOrdered(node.getPuzzle()),
				queueSizeOver(1_000_000));

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

	private void setSolution(List<Node> solution) {
		this.solution = solution;
		actionPlaySolution.setEnabled(solution != null && !solution.isEmpty());
	}

}