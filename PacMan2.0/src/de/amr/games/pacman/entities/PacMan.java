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
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.FoodEvent;
import de.amr.games.pacman.board.SpriteSheet;
import de.amr.games.pacman.board.Tile;

public class PacMan extends BoardMover {

	private static boolean DEBUG = false;

	private Sprite[] walkingSprites = new Sprite[4];
	private Sprite standingSprite;

	public Consumer<FoodEvent> fnFoodFound;

	public PacMan(Board board) {
		super(board);
		fnFoodFound = e -> {
			System.out.println(String.format("Eat %s at col=%d, row=%d", e.food, e.col, e.row));
			board.setTile(e.col, e.row, Tile.EMPTY);
		};
	}

	private void readSprites() {
		standingSprite = new Sprite(SpriteSheet.get().getPacManStanding());
		standingSprite.scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
		Stream.of(Top4.E, Top4.W, Top4.N, Top4.S).forEach(direction -> {
			walkingSprites[direction] = new Sprite(SpriteSheet.get().getPacManWalking(direction));
			walkingSprites[direction].scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
			walkingSprites[direction].makeAnimated(AnimationMode.CYCLIC, 120);
		});
	}

	@Override
	public void init() {
		readSprites();
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
			return standingSprite;
		}
		return walkingSprites[moveDirection];
	}

	@Override
	public void update() {
		lookForFood();
		readNextMoveDirection();
		changeDirection();
		move();
	}

	private void readNextMoveDirection() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			setNextMoveDirection(Top4.W);
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			setNextMoveDirection(Top4.E);
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			setNextMoveDirection(Top4.S);
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			setNextMoveDirection(Top4.N);
		}
	}

	private void lookForFood() {
		int col = col(), row = row();
		char tile = board.getTile(col, row);
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