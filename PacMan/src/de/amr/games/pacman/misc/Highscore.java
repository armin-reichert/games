package de.amr.games.pacman.misc;

import static de.amr.easy.game.Application.LOG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class for loading and storing highscore.
 * 
 * @author Armin Reichert
 */
public class Highscore {

	private File file;
	private int points;
	private int level;

	public Highscore(String fileName) {
		file = new File(System.getProperty("user.dir") + File.separator + fileName);
		points = 0;
		level = 1;
		load();
	}

	public void load() {
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			String[] record = r.readLine().split(",");
			points = Integer.parseInt(record[0]);
			level = record.length == 2 ? Integer.parseInt(record[1]) : 1;
		} catch (FileNotFoundException e) {
			LOG.warning("Highscore file not found: " + file);
		} catch (IOException e) {
			LOG.warning("Could not read from highscore file: " + file);
		}
	}

	public void save(int points, int level) {
		try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
			w.println(points + "," + level);
		} catch (IOException e) {
			LOG.warning("Could not save highscore: " + file);
		}
	}

	public int getPoints() {
		return points;
	}

	public int getLevel() {
		return level;
	}
}
