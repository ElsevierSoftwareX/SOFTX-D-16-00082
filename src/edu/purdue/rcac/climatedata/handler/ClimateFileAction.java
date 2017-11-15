package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

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
public class ClimateFileAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private MainFrame gui;
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());

	
	public ClimateFileAction(MainFrame gui) 
	{
		putValue(NAME, "Browse");
		putValue(SHORT_DESCRIPTION, "select climate files");
		
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent e) 
	{
		new FileBrowser(gui, "climateFileSelection").execute();
		
	}
}