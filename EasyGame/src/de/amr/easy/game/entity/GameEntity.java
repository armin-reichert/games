package de.amr.easy.game.entity;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.entity.collision.CollisionBoxSupplier;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.game.view.View;

/**
 * Base class for game entities. Provides a {@link Transform transform} object for storing position,
 * velocity and rotation. A game entity can store a list of sprites. By overriding the
 * {@link #currentSprite()} method, the sprite used for drawing is defined.
 * <p>
 * Game entities can be stored and accessed in the entity set of an application which serves as a
 * generic container for the application's entities.
 * 
 * @author Armin Reichert
 */
public class GameEntity implements View, CollisionBoxSupplier {

	public final Transform tf = new Transform();

	private Sprite[] sprites;
	private String name;

	public GameEntity(Sprite... sprites) {
		this.sprites = sprites;
		this.name = toString();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		if (currentSprite() != null) {
			Graphics2D pen = (Graphics2D) g.create();
			pen.translate(tf.getX(), tf.getY());
			pen.rotate(tf.getRotation());
			currentSprite().draw(pen);
			pen.dispose();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return currentSprite() != null ? currentSprite().getWidth() : 0;
	}

	public int getHeight() {
		return currentSprite() != null ? currentSprite().getHeight() : 0;
	}

	@Override
	public Rectangle2D getCollisionBox() {
		return new Rectangle2D.Double(tf.getX(), tf.getY(), getWidth(), getHeight());
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

	public Vector2 getCenter() {
		return new Vector2(tf.getX() + getWidth() / 2, tf.getY() + getHeight() / 2);
	}

	public void hCenter(int width) {
		tf.setX((width - getWidth()) / 2);
	}

	public void vCenter(int height) {
		tf.setY((height - getHeight()) / 2);
	}

	public void center(int width, int height) {
		tf.moveTo((width - getWidth()) / 2, (height - getHeight()) / 2);
	}
}
