package de.amr.games.windrad;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.games.windrad.model.Windpark;
import de.amr.games.windrad.ui.WindparkFenster;

public class WindradApp {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Windpark windpark = new Windpark();
		windpark.laden();
		EventQueue.invokeLater(() -> new WindparkFenster(windpark, Windpark.BREITE, Windpark.HÖHE));
	}
}