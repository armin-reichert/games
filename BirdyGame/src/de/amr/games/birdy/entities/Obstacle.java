package de.amr.games.birdy.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.CollisionBoxSupplier;
import de.amr.games.birdy.BirdyGame;

/**
 * An obstacle consisting of a hanging and a standing pipe with a passage in the middle.
 * 
 * @author Armin Reichert
 */
public class Obstacle extends GameEntity {

	private int width;
	private int height;
	private Rectangle2D upperPart;
	private Rectangle2D lowerPart;
	private Rectangle2D passage;
	private Image pipeDown;
	private Image pipeUp;
	private boolean lighted;

	public Obstacle(BirdyGame app, int width, int height, int passageHeight, int centerY) {
		upperPart = new Rectangle2D.Double(0, 0, width, centerY - passageHeight / 2);
		passage = new Rectangle2D.Double(0, centerY - passageHeight / 2, width, passageHeight);
		lowerPart = new Rectangle2D.Double(0, centerY + passageHeight / 2, width,
				height - passage.getHeight() - upperPart.getHeight());
		pipeDown = app.assets.image("pipe_down").getScaledInstance(width, (int) upperPart.getHeight(),
				BufferedImage.SCALE_SMOOTH);
		pipeUp = app.assets.image("pipe_up").getScaledInstance(width, (int) lowerPart.getHeight(),
				BufferedImage.SCALE_SMOOTH);
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
		tr.move();
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tr.getX(), tr.getY());
		g.drawImage(pipeDown, 0, 0, null);
		if (lighted) {
			g.setColor(new Color(255, 255, 0, new Random().nextInt(170)));
			g.fillRect((int) passage.getX() + 3, (int) passage.getY(), (int) (passage.getWidth() - 6),
					(int) passage.getHeight());
		}
		g.drawImage(pipeUp, 0, (int) (upperPart.getHeight() + passage.getHeight()), null);
		g.translate(-tr.getX(), -tr.getY());
	}

	public void setLighted(boolean lighted) {
		this.lighted = lighted;
	}

	public CollisionBoxSupplier getUpperPart() {
		return () -> new Rectangle2D.Double(tr.getX(), tr.getY(), upperPart.getWidth(), upperPart.getHeight());
	}

	public CollisionBoxSupplier getLowerPart() {
		return () -> new Rectangle2D.Double(tr.getX(), tr.getY() + upperPart.getHeight() + passage.getHeight(),
				lowerPart.getWidth(), lowerPart.getHeight());
	}

	public CollisionBoxSupplier getPassage() {
		return () -> new Rectangle2D.Double(tr.getX(), tr.getY() + upperPart.getHeight(), passage.getWidth(),
				passage.getHeight());
	}
}