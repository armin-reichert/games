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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class PuzzleView extends JComponent {

	private Puzzle puzzle;
	private int tileSize;
	private Font font;

	private Action actionShuffle = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			puzzle = puzzle.shuffle(50);
			repaint();
		}
	};
	
	private Action actionShowChildren = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			puzzle.possibleMoves().forEach(dir -> {
				System.out.println(dir);
				puzzle.move(dir).println();
			});
		}
	};

	private class MouseHandler extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			int col = e.getX() / tileSize;
			int row = e.getY() / tileSize;
			int number = puzzle.get(row, col);
			int blankCol = puzzle.col(puzzle.blank());
			int blankRow = puzzle.row(puzzle.blank());
			if (blankCol == col) {
				if (blankRow == row - 1) {
					puzzle = puzzle.up();
					System.out.println("UP: " + number);
					repaint();
				} else if (blankRow == row + 1) {
					puzzle = puzzle.down();
					System.out.println("DOWN: " + number);
					repaint();
				}
			} else if (blankRow == row) {
				if (blankCol == col - 1) {
					puzzle = puzzle.left();
					System.out.println("LEFT: " + number);
					repaint();
				} else if (blankCol == col + 1) {
					puzzle = puzzle.right();
					System.out.println("RIGHT: " + number);
					repaint();
				}
			}
		}
	}

	public PuzzleView(Puzzle puzzle, int tileSize) {
		this.puzzle = puzzle;
		this.tileSize = tileSize;
		font = new Font(Font.SANS_SERIF, Font.BOLD, tileSize / 2);
		setPreferredSize(new Dimension(puzzle.size() * tileSize, puzzle.size() * tileSize));
		addMouseListener(new MouseHandler());
		getInputMap().put(KeyStroke.getKeyStroke('s'), "shuffle");
		getActionMap().put("shuffle", actionShuffle);
		getInputMap().put(KeyStroke.getKeyStroke('n'), "next");
		getActionMap().put("next", actionShowChildren);
		requestFocus();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPuzzle((Graphics2D) g);
	}

	private void drawPuzzle(Graphics2D g) {
		Color blankColor = puzzle.isSolved() ? Color.GREEN : Color.ORANGE;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		for (int row = 0; row < puzzle.size(); ++row) {
			for (int col = 0; col < puzzle.size(); ++col) {
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
