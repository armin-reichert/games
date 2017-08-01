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

	boolean clicked;
	boolean pressed;
	boolean released;
	int x;
	int y;
	int button;

	private boolean clickedDetected;
	private boolean pressedDetected;
	private boolean releasedDetected;
	private MouseEvent event;

	@Override
	public synchronized void mouseClicked(MouseEvent event) {
		this.event = event;
		clickedDetected = true;
	}

	@Override
	public synchronized void mousePressed(MouseEvent event) {
		this.event = event;
		pressedDetected = true;
	}

	@Override
	public synchronized void mouseReleased(MouseEvent event) {
		this.event = event;
		releasedDetected = true;
	}

	@Override
	public synchronized void mouseEntered(MouseEvent event) {
		this.event = event;
	}

	@Override
	public synchronized void mouseExited(MouseEvent event) {
		this.event = event;
	}

	private synchronized void _poll() {
		clicked = clickedDetected;
		pressed = pressedDetected;
		released = releasedDetected;
		x = event != null ? event.getX() : -1;
		y = event != null ? event.getY() : -1;

		button = event != null ? event.getButton() : -1;
		clickedDetected = pressedDetected = releasedDetected = false;
		event = null;
	}
};
