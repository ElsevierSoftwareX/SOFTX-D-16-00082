package edu.purdue.rcac.climatedata.handler;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

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
 * A handler class for toggle radio buttons text fields
 * that related to "enable function" button.
 */
public class CheckBoxAction extends AbstractAction 
{
	private MainFrame gui;
	
	public CheckBoxAction(MainFrame gui)
	{
		putValue(NAME, "Enable Functions");
		putValue(SHORT_DESCRIPTION, "check to enable functions");
		
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent e) {
		if (gui.getChckbxFunctionEnable().isSelected() == true) 
		{
			gui.getRdbtnMax().setEnabled(true);
			gui.getRdbtnMin().setEnabled(true);
			gui.getRdbtnMean().setEnabled(true);
			gui.getRdbtnSd().setEnabled(true);
		
			gui.getTxtWeightFile().setEnabled(false);
			gui.getTxtWeightFile().setForeground(Color.LIGHT_GRAY);
			
			gui.setWeightFile(null);

		} else 
		{
			gui.getRdbtnMax().setEnabled(false);
			gui.getRdbtnMin().setEnabled(false);
			gui.getRdbtnMean().setEnabled(false);
			gui.getRdbtnSd().setEnabled(false);
			gui.getTxtWeightFile().setEnabled(true);
			gui.getTxtWeightFile().setForeground(Color.BLACK);
			
			gui.setWeightFile( new File(gui.getTxtWeightFile().getText()) );
			gui.getAggregationButtonGroup().clearSelection();
			gui.setFunctioName(null);
		}
	}
}