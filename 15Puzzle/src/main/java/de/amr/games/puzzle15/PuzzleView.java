package de.amr.games.puzzle15;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

public class PuzzleView extends JComponent {

	private Puzzle15 puzzle;
	private int tileSize;
	private Font font;

	private Action actionShuffle = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// puzzle = Puzzle15.random();
			puzzle = Puzzle15.shuffled(10);
			repaint();
		}
	};

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
			return new PuzzleSolver().solve(puzzle);
		}

		@Override
		protected void done() {
			try {
				get().stream().filter(node -> node.getDir() != null).forEach(node -> {
					System.out.print(node.getDir() + " ");
				});
				System.out.println();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	private class MouseHandler extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			int col = e.getX() / tileSize;
			int row = e.getY() / tileSize;
			int blankCol = puzzle.col(puzzle.blank());
			int blankRow = puzzle.row(puzzle.blank());
			if (blankCol == col) {
				if (blankRow == row - 1) {
					puzzle = puzzle.up();
				} else if (blankRow == row + 1) {
					puzzle = puzzle.down();
				}
			} else if (blankRow == row) {
				if (blankCol == col - 1) {
					puzzle = puzzle.left();
				} else if (blankCol == col + 1) {
					puzzle = puzzle.right();
				}
			}
			repaint();
		}
	}

	public PuzzleView(Puzzle15 puzzle, int tileSize) {
		this.puzzle = puzzle;
		this.tileSize = tileSize;
		font = new Font(Font.SANS_SERIF, Font.BOLD, tileSize / 2);
		setPreferredSize(new Dimension(4 * tileSize, 4 * tileSize));
		addMouseListener(new MouseHandler());
		getInputMap().put(KeyStroke.getKeyStroke('r'), "random");
		getActionMap().put("random", actionShuffle);
		getInputMap().put(KeyStroke.getKeyStroke('s'), "solve");
		getActionMap().put("solve", actionSolve);
		requestFocus();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPuzzle((Graphics2D) g);
	}

	private void drawPuzzle(Graphics2D g) {
		Color blankColor = puzzle.isOrdered() ? Color.GREEN : Color.ORANGE;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < 4; ++col) {
				int number = puzzle.get(row, col);
				g.translate(col * tileSize, row * tileSize);
				if (number == 0) {
					g.setColor(blankColor);
					g.fillRect(0, 0, tileSize, tileSize);
				} else {
					g.setColor(Color.BLACK);
					g.setFont(font);
					FontMetrics fm = g.getFontMetrics();
					String text = String.valueOf(number);
					Rectangle2D box = fm.getStringBounds(text, g);
					g.drawString(text, (tileSize - (int) box.getWidth()) / 2, tileSize - fm.getAscent() / 2);
				}
				g.setColor(Color.DARK_GRAY);
				g.drawRect(0, 0, tileSize, tileSize);
				g.translate(-col * tileSize, -row * tileSize);
			}
		}
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

}
