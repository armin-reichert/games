package de.amr.games.pacman.scenes;

import static de.amr.easy.game.Application.Assets;
import static de.amr.easy.game.Application.GameLoop;

import java.awt.Image;
import java.awt.image.BufferedImage;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.PacManGame;

public class TestScene extends Scene<PacManGame> {

	public TestScene(PacManGame app) {
		super(app);
	}

	@Override
	public void init() {
		GameLoop.setFrameRate(60);
		GameLoop.log = true;
		BufferedImage sheet = Assets.image("pacman_original.png");
		BufferedImage maze = sheet.getSubimage(0, 0, 224, sheet.getHeight());
		Image scaled = maze.getScaledInstance(getWidth(), getHeight(), BufferedImage.SCALE_SMOOTH);
		setBgImage(scaled);
	}

	@Override
	public void update() {
		try {
			Thread.sleep(6);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
