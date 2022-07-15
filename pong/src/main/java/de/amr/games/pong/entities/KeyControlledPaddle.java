package de.amr.games.pong.entities;

import de.amr.easy.game.input.Keyboard;

public class KeyControlledPaddle extends Paddle {

	public int upKey;
	public int downKey;

	public KeyControlledPaddle(int up, int down) {
		upKey = up;
		downKey = down;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Keyboard.keyDown(upKey)) {
			tf.vy = -speed;
		} else if (Keyboard.keyDown(downKey)) {
			tf.vy = speed;
		}
		move();
		tf.vy = 0;
	}
}