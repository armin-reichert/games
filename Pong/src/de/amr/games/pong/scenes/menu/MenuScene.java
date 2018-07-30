package de.amr.games.pong.scenes.menu;

import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.pong.PongGame.PlayMode.Player1_Player2;
import static java.awt.event.KeyEvent.VK_ENTER;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.ViewController;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.pong.PongGame;
import de.amr.games.pong.PongGame.PlayMode;

/**
 * The menu scene of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class MenuScene implements ViewController {

	private final PongGame app;
	private final StateMachine<PlayMode, String> control;
	private Color bgColor;
	private Color bgColorSelected;
	private Color hilightColor;

	public MenuScene(PongGame app) {
		this.app = app;
		control = createStateMachine();
		control.setLogger(Application.LOG);
	}

	@Override
	public int getWidth() {
		return app.getWidth();
	}

	@Override
	public int getHeight() {
		return app.getHeight();
	}

	@Override
	public void init() {
		bgColor = Color.LIGHT_GRAY;
		bgColorSelected = bgColor.darker();
		hilightColor = Color.YELLOW;
	}

	private StateMachine<PlayMode, String> createStateMachine() {
		StateMachine<PlayMode, String> fsm = new StateMachine<>("Pong Menu", PlayMode.class,
				Player1_Player2);
		PlayMode[] playModes = PlayMode.values();
		for (int i = 0, n = playModes.length; i < n; i += 1) {
			fsm.change(playModes[i], playModes[(i + 1) % n], () -> keyPressedOnce(KeyEvent.VK_DOWN));
			fsm.change(playModes[i], playModes[(i - 1 + n) % n], () -> keyPressedOnce(KeyEvent.VK_UP));
		}
		return fsm;
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
		if (keyPressedOnce(VK_ENTER)) {
			app.setController(app.playScene);
		}
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