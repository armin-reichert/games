package de.amr.games.pacman.scenes;

import static de.amr.games.pacman.data.Board.BONUS_COL;
import static de.amr.games.pacman.data.Board.BONUS_ROW;
import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.data.TileContent.Energizer;
import static de.amr.games.pacman.data.TileContent.Pellet;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_I;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;

import de.amr.easy.game.common.FlashText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.PacManGame.PlayState;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.ui.PacManUI;

/**
 * The play scene of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<PacManGame> {

	public PlayScene(PacManGame game) {
		super(game);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Keyboard.pressedOnce(VK_CONTROL, VK_I)) {
			getApp().settings.set("drawInternals", !getApp().settings.getBool("drawInternals"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_CONTROL, KeyEvent.VK_G)) {
			getApp().settings.set("drawGrid", !getApp().settings.getBool("drawGrid"));
		}
		getApp().updateGameState();
	}

	// --- drawing ---

	@Override
	public void draw(Graphics2D g) {
		drawBoard(g, 3);
		getApp().entities.findAny(PacMan.class).draw(g);
		if (getApp().getPlayState() != PlayState.Crashing) {
			getApp().entities.allOf(Ghost.class).forEach(ghost -> ghost.draw(g));
		}
		drawGameState(g);
		getApp().entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}

	private void drawBoard(Graphics2D g, int firstRow) {
		drawSpriteAt(g, firstRow, 0, getApp().selectedTheme().getBoard());
	
		range(firstRow + 1, NUM_ROWS - 3).forEach(row -> range(0, NUM_COLS).forEach(col -> {
			if (getApp().board.contains(row, col, Pellet)) {
				drawSpriteAt(g, row, col, getApp().selectedTheme().getPellet());
			} else if (getApp().board.contains(row, col, Energizer)) {
				drawSpriteAt(g, row, col, getApp().selectedTheme().getEnergizer());
			}
		}));
	
		getApp().bonus.ifPresent(bonus -> drawSpriteAt(g, BONUS_ROW, BONUS_COL, getApp().selectedTheme().getBonus(bonus)));
	
		if (getApp().settings.getBool("drawGrid")) {
			g.drawImage(drawGridLines(), 0, 0, null);
		}
	
		if (getApp().settings.getBool("drawInternals")) {
			// mark home positions of ghosts
			getApp().entities.allOf(Ghost.class).forEach(ghost -> {
				g.setColor(ghost.color);
				g.fillRect(Math.round(ghost.home.x * TILE_SIZE), Math.round(ghost.home.y * TILE_SIZE), TILE_SIZE, TILE_SIZE);
			});
		}
	}

	private void drawGameState(Graphics2D g) {
		g.setFont(getApp().selectedTheme().getTextFont());
		g.setColor(getApp().selectedTheme().getHUDColor());
	
		// HUD
		g.setColor(Color.LIGHT_GRAY);
		drawTextAt(g, 1, 1, "SCORE");
		drawTextAt(g, 1, 8, "HIGH");
		drawTextAt(g, 1, 12, "SCORE");
		g.setColor(getApp().selectedTheme().getHUDColor());
		drawTextAt(g, 2, 1, String.format("%02d", getApp().score));
		drawTextAt(g, 2, 8, String.format("%02d   L%d", getApp().highscorePoints, getApp().highscoreLevel));
		drawTextAt(g, 2, 20, "Level " + getApp().level);
	
		// Ready!, Game Over!
		if (getApp().getPlayState() == PlayState.StartingGame) {
			g.setColor(Color.RED);
			drawTextCenteredAt(g, 9.5f, "Press ENTER to start");
			g.setColor(getApp().selectedTheme().getHUDColor());
			drawTextCenteredAt(g, 21f, "Ready!");
		} else if (getApp().getPlayState() == PlayState.GameOver) {
			g.setColor(Color.RED);
			drawTextCenteredAt(g, 9.5f, "Press SPACE for new game");
			g.setColor(getApp().selectedTheme().getHUDColor());
			drawTextCenteredAt(g, 21f, "Game Over!");
		}
	
		// Lives
		range(0, getApp().liveCount)
				.forEach(i -> drawSpriteAt(g, NUM_ROWS - 2, 2 * (i + 1), getApp().selectedTheme().getLife()));
	
		// Bonus score
		float col = NUM_COLS - 2;
		for (Bonus bonus : getApp().bonusScore) {
			drawSpriteAt(g, NUM_ROWS - 2, col, getApp().selectedTheme().getBonus(bonus));
			col -= 2f;
		}
	
		if (getApp().settings.getBool("drawInternals")) {
			drawTextCenteredAt(g, 33, getApp().getPlayState().toString());
		}
	}

	private void drawSpriteAt(Graphics2D g, float row, float col, Sprite sprite) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.translate(TILE_SIZE * col, TILE_SIZE * row);
		sprite.draw(gg);
		gg.dispose();
	}

	private void drawTextAt(Graphics2D g, float row, float col, String text) {
		g.drawString(text, TILE_SIZE * col, TILE_SIZE * row);
	}

	private void drawTextCenteredAt(Graphics2D g, float row, String text) {
		g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, TILE_SIZE * row);
	}

	private Image gridLines;

	private Image drawGridLines() {
		if (gridLines == null) {
			gridLines = PacManUI.createTransparentImage(getWidth(), getHeight());
			Graphics g = gridLines.getGraphics();
			g.setColor(new Color(200, 200, 200, 100));
			for (int col = 1, x = TILE_SIZE; col < NUM_COLS; ++col, x += TILE_SIZE) {
				g.drawLine(x, 0, x, getHeight());
			}
			for (int row = 1, y = TILE_SIZE; row < NUM_ROWS; ++row, y += TILE_SIZE) {
				g.drawLine(0, y, getWidth(), y);
			}
		}
		return gridLines;
	}
}