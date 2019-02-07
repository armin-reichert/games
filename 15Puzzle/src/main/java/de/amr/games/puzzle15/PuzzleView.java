package de.amr.games.puzzle15;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class PuzzleView extends JComponent {

	private PuzzleApp app;
	private int tileSize;
	private Font font;

	private class MouseHandler extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			int col = e.getX() / tileSize;
			int row = e.getY() / tileSize;
			int blankCol = Puzzle15.col(app.getPuzzle().blank());
			int blankRow = Puzzle15.row(app.getPuzzle().blank());
			if (blankCol == col) {
				if (blankRow == row - 1) {
					app.setPuzzle(app.getPuzzle().up());
				} else if (blankRow == row + 1) {
					app.setPuzzle(app.getPuzzle().down());
				}
			} else if (blankRow == row) {
				if (blankCol == col - 1) {
					app.setPuzzle(app.getPuzzle().left());
				} else if (blankCol == col + 1) {
					app.setPuzzle(app.getPuzzle().right());
				}
			}
			repaint();
		}
	}

	public PuzzleView(PuzzleApp app, int tileSize) {
		this.app = app;
		this.tileSize = tileSize;
		font = new Font(Font.SANS_SERIF, Font.BOLD, tileSize / 2);
		setPreferredSize(new Dimension(4 * tileSize, 4 * tileSize));
		addMouseListener(new MouseHandler());
		requestFocusInWindow();
	}

	public void onKey(char key, Action action) {
		getInputMap().put(KeyStroke.getKeyStroke(key), action.hashCode());
		getActionMap().put(action.hashCode(), action);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPuzzle((Graphics2D) g);
	}

	private void drawPuzzle(Graphics2D g) {
		Color blankColor = app.getPuzzle().isOrdered() ? Color.GREEN : Color.ORANGE;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		for (int row = 0; row < 4; ++row) {
			for (int col = 0; col < 4; ++col) {
				int number = app.getPuzzle().get(row, col);
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
