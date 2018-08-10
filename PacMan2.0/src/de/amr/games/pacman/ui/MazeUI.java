package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Content.EATEN;
import static de.amr.games.pacman.model.Content.ENERGIZER;
import static de.amr.games.pacman.ui.GameUI.SPRITES;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Animation;
import de.amr.easy.game.sprite.CyclicAnimation;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.model.Maze;

public class MazeUI extends GameEntity {

	private final Maze maze;
	private final GameActors actors;
	private final Animation energizerBlinking;
	private final Sprite s_maze_normal;
	private final Sprite s_maze_flashing;
	private boolean flashing;
	private int bonusTimer;

	public MazeUI(Maze maze, GameActors actors) {
		this.maze = maze;
		this.actors = actors;
		s_maze_normal = SPRITES.mazeFull().scale(getWidth(), getHeight());
		s_maze_flashing = SPRITES.mazeFlashing().scale(getWidth(), getHeight());
		energizerBlinking = new CyclicAnimation(2);
		energizerBlinking.setFrameDuration(250);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_maze_normal, s_maze_flashing);
	}

	@Override
	public Sprite currentSprite() {
		return flashing ? s_maze_flashing : s_maze_normal;
	}

	@Override
	public void update() {
		if (bonusTimer > 0) {
			bonusTimer -= 1;
			if (bonusTimer == 0) {
				actors.removeBonus();
			}
		}
		energizerBlinking.update();
	}

	@Override
	public int getWidth() {
		return maze.numCols() * TS;
	}

	@Override
	public int getHeight() {
		return maze.numRows() * TS;
	}

	public void setFlashing(boolean on) {
		flashing = on;
	}

	public void setBonusTimer(int ticks) {
		bonusTimer = ticks;
	}
	
	@Override
	public void enableAnimation(boolean enable) {
		super.enableAnimation(enable);
		energizerBlinking.setEnabled(enable);
		actors.getPacMan().enableAnimation(enable);
		actors.getActiveGhosts().forEach(ghost -> ghost.enableAnimation(enable));
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		if (flashing) {
			s_maze_flashing.draw(g);
		} else {
			s_maze_normal.draw(g);
			drawFood(g);
			drawActors(g);
		}
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawActors(Graphics2D g) {
		actors.getBonus().ifPresent(bonus -> {
			bonus.placeAt(maze.infoTile);
			g.translate(0, -TS/2);
			bonus.draw(g);
			g.translate(0, TS/2);
		});
		actors.getPacMan().draw(g);
		actors.getActiveGhosts().filter(ghost -> ghost.getState() != Ghost.State.DYING).forEach(ghost -> ghost.draw(g));
		actors.getActiveGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING).forEach(ghost -> ghost.draw(g));
	}

	private void drawFood(Graphics2D g) {
		maze.tiles().forEach(tile -> {
			char c = maze.getContent(tile);
			if (c == EATEN || c == ENERGIZER && energizerBlinking.currentFrame() % 2 != 0) {
				g.translate(tile.col * TS, tile.row * TS);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, TS, TS);
				g.translate(-tile.col * TS, -tile.row * TS);
			}
		});
	}
}