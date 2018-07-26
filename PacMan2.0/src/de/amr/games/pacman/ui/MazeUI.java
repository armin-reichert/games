package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.ENERGIZER;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.GameEventSupport;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeUI extends GameEntity {

	public final GameEventSupport observers = new GameEventSupport();

	private final Maze maze;
	private PacMan pacMan;
	private Ghost[] ghosts = new Ghost[0];

	private Sprite spriteMazeNormal;
	private Sprite spriteMazeFlashing;
	private Sprite spriteEnergizer;

	private boolean flashing;
	private String infoText;
	private Bonus bonus;
	private int bonusTimeLeft;

	public MazeUI(Maze maze) {
		this.maze = maze;
		createSprites();
	}

	@Override
	public int getWidth() {
		return maze.numCols() * TS;
	}

	@Override
	public int getHeight() {
		return maze.numRows() * TS;
	}

	public void populate(PacMan pacMan, Ghost... ghosts) {
		this.pacMan = pacMan;
		this.ghosts = ghosts;
	}

	private void createSprites() {
		spriteMazeNormal = new Sprite(Spritesheet.getMaze()).scale(getWidth(), getHeight());
		spriteMazeFlashing = new Sprite(Spritesheet.getMaze(), Spritesheet.getMazeWhite())
				.scale(getWidth(), getHeight());
		spriteMazeFlashing.animation(AnimationMode.CYCLIC, 100);
		spriteEnergizer = new Sprite(Spritesheet.getEnergizer()).scale(TS, TS);
		spriteEnergizer.animation(AnimationMode.BACK_AND_FORTH, 250);
	}

	public Maze getMaze() {
		return maze;
	}

	public Optional<PacMan> getPacMan() {
		return Optional.ofNullable(pacMan);
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

	public void showBonus(Bonus bonus, int ticks) {
		this.bonus = bonus;
		bonusTimeLeft = ticks;
		bonus.tf.moveTo(maze.infoTile.col * TS, maze.infoTile.row * TS - TS / 2);
		getPacMan().ifPresent(pacMan -> pacMan.interestingThings.add(bonus));
	}

	public Optional<Bonus> getBonus() {
		return Optional.ofNullable(bonus);
	}

	public void honorBonus(int ticks) {
		getBonus().ifPresent(bonus -> {
			getPacMan().ifPresent(pacMan -> pacMan.interestingThings.remove(bonus));
			bonusTimeLeft = ticks;
			bonus.setHonored();
		});
	}

	private void removeBonus() {
		getPacMan().ifPresent(pacMan -> pacMan.interestingThings.remove(bonus));
		bonus = null;
	}

	@Override
	public void update() {
		getBonus().ifPresent(bonus -> {
			--bonusTimeLeft;
			Debug.log(() -> "Bonus time left: " + bonusTimeLeft);
			if (bonusTimeLeft <= 0) {
				removeBonus();
			}
		});
	}

	@Override
	protected Stream<Sprite> getSprites() {
		List<Sprite> sprites = new ArrayList<>();
		sprites.add(spriteMazeNormal);
		sprites.add(spriteEnergizer);
		return sprites.stream();
	}

	@Override
	public Sprite currentSprite() {
		return flashing ? spriteMazeFlashing : spriteMazeNormal;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		if (flashing) {
			spriteMazeFlashing.draw(g);
		} else {
			spriteMazeNormal.draw(g);
			maze.tiles().forEach(tile -> drawContent(g, tile));
			Arrays.stream(ghosts).forEach(ghost -> ghost.draw(g));
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
			spriteEnergizer.draw(g);
		}
		g.translate(-tile.col * TS, -tile.row * TS);
	}
}