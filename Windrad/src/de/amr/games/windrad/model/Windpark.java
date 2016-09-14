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
public class Windpark {

	private static final File WINDPARK_DATEI = new File(
			System.getProperty("user.dir") + File.separator + "windpark.txt");

	public final Point2D.Float zentrumSonne = new Point2D.Float();
	public final List<Windrad> windräder = new ArrayList<>();

	public void speichern() {
		try (PrintWriter w = new PrintWriter(new FileWriter(WINDPARK_DATEI))) {
			w.println(
					"# x, y, turmHöhe, turmBreiteUnten, turmBreiteOben, nabenRadius, rotorLänge, rotorBreite");
			for (Windrad windrad : windräder) {
				speichern(w, windrad);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void speichern(PrintWriter w, Windrad windrad) {
		w.print(windrad.getPosition().x);
		w.print(", ");
		w.print(windrad.getPosition().y);
		w.print(", ");
		w.print(windrad.getTurmHöhe());
		w.print(", ");
		w.print(windrad.getTurmBreiteUnten());
		w.print(", ");
		w.print(windrad.getTurmBreiteOben());
		w.print(", ");
		w.print(windrad.getNabenRadius());
		w.print(", ");
		w.print(windrad.getRotorLänge());
		w.print(", ");
		w.print(windrad.getRotorBreite());
		w.println();
	}

	public static Windpark laden() {
		Windpark windpark = new Windpark();
		try (BufferedReader r = new BufferedReader(new FileReader(WINDPARK_DATEI))) {
			String line = null;
			while ((line = r.readLine()) != null) {
				if (line.startsWith("#")) {
					continue; // Comment line
				}
				String[] record = line.split(",");
				int i = 0;
				float x = Float.parseFloat(record[i++]);
				float y = Float.parseFloat(record[i++]);
				float turmHöhe = Float.parseFloat(record[i++]);
				float turmBreiteUnten = Float.parseFloat(record[i++]);
				float turmBreiteOben = Float.parseFloat(record[i++]);
				float nabeRadius = Float.parseFloat(record[i++]);
				float rotorLänge = Float.parseFloat(record[i++]);
				float rotorBreite = Float.parseFloat(record[i++]);
				windpark.windräder.add(new Windrad(x, y, turmHöhe, turmBreiteUnten, turmBreiteOben,
						nabeRadius, rotorLänge, rotorBreite));
			}
		} catch (FileNotFoundException e) {
			System.out.println("Windparkdatei nicht gefunden " + WINDPARK_DATEI);
		} catch (IOException e) {
			System.out.println("Windparkdatei nicht lesbar " + WINDPARK_DATEI);
		}
		return windpark;
	}
}