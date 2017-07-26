package de.amr.games.muehle.mouse;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {

	private MouseEvent event;

	private boolean clicked;
	private int x;
	private int y;

	public synchronized void poll() {
		clicked = event != null;
		if (clicked) {
			x = event.getX();
			y = event.getY();
		}
		event = null;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		this.event = event;
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
};
