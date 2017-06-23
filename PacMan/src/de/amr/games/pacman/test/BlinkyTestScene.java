package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.PACMAN_HOME;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManEvent;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Tests Blinky's behavior.
 * 
 * @author Armin Reichert
 */
public class BlinkyTestScene extends Scene<BlinkyTestApp> {

	private final Random rand = new Random();
	private final PacManTheme theme;
	private final Board board;

	public BlinkyTestScene(BlinkyTestApp app) {
		super(app);
		board = new Board(app.assets.text("board.txt"));
		theme = new ClassicTheme(app.assets);
	}

	@Override
	public void init() {

		PacMan pacMan = new PacMan(app, board, () -> theme);
		pacMan.control.changeOnInput(PacManEvent.StartWalking, PacManState.Initialized, PacManState.Peaceful);
		pacMan.control.state(PacManState.Peaceful).update = state -> {
			pacMan.enemies().forEach(enemy -> {
				if (pacMan.currentTile().equals(enemy.currentTile())) {
					pacMan.onEnemyContact.accept(enemy);
				} else {
					escape(pacMan, enemy);
				}
			});
		};

		pacMan.onEnemyContact = ghost -> {
			pacMan.placeAt(PACMAN_HOME);
			int dir = rand.nextBoolean() ? E : W;
			pacMan.setMoveDir(dir);
			pacMan.setNextMoveDir(dir);
			ghost.placeAt(new Tile(4, 1));
			dir = rand.nextBoolean() ? W : E;
			ghost.setMoveDir(dir); // TODO without this, ghost might get stuck
			ghost.setNextMoveDir(dir);
		};
		
		app.entities.add(pacMan);

		Ghost blinky = new Ghost(app, board, "Blinky", () -> theme);
		blinky.control.changeOnInput(GhostEvent.ChasingStarts, Initialized, Chasing);
		blinky.control.state(Chasing).update = state -> blinky.follow(pacMan.currentTile());

		app.entities.add(blinky);
		
		pacMan.init();
		pacMan.placeAt(PACMAN_HOME);
		pacMan.speed = () -> 2f;
		pacMan.enemies().add(blinky);

		blinky.init();
		blinky.color = Color.RED;
		blinky.setAnimated(true);
		blinky.placeAt(4, 1);
		blinky.speed = () -> pacMan.speed.get() * 1.1f;

		pacMan.receiveEvent(PacManEvent.StartWalking);
		blinky.receiveEvent(GhostEvent.ChasingStarts);
	};

	@Override
	public void update() {
		if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
			board.loadContent();
		}
		app.entities.all().forEach(GameEntity::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoardSprite());
		drawGridLines(g, getWidth(), getHeight());
		app.entities.all().forEach(entity -> entity.draw(g));
	}

	private void escape(PacMan pacMan, Ghost enemy) {
		pacMan.move();
		Tile pacManTile = pacMan.currentTile();

		if (!pacMan.isExactlyOver(pacManTile)) {
			return;
		}

		int dir = pacMan.getMoveDir();
		int max = -1;
		for (int i = 0; i < 4; ++i) {
			Tile neighbor = pacManTile.neighbor(i);
			if (pacMan.canEnterTile.apply(neighbor)) {
				int dist = board.shortestRoute(enemy.currentTile(), neighbor).size();
				if (dist > max) {
					max = dist;
					dir = i;
				}
			}
		}
		if (dir != Top4.inv(pacMan.getMoveDir())) {
			pacMan.setMoveDir(dir);
		} else {
			for (int i = 0; i < 4; ++i) {
				Tile neighbor = pacManTile.neighbor(i);
				if (i != dir && pacMan.canEnterTile.apply(neighbor)) {
					pacMan.setMoveDir(i);
				}
			}
		}
	}
}