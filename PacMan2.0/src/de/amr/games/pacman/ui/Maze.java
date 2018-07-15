package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Tile.BONUS_APPLE;
import static de.amr.games.pacman.model.Tile.BONUS_BELL;
import static de.amr.games.pacman.model.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.model.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.model.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.model.Tile.BONUS_KEY;
import static de.amr.games.pacman.model.Tile.BONUS_PEACH;
import static de.amr.games.pacman.model.Tile.BONUS_STRAWBERRY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.ui.Spritesheet.BLUE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.MazeContent;

public class Maze extends GameEntity {

	private final MazeContent board;
	private final Sprite sprite;

	public final PacMan pacMan;
	public final Ghost[] ghosts = new Ghost[4];

	public Maze(MazeContent board, int width, int height) {
		this.board = board;
		this.sprite = new Sprite(Spritesheet.getMaze()).scale(width, height);
		ghosts[RED_GHOST] = new Ghost(board, RED_GHOST);
		ghosts[PINK_GHOST] = new Ghost(board, PINK_GHOST);
		ghosts[BLUE_GHOST] = new Ghost(board, BLUE_GHOST);
		ghosts[ORANGE_GHOST] = new Ghost(board, ORANGE_GHOST);
		pacMan = new PacMan(board);
		pacMan.enemies.addAll(Arrays.asList(ghosts));
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		g.translate(tf.getX(), tf.getY());
		board.positions().forEach(pos -> drawTile(g, pos.x, pos.y));
		pacMan.draw(g);
		Arrays.stream(ghosts).forEach(e -> e.draw(g));
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawTile(Graphics2D g, int col, int row) {
		g.translate(col * MazeContent.TS, row * MazeContent.TS);
		char tile = board.getContent(col, row);
		switch (tile) {
		case PELLET:
			drawPellet(g, row, col);
			break;
		case ENERGIZER:
			drawEnergizer(g, row, col);
			break;
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			drawBonus(g, row, col, tile);
		default:
			break;
		}
		g.translate(-col * MazeContent.TS, -row * MazeContent.TS);
	}

	private void drawBonus(Graphics2D g, int row, int col, char bonus) {
		g.drawImage(Spritesheet.getBonus(bonus), 0, -MazeContent.TS / 2, MazeContent.TS * 2, MazeContent.TS * 2, null);
	}

	private void drawPellet(Graphics2D g, int row, int col) {
		drawCircle(g, Color.YELLOW, row, col, MazeContent.TS / 8);
	}

	private void drawEnergizer(Graphics2D g, int row, int col) {
		drawCircle(g, Color.YELLOW, row, col, MazeContent.TS / 2);
	}

	private void drawCircle(Graphics2D g, Color color, int row, int col, int r) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillOval(MazeContent.TS / 2 - r, MazeContent.TS / 2 - r, 2 * r, 2 * r);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}