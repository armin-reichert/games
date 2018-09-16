package de.amr.games.pong.ui.menu;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.pong.model.Game.PlayMode.Computer_Computer;
import static de.amr.games.pong.model.Game.PlayMode.Computer_Player2;
import static de.amr.games.pong.model.Game.PlayMode.Player1_Computer;
import static de.amr.games.pong.model.Game.PlayMode.Player1_Player2;
import static java.awt.event.KeyEvent.VK_ENTER;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pong.model.Game.PlayMode;
import de.amr.games.pong.ui.ScreenManager;
import de.amr.statemachine.StateMachine;

/**
 * The menu of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class MenuScreen implements Controller, View {

	private final ScreenManager screenManager;
	private final Dimension size;
	private final StateMachine<PlayMode, Object> fsm;
	private Color bgColor;
	private Color bgColorSelected;
	private Color hilightColor;

	public MenuScreen(ScreenManager screenManager, Dimension size) {
		this.screenManager = screenManager;
		this.size = size;
		fsm = createStateMachine();
		fsm.traceTo(LOGGER, app().clock::getFrequency);
	}

	private StateMachine<PlayMode, Object> createStateMachine() {
		return
		//@formatter:off
		StateMachine.beginStateMachine(PlayMode.class, Object.class)
			.description("Pong Menu")
			.initialState(Player1_Player2)
			
			.states()
			
				.state(Player1_Player2)

				.state(Player1_Computer)
				
				.state(Computer_Player2)
				
				.state(Computer_Computer)
		
			.transitions()
				.when(PlayMode.Player1_Player2).then(Player1_Computer)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_DOWN))
	
				.when(PlayMode.Player1_Computer).then(Computer_Player2)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_DOWN))
				
				.when(PlayMode.Computer_Player2).then(Computer_Computer)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_DOWN))
				
				.when(PlayMode.Computer_Computer).then(Player1_Player2)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_DOWN))
				
				.when(PlayMode.Player1_Player2).then(Computer_Computer)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_UP))
				
				.when(PlayMode.Computer_Computer).then(Computer_Player2)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_UP))
				
				.when(PlayMode.Computer_Player2).then(Player1_Computer)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_UP))
				
				.when(PlayMode.Player1_Computer).then(Player1_Player2)
					.condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_UP))
		
		.endStateMachine();
		//@formatter:on
	}
	
	
	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}
	
	
	public void setBgColorSelected(Color bgColorSelected) {
		this.bgColorSelected = bgColorSelected;
	}
	
	
	public void setHilightColor(Color hilightColor) {
		this.hilightColor = hilightColor;
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		if (keyPressedOnce(VK_ENTER)) {
			screenManager.selectPlayScreen(fsm.getState());
		}
		fsm.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(bgColor);
		g.fillRect(0, 0, size.width, size.height);
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		PlayMode[] playModes = PlayMode.values();
		int y = 60;
		int h = size.height / playModes.length;
		for (int i = 0; i < playModes.length; ++i) {
			if (playModes[i] == fsm.getState()) {
				g.setColor(bgColorSelected);
				g.fillRect(0, h * i, size.width, h);
				g.setColor(hilightColor);
			} else {
				g.setColor(Color.WHITE);
			}
			String text = itemText(playModes[i]);
			int w = g.getFontMetrics().stringWidth(text);
			g.drawString(text, size.width / 2 - w / 2, y);
			y += h;
		}
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	private String itemText(PlayMode mode) {
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