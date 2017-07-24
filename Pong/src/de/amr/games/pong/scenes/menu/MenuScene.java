package de.amr.games.pong.scenes.menu;

import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.pong.PongGame.PlayMode.Computer_Computer;
import static de.amr.games.pong.PongGame.PlayMode.Computer_Player2;
import static de.amr.games.pong.PongGame.PlayMode.Player1_Computer;
import static de.amr.games.pong.PongGame.PlayMode.Player1_Player2;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_ENTER;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.amr.easy.game.Application;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.pong.PongGame;
import de.amr.games.pong.PongGame.PlayMode;
import de.amr.games.pong.scenes.play.PongPlayScene;

/**
 * The menu scene of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class MenuScene extends Scene<PongGame> {

	private class MenuControl extends StateMachine<PlayMode, String> {

		public MenuControl() {
			super("Pong Menu", PlayMode.class, Player1_Player2);
			change(Player1_Player2, Player1_Computer, () -> keyPressedOnce(VK_DOWN));
			change(Player1_Computer, Computer_Player2, () -> keyPressedOnce(VK_DOWN));
			change(Computer_Player2, Computer_Computer, () -> keyPressedOnce(VK_DOWN));
			change(Computer_Computer, Player1_Player2, () -> keyPressedOnce(VK_DOWN));
		}

		@Override
		public void update() {
			// "Global" transition
			if (keyPressedOnce(VK_ENTER)) {
				app.selectView(PongPlayScene.class);
			}
			super.update();
		}
	};

	private final MenuControl control;
	private Color bgColor = Color.LIGHT_GRAY;
	private Color bgColorSelected = bgColor.darker();
	private Color hilightColor = Color.YELLOW;

	public MenuScene(PongGame app) {
		super(app);
		control = new MenuControl();
		control.setLogger(Application.LOG);
	}

	public PlayMode getSelectedPlayMode() {
		return control.stateID();
	}

	public void setSelectedPlayMode(PlayMode mode) {
		control.init();
		control.setState(mode);
	}

	@Override
	public void update() {
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(bgColor);
		g.fillRect(0, 0, app.getWidth(), getHeight());
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));

		PlayMode[] playModes = PlayMode.values();
		int y = 60;
		int h = getHeight() / playModes.length;
		for (int i = 0; i < playModes.length; ++i) {
			if (playModes[i] == getSelectedPlayMode()) {
				g.setColor(bgColorSelected);
				g.fillRect(0, h * i, getWidth(), h);
				g.setColor(hilightColor);
			} else {
				g.setColor(Color.WHITE);
			}
			String text = getTitle(playModes[i]);
			int w = g.getFontMetrics().stringWidth(text);
			g.drawString(text, getWidth() / 2 - w / 2, y);
			y += h;
		}
	}

	private String getTitle(PlayMode mode) {
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
}