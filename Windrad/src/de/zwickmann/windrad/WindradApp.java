package de.zwickmann.windrad;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.zwickmann.windrad.model.WindparkModell;
import de.zwickmann.windrad.ui.WindparkFenster;

public class WindradApp {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		WindparkModell windpark = new WindparkModell();
		windpark.laden();
		EventQueue.invokeLater(() -> new WindparkFenster(windpark, WindparkModell.BREITE, WindparkModell.HÖHE));
	}
}