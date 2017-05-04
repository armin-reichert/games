package de.amr.games.pacman.scenes;

import static de.amr.games.pacman.data.Board.BONUS_COL;
import static de.amr.games.pacman.data.Board.BONUS_ROW;
import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.data.TileContent.Energizer;
import static de.amr.games.pacman.data.TileContent.Pellet;
import static de.amr.games.pacman.scenes.DrawUtil.drawGridLines;
import static de.amr.games.pacman.scenes.DrawUtil.drawSprite;
import static de.amr.games.pacman.scenes.DrawUtil.drawText;
import static de.amr.games.pacman.scenes.DrawUtil.drawTextCentered;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.lang.Math.round;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.common.FlashText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.PacManGame.PlayState;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.data.TileContent;
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
		// cheats and debug keys
		if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_I)) {
			getApp().settings.set("drawInternals", !getApp().settings.getBool("drawInternals"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_G)) {
			getApp().settings.set("drawGrid", !getApp().settings.getBool("drawGrid"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_L)) {
			getApp().lives += 1;
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_B)) {
			getApp().bonusScore.add(getApp().getBonus());
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_P)) {
			getApp().board.tilesWithContent(Pellet).forEach(tile -> getApp().board.setContent(tile, TileContent.None));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_E)) {
			getApp().board.tilesWithContent(Energizer).forEach(tile -> getApp().board.setContent(tile, TileContent.None));
		}
		getApp().update();
	}

	@Override
	public void draw(Graphics2D g) {
		final PacManUI theme = getApp().selectedTheme();

		// Board
		drawSprite(g, 3, 0, theme.getBoard());
		range(4, NUM_ROWS - 3).forEach(row -> range(0, NUM_COLS).forEach(col -> {
			if (getApp().board.contains(row, col, Pellet)) {
				drawSprite(g, row, col, theme.getPellet());
			} else if (getApp().board.contains(row, col, Energizer)) {
				drawSprite(g, row, col, theme.getEnergizer());
			}
		}));
		getApp().bonus.ifPresent(bonus -> drawSprite(g, BONUS_ROW, BONUS_COL, theme.getBonus(bonus)));

		// Grid
		if (getApp().settings.getBool("drawGrid")) {
			drawGridLines(g, getWidth(), getHeight());
		}
		if (getApp().settings.getBool("drawInternals")) {
			// mark home positions of ghosts
			getApp().entities.allOf(Ghost.class).forEach(ghost -> {
				g.setColor(ghost.color);
				g.fillRect(round(ghost.home.x * TILE_SIZE), round(ghost.home.y * TILE_SIZE), TILE_SIZE, TILE_SIZE);
			});
		}

		// Entities
		getApp().pacMan.draw(g);
		if (getApp().getPlayState() != PlayState.Crashing) {
			getApp().entities.allOf(Ghost.class).forEach(ghost -> ghost.draw(g));
		}

		// HUD
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(theme.getTextFont());
		drawText(g, 1, 1, "SCORE");
		drawText(g, 1, 8, "HIGH");
		drawText(g, 1, 12, "SCORE");
		g.setColor(theme.getHUDColor());
		drawText(g, 2, 1, String.format("%02d", getApp().score));
		drawText(g, 2, 8, String.format("%02d   L%d", getApp().highscore.getPoints(), getApp().highscore.getLevel()));
		drawText(g, 2, 20, "Level " + getApp().level);

		// Status messages
		switch (getApp().getPlayState()) {
		case Ready:
			g.setColor(Color.RED);
			drawTextCentered(g, getWidth(), 9.5f, "Press ENTER to start");
			g.setColor(theme.getHUDColor());
			drawTextCentered(g, getWidth(), 21f, "Ready!");
			break;
		case StartingLevel:
			g.setColor(theme.getHUDColor());
			drawTextCentered(g, getWidth(), 21f, "Level " + getApp().level);
			break;
		case GameOver:
			g.setColor(Color.RED);
			drawTextCentered(g, getWidth(), 9.5f, "Press SPACE for new game");
			g.setColor(theme.getHUDColor());
			drawTextCentered(g, getWidth(), 21f, "Game Over!");
			break;
		default:
			break;
		}

		// Lives
		range(0, getApp().lives).forEach(i -> drawSprite(g, NUM_ROWS - 2, 2 * (i + 1), theme.getLife()));

		// Bonus score
		float col = NUM_COLS - 2;
		for (Bonus bonus : getApp().bonusScore) {
			drawSprite(g, NUM_ROWS - 2, col, theme.getBonus(bonus));
			col -= 2f;
		}

		// Play state
		if (getApp().settings.getBool("drawInternals")) {
			drawTextCentered(g, getWidth(), 33, getApp().getPlayState().toString());
		}

		// Flash texts
		getApp().entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}

}