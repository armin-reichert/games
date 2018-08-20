package de.amr.games.windrad.model;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Windpark (Model).
 */
public class WindFarm {

	private static final File FILE = new File(System.getProperty("user.dir") + File.separator + "windpark.txt");

	public final Point2D.Float sunCenter = new Point2D.Float();
	public final List<WindTurbine> turbines = new ArrayList<>();

	public void save() {
		try (PrintWriter w = new PrintWriter(new FileWriter(FILE))) {
			w.println("# x, y, turmHöhe, turmBreiteUnten, turmBreiteOben, nabenRadius, rotorLänge, rotorBreite");
			for (WindTurbine turbine : turbines) {
				saveTurbine(w, turbine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveTurbine(PrintWriter w, WindTurbine turbine) {
		w.print(turbine.getPosition().x);
		w.print(", ");
		w.print(turbine.getPosition().y);
		w.print(", ");
		w.print(turbine.getTowerHeight());
		w.print(", ");
		w.print(turbine.getTowerWidthBottom());
		w.print(", ");
		w.print(turbine.getTowerWidthTop());
		w.print(", ");
		w.print(turbine.getNacelleRadius());
		w.print(", ");
		w.print(turbine.getRotorLength());
		w.print(", ");
		w.print(turbine.getRotorThickness());
		w.println();
	}

	public static WindFarm loadFromFile() {
		WindFarm windFarm = new WindFarm();
		try (BufferedReader r = new BufferedReader(new FileReader(FILE))) {
			String line = null;
			while ((line = r.readLine()) != null) {
				if (line.startsWith("#")) {
					continue; // Comment line
				}
				String[] record = line.split(",");
				int i = 0;
				float x = Float.parseFloat(record[i++]);
				float y = Float.parseFloat(record[i++]);
				float towerHeight = Float.parseFloat(record[i++]);
				float towerWidthBottom = Float.parseFloat(record[i++]);
				float towerWidthTop = Float.parseFloat(record[i++]);
				float nacelleRadius = Float.parseFloat(record[i++]);
				float rotorLength = Float.parseFloat(record[i++]);
				float rotorWidth = Float.parseFloat(record[i++]);
				windFarm.turbines.add(new WindTurbine(x, y, towerHeight, towerWidthBottom, towerWidthTop, nacelleRadius,
						rotorLength, rotorWidth));
			}
		} catch (FileNotFoundException e) {
			System.out.println("Windparkdatei nicht gefunden " + FILE);
		} catch (IOException e) {
			System.out.println("Windparkdatei nicht lesbar " + FILE);
		}
		return windFarm;
	}
}