package de.amr.games.pacman;

import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.data.BonusSymbol.Apple;
import static de.amr.games.pacman.data.BonusSymbol.Bell;
import static de.amr.games.pacman.data.BonusSymbol.Cherries;
import static de.amr.games.pacman.data.BonusSymbol.Galaxian;
import static de.amr.games.pacman.data.BonusSymbol.Grapes;
import static de.amr.games.pacman.data.BonusSymbol.Key;
import static de.amr.games.pacman.data.BonusSymbol.Peach;
import static de.amr.games.pacman.data.BonusSymbol.Strawberry;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.util.Arrays.asList;

import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.data.BonusSymbol;
import de.amr.games.pacman.entities.PacManGameEntity;
import de.amr.games.pacman.scenes.BlinkyTestScene;
import de.amr.games.pacman.scenes.PlayScene;
import de.amr.games.pacman.scenes.RoutingTestScene;
import de.amr.games.pacman.scenes.ScatteringTestScene;
import de.amr.games.pacman.ui.ClassicUI;
import de.amr.games.pacman.ui.ModernUI;
import de.amr.games.pacman.ui.PacManUI;

/**
 * The Pac-Man game application.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends Application {

	public static final PacManGame Game = new PacManGame();

	public static void main(String... args) {
		Game.settings.title = "Armin's Pac-Man";
		Game.settings.width = NUM_COLS * TILE_SIZE;
		Game.settings.height = NUM_ROWS * TILE_SIZE;
		Game.settings.scale = args.length > 0 ? Float.valueOf(args[0]) / Game.settings.height : 1;
		Game.settings.fullScreenOnStart = false;
		Game.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		Game.settings.set("themes", asList(new ClassicUI(), new ModernUI()));
		Game.settings.set("drawInternals", false);
		Game.settings.set("drawGrid", false);
		Game.settings.set("testMode", false);
		Game.gameLoop.log = false;
		Game.gameLoop.setTargetFrameRate(60);
		launch(Game);
	}

	// Game parameters

	public static final Object[][] LEVELS = {
		/*@formatter:off*/
		null,
		{ Cherries, 		100, 	.80f, .71f, .75f, .40f, 20, .8f, 10, 	.85f, 	.90f, 	.79f, 	.50f, 6 },
		{ Strawberry, 	300, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 5 },
		{ Peach, 				500, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 4 },
		{ Peach, 				500, 	.90f, .79f, .85f, .50f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 3 },
		{ Apple, 				700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f,   	1f, 	.79f, 	.60f, 2 },
		{ Apple, 				700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ Grapes, 			1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Grapes, 			1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Galaxian, 		2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Galaxian, 		2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ Bell, 				3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Bell, 				3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 3 }, 
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 0 }, 
		/*@formatter:on*/
	};

	public static final int POINTS_FOR_PELLET = 10;
	public static final int POINTS_FOR_ENERGIZER = 50;
	public static final int BONUS1_REMAINING_PELLETS = 170;
	public static final int BONUS2_REMAINING_PELLETS = 70;
	public static final int EXTRA_LIFE_SCORE = 10000;
	public static final int FIRST_GHOST_POINTS = 200;
	public static final int WAIT_TICKS_ON_EATING_PELLET = 1;
	public static final int WAIT_TICKS_ON_EATING_ENERGIZER = 3;

	private float baseSpeed;
	private int themeIndex;

	@Override
	protected void init() {
		baseSpeed = 8 * TILE_SIZE / settings.fps;
		views.add(new PlayScene());
		views.add(new RoutingTestScene());
		views.add(new BlinkyTestScene());
		views.add(new ScatteringTestScene());
//		 views.show(PlayScene.class);
		views.show(ScatteringTestScene.class);
	}

	private List<PacManUI> themes() {
		return settings.get("themes");
	}

	public PacManUI selectedTheme() {
		return themes().get(themeIndex);
	}

	public void nextTheme() {
		if (++themeIndex == themes().size()) {
			themeIndex = 0;
		}
		applyTheme();
	}

	public void applyTheme() {
		entities.allOf(PacManGameEntity.class).forEach(e -> e.setTheme(selectedTheme()));
		selectedTheme().getEnergizer().setAnimated(false);
	}

	public BonusSymbol getBonusSymbol(int level) {
		return (BonusSymbol) LEVELS[level][0];
	}

	public int getBonusValue(int level) {
		return (Integer) LEVELS[level][1];
	}

	public float getPacManSpeed(int level) {
		return baseSpeed * (Float) LEVELS[level][2];
	}

	public float getGhostSpeedNormal(int level) {
		return baseSpeed * (Float) LEVELS[level][4];
	}

	public float getGhostSpeedInTunnel(int level) {
		return baseSpeed * (Float) LEVELS[level][5];
	}

	public float getGhostSpeedInHouse() {
		return baseSpeed / 2;
	}

	public float getPacManAttackingSpeed(int level) {
		return baseSpeed * (Float) LEVELS[level][10];
	}

	public float getGhostSpeedWhenFrightened(int level) {
		return baseSpeed * (Float) LEVELS[level][12];
	}

	public int getGhostFrightenedDuration(int level) {
		return gameLoop.secToFrames((Integer) LEVELS[level][13]);
	}
}