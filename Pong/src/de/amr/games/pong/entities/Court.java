package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.view.ViewController;

public class Court extends GameEntity implements ViewController {

	private int width;
	private int height;
	private Color bgColor = Color.BLACK;
	private Color lineColor = Color.WHITE;
	private int lineWidth = 5;

	public Court(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(lineColor);
		g.fillRect(0, 0, getWidth(), lineWidth);
		g.fillRect(0, getHeight() - lineWidth, getWidth(), lineWidth);
		g.fillRect(0, 0, lineWidth, getHeight());
		g.fillRect(getWidth() - lineWidth, 0, lineWidth, getHeight());
		g.fillRect(getWidth() / 2 - lineWidth / 2, 0, lineWidth, getHeight());
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}