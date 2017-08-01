package de.amr.easy.game.input;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public enum MouseHandler implements MouseListener {

	INSTANCE;

	public static void handleMouseEventsFor(Component component) {
		component.addMouseListener(INSTANCE);
	}

	public static synchronized void poll() {
		INSTANCE._poll();
	}

	private MouseEvent event;

	private boolean clicked;
	private int x;
	private int y;

	@Override
	public synchronized void mouseClicked(MouseEvent event) {
		this.event = event;
	}

	private synchronized void _poll() {
		clicked = event != null;
		if (clicked) {
			x = event.getX();
			y = event.getY();
		}
		event = null;
	}

	public boolean clicked() {
		return clicked;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
};
