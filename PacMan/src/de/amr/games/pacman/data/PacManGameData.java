package de.amr.games.pacman.data;

import static de.amr.easy.game.Application.Assets;
import static de.amr.easy.game.Application.GameLoop;
import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.data.Bonus.Apple;
import static de.amr.games.pacman.data.Bonus.Bell;
import static de.amr.games.pacman.data.Bonus.Cherries;
import static de.amr.games.pacman.data.Bonus.Galaxian;
import static de.amr.games.pacman.data.Bonus.Grapes;
import static de.amr.games.pacman.data.Bonus.Key;
import static de.amr.games.pacman.data.Bonus.Peach;
import static de.amr.games.pacman.data.Bonus.Strawberry;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.entities.ghost.GhostName;

public class PacManGameData {

	// Constants

	public final int PointsForPellet = 10;
	public final int PointsForEnergizer = 50;
	public final int PelletsLeftForBonus1 = 170;
	public final int PelletsLeftForBonus2 = 70;
	public final int ScoreForExtraLife = 10000;
	public final int PointsForFirstGhost = 200;
	public final int WaitTicksOnEatingPellet = 1;
	public final int WaitTicksOnEatingEnergizer = 3;
	public final int WaitTicksOnLevelStart = 240;

	public final float BaseSpeed = 8 * TILE_SIZE / 60f;

	private final File HighscoreFile = new File(System.getProperty("user.dir") + File.separator + "pacman.high.txt");

	private final Object[][] LevelData = {
			///*@formatter:off*/
			null,
			{ Cherries, 	100, 	.80f, .71f, .75f, .40f, 20, .8f, 10, .85f, .90f, .79f, .50f, 6 },
			{ Strawberry, 300, 	.90f, .79f, .85f, .45f, 20, .8f, 10, .85f, .95f, .79f, .55f, 5 },
			{ Peach, 			500, 	.90f, .79f, .85f, .45f, 20, .8f, 10, .85f, .95f, .79f, .55f, 4 },
			{ Peach, 			500, 	.90f, .79f, .85f, .50f, 20, .8f, 10, .85f, .95f, .79f, .55f, 3 },
			{ Apple, 			700, 		1f, .87f, .95f, .50f, 20, .8f, 10, .85f,   1f, .79f, .60f, 2 },
			{ Apple, 			700, 		1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 5 },
			{ Grapes, 		1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 2 },
			{ Grapes, 		1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 2 },
			{ Galaxian, 	2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 1 },
			{ Galaxian, 	2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 5 },
			{ Bell, 			3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 2 },
			{ Bell, 			3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 1 },
			{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 1 },
			{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 3 }, 
			{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 1 }, 
			{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 1 }, 
			{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, .85f, 	1f, .79f, .60f, 0 }, 
			/*@formatter:on*/
	};

	private final int[][] ScatterDuractionSeconds = {
		/*@formatter:off*/
		{ 7, 7, 5, 5, 0 }, 	// level 1
		{ 7, 7, 5, 0, 0 }, 	// level 2-4
		{ 5, 5, 5, 0, 0 }   // level 5-
		/*@formatter:on*/
	};

	private final int[][] ChaseDurationSeconds = {
		/*@formatter:off*/
		{ 20, 20, 20, 	Integer.MAX_VALUE },  // level 1 
		{ 20, 20, 1033, Integer.MAX_VALUE },	// level 2-4
		{ 20, 20, 1037, Integer.MAX_VALUE } 	// level 5-
		/*@formatter:on*/
	};

	// Mutable data

	public Board board;
	public RouteMap routeMap;
	public int levelNumber;
	public int waveNumber;
	public int liveCount;
	public int score;
	public int highscore;
	public int highscoreLevel;
	public List<Bonus> bonusScore;
	public Optional<Bonus> bonus;
	public int bonusTimeRemaining;
	public int ghostValue;
	public int ghostsEatenAtLevel;

	// Highscore

	private void loadHighscore() {
		try (BufferedReader r = new BufferedReader(new FileReader(HighscoreFile))) {
			String[] record = r.readLine().split(",");
			highscore = Integer.parseInt(record[0]);
			highscoreLevel = record.length == 2 ? Integer.parseInt(record[1]) : 1;
		} catch (FileNotFoundException e) {
			Log.warning("Highscore file not found: " + HighscoreFile);
		} catch (IOException e) {
			Log.warning("Could not read from highscore file: " + HighscoreFile);
		}
	}

	public void saveHighscore() {
		try (PrintWriter w = new PrintWriter(new FileWriter(HighscoreFile))) {
			w.println(highscore + "," + levelNumber);
		} catch (IOException e) {
			Log.warning("Could not save highscore: " + HighscoreFile);
		}
	}

	public void newBoard() {
		board = new Board(Assets.text("board.txt"));
		routeMap = new RouteMap(board);
		levelNumber = 1;
		waveNumber = 1;
		liveCount = 3;
		score = 0;
		highscore = 0;
		highscoreLevel = 1;
		bonusScore = new ArrayList<>();
		bonus = Optional.empty();
		bonusTimeRemaining = 0;
		ghostValue = 0;
		ghostsEatenAtLevel = 0;
		loadHighscore();
	}

	public void initLevel() {
		board.reset();
		waveNumber = 1;
		bonus = Optional.empty();
		bonusTimeRemaining = 0;
		ghostsEatenAtLevel = 0;
		Log.info(String.format("Level %d: %d pellets and %d energizers. Frames/sec: %d", levelNumber,
				board.count(TileContent.Pellet), board.count(TileContent.Energizer), GameLoop.getFrameRate()));
	}

	public int getGhostWaitingDuration(GhostName ghostName) {
		switch (ghostName) {
		case Blinky:
			return GameLoop.secToFrames(0);
		case Clyde:
			return GameLoop.secToFrames(1.5f);
		case Inky:
			return GameLoop.secToFrames(1);
		case Pinky:
			return GameLoop.secToFrames(0.5f);
		case Stinky:
			return GameLoop.secToFrames(10);
		}
		return 0;
	}

	public int getGhostRecoveringDuration(GhostName ghostName) {
		return GameLoop.secToFrames(2);
	}

	public int getScatteringDuration() {
		int nCols = ScatterDuractionSeconds[0].length;
		int row = (levelNumber == 1) ? 0 : (levelNumber <= 4) ? 1 : 2;
		int col = waveNumber <= nCols ? waveNumber - 1 : nCols - 1;
		return GameLoop.secToFrames(ScatterDuractionSeconds[row][col]);
	}

	public int getChasingDuration() {
		int nCols = ChaseDurationSeconds[0].length;
		int row = (levelNumber == 1) ? 0 : (levelNumber <= 4) ? 1 : 2;
		int col = waveNumber <= nCols ? waveNumber - 1 : nCols - 1;
		return GameLoop.secToFrames(ChaseDurationSeconds[row][col]);
	}

	public Bonus getBonus() {
		return (Bonus) LevelData[levelNumber][0];
	}

	public int getBonusValue() {
		return (Integer) LevelData[levelNumber][1];
	}

	public float getPacManSpeed() {
		return BaseSpeed * (Float) LevelData[levelNumber][2];
	}

	public float getGhostSpeedNormal() {
		return BaseSpeed * (Float) LevelData[levelNumber][4];
	}

	public float getGhostSpeedInTunnel() {
		return BaseSpeed * (Float) LevelData[levelNumber][5];
	}

	public float getGhostSpeedInHouse() {
		return getGhostSpeedNormal() / 2;
	}

	public float getPacManAttackingSpeed() {
		return BaseSpeed * (Float) LevelData[levelNumber][10];
	}

	public float getGhostSpeedWhenFrightened() {
		return BaseSpeed * (Float) LevelData[levelNumber][12];
	}

	public int getGhostFrightenedDuration() {
		return GameLoop.secToFrames((Integer) LevelData[levelNumber][13]);
	}
}