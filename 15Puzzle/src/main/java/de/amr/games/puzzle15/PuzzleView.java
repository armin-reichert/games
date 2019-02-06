package de.amr.games.puzzle15;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
			puzzle = puzzle.shuffle(100);
			repaint();
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
		requestFocus();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPuzzle((Graphics2D) g);
	}

	private void drawPuzzle(Graphics2D g) {
		Color solvedColor = puzzle.isSolved() ? Color.GREEN : Color.ORANGE;
		for (int row = 0; row < puzzle.size(); ++row) {
			for (int col = 0; col < puzzle.size(); ++col) {
				int number = puzzle.get(row, col);
				String text = number != 0 ? String.valueOf(number) : "";
				g.translate(col * tileSize, row * tileSize);
				g.setColor(number != 0 ? Color.LIGHT_GRAY : solvedColor);
				g.fillRect(0, 0, tileSize, tileSize);
				g.setColor(Color.DARK_GRAY);
				g.setStroke(new BasicStroke(4));
				g.drawRect(0, 0, tileSize, tileSize);
				g.setColor(Color.BLACK);
				g.setFont(font);
				Rectangle2D box = g.getFontMetrics().getStringBounds(text, g);
				int dx = (tileSize - (int) box.getWidth()) / 2;
				int dy = (tileSize + g.getFontMetrics().getAscent()) / 2;
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.drawString(text, dx, dy);
				g.translate(-col * tileSize, -row * tileSize);
			}
		}
	}

}
