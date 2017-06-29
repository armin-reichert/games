package de.amr.easy.game.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.entity.collision.CollisionBoxSupplier;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.game.view.View;

public class GameEntity implements View, CollisionBoxSupplier {

	public final Transform tr = new Transform();
	private Sprite[] sprites;
	private String name;

	public GameEntity(Sprite... sprites) {
		this.sprites = sprites;
		this.name = "unnamed";
	}

	@Override
	public void draw(Graphics2D g) {
		if (currentSprite() != null) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.translate(tr.getX(), tr.getY());
			g2.rotate(tr.getRotation());
			currentSprite().draw(g2);
			g2.dispose();
		}
	}

	protected void drawCollisionBox(Graphics2D g, Color color) {
		Rectangle2D box = getCollisionBox();
		g.setColor(color);
		g.draw(box);
	}

	public Sprite currentSprite() {
		return sprites.length == 0 ? null : sprites[0];
	}

	public Sprite getSprite(int i) {
		return sprites[i];
	}

	protected Sprite[] getSprites() {
		return sprites;
	}

	public void setSprites(Sprite... sprites) {
		this.sprites = sprites;
	}

	public void setAnimated(boolean animated) {
		for (Sprite sprite : sprites) {
			sprite.setAnimated(animated);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return currentSprite() != null ? currentSprite().getWidth() : 0;
	}

	public int getHeight() {
		return currentSprite() != null ? currentSprite().getHeight() : 0;
	}

	@Override
	public Rectangle2D getCollisionBox() {
		return new Rectangle2D.Double(tr.getX(), tr.getY(), getWidth(), getHeight());
	}

	public Vector2 getCenter() {
		return new Vector2(tr.getX() + getWidth() / 2, tr.getY() + getHeight() / 2);
	}

	public void centerHor(int width) {
		tr.setX((width - getWidth()) / 2);
	}

	public void centerVert(int height) {
		tr.setY((height - getHeight()) / 2);
	}

	public void center(int width, int height) {
		tr.moveTo((width - getWidth()) / 2, (height - getHeight()) / 2);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
