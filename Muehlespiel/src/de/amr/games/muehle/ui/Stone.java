package de.amr.games.muehle.ui;

import static de.amr.games.muehle.board.StoneType.BLACK;
import static de.amr.games.muehle.board.StoneType.WHITE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.muehle.board.StoneType;

public class Stone extends GameEntity {

	public static int radius = 20;

	private static final EnumMap<StoneType, Color> FILL = new EnumMap<>(StoneType.class);
	private static final EnumMap<StoneType, Color> EDGE = new EnumMap<>(StoneType.class);

	static {
		FILL.put(WHITE, new Color(255, 248, 220));
		FILL.put(BLACK, Color.DARK_GRAY);
		EDGE.put(WHITE, new Color(255, 248, 220).darker());
		EDGE.put(BLACK, Color.BLACK);
	}

	private StoneType type;

	public Stone(StoneType type) {
		this.type = type;
	}

	public StoneType getType() {
		return type;
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
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(tf.getX() - radius, tf.getY() - radius);
		g.setColor(FILL.get(type));
		g.fillOval(0, 0, 2 * radius, 2 * radius);
		g.setStroke(new BasicStroke(2));
		g.setColor(EDGE.get(type));
		g.drawOval(0, 0, 2 * radius, 2 * radius);
		g.translate(-tf.getX() + radius, -tf.getY() + radius);
	}
}
