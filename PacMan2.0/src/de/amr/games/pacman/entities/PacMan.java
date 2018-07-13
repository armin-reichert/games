package de.amr.games.pacman.entities;

import static de.amr.games.pacman.board.Tile.BONUS_APPLE;
import static de.amr.games.pacman.board.Tile.BONUS_BELL;
import static de.amr.games.pacman.board.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.board.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.board.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.board.Tile.BONUS_KEY;
import static de.amr.games.pacman.board.Tile.BONUS_PEACH;
import static de.amr.games.pacman.board.Tile.BONUS_STRAWBERRY;
import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.PELLET;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.FoodEvent;
import de.amr.games.pacman.board.SpriteSheet;
import de.amr.games.pacman.board.Tile;

public class PacMan extends BoardMover {

	private static boolean DEBUG = false;

	private Sprite[] spriteWalking = new Sprite[4];
	private Sprite spriteStanding;

	public Consumer<FoodEvent> fnFoodFound;
	public Consumer<GhostEvent> fnGhostTouched;

	public final List<Ghost> enemies = new ArrayList<>();

	public PacMan(Board board) {
		super(board);
		readSprites();
		fnFoodFound = e -> {
			System.out.println(String.format("Eat %s at col=%d, row=%d", e.food, e.col, e.row));
			board.setContent(e.col, e.row, Tile.EMPTY);
			if (e.food == Tile.ENERGIZER) {
				enemies.forEach(enemy -> {
					enemy.setState(Ghost.State.FRIGHTENED);
				});
			}
		};
		fnGhostTouched = e -> {
			System.out.println(String.format("Met ghost %s at col=%d, row=%d", e.ghost, e.col, e.row));
			e.ghost.setState(Ghost.State.DEAD);
		};
	}

	private void readSprites() {
		spriteStanding = new Sprite(SpriteSheet.getPacManStanding());
		spriteStanding.scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
		Stream.of(Top4.E, Top4.W, Top4.N, Top4.S).forEach(direction -> {
			spriteWalking[direction] = new Sprite(SpriteSheet.getPacManWalking(direction));
			spriteWalking[direction].scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
			spriteWalking[direction].makeAnimated(AnimationMode.CYCLIC, 120);
		});
	}

	@Override
	public void init() {
		setSpeed(Board.TILE_SIZE / 8f);
		setMoveDirection(Top4.E);
		setNextMoveDirection(Top4.E);
	}

	@Override
	public void draw(Graphics2D g) {
		if (DEBUG) {
			g.translate(tf.getX(), tf.getY());
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.fillRect(0, 0, Board.TILE_SIZE, Board.TILE_SIZE);
			g.translate(-tf.getX(), -tf.getY());
		} else {
			super.draw(g);
		}
	}

	@Override
	public Sprite currentSprite() {
		if (!canMove(moveDirection)) {
			return spriteStanding;
		}
		return spriteWalking[moveDirection];
	}

	@Override
	public void update() {
		lookForFood();
		lookForEnemy();
		changeDirection();
		move();
	}

	private void lookForEnemy() {
		enemies.stream().filter(enemy -> enemy.col() == col() && enemy.row() == row()).findFirst().ifPresent(enemy -> {
			fnGhostTouched.accept(new GhostEvent(enemy, col(), row()));
		});
	}

	private void lookForFood() {
		int col = col(), row = row();
		char tile = board.getContent(col, row);
		switch (tile) {
		case PELLET:
		case ENERGIZER:
			fnFoodFound.accept(new FoodEvent(col, row, tile));
			break;
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			fnFoodFound.accept(new FoodEvent(col, row, tile));
			break;
		default:
			break;
		}
	}

}