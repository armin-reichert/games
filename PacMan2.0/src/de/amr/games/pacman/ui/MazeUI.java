package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Spritesheet.getEnergizer;
import static de.amr.games.pacman.model.Spritesheet.getMazeImage;
import static de.amr.games.pacman.model.Spritesheet.getMazeImageWhite;
import static de.amr.games.pacman.model.Tile.ENERGIZER;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.GameEventSupport;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.PacMan;

public class MazeUI extends GameEntity {

	public final GameEventSupport observers = new GameEventSupport();

	private final Maze maze;
	private final PacMan pacMan;
	private final Set<Ghost> ghosts = new HashSet<>();

	private final Sprite s_normal;
	private final Sprite s_flashing;
	private final Sprite s_energizer;

	private boolean flashing;
	private String infoText;
	private Bonus bonus;
	private int bonusTimeLeft;

	public MazeUI(Maze maze, PacMan pacMan) {
		this.maze = maze;
		this.pacMan = pacMan;
		s_normal = new Sprite(getMazeImage()).scale(getWidth(), getHeight());
		s_flashing = new Sprite(getMazeImage(), getMazeImageWhite()).scale(getWidth(), getHeight())
				.animation(AnimationMode.CYCLIC, 100);
		s_energizer = new Sprite(getEnergizer()).scale(TS).animation(AnimationMode.BACK_AND_FORTH, 250);
	}

	@Override
	public int getWidth() {
		return maze.numCols() * TS;
	}

	@Override
	public int getHeight() {
		return maze.numRows() * TS;
	}

	public void addGhost(Ghost ghost) {
		ghosts.add(ghost);
		pacMan.lookFor.add(ghost);
	}

	public void removeGhost(Ghost ghost) {
		ghosts.remove(ghost);
		pacMan.lookFor.remove(ghost);
	}

	public boolean containsGhost(Ghost ghost) {
		return ghosts.contains(ghost);
	}

	public Stream<Ghost> getGhosts() {
		return ghosts.stream();
	}

	public Maze getMaze() {
		return maze;
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public void showInfo(String text) {
		this.infoText = text;
	}

	public void hideInfo() {
		this.infoText = null;
	}

	Optional<String> getInfo() {
		return Optional.ofNullable(infoText);
	}

	public void setFlashing(boolean on) {
		flashing = on;
	}

	public void showBonus(BonusSymbol bonusSymbol, int value, int ticks) {
		this.bonus = new Bonus(bonusSymbol, value);
		bonusTimeLeft = ticks;
		bonus.tf.moveTo(maze.infoTile.col * TS, maze.infoTile.row * TS - TS / 2);
		pacMan.lookFor.add(bonus);
	}

	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	public void honorBonus(int ticks) {
		if (bonus != null) {
			pacMan.lookFor.remove(bonus);
			bonus.setHonored();
			bonusTimeLeft = ticks;
		}
	}

	public void removeBonus() {
		if (bonus != null) {
			pacMan.lookFor.remove(bonus);
			bonus = null;
			bonusTimeLeft = 0;
		}
	}

	@Override
	public void update() {
		if (bonus != null) {
			if (bonusTimeLeft-- == 0) {
				removeBonus();
			}
		}
		getPacMan().update();
		getGhosts().forEach(Ghost::update);
	}

	@Override
	protected Stream<Sprite> getSprites() {
		List<Sprite> sprites = new ArrayList<>();
		sprites.add(s_normal);
		sprites.add(s_energizer);
		return sprites.stream();
	}

	@Override
	public Sprite currentSprite() {
		return flashing ? s_flashing : s_normal;
	}

	@Override
	public void enableAnimation(boolean animated) {
		super.enableAnimation(animated);
		getPacMan().enableAnimation(animated);
		getGhosts().forEach(ghost -> ghost.enableAnimation(animated));
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		if (flashing) {
			s_flashing.draw(g);
		} else {
			s_normal.draw(g);
			maze.tiles().forEach(tile -> drawContent(g, tile));
			ghosts.stream().filter(ghost -> ghost.getState() != Ghost.State.DYING)
					.forEach(ghost -> ghost.draw(g));
			ghosts.stream().filter(ghost -> ghost.getState() == Ghost.State.DYING)
					.forEach(ghost -> ghost.draw(g));
			pacMan.draw(g);
			getBonus().ifPresent(bonus -> bonus.draw(g));
			getInfo().ifPresent(info -> drawInfo(g, info));
		}
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawInfo(Graphics2D g, String text) {
		Tile tile = maze.infoTile;
		g.translate((tile.col + 1) * TS, tile.row * TS + TS / 4);
		g.setFont(Assets.font("scoreFont"));
		g.setColor(Color.YELLOW);
		Rectangle2D box = g.getFontMetrics().getStringBounds(infoText, g);
		g.drawString(infoText, (int) (-box.getWidth() / 2), (int) (box.getHeight() / 2));
		g.translate(-tile.col * TS, -tile.row * TS);
	}

	private void drawContent(Graphics2D g, Tile tile) {
		g.translate(tile.col * TS, tile.row * TS);
		char c = maze.getContent(tile);
		if (c == Tile.PELLET) {
			g.setColor(Color.PINK);
			g.fillRect(TS * 3 / 8, TS * 3 / 8, TS / 4, TS / 4);
		} else if (c == ENERGIZER) {
			s_energizer.draw(g);
		}
		g.translate(-tile.col * TS, -tile.row * TS);
	}
}