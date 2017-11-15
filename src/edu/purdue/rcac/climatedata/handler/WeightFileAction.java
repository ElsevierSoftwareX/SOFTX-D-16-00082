package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.purdue.rcac.climatedata.FileBrowser;
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
 * A handler class to call file browser from "browse" button.
 */
public class WeightFileAction extends AbstractAction 
{
	private MainFrame gui;
	
	public WeightFileAction(MainFrame gui) 
	{
		putValue(NAME, "Browse");
		putValue(SHORT_DESCRIPTION, "select weight.map file");
		
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent e) 
	{
		new FileBrowser(gui, "weightFileSelection").execute();
	}
}