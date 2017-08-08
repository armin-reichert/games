package de.amr.games.muehle.ui;

import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.muehle.board.StoneColor;

public class Stone extends GameEntity {

	public static int radius = 20;

	private static final EnumMap<StoneColor, Color> FILL = new EnumMap<>(StoneColor.class);
	private static final EnumMap<StoneColor, Color> EDGE = new EnumMap<>(StoneColor.class);

	static {
		FILL.put(WHITE, new Color(255, 248, 220));
		FILL.put(BLACK, Color.BLACK);
		EDGE.put(WHITE, new Color(255, 248, 220).darker());
		EDGE.put(BLACK, Color.DARK_GRAY);
	}

	private StoneColor color;

	public Stone(StoneColor color) {
		this.color = color;
	}

	public StoneColor getColor() {
		return color;
	}

	@Override
	public int getWidth() {
		return 2 * radius;
	}

	@Override
	public int getHeight() {
		return 2 * radius;
	}

	@Override
	public void draw(Graphics2D g) {
		int diameter = 2 * radius;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(tf.getX() - radius, tf.getY() - radius);
		g.setColor(FILL.get(color));
		g.fillOval(0, 0, diameter, diameter);
		g.setStroke(new BasicStroke(3));
		g.setColor(EDGE.get(color));
		g.setStroke(new BasicStroke(2));
		g.drawOval(0, 0, diameter, diameter);
		g.drawOval(diameter / 6, diameter / 6, 2 * diameter / 3, 2 * diameter / 3);
		g.drawOval(diameter / 3, diameter / 3, diameter / 3, diameter / 3);
		g.translate(-tf.getX() + radius, -tf.getY() + radius);
	}
}
