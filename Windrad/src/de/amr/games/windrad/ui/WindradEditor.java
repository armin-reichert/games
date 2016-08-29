package de.amr.games.windrad.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.amr.games.windrad.model.WindradModell;
import net.miginfocom.swing.MigLayout;

public class WindradEditor extends JPanel {

	private WindradModell windrad;
	private WindparkAnsicht view;
	private JSlider turmHoeheSlider;
	private JSlider rotorLaengeSlider;

	public void setModel(WindradModell windrad) {
		this.windrad = windrad;
		turmHoeheSlider.setValue((int)windrad.turmHöhe());
		rotorLaengeSlider.setValue((int)windrad.rotorLänge);
	}
	
	public void setView(WindparkAnsicht view) {
		this.view = view;
	}

	public WindradEditor() {
		setLayout(new MigLayout("", "[][]", "[][]"));

		JLabel lblTurmhoehe = new JLabel("Turmhöhe");
		add(lblTurmhoehe, "cell 0 0");

		turmHoeheSlider = new JSlider();
		turmHoeheSlider.setMajorTickSpacing(50);
		turmHoeheSlider.setMinorTickSpacing(10);
		turmHoeheSlider.setPaintLabels(true);
		turmHoeheSlider.setPaintTicks(true);
		turmHoeheSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (windrad != null && !turmHoeheSlider.getValueIsAdjusting()) {
					setWindradTurmHöhe(turmHoeheSlider.getValue());
				}
			}
		});
		turmHoeheSlider.setValue(150);
		turmHoeheSlider.setMinimum(50);
		turmHoeheSlider.setMaximum(400);
		add(turmHoeheSlider, "cell 1 0");

		JLabel lblRotorlnge = new JLabel("Rotorlänge");
		add(lblRotorlnge, "cell 0 1");

		rotorLaengeSlider = new JSlider();
		rotorLaengeSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (windrad != null && !rotorLaengeSlider.getValueIsAdjusting()) {
					setWindradRotorLänge(rotorLaengeSlider.getValue());
				}
			}
		});
		rotorLaengeSlider.setMaximum(180);
		rotorLaengeSlider.setPaintLabels(true);
		rotorLaengeSlider.setMinorTickSpacing(5);
		rotorLaengeSlider.setMajorTickSpacing(20);
		rotorLaengeSlider.setMinimum(30);
		rotorLaengeSlider.setPaintTicks(true);
		add(rotorLaengeSlider, "cell 1 1");

	}

	public JSlider getTurmHoeheSlider() {
		return turmHoeheSlider;
	}

	public JSlider getRotorLaengeSlider() {
		return rotorLaengeSlider;
	}

	public void setWindradRotorLänge(int value) {
		try {
			windrad.setzeRotorLänge(value);
		} catch (IllegalStateException x) {
			System.out.println("Ungültiger Zustand gemeldet, Wert zurückgesetzt");
			getRotorLaengeSlider().setValue((int)windrad.rotorLänge);
		}
		if (view != null) {
			view.repaint();
		}
	}

	public void setWindradTurmHöhe(int value) {
		try {
			windrad.baueWindrad(value);
		} catch (IllegalStateException x) {
			System.out.println("Ungültiger Zustand gemeldet, Wert zurückgesetzt");
			getTurmHoeheSlider().setValue((int)windrad.turmHöhe());
		}
		if (view != null) {
			view.repaint();
		}
	}
}
