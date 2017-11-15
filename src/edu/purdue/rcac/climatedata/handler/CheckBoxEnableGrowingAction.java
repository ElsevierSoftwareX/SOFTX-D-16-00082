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
 * that related to "enable growing season" button.
 */
public class CheckBoxEnableGrowingAction extends AbstractAction 
{
	private MainFrame gui;
	
	public CheckBoxEnableGrowingAction(MainFrame gui)
	{
		putValue(NAME, "Obtain annual growing season averages");
		putValue(SHORT_DESCRIPTION, "check to enable growing aggregate");
		
		this.gui = gui;
		
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (gui.getChckbxEnableGrowingSeasons().isSelected() == true)
		{
			gui.getTextGrwoingFile().setEnabled(true);
			gui.getTextGrwoingFile().setForeground(Color.BLACK);
			
			gui.getBtnGrowingButton().setEnabled(true);
			
			gui.setGrowingSeasonFile(new File(gui.getTextGrwoingFile().getText()));
		}
		else
		{
			gui.getTextGrwoingFile().setEnabled(false);
			gui.getTextGrwoingFile().setForeground(Color.LIGHT_GRAY);
			
			gui.getBtnGrowingButton().setEnabled(false);
			
			gui.setGrowingSeasonFile(null);
		}
		
	}
}