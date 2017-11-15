package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JRadioButton;

import edu.purdue.rcac.climatedata.MainFrame;

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
 * A handler class to set correct path to input files' directory.
 */
public class ClimateRadioButtonAction extends AbstractAction 
{
	private MainFrame gui;
	
	public ClimateRadioButtonAction(JRadioButton radiobutton, MainFrame gui) 
	{
		putValue(NAME, radiobutton.getText());
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent e) 
	{
		gui.setGcm(gui.getSelectedButtonText(gui.getGCMButtonGroup()));
		gui.setRcp(gui.getSelectedButtonText(gui.getRCPButtonGroup()));
		gui.setClimate(gui.getSelectedButtonText(gui.getClimateButtonGroup()));
		
		String path = gui.getGcm() + "/" + gui.getRcp() + "/" + gui.getClimate();
		
		gui.getTxtPathfields().setText(gui.getPrefix() + "/" + path);
	}
}