package de.amr.games.pong.view.menu;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.PULSE;
import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.pong.model.Game.PlayMode.Computer_Computer;
import static de.amr.games.pong.model.Game.PlayMode.Computer_Player2;
import static de.amr.games.pong.model.Game.PlayMode.Player1_Computer;
import static de.amr.games.pong.model.Game.PlayMode.Player1_Player2;
import static java.awt.event.KeyEvent.VK_ENTER;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pong.PongGameApp;
import de.amr.games.pong.model.Game;
import de.amr.games.pong.model.Game.PlayMode;
import de.amr.statemachine.StateMachine;

/**
 * The menu of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class MenuScreen implements Controller, View {

	private final PongGameApp app;
	private final Game game;
	private final int width;
	private final int height;
	private final StateMachine<PlayMode, Object> fsm;
	private Color bgColor;
	private Color bgColorSelected;
	private Color hilightColor;

	public MenuScreen(PongGameApp app) {
		this.app = app;
		this.game = app.game;
		this.width = app.settings.width;
		this.height = app.settings.height;
		fsm = createStateMachine();
		fsm.traceTo(LOGGER, PULSE::getFrequency);
	}

	private StateMachine<PlayMode, Object> createStateMachine() {
		return
		//@formatter:off
		StateMachine.define(PlayMode.class, Object.class)
			.description("Pong Menu")
			.initialState(Player1_Player2)
			
			.states()
			
				.state(Player1_Player2).onEntry(this::setPlayMode)

				.state(Player1_Computer).onEntry(this::setPlayMode)
				
				.state(Computer_Player2).onEntry(this::setPlayMode)
				
				.state(Computer_Computer).onEntry(this::setPlayMode)
		
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
	
	private void setPlayMode() {
		game.playMode = fsm.currentState();
	}

	@Override
	public void init() {
		bgColor = Color.LIGHT_GRAY;
		bgColorSelected = bgColor.darker();
		hilightColor = Color.YELLOW;
		fsm.init();
	}

	@Override
	public void update() {
		if (keyPressedOnce(VK_ENTER)) {
			app.setController(app.playViewController);
		}
		fsm.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(bgColor);
		g.fillRect(0, 0, width, height);
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		PlayMode[] playModes = PlayMode.values();
		int y = 60;
		int h = height / playModes.length;
		for (int i = 0; i < playModes.length; ++i) {
			if (playModes[i] == app.game.playMode) {
				g.setColor(bgColorSelected);
				g.fillRect(0, h * i, width, h);
				g.setColor(hilightColor);
			} else {
				g.setColor(Color.WHITE);
			}
			String text = getTitle(playModes[i]);
			int w = g.getFontMetrics().stringWidth(text);
			g.drawString(text, width / 2 - w / 2, y);
			y += h;
		}
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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