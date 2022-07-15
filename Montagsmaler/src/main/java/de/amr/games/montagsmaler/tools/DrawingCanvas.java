package de.amr.games.montagsmaler.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * A canvas with interactive drawiing capability.
 * 
 * @author Armin Reichert
 */
public class DrawingCanvas extends JPanel {

	private static final Cursor BRUSH_CURSOR = Tools.createCursor("icons32/bullet_brush.png", new Point(0, 31),
			"Brush Cursor");

	private static final int CLEAR_AREA_SIZE = 80;

	private static final Image CLEAR_DRAWING = Tools.loadImageIcon("images/remove_256.png").getImage()
			.getScaledInstance(CLEAR_AREA_SIZE, CLEAR_AREA_SIZE, Image.SCALE_SMOOTH);

	private final MouseController mouseHandler = new MouseController();
	protected final BufferedImage buffer;
	protected final Graphics2D bufferContext;

	private class MouseController extends MouseAdapter {
		private int penSize = 8;

		private void clearOrDraw(MouseEvent e) {
			if (e.getPoint().distance(0, 0) <= CLEAR_AREA_SIZE) {
				clear();
			} else {
				draw(e.getX(), e.getY());
			}
		}

		private void draw(int x, int y) {
			bufferContext.setColor(getForeground());
			bufferContext.fillOval(x, y, penSize, penSize);
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			clearOrDraw(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			clearOrDraw(e);
		}
	}

	public DrawingCanvas(Dimension size) {
		buffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		bufferContext = buffer.createGraphics();
		setBackground(Color.BLACK);
		setEnabled(true);
		setPreferredSize(size);
		setMaximumSize(size);
	}

	public void clear() {
		bufferContext.setColor(getBackground());
		bufferContext.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
		repaint();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		enableInteractiveDrawing(enabled);
		repaint();
	}

	private void enableInteractiveDrawing(boolean enable) {
		if (enable) {
			// TODO check if mouseHandler is contained in array instead
			if (getMouseListeners().length == 0) {
				addMouseListener(mouseHandler);
			}
			if (getMouseMotionListeners().length == 0) {
				addMouseMotionListener(mouseHandler);
			}
			setCursor(BRUSH_CURSOR);
		} else {
			removeMouseListener(mouseHandler);
			removeMouseMotionListener(mouseHandler);
			setCursor(Cursor.getDefaultCursor());
		}
	}

	@Override
	protected final void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintBelowDrawing(g);
		g.drawImage(buffer, 0, 0, null);
	}

	protected void paintBelowDrawing(Graphics g) {
		if (isEnabled()) {
			bufferContext.drawImage(CLEAR_DRAWING, 0, 0, null);
		} else {
			bufferContext.clearRect(0, 0, CLEAR_AREA_SIZE, CLEAR_AREA_SIZE);
		}
	}
}
