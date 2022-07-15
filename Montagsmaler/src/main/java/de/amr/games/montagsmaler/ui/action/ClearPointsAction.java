/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package de.amr.games.montagsmaler.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.amr.games.montagsmaler.game.Player;
import de.amr.games.montagsmaler.ui.MontagsmalerUI;

/**
 * @author Armin Reichert
 */
public class ClearPointsAction extends AbstractAction {

	private final MontagsmalerUI ui;

	public ClearPointsAction(MontagsmalerUI ui) {
		this.ui = ui;
		putValue(NAME, "Punkte zurücksetzen");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (Player player : ui.getGame().getTeam1().getPlayers()) {
			player.setPoints(0);
		}
		for (Player player : ui.getGame().getTeam2().getPlayers()) {
			player.setPoints(0);
		}
		ui.updateCurrentTeamPanel();
	}
}