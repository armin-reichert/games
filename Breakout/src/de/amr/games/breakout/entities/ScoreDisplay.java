package de.amr.games.breakout.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.breakout.BreakoutGame;

public class ScoreDisplay extends GameEntity {

	private final BreakoutGame game;
	private Font font = new Font(Font.SANS_SERIF, Font.BOLD, 60);
	private Color color = Color.RED;

	public ScoreDisplay(BreakoutGame game) {
		this.game = game;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		String text = game.getScore().points + "";
		Rectangle2D bounds = fm.getStringBounds(text, g);
		int x = game.getWidth() / 2 - (int) bounds.getWidth() / 2;
		int y = game.getHeight() / 2 - fm.getDescent();
		g.drawString(text, x, y);
	}
}
