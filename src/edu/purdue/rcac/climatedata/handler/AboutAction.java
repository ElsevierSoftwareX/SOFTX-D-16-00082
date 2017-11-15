package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractAction;

import edu.purdue.rcac.climatedata.AboutFrame;
import edu.purdue.rcac.climatedata.Utils;

/**
 * Climate Scenario Agregator
 * Copyright (C) 2016  N. Villoria
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

/**
 * @author nujwoo
 * A handler class for the "About" frame. 
 */
public class AboutAction extends AbstractAction {
	public AboutAction() 
	{
		putValue(NAME, "Help");////
		putValue(SHORT_DESCRIPTION, "About this tool");
	}

	public void actionPerformed(ActionEvent e) {
		String urlstr = "https://mygeohub.org/tools/climatetool";

		final URL url;
		try {
			url = new URL(urlstr);

			class OpenUrlAction implements ActionListener {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Utils.viewExternally(url);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			AboutFrame frame = new AboutFrame();
			frame.setSize(450, 250);
			frame.setVisible(true);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}