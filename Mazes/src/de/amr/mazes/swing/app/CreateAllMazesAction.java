package de.amr.mazes.swing.app;

import static de.amr.mazes.swing.model.MazeDemoModel.Tag.Slow;
import static de.amr.mazes.swing.model.MazeDemoModel.Tag.SmallGridOnly;

import java.awt.event.ActionEvent;

import de.amr.mazes.swing.model.AlgorithmInfo;

public class CreateAllMazesAction extends CreateSingleMazeAction {

	private boolean readyForNext;

	public CreateAllMazesAction(MazeDemoApp app) {
		super(app);
		putValue(NAME, "Create All Mazes");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (app.model.isHidingControlsWhenRunning()) {
			app.settingsWindow.setVisible(false);
		}
		app.mazeWindow.getCanvas().setDelay(app.model.getDelay());
		app.mazeWindow.getCanvas().resetRenderingModel();
		app.mazeWindow.setVisible(true);
		app.startTask(() -> {
			enableControls(false);
			generateAllMazes();
			enableControls(true);
			app.settingsWindow.setVisible(true);
		});
	}

	private void generateAllMazes() {
		readyForNext = true;
		/*@formatter:off*/
		app.model.algorithms()
			.filter(alg -> !(alg.isTagged(Slow) || alg.isTagged(SmallGridOnly)))
			.forEachOrdered(alg -> {
				if (app.isTaskStopped()) {
					return;
				}
				if (readyForNext) {
					createNextMaze(alg);
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				}
		});
		/*@formatter:on*/
		app.showMessage("Done.");
	}

	private void createNextMaze(AlgorithmInfo<?> algorithm) {
		readyForNext = false;
		app.mazeWindow.getCanvas().clear();
		try {
			generateMaze(algorithm);
			final AlgorithmInfo<?> pathFinder = app.settingsWindow.getPathFinderMenu()
					.getSelectedPathFinder();
			if (pathFinder != null) {
				runPathFinder(pathFinder);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		readyForNext = true;
	}
}