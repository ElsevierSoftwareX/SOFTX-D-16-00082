package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;

import edu.purdue.rcac.climatedata.MainFrame;
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
 * A handler class to upload a weight file from the tool.
 */
public class WeightFileUploadAction extends AbstractAction
{
	private MainFrame gui;
	
	public WeightFileUploadAction(MainFrame gui) 
	{
		putValue(NAME, "Upload");
		putValue(SHORT_DESCRIPTION, "Upload weight.map file");
		
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent e) 
	{
	
		Executor executor = Executors.newFixedThreadPool(2);
		Runnable runnable = new Runnable() 
		{
			public void run() 
			{
				// import the file
				String path = Utils.importFile(gui.getUserHome(),
						"for Region File", -1);
				
				gui.getTxtWeightFile().setText(path);
				gui.setWeightFile( new File(gui.getTxtWeightFile().getText()) );

				gui.getRdbtnMax().setEnabled(false);
				gui.getRdbtnMin().setEnabled(false);
				gui.getRdbtnMean().setEnabled(false);
				gui.getRdbtnSd().setEnabled(false);
			}
		};
		executor.execute(runnable);
	}
}