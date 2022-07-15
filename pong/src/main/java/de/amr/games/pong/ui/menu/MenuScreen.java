package de.amr.games.pong.ui.menu;

import static de.amr.games.pong.model.PongGame.PlayMode.Computer_Computer;
import static de.amr.games.pong.model.PongGame.PlayMode.Computer_Player2;
import static de.amr.games.pong.model.PongGame.PlayMode.Player1_Computer;
import static de.amr.games.pong.model.PongGame.PlayMode.Player1_Player2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.games.pong.model.PongGame.PlayMode;
import de.amr.games.pong.ui.ScreenManager;
import de.amr.statemachine.core.StateMachine;

/**
 * The menu of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class MenuScreen extends StateMachine<PlayMode, Void> implements View, Lifecycle {

	private final ScreenManager screenManager;
	private final Dimension size;
	private Color bgColor;
	private Color bgColorSelected;
	private Color hilightColor;

	public MenuScreen(ScreenManager screenManager, Dimension size) {
		super(PlayMode.class);
		this.screenManager = screenManager;
		this.size = size;
		beginStateMachine()
		//@formatter:off
			.description("Pong Menu")
			.initialState(Player1_Player2)
			
			.states()

				// for clarity, all states are listed, would also work without!
				.state(Player1_Player2)
				.state(Player1_Computer)
				.state(Computer_Player2)
				.state(Computer_Computer)
		
			.transitions()
			
				.when(Player1_Player2)	.then(Player1_Computer)	.condition(this::nextEntrySelected) .act(this::plip)
				.when(Player1_Computer)	.then(Computer_Player2)	.condition(this::nextEntrySelected) .act(this::plip)
				.when(Computer_Player2)	.then(Computer_Computer).condition(this::nextEntrySelected) .act(this::plip)
				.when(Computer_Computer).then(Player1_Player2)	.condition(this::nextEntrySelected) .act(this::plip)
				
				.when(Player1_Player2)	.then(Computer_Computer).condition(this::prevEntrySelected) .act(this::plop)
				.when(Computer_Computer).then(Computer_Player2)	.condition(this::prevEntrySelected) .act(this::plop)
				.when(Computer_Player2)	.then(Player1_Computer)	.condition(this::prevEntrySelected) .act(this::plop)
				.when(Player1_Computer)	.then(Player1_Player2)	.condition(this::prevEntrySelected) .act(this::plop)
		
		.endStateMachine();
		//@formatter:on
	}

	private void plip() {
		Assets.sound("plip.mp3").play();
	}

	private void plop() {
		Assets.sound("plop.mp3").play();
	}

	private boolean nextEntrySelected() {
		return Keyboard.keyPressedOnce(KeyEvent.VK_DOWN);
	}

	private boolean prevEntrySelected() {
		return Keyboard.keyPressedOnce(KeyEvent.VK_UP);
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
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			screenManager.selectPlayScreen(getState());
		}
		super.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(bgColor);
		g.fillRect(0, 0, size.width, size.height);
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		PlayMode[] playModes = PlayMode.values();
		int y = 70;
		int h = size.height / playModes.length;
		for (int i = 0; i < playModes.length; ++i) {
			if (playModes[i] == getState()) {
				g.setColor(bgColorSelected);
				g.fillRect(0, h * i, size.width, h);
				g.setColor(hilightColor);
			} else {
				g.setColor(Color.WHITE);
			}
			String text = playModeText(playModes[i]);
			int w = g.getFontMetrics().stringWidth(text);
			g.drawString(text, size.width / 2 - w / 2, y);
			y += h;
		}
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	private String playModeText(PlayMode mode) {
		switch (mode) {
		case Computer_Computer:
			return "Computer - Computer";
		case Computer_Player2:
			return "Computer - Player 2";
		case Player1_Computer:
			return "Player 1 - Computer";
		case Player1_Player2:
			return "Player 1 - Player 2";
		default:
			return "";
		}
	}
}