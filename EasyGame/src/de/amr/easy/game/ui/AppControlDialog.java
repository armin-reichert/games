package de.amr.easy.game.ui;

import static de.amr.easy.game.Application.GameLoop;
import static de.amr.easy.game.Application.Settings;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AppControlDialog extends JDialog {

	private JSlider fpsControl;

	public AppControlDialog(JFrame parent) {
		super(parent);
		setTitle("Application Control for: " + Settings.title);
		addFPSControl();
		setSize(600, 100);
	}

	private void addFPSControl() {
		fpsControl = new JSlider(0, 100);
		fpsControl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				GameLoop.setFrameRate(fpsControl.getValue());
			}
		});
		fpsControl.setValue(GameLoop.getFrameRate());
		fpsControl.setMajorTickSpacing(10);
		fpsControl.setMinorTickSpacing(1);
		fpsControl.setPaintTicks(true);
		fpsControl.setLabelTable(fpsControl.createStandardLabels(5));
		fpsControl.setPaintLabels(true);
		fpsControl.setToolTipText("Rendering FPS");
		add(fpsControl);
	}

}
