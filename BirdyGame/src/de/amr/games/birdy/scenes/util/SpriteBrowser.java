package de.amr.games.birdy.scenes.util;

import static de.amr.games.birdy.BirdyGame.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.amr.easy.game.input.Key;
import de.amr.easy.game.scene.Scene;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.scenes.start.StartScene;

public class SpriteBrowser extends Scene<BirdyGame> {

	private final Font font = new Font("Courier New", Font.PLAIN, 16);
	private final List<String> spriteNames;
	private int index;

	public SpriteBrowser(BirdyGame game) {
		super(game);
		spriteNames = new ArrayList<>();
		for (String name : Game.assets.imageNames()) {
			spriteNames.add(name);
		}
		Collections.sort(spriteNames);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Key.pressedOnce(KeyEvent.VK_RIGHT)) {
			index = index + 1 == spriteNames.size() ? 0 : index + 1;
		} else if (Key.pressedOnce(KeyEvent.VK_LEFT)) {
			index = index == 0 ? spriteNames.size() - 1 : index - 1;
		} else if (Key.pressedOnce(KeyEvent.VK_X)) {
			Game.views.show(StartScene.class);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		String name = spriteNames.get(index);
		BufferedImage image = Game.assets.image(name);
		String text = name + " (" + image.getWidth() + "x" + image.getHeight() + ")";
		text += " (Keys: LEFT=Previous, RIGHT=Next, X=Exit)";
		g.setColor(Color.WHITE);
		g.setFont(font);
		g.drawImage(image, 0, 0, null);
		g.drawString(text, 0, getApp().getHeight() - 2 * font.getSize());
	}
}
