package de.amr.easy.game.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.Application;

public class DefaultView implements View {

	private final Application app;
	private Font font = new Font("Georgia", Font.BOLD, 36);
	private String text;
	private int width;
	private int y;

	public DefaultView(Application app) {
		this.app = app;
		text = app.getClass().getSimpleName();
		width = 0;
	}

	@Override
	public void init() {
		y = app.getHeight() * 3 / 4;
	}

	@Override
	public void update() {
		y -= 1;
		if (y <= 0) {
			y = app.getHeight();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.setFont(font);
		if (width == 0) {
			width = g.getFontMetrics().stringWidth(text);
		}
		g.drawString(text, (app.getWidth() - width) / 2, y);
	}
}
