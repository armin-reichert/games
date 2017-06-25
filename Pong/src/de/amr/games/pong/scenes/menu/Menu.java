package de.amr.games.pong.scenes.menu;

import static de.amr.games.pong.PongGlobals.FONT;
import static de.amr.games.pong.PongGlobals.MENU_BACKGROUND;
import static de.amr.games.pong.PongGlobals.MENU_HIGHLIGHT;
import static de.amr.games.pong.PongGlobals.MENU_SELECTED_BACKGROUND;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pong.PongGame;
import de.amr.games.pong.PongGame.PlayMode;
import de.amr.games.pong.scenes.play.PongPlayScene;

public class Menu extends Scene<PongGame> {

	private static String getTitle(PlayMode mode) {
		switch (mode) {
		case Computer_Computer:
			return "Computer - Computer";
		case Computer_Player2:
			return "Computer - Player 2";
		case Player1_Computer:
			return "Player 1 - Computer";
		case Player1_Player2:
			return "Player 1 - Player 2";
		}
		return "";
	}

	public Menu(PongGame game) {
		super(game);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
			PlayMode[] values = PlayMode.values();
			int current = getApp().getPlayMode().ordinal();
			getApp().setPlayMode(current == values.length - 1 ? values[0] : values[current + 1]);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_UP)) {
			PlayMode[] values = PlayMode.values();
			int current = getApp().getPlayMode().ordinal();
			getApp().setPlayMode(current == 0 ? values[values.length - 1] : values[current - 1]);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			app.views.show(PongPlayScene.class);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(MENU_BACKGROUND);
		g.fillRect(0, 0, getApp().getWidth(), getHeight());
		g.setFont(FONT);
		PlayMode[] values = PlayMode.values();
		int y = 60;
		int h = getHeight() / values.length;
		for (int i = 0; i < values.length; ++i) {
			PlayMode mode = values[i];
			if (mode == getApp().getPlayMode()) {
				g.setColor(MENU_SELECTED_BACKGROUND);
				g.fillRect(0, h * i, getWidth(), h);
				g.setColor(MENU_HIGHLIGHT);
			} else {
				g.setColor(Color.WHITE);
			}
			int w = g.getFontMetrics().stringWidth(getTitle(mode));
			g.drawString(getTitle(mode), getWidth() / 2 - w / 2, y);
			y += h;
		}
	}
}