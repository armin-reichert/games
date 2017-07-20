package de.amr.games.breakout.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.breakout.BreakoutGame;

public class ScoreDisplay extends GameEntity {

	private final BreakoutGame app;
	private Font font = new Font(Font.SANS_SERIF, Font.BOLD, 60);
	private Color color = Color.RED;

	public ScoreDisplay(BreakoutGame app) {
		this.app = app;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		String text = app.score.points + "";
		Rectangle2D bounds = fm.getStringBounds(text, g);
		int x = app.getWidth() / 2 - (int) bounds.getWidth() / 2;
		int y = app.getHeight() / 2 - fm.getDescent();
		g.drawString(text, x, y);
	}
}