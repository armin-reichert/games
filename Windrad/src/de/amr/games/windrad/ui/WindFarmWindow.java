package de.amr.games.windrad.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import de.amr.games.windrad.model.WindFarm;

public class WindFarmWindow extends JFrame {

	public WindFarmWindow(WindFarm farm, int viewWidth, int viewHeight) {
		super("Windpark Simulation");
		try {
			WindFarmView farmView = new WindFarmView(farm, viewWidth, viewHeight);
			farmView.setPreferredSize(new Dimension(viewWidth, viewHeight));
			farmView.setMinimumSize(farmView.getPreferredSize());
			add(farmView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				farm.save();
			}
		});
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}