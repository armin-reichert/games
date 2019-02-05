package de.amr.games.puzzle15;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

public class PuzzleView extends JComponent {

	private static final int TILE_SIZE = 80;

	private Puzzle puzzle;
	private Font font;
	private MouseHandler mouse;

	private class MouseHandler extends MouseAdapter {

		private int row;
		private int col;

		@Override
		public void mouseClicked(MouseEvent e) {
			col = e.getX() / TILE_SIZE;
			row = e.getY() / TILE_SIZE;
			System.out.println("Clicked on number " + puzzle.get(row, col));
		}
	}

	public PuzzleView(Puzzle puzzle) {
		this.puzzle = puzzle;
		font = new Font(Font.SANS_SERIF, Font.BOLD, TILE_SIZE / 2);
		setPreferredSize(new Dimension(puzzle.size() * TILE_SIZE, puzzle.size() * TILE_SIZE));
		mouse = new MouseHandler();
		addMouseListener(mouse);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawPuzzle((Graphics2D) g);
	}

	private void drawPuzzle(Graphics2D g) {
		for (int row = 0; row < puzzle.size(); ++row) {
			for (int col = 0; col < puzzle.size(); ++col) {
				int number = puzzle.get(row, col);
				String text = number != 0 ? String.valueOf(number) : "";
				g.translate(col * TILE_SIZE, row * TILE_SIZE);
				g.setColor(number != 0 ? Color.LIGHT_GRAY : Color.ORANGE);
				g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
				g.setColor(Color.DARK_GRAY);
				g.drawRect(0, 0, TILE_SIZE, TILE_SIZE);
				g.setColor(Color.BLACK);
				g.setFont(font);
				Rectangle2D box = g.getFontMetrics().getStringBounds(text, g);
				int dx = (TILE_SIZE - (int) box.getWidth()) / 2;
				int dy = (TILE_SIZE + g.getFontMetrics().getAscent()) / 2;
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.drawString(text, dx, dy);
				g.translate(-col * TILE_SIZE, -row * TILE_SIZE);
			}
		}
	}

}
