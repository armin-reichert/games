package de.zwickmann.windrad.model;

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

public class WindparkModell {

	private static final File WINDPARK_DATEI = new File(
			System.getProperty("user.dir") + File.separator + "windpark.txt");

	public static final int BREITE = 1024;
	public static final int HÖHE = 600;

	public final Point2D.Float zentrumSonne = new Point2D.Float();
	public final List<WindradModell> windräder = new ArrayList<>();

	public void speichern() {
		try (PrintWriter w = new PrintWriter(new FileWriter(WINDPARK_DATEI))) {
			for (WindradModell windrad : windräder) {
				speichern(w, windrad);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void speichern(PrintWriter w, WindradModell windrad) {
		w.print(windrad.basis().x);
		w.print(",");
		w.print(windrad.basis().y);
		w.print(",");
		w.print(windrad.turmHöhe());
		w.print(",");
		w.print(windrad.turmBreite());
		w.print(",");
		w.print(windrad.nabeRadius);
		w.print(",");
		w.print(windrad.rotorLänge);
		w.print(",");
		w.print(windrad.rotorBreite);
		w.println();
	}

	public void laden() {
		windräder.clear();
		try (BufferedReader r = new BufferedReader(new FileReader(WINDPARK_DATEI))) {
			String line = r.readLine();
			while (line != null) {
				String[] record = line.split(",");
				float x = Float.parseFloat(record[0]);
				float y = Float.parseFloat(record[1]);
				float turmHöhe = Float.parseFloat(record[2]);
				float turmBreite = Float.parseFloat(record[3]);
				float nabeRadius = Float.parseFloat(record[4]);
				float rotorLänge = Float.parseFloat(record[5]);
				float rotorBreite = Float.parseFloat(record[6]);
				windräder.add(new WindradModell(x, y, turmHöhe, turmBreite, nabeRadius, rotorLänge, rotorBreite));
				line = r.readLine();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Windparkdatei nicht gefunden " + WINDPARK_DATEI);
		} catch (IOException e) {
			System.out.println("Windparkdatei nicht lesbar " + WINDPARK_DATEI);
		}
	}

	//TODO: funktioniert nur, wenn Sonne höher als Turmspitze steht
	public Point2D.Float berechneSchattenPunkt(WindradModell windrad) {
		Point2D.Float basis = windrad.basis();
		Point2D.Float sonne = zentrumSonne;
		float h = windrad.turmHöhe();
		float lambda = (basis.y - sonne.y) / (basis.y + h - sonne.y);
		Point2D.Float schattenPunkt = new Point2D.Float();
		schattenPunkt.x = sonne.x + lambda * (basis.x - sonne.x);
		schattenPunkt.y = basis.y;
		return schattenPunkt;
	}
}