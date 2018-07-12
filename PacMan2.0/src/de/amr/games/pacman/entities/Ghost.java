package de.amr.games.pacman.entities;

import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.easy.util.StreamUtils;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.SpriteSheet;

public class Ghost extends BoardMover {

	private Top4 top = new Top4();
	private Sprite[] spriteByDir = new Sprite[4];

	public Ghost(Board board, int color) {
		super(board);
		top.dirs().forEach(dir -> {
			spriteByDir[dir] = new Sprite(SpriteSheet.get().getGhostImagesByDirection(color, dir));
			spriteByDir[dir].scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
		});
	}

	@Override
	public void init() {
		setMoveDirection(Top4.E);
		setNextMoveDirection(Top4.E);
		setSpeed(1f);
	}

	@Override
	public void update() {
		if (canMove(moveDirection)) {
			move();
		} else {
			int direction = moveDirection;
			do {
				direction = StreamUtils.randomElement(top.dirs()).getAsInt();
			} while (!canMove(direction));
			setMoveDirection(direction);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(-Board.TILE_SIZE / 2, -Board.TILE_SIZE / 2);
		super.draw(g);
		g.translate(Board.TILE_SIZE / 2, Board.TILE_SIZE / 2);
	}

	@Override
	public Sprite currentSprite() {
		return spriteByDir[moveDirection];
	}

}
