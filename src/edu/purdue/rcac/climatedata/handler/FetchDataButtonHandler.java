package edu.purdue.rcac.climatedata.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import edu.purdue.rcac.climatedata.DownloadManager;
import edu.purdue.rcac.climatedata.MainFrame;
import edu.purdue.rcac.climatedata.ProgressChecker;

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
 * A handler class to download raw data files from globus online.
 * DownloadManager will proceed to download by calling "submit isimiptransfer".
 */
public class FetchDataButtonHandler implements ActionListener
{
	private static Logger logger = Logger.getLogger(MainFrame.class.getName());
	private MainFrame gui;
	
	public FetchDataButtonHandler(MainFrame gui)
	{
		this.gui = gui;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (MainFrame.getDownloadProgress() == true) 
		{
			JOptionPane.showMessageDialog(null,
					"Please wait until fetching completed",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		MainFrame.setDownloadProgress(true);
		
		String path = "";
		if (gui.getGcm().equals("") || gui.getRcp().equals("")
				|| gui.getClimate().equals("")) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please check all the options for crop data before clicking on \"Fetch Data\"",
							"Warning", JOptionPane.WARNING_MESSAGE);
			MainFrame.setDownloadProgress(false);
			return;
		}
		MainFrame.setCounter(0);
		
		ProgressChecker checker = new ProgressChecker(gui.getMainProgressBar());
		checker.start();

		path = gui.getTxtPathfields().getText();

		if (path.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					"Please build a path by selecting Crop Data",
					"Warning", JOptionPane.WARNING_MESSAGE);
			MainFrame.setDownloadProgress(false);
			return;
		}
		gui.setDownloadPath(gui.getDefaultDownloadPath() + path);

		// check if path already downloaed
		File f = new File(gui.getDownloadPath());
		String variable = f.getName();
		File f2 = f.getParentFile();
		
		
		if (f2.exists() && f2.isDirectory()) 
		{
			boolean stop = false;
			
			// find there is a variable directory
			String[] list = f2.list();
			for(String s : list)
			{
				if(s.startsWith(variable + "_"))
				{
					// if yes,
					stop = true;
				}
			}
			String max = "";
			for(String s : list)
			{	
				if(s.startsWith(variable + "_")) 
				{
					if(s.compareTo(max) > 0)
						max = s;
				}
				
			}			
			logger.info("max " + max + " added\n");

			
			if(stop && !gui.getChckbxByForce().isSelected())
			{
				path = path.substring(0, path.length() - variable.length()) + max;
				gui.setDownloadPath(gui.getDefaultDownloadPath() + path);
				
				JOptionPane
				.showMessageDialog(
						null,
						"The data has been fetched previously. Please proceed to the \"Aggregation\" tab."
						+ "\nIf you want to fetch the data again, check \"Clear cache\" under the \"Fetch Data\".",
						"Warning", JOptionPane.WARNING_MESSAGE);
				MainFrame.setDownloadProgress(false);
				
//				logger.info("selected path = " + path + "\n");
				logger.info("full path = " + gui.getDownloadPath() + "\n");
				
				return;
			}
		}

		DownloadManager dm = new DownloadManager(gui, path, variable, gui.getOutputTextArea());

		dm.start();

	}
}

