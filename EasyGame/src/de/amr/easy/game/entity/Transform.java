package de.amr.easy.game.entity;

import de.amr.easy.game.math.Vector2;

public class Transform {

	private final Vector2 position;
	private final Vector2 velocity;
	private double rotation;

	public Transform() {
		position = new Vector2(0, 0);
		velocity = new Vector2(0, 0);
		rotation = 0.0;
	}

	public float getX() {
		return position.x;
	}

	public float getY() {
		return position.y;
	}

	public void setX(float x) {
		position.x = x;
	}

	public void setY(float y) {
		position.y = y;
	}

	public void moveTo(float x, float y) {
		position.assign(x, y);
	}

	public void moveTo(Vector2 p) {
		position.assign(p);
	}

	public void move() {
		position.add(velocity);
	}

	public void setVel(float vx, float vy) {
		velocity.assign(vx, vy);
	}

	public void setVel(Vector2 v) {
		velocity.assign(v);
	}

	public void setVelX(float vx) {
		velocity.x = vx;
	}

	public void setVelY(float vy) {
		velocity.y = vy;
	}

	public float getVelX() {
		return velocity.x;
	}

	public float getVelY() {
		return velocity.y;
	}

	public double getRot() {
		return rotation;
	}

	public void setRot(double rotation) {
		this.rotation = rotation;
	}
}
