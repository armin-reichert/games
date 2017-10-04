package de.amr.games.pacman2.play.entities;

import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.theme.PacManTheme;

public class Pacman extends GameEntity {

	public final PacManTheme theme;
	private float row, col;
	public int dir;

	public Pacman(PacManTheme theme) {
		this.theme = theme;
		this.dir = Top4.W;
	}

	public void setRow(float row) {
		this.row = row;
		tf.setY(TILE_SIZE * (row - 0.5f));
	}

	public float getRow() {
		return row;
	}

	public void setCol(float col) {
		this.col = col;
		tf.setX(TILE_SIZE * col);
	}

	public float getCol() {
		return col;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public Sprite currentSprite() {
		return theme.getPacManRunningSprite(dir);
	}
}
