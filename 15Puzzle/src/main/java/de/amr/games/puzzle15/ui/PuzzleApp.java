package de.amr.games.puzzle15.ui;

import static de.amr.games.puzzle15.solver.Solver.queueSizeOver;
import static de.amr.games.puzzle15.solver.Solver.runtimeOver;
import static java.util.stream.Collectors.joining;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
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

/**
 * 15-puzzle application.
 * 
 * @author Armin Reichert
 */
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
			selectedSolver = new SolverBestFirstSearch(Heuristics::manhattan, queueSizeOver(1_000_000));
		}
	};

	private Action actionSolveAStar = new AbstractAction("A* Search") {

		@Override
		public void actionPerformed(ActionEvent e) {
			selectedSolver = new SolverAStar(Heuristics::manhattan,
					queueSizeOver(1_000_000).or(runtimeOver(30_000)));
		}
	};

	private Action actionRunSolver = new AbstractAction("Solve") {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedSolver != null) {
				writeConsole(String.format("\nSolving puzzle using %s...", selectedSolverName()));
				new SolverTask().execute();
			}
		}
	};

	private Action actionPlaySolution = new AbstractAction("Play Solution") {

		@Override
		public void actionPerformed(ActionEvent event) {
			if (solution == null) {
				return;
			}
			savedPuzzle = puzzle;
			Timer playTimer = new Timer(500, null);
			Timer resetTimer = new Timer(2000, null);
			resetTimer.addActionListener(e -> {
				setPuzzle(savedPuzzle);
				resetTimer.stop();
				updateActionState();
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
			writeConsole(solvable ? "Solvable!" : "Not solvable!");
			updateActionState();
		}
	};

	private Action actionRandomMoves = new AbstractAction("Make Random Moves") {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int numMoves = Integer
						.valueOf(JOptionPane.showInputDialog(PuzzleApp.this, "How many random moves?", 10));
				setPuzzle(Puzzle15.randomMoves(numMoves));
				savedPuzzle = puzzle;
				setSolution(null);
			} catch (NumberFormatException x) {
			}
		}
	};

	private Action actionResetPuzzle = new AbstractAction("Reset") {

		@Override
		public void actionPerformed(ActionEvent e) {
			setPuzzle(Puzzle15.ordered());
			savedPuzzle = puzzle;
			setSolution(null);
		}
	};

	private class SolverTask extends SwingWorker<Optional<List<Node>>, Void> {

		@Override
		protected Optional<List<Node>> doInBackground() throws Exception {
			return selectedSolver.solve(puzzle);
		}

		@Override
		protected void done() {
			try {
				Optional<List<Node>> solution = get();
				if (!solution.isPresent()) {
					writeConsole("No solution found");
					setSolution(Collections.emptyList());
					return;
				}
				setSolution(solution.get());
				writeConsole("Max queue size " + selectedSolver.getMaxFrontierSize());
				writeConsole("Found solution of length " + (solution.get().size() - 1));
				writeConsole(solution.get().stream().map(Node::getDir).filter(Objects::nonNull).map(Object::toString)
						.collect(joining(" ")));
			} catch (ExecutionException x) {
				writeConsole("Solver aborted: " + x.getMessage());
				x.printStackTrace();
			} catch (InterruptedException x) {
				writeConsole("Solver interrupted: " + x.getMessage());
				x.printStackTrace();
			}
			view.repaint();
		}
	}

	private void writeConsole(String text) {
		console.append(text + "\n");
		System.out.println(text);
	}

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

	private void setSolution(List<Node> solution) {
		this.solution = solution;
		updateActionState();
	}

	private void updateActionState() {
		boolean solvable = puzzle.isSolvable();
		actionRunSolver.setEnabled(solvable);
		actionSolveAStar.setEnabled(solvable);
		actionSolveBestFirst.setEnabled(solvable);
		actionSolveBFS.setEnabled(solvable);
		actionPlaySolution.setEnabled(solution != null && solution.size() > 0);
	}

	public Puzzle15 getPuzzle() {
		return puzzle;
	}

	public void setPuzzle(Puzzle15 puzzle) {
		this.puzzle = puzzle;
		view.repaint();
	}

	public PuzzleApp() {
		puzzle = Puzzle15.ordered();

		setTitle("15-Puzzle");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		view = new PuzzleView(this, 100);
		add(view, BorderLayout.CENTER);

		console = new JTextArea();
		console.setBackground(Color.BLACK);
		console.setForeground(Color.GREEN);
		console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		console.setEditable(false);
		console.setColumns(80);
		console.setLineWrap(true);
		add(new JScrollPane(console), BorderLayout.EAST);

		setJMenuBar(new JMenuBar());

		JMenu puzzleMenu = new JMenu("Puzzle");
		getJMenuBar().add(puzzleMenu);
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
		getJMenuBar().add(solverMenu);
		bg.getElements().nextElement().setSelected(true);
		selectedSolver = new SolverBestFirstSearch(Heuristics::manhattan, queueSizeOver(1_000_000));

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}